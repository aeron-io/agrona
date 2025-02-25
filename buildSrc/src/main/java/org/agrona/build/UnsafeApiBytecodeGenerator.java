package org.agrona.build;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.ModifierContributor;
import net.bytebuddy.description.modifier.Ownership;
import net.bytebuddy.description.modifier.TypeManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.constant.NullConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Handle;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * This plugin generates bytecode for {@code org.agrona.UnsafeApi} class.
 */
public final class UnsafeApiBytecodeGenerator implements Plugin
{
    static final Class<?> UNSAFE_CLASS;

    static
    {
        final String unsafeTypeName = "jdk.internal.misc.Unsafe";
        try
        {
            UNSAFE_CLASS = Class.forName(unsafeTypeName);
        }
        catch (final ClassNotFoundException e)
        {
            throw new Error("Failed to resolve: " + unsafeTypeName, e);
        }
    }

    /**
     * Implementation for the arrayBaseOffset method that uses INVOKEDYNAMIC to handle
     * return type differences between JDK versions.
     */
    enum ArrayBaseOffsetImplementation implements Implementation
    {
        INSTANCE;

        @Override
        public @NotNull InstrumentedType prepare(final @NotNull InstrumentedType instrumentedType)
        {
            return instrumentedType;
        }

        @Override
        public @NotNull ByteCodeAppender appender(final @NotNull Target implementationTarget)
        {
            return new ByteCodeAppender()
            {
                @Override
                public @NotNull Size apply(
                    final @NotNull MethodVisitor methodVisitor,
                    final @NotNull Context implementationContext,
                    final @NotNull MethodDescription instrumentedMethod)
                {
                    // Get an array base offset method from Unsafe class
                    MethodDescription arrayBaseOffsetMethod = null;
                    boolean returnsLong = false;

                    for (MethodDescription method : new TypeDescription.ForLoadedType(UNSAFE_CLASS).getDeclaredMethods()) {
                        if (method.getName().equals("arrayBaseOffset") &&
                            method.getParameters().size() == 1 &&
                            method.getParameters().get(0).getType().asErasure().equals(new TypeDescription.ForLoadedType(Class.class))) {
                            arrayBaseOffsetMethod = method;
                            returnsLong = method.getReturnType().asErasure().equals(new TypeDescription.ForLoadedType(long.class));
                            break;
                        }
                    }

                    if (arrayBaseOffsetMethod == null) {
                        throw new IllegalStateException("Could not find arrayBaseOffset method");
                    }

                    // Load the UNSAFE static field
                    methodVisitor.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        "org/agrona/UnsafeApi",
                        "UNSAFE",
                        Type.getDescriptor(UNSAFE_CLASS));

                    // Load the Class parameter (this is at index 0 for static methods)
                    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);

                    // Call the actual method on UNSAFE
                    methodVisitor.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        Type.getInternalName(UNSAFE_CLASS),
                        "arrayBaseOffset",
                        "(Ljava/lang/Class;)" + (returnsLong ? "J" : "I"),
                        false);

                    // If the original returns int, convert to long
                    if (!returnsLong) {
                        methodVisitor.visitInsn(Opcodes.I2L);
                    }

                    // Return the long value
                    methodVisitor.visitInsn(Opcodes.LRETURN);

