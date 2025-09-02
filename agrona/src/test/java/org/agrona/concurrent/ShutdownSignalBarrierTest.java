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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShutdownSignalBarrierTest
{
    @Test
    void signalAndAwait() throws InterruptedException
    {
        final ShutdownSignalBarrier barrier = new ShutdownSignalBarrier();

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
    }

    @Test
    void isActiveSignal()
    {
        try (ShutdownSignalBarrier barrier = new ShutdownSignalBarrier())
        {
            assertTrue(barrier.isActive());

            barrier.signal();

            assertFalse(barrier.isActive());
        }
    }

    @Test
    void isActiveSignalAll()
    {
        try (ShutdownSignalBarrier barrier = new ShutdownSignalBarrier())
        {
            assertTrue(barrier.isActive());

            barrier.signalAll();

            assertFalse(barrier.isActive());
        }
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
