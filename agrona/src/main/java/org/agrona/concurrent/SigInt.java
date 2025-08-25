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
package org.agrona.concurrent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * Utility to allow the registration of a SIGINT handler that hides the unsupported
 * {@code jdk.internal.misc.Signal} class.
 */
public final class SigInt
{
    private static final Class<?> SIGNAL_HANDLER_CLASS;
    private static final Constructor<?> SIGNAL_CONSTRUCTOR;
    private static final Method SIGNAL_HANDLE_METHOD;
    private static final Method SIGNAL_RAISE_METHOD;
    private static final Method HANDLER_HANDLE_METHOD;
    static final Object NO_OP_SIGNAL_HANDLER;

    static
    {
        try
        {
            final Class<?> signalClass = Class.forName("jdk.internal.misc.Signal");
            SIGNAL_HANDLER_CLASS = Class.forName("jdk.internal.misc.Signal$Handler");
            SIGNAL_CONSTRUCTOR = signalClass.getConstructor(String.class);
            SIGNAL_HANDLE_METHOD = signalClass.getMethod("handle", signalClass, SIGNAL_HANDLER_CLASS);
            SIGNAL_RAISE_METHOD = signalClass.getMethod("raise", signalClass);
            HANDLER_HANDLE_METHOD = SIGNAL_HANDLER_CLASS.getMethod("handle", signalClass);

            NO_OP_SIGNAL_HANDLER = Proxy.newProxyInstance(
                SIGNAL_HANDLER_CLASS.getClassLoader(),
                new Class<?>[]{ SIGNAL_HANDLER_CLASS },
                (proxy, method, args) ->
                {
                    if (Object.class == method.getDeclaringClass())
                    {
                        return switch (method.getName())
                        {
                            case "toString" -> proxy.toString();
                            case "hashCode" -> proxy.hashCode();
                            case "equals" -> proxy.equals(args[0]);
                            default -> null;
                        };
                    }
                    return null;
                });
        }
        catch (final ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register a task to be run when a SIGINT is received.
     *
     * @param task to run on reception of the signal.
     */
    public static void register(final Runnable task)
    {
        register("INT", task);
    }

    static Object register(final String name, final Runnable task)
    {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(task, "task");
        try
        {
            final Object signal = SIGNAL_CONSTRUCTOR.newInstance(name);
            final Object prevHandler = SIGNAL_HANDLE_METHOD.invoke(null, signal, NO_OP_SIGNAL_HANDLER);

            final Object handler = Proxy.newProxyInstance(
                SIGNAL_HANDLER_CLASS.getClassLoader(),
                new Class<?>[]{ SIGNAL_HANDLER_CLASS },
                (proxy, method, args) ->
                {
                    if (SIGNAL_HANDLER_CLASS == method.getDeclaringClass() && HANDLER_HANDLE_METHOD.equals(method))
                    {
                        Throwable error = null;
                        try
                        {
                            task.run();
                        }
                        catch (final Throwable ex)
                        {
                            error = ex;
                        }
                        finally
                        {
                            if (null != prevHandler && NO_OP_SIGNAL_HANDLER != prevHandler)
                            {
                                try
                                {
                                    HANDLER_HANDLE_METHOD.invoke(prevHandler, args[0]);
                                }
                                catch (final InvocationTargetException ex)
                                {
                                    final Throwable cause = ex.getCause();
                                    if (null == error)
                                    {
                                        error = cause;
                                    }
                                    else
                                    {
                                        error.addSuppressed(cause);
                                    }
                                }
                            }
                        }

                        if (null != error)
                        {
                            throw error;
                        }
                    }
                    else if (Object.class == method.getDeclaringClass())
                    {
                        return switch (method.getName())
                        {
                            case "toString" -> proxy.toString();
                            case "hashCode" -> proxy.hashCode();
                            case "equals" -> proxy.equals(args[0]);
                            default -> null;
                        };
                    }
                    return null;
                });

            SIGNAL_HANDLE_METHOD.invoke(null, signal, handler);

            return prevHandler;
        }
        catch (final ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
    }

    static Object installHandler(final String name, final Object handler) throws ReflectiveOperationException
    {
        return SIGNAL_HANDLE_METHOD.invoke(null, SIGNAL_CONSTRUCTOR.newInstance(name), handler);
    }

    static Object raiseSignal(final String name) throws ReflectiveOperationException
    {
        return SIGNAL_RAISE_METHOD.invoke(null, SIGNAL_CONSTRUCTOR.newInstance(name));
    }

    private SigInt()
    {
    }
}
