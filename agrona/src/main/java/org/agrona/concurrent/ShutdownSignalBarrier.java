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

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * One time witness for blocking one or more threads until:
 * <ul>
 *     <li>Thread blocked in {@link #await()} is interrupted.</li>
 *     <li>JVM shutdown sequence begins (see {@link Runtime#addShutdownHook(Thread)}).</li>
 *     <li>{@link #signal()} or {@link #signalAll()} is invoked programmatically.</li>
 * </ul>
 * Useful for shutting down a service.
 *
 * <p><em><strong>Note: </strong> {@link ShutdownSignalBarrier} must be closed last to ensure complete service(s)
 * termination and to allow JVM to exit. Not calling {@link #close()} method might result in JVM hanging
 * indefinitely.</em>
 *
 * <p>Here is an example on how to use this API correctly.
 * <pre>
 * {@code
 * class UsageSample
 * {
 *   public static void main(final String[] args)
 *   {
 *     try (ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();
 *         MyService service = new MyService())
 *     {
 *          barrier.await();
 *     }
 *   }
 * }
 *
 * class MyService implements AutoCloseable
 * {
 *     ...
 * }
 * }</pre>
 *
 * @see Runtime#addShutdownHook(Thread)
 */
public final class ShutdownSignalBarrier implements AutoCloseable
{
    private static final CopyOnWriteArrayList<ShutdownSignalBarrier> BARRIERS = new CopyOnWriteArrayList<>();
    private static final Thread SIGNAL_ALL_SHUTDOWN_HOOK = new Thread(() ->
    {
        final Object[] barriers = signalAndClearAll();
        awaitTermination(barriers);
    }, "ShutdownSignalBarrier");

    static
    {
        Runtime.getRuntime().addShutdownHook(SIGNAL_ALL_SHUTDOWN_HOOK);
    }

    private final CountDownLatch waitLatch = new CountDownLatch(1);
    private final CountDownLatch closeLatch = new CountDownLatch(1);

    /**
     * Construct and register the witness ready for use.
     */
    public ShutdownSignalBarrier()
    {
        BARRIERS.add(this);
    }

    /**
     * Programmatically signal awaiting thread on the latch associated with this witness.
     */
    public void signal()
    {
        BARRIERS.remove(this);
        waitLatch.countDown();
    }

    /**
     * Programmatically signal all awaiting threads.
     */
    public void signalAll()
    {
        if (Runtime.getRuntime().removeShutdownHook(SIGNAL_ALL_SHUTDOWN_HOOK))
        {
            signalAndClearAll();
        }
    }

    /**
     * Remove the witness from the shutdown signals.
     */
    public void remove()
    {
        BARRIERS.remove(this);
    }

    /**
     * Await the reception of the shutdown signal.
     */
    public void await()
    {
        try
        {
            waitLatch.await();
        }
        catch (final InterruptedException ignore)
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Close this {@link ShutdownSignalBarrier} to allow JVM termination.
     */
    public void close()
    {
        signal();
        closeLatch.countDown();
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "ShutdownSignalBarrier{" +
            "waitLatch=" + waitLatch +
            ", closeLatch=" + closeLatch +
            '}';
    }

    private static Object[] signalAndClearAll()
    {
        final Object[] barriers = BARRIERS.toArray();
        BARRIERS.clear();

        for (final Object barrier : barriers)
        {
            ((ShutdownSignalBarrier)barrier).waitLatch.countDown();
        }

        return barriers;
    }

    private static void awaitTermination(final Object[] barriers)
    {
        boolean wasInterruped = false;
        try
        {
            for (final Object barrier : barriers)
            {
                try
                {
                    ((ShutdownSignalBarrier)barrier).closeLatch.await();
                }
                catch (final InterruptedException e)
                {
                    wasInterruped = true;
                }
            }
        }
        finally
        {
            if (wasInterruped)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
}
