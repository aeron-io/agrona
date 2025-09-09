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

import org.agrona.concurrent.ShutdownSignalBarrier.SignalHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class ShutdownSignalBarrierTest
{
    @Test
    void signalAndAwait() throws InterruptedException
    {
        try (ShutdownSignalBarrier barrier = new ShutdownSignalBarrier())
        {
            final AtomicLong awaitTimeNs = new AtomicLong();
            final Thread thread = new Thread(() ->
            {
                barrier.await();
                awaitTimeNs.set(System.nanoTime());
            });

            thread.start();

            Thread.sleep(356);

            assertEquals(0, awaitTimeNs.get());
            final long signalTimeNs = System.nanoTime();

            barrier.signal();

            thread.join();
            assertThat(awaitTimeNs.get(), greaterThanOrEqualTo(signalTimeNs));
        }
    }

    @Test
    void signalAll() throws InterruptedException
    {
        record Test(ShutdownSignalBarrier barrier, AtomicLong awaitTimeNs, Thread thread)
        {
        }

        final List<Test> data = new ArrayList<>();
        for (int i = 0; i < 3; i++)
        {
            final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
            final AtomicLong awaitTimeNs = new AtomicLong();
            final Thread thread = new Thread(() ->
            {
                barrier.await();
                awaitTimeNs.set(System.nanoTime());
            });

            data.add(new Test(barrier, awaitTimeNs, thread));
            thread.start();
        }

        Thread.sleep(123);

        final long signalTimeNs = System.nanoTime();

        data.get(0).barrier.signalAll();

        for (final Test test : data)
        {
            test.thread.join();
        }

        for (final Test test : data)
        {
            assertThat(test.awaitTimeNs.get(), greaterThanOrEqualTo(signalTimeNs));
        }

        for (final Test test : data)
        {
            test.barrier.close();
        }
    }

    @Test
    void notifySignalHandler()
    {
        final AtomicInteger notified = new AtomicInteger(0);
        try (ShutdownSignalBarrier barrier = new ShutdownSignalBarrier(notified::getAndIncrement))
        {
            assertEquals(0, notified.get());

            barrier.signal();
            assertEquals(1, notified.get());
            assertTrue(barrier.signaled.get());
            assertEquals(0, barrier.waitLatch.getCount());
            assertEquals(1, barrier.closeLatch.getCount());

            barrier.signal();
            assertEquals(1, notified.get());
        }
    }

    @Test
    void notifySignalHandlerSignalAll()
    {
        final AtomicInteger notified = new AtomicInteger(0);
        try (ShutdownSignalBarrier barrier = new ShutdownSignalBarrier(notified::getAndDecrement))
        {
            assertEquals(0, notified.get());

            for (int i = 0; i < 100; i++)
            {
                barrier.signalAll();
            }

            assertEquals(-1, notified.get());
            assertTrue(barrier.signaled.get());
            assertEquals(0, barrier.waitLatch.getCount());
            assertEquals(1, barrier.closeLatch.getCount());
        }
    }

    @Test
    void signalAllShouldNotifyAllSignalHandlers()
    {
        final SignalHandler signalHandler1 = mock(SignalHandler.class);
        final SignalHandler signalHandler2 = mock(SignalHandler.class);
        final IllegalArgumentException error1 = new IllegalArgumentException("error1");
        doThrow(error1).when(signalHandler2).onSignal();
        final SignalHandler signalHandler3 = mock(SignalHandler.class);
        final UnsupportedOperationException error2 = new UnsupportedOperationException("error2");
        doThrow(error2).when(signalHandler3).onSignal();

        try (ShutdownSignalBarrier barrier1 = new ShutdownSignalBarrier(signalHandler1);
            ShutdownSignalBarrier barrier2 = new ShutdownSignalBarrier(signalHandler2);
            ShutdownSignalBarrier barrier3 = new ShutdownSignalBarrier(signalHandler3))
        {
            assertNotNull(barrier1);
            assertNotNull(barrier2);

            final IllegalArgumentException exception =
                assertThrowsExactly(IllegalArgumentException.class, barrier3::signalAll);

            assertSame(error1, exception);
            final Throwable[] suppressed = exception.getSuppressed();
            assertNotNull(suppressed);
            assertEquals(1, suppressed.length);
            assertSame(error2, suppressed[0]);
            final InOrder inOrder = inOrder(signalHandler1, signalHandler2, signalHandler3);
            inOrder.verify(signalHandler1).onSignal();
            inOrder.verify(signalHandler2).onSignal();
            inOrder.verify(signalHandler3).onSignal();
            inOrder.verifyNoMoreInteractions();
        }
    }

    @Test
    void awaitShouldSignalIfThreadIsInterrupted()
    {
        assertFalse(Thread.interrupted());
        try
        {
            final SignalHandler signalHandler = mock(SignalHandler.class);
            doThrow(IllegalArgumentException.class).when(signalHandler).onSignal();
            try (ShutdownSignalBarrier shutdownSignalBarrier = new ShutdownSignalBarrier(signalHandler))
            {
                Thread.currentThread().interrupt();

                assertThrowsExactly(IllegalArgumentException.class, shutdownSignalBarrier::await);

                verify(signalHandler).onSignal();
                verifyNoMoreInteractions(signalHandler);
            }
        }
        finally
        {
            assertTrue(Thread.interrupted());
        }
    }

    @Test
    void awaitShouldNotSignalIfLatchIsReleased() throws InterruptedException
    {
        final SignalHandler signalHandler = mock(SignalHandler.class);
        try (ShutdownSignalBarrier shutdownSignalBarrier = new ShutdownSignalBarrier(signalHandler))
        {
            shutdownSignalBarrier.waitLatch.countDown();

            shutdownSignalBarrier.await();

            verifyNoInteractions(signalHandler);
        }
    }

    @Test
    void closeShouldSignal()
    {
        final SignalHandler signalHandler = mock(SignalHandler.class);
        final RuntimeException error = new RuntimeException(new IndexOutOfBoundsException("1/0"));
        doThrow(error).when(signalHandler).onSignal();
        final ShutdownSignalBarrier shutdownSignalBarrier = new ShutdownSignalBarrier(signalHandler);

        assertFalse(shutdownSignalBarrier.signaled.get());
        assertEquals(1, shutdownSignalBarrier.waitLatch.getCount());
        assertEquals(1, shutdownSignalBarrier.closeLatch.getCount());

        final RuntimeException exception = assertThrowsExactly(RuntimeException.class, shutdownSignalBarrier::close);
        assertSame(error, exception);

        assertTrue(shutdownSignalBarrier.signaled.get());
        assertEquals(0, shutdownSignalBarrier.waitLatch.getCount());
        assertEquals(0, shutdownSignalBarrier.closeLatch.getCount());
    }

    @Test
    void awaitTerminationShouldWarnOnTimeoutAndContinueWaiting()
    {
        assertFalse(Thread.interrupted());

        final List<ShutdownSignalBarrier> barriers =
            new ArrayList<>(List.of(new ShutdownSignalBarrier(), new ShutdownSignalBarrier()));
        final PrintStream out = mock(PrintStream.class);
        doAnswer(
            invocationOnMock ->
            {
                final ShutdownSignalBarrier barrier = barriers.remove(0);
                barrier.close();
                return null;
            })
            .when(out)
            .println(ArgumentMatchers.startsWith("WARN: ShutdownSignalBarrier hasn't terminated in"));

        Thread.currentThread().interrupt();
        try
        {
            ShutdownSignalBarrier.awaitTermination(
                barriers.toArray(), 123, TimeUnit.MICROSECONDS, out);
        }
        finally
        {
            assertTrue(Thread.interrupted());
        }

        verify(out, times(2)).println(ArgumentMatchers.anyString());
        verifyNoMoreInteractions(out);
    }

    public static void main(final String[] args)
    {
        class MyResource implements AutoCloseable
        {
            private final String name;

            MyResource(final String name)
            {
                this.name = name;
                System.out.println("Resource created: " + name);
            }

            public void close()
            {
                System.out.println("Resource closed: " + name);
            }

            public String toString()
            {
                return "MyResource{" +
                    "name='" + name + '\'' +
                    '}';
            }
        }

        try (ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
            MyResource resource = new MyResource("test"))
        {
            System.out.println("Awaiting termination: " + resource + " ...");

            barrier.await();
        }

        System.out.println("Shutdown complete!");
    }
}