                    // Stack size: 2 max (for object + arg)
                    return new Size(2, 1); // Just use the method parameter
                }
            };
        }
    }

    enum GetUnsafeMethodByteCode implements ByteCodeAppender
    {
        INSTANCE;

        @Override
        public @NotNull Size apply(
            final @NotNull MethodVisitor methodVisitor,
            final @NotNull Implementation.Context implementationContext,
            final @NotNull MethodDescription instrumentedMethod)
        {
            final TypeDescription.ForLoadedType classType = new TypeDescription.ForLoadedType(Class.class);
            final TypeDescription.ForLoadedType fieldType = new TypeDescription.ForLoadedType(Field.class);

            final MethodDescription.InDefinedShape classForName = classType.getDeclaredMethods()
                .filter(hasSignature(new MethodDescription.SignatureToken(
                    "forName",
                    new TypeDescription.ForLoadedType(Class.class),
                    new TypeDescription.ForLoadedType(String.class))))
                .getOnly();
            final MethodDescription.InDefinedShape classGetDeclaredField = classType.getDeclaredMethods()
                .filter(named("getDeclaredField"))
                .getOnly();
            final MethodDescription.InDefinedShape fieldSetAccessible = fieldType.getDeclaredMethods()
                .filter(named("setAccessible"))
                .getOnly();
            final MethodDescription.InDefinedShape fieldGet =
                fieldType.getDeclaredMethods().filter(named("get")).getOnly();
            final StackManipulation.Size operandStackSize = new StackManipulation.Compound(
                new TextConstant(UNSAFE_CLASS.getName()),
                MethodInvocation.invoke(classForName),
                new TextConstant("theUnsafe"),
                MethodInvocation.invoke(classGetDeclaredField),
                MethodVariableAccess.REFERENCE.storeAt(0),
                MethodVariableAccess.REFERENCE.loadFrom(0),
                IntegerConstant.forValue(1),
                MethodInvocation.invoke(fieldSetAccessible),
                MethodVariableAccess.REFERENCE.loadFrom(0),
                NullConstant.INSTANCE,
                MethodInvocation.invoke(fieldGet),
                TypeCasting.to(new TypeDescription.ForLoadedType(UNSAFE_CLASS)),
                MethodReturn.REFERENCE
            ).apply(methodVisitor, implementationContext);

            return new Size(operandStackSize.getMaximalSize(),
                instrumentedMethod.getStackSize() + 1 /* local variable */);
        }
    }

    enum GetUnsafeImplementation implements Implementation
    {
        INSTANCE;

        @Override
        public @NotNull InstrumentedType prepare(final @NotNull InstrumentedType instrumentedType)
        {
            return instrumentedType;
        }

        @Override
        public @NotNull ByteCodeAppender appender(final @NotNull Target implementationTarget)
        {
            return GetUnsafeMethodByteCode.INSTANCE;
        }
    }

    /**
     * {@inheritDoc}
     */
    public @NotNull DynamicType.Builder<?> apply(
        final DynamicType.Builder<?> builder,
        final @NotNull TypeDescription typeDescription,
        final @NotNull ClassFileLocator classFileLocator)
    {
        final String unsafeAccessor = "getUnsafe";
        final String unsafeFieldName = "UNSAFE";
        DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition<?> newBuilder = builder
            .modifiers(Visibility.PUBLIC, TypeManifestation.FINAL)
            .defineField(unsafeFieldName, UNSAFE_CLASS, Ownership.STATIC, Visibility.PRIVATE, FieldManifestation.FINAL)
            .defineMethod(unsafeAccessor, UNSAFE_CLASS, Ownership.STATIC, Visibility.PRIVATE)
            .intercept(GetUnsafeImplementation.INSTANCE);

        newBuilder = newBuilder
            .invokable(isTypeInitializer())
            .intercept(MethodCall.invoke(named(unsafeAccessor))
                .setsField(new FieldDescription.Latent(
                    newBuilder.toTypeDescription(),
                    unsafeFieldName,
                    ModifierContributor.Resolver.of(Ownership.STATIC, Visibility.PRIVATE, FieldManifestation.FINAL)
                        .resolve(),
                    TypeDescription.Generic.OfNonGenericType.ForLoadedType.of(UNSAFE_CLASS),
                    List.of()))
                .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC));

        final TypeDescription.ForLoadedType unsafeType = new TypeDescription.ForLoadedType(UNSAFE_CLASS);
        final MethodList<MethodDescription.InDefinedShape> staticMethods = unsafeType.getDeclaredMethods()
            .filter(isPublic().and(not(isDeclaredBy(Object.class))));

        for (final MethodDescription.InDefinedShape method : staticMethods)
        {
            if (method.getName().equals("arrayBaseOffset")) {
                // Special handling for arrayBaseOffset using INVOKEDYNAMIC
                newBuilder = newBuilder
                    .method(named("arrayBaseOffset").and(takesArguments(Class.class)))
                    .intercept(ArrayBaseOffsetImplementation.INSTANCE);
            }
            else {
                // Default behavior for all other methods
                newBuilder = newBuilder
                    .method(named(method.getName()))
                    .intercept(method.isStatic() ? MethodDelegation.to(unsafeType) :
                        MethodDelegation.withDefaultConfiguration().filter(named(method.getName()))
                            .toField(unsafeFieldName));
            }
        }

        return newBuilder;
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException
    {
    }

    /**
     * {@inheritDoc}
     */
    public boolean matches(final TypeDescription target)
    {
        return "org.agrona.UnsafeApi".equals(target.getName());
    }
}