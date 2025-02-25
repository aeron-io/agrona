/*
 * Copyright 2014-2025 Real Logic Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.agrona;


import java.lang.invoke.*;
import java.lang.reflect.Method;

/**
 * Great stuff.
 */
public final class UnsafeApiBootstrap
{

    /**
     * We don't want instances.
     */
    private UnsafeApiBootstrap()
    {

    }

    /**
     * Bootstrap method for arrayBaseOffset that will be called by the JVM when
     * the invokedynamic instruction is executed for the first time.
     *
     * @param lookup     The lookup context
     * @param methodName The name of the method to find
     * @param methodType The method type (signature) expected at the call site
     * @return A CallSite bound to the appropriate implementation
     * @throws Throwable If method resolution fails
     */
    public static CallSite bootstrapArrayBaseOffset(
        final MethodHandles.Lookup lookup,
        final String methodName,
        final MethodType methodType) throws Throwable
    {
        System.out.println("UnsafeApiBootstrap.bootstrapArrayBaseOffset");

        // Get Unsafe class from method type
        final Class<?> unsafeClass = methodType.parameterType(0);

        try
        {
            // First try to find the method returning long (Java 25+)
            final Method arrayBaseOffsetMethod = unsafeClass.getMethod("arrayBaseOffset", Class.class);
            final MethodHandle targetMethod = lookup.unreflect(arrayBaseOffsetMethod);

            // Check if method returns long or int
            final boolean returnsLong = arrayBaseOffsetMethod.getReturnType() == long.class;

            if (returnsLong)
            {
                System.out.println("arrayBaseOffset returns long");

                // Method already returns long, use it directly
                return new ConstantCallSite(targetMethod);
            }
            else
            {
                System.out.println("arrayBaseOffset returns int");

                // Method returns int, create an adapter to convert to long
                final MethodType originalType = targetMethod.type();
                final MethodType longReturnType = originalType.changeReturnType(long.class);

                // Convert int to long
                final MethodHandle convertedMethod = MethodHandles.explicitCastArguments(
                    targetMethod,
                    longReturnType
                );

                return new ConstantCallSite(convertedMethod);
            }
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Failed to create method handle for arrayBaseOffset", e);
        }
    }
}
