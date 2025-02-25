package org.agrona;


import java.lang.invoke.*;
import java.lang.reflect.Method;

/**
 * todo
 */
public class UnsafeApiBootstrap
{
    /**
     * Bootstrap method for arrayBaseOffset that will be called by the JVM when
     * the invokedynamic instruction is executed for the first time.
     *
     * @param lookup      The lookup context
     * @param methodName  The name of the method to find
     * @param methodType  The method type (signature) expected at the call site
     * @return            A CallSite bound to the appropriate implementation
     * @throws Throwable  If method resolution fails
     */
    public static CallSite bootstrapArrayBaseOffset(
        MethodHandles.Lookup lookup,
        String methodName,
        MethodType methodType) throws Throwable {

        if (!methodName.equals("arrayBaseOffset") ||
            !methodType.returnType().equals(long.class) ||
            methodType.parameterCount() != 2) {
            throw new IllegalArgumentException("Invalid method signature for arrayBaseOffset");
        }

        Class<?> unsafeClass = methodType.parameterType(1);

        MethodHandle mh;
        try {
            Method longMethod = unsafeClass.getMethod("arrayBaseOffset", Class.class);
            if (longMethod.getReturnType() == long.class) {
                mh = lookup.unreflect(longMethod);
                // Ensure method handle has the expected type
                mh = mh.asType(methodType);
                return new ConstantCallSite(mh);
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
        }

        try {
            Method intMethod = unsafeClass.getMethod("arrayBaseOffset", Class.class);
            if (intMethod.getReturnType() == int.class) {
                // We need to convert the int return value to long
                mh = lookup.unreflect(intMethod);

                MethodHandle filter = MethodHandles.explicitCastArguments(
                    mh,
                    mh.type().changeReturnType(long.class)
                );

                filter = filter.asType(methodType);
                return new ConstantCallSite(filter);
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Could not find arrayBaseOffset method", e);
        }

        throw new RuntimeException("No suitable arrayBaseOffset method found");
    }
}
