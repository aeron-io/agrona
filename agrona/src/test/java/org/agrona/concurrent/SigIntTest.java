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

import org.agrona.collections.MutableBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.agrona.concurrent.SigInt.NO_OP_SIGNAL_HANDLER;
import static org.agrona.concurrent.SigInt.installHandler;
import static org.agrona.concurrent.SigInt.raiseSignal;
import static org.agrona.concurrent.SigInt.register;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class SigIntTest
{
    private final Object[] originalSignalHandlers = new Object[2];

    @BeforeEach
    void before() throws Exception
    {
        originalSignalHandlers[0] = installHandler("INT", NO_OP_SIGNAL_HANDLER);
        originalSignalHandlers[1] = installHandler("TERM", NO_OP_SIGNAL_HANDLER);
    }

    @AfterEach
    void after() throws Exception
    {
        installHandler("INT", originalSignalHandlers[0]);
        installHandler("TERM", originalSignalHandlers[1]);
    }

    @Test
    void throwsNullPointerExceptionIfNameIsNull()
    {
        assertThrowsExactly(NullPointerException.class, () -> register(null, () -> {}));
    }

    @Test
    void throwsNullPointerExceptionIfRunnableIsNull()
    {
        assertThrowsExactly(NullPointerException.class, () -> register("INT", null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "INT", "TERM" })
    void shouldDelegateToExistingSignalHandler(final String name) throws Exception
    {
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler =
            Thread.getDefaultUncaughtExceptionHandler();
        try
        {
            final AtomicReference<Throwable> exception = new AtomicReference<>();
            final Thread.UncaughtExceptionHandler exceptionHandler = mock(Thread.UncaughtExceptionHandler.class);
            doAnswer((Answer<Void>)invocation ->
            {
                exception.set(invocation.getArgument(1));
                return null;
            }).when(exceptionHandler).uncaughtException(any(), any());
            Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

            final IllegalStateException firstError = new IllegalStateException("something went wrong");
            final CountDownLatch first = new CountDownLatch(1);
            assertSame(NO_OP_SIGNAL_HANDLER, register(
                name,
                () ->
                {
                    first.countDown();
                    throw firstError;
                }
            ));

            final MutableBoolean second = new MutableBoolean(false);
            assertNotSame(NO_OP_SIGNAL_HANDLER, register(name, () -> second.set(true)));

            final UncheckedIOException thirdError = new UncheckedIOException(new IOException("I/O error"));
            assertNotSame(NO_OP_SIGNAL_HANDLER, register(
                name,
                () ->
                {
                    throw thirdError;
                }));

            raiseSignal(name);

            first.await();
            assertTrue(second.get());
            while (null == exception.get())
            {
                Thread.yield();
            }

            final Throwable actualError = exception.get();
            assertSame(thirdError, actualError);
            assertSame(firstError, actualError.getSuppressed()[0]);
        }
        finally
        {
            Thread.setDefaultUncaughtExceptionHandler(defaultUncaughtExceptionHandler);
        }
    }
}
