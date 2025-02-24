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
package org.agrona.concurrent.status;

import org.agrona.concurrent.AtomicBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AtomicCountersTest
{

    @Test
    public void testPlain()
    {
        final AtomicBuffer buffer = new UnsafeBuffer(new byte[8]);
        final AtomicCounter counter = new AtomicCounter(buffer, 0);

        assertEquals(0, counter.getPlain());

        assertEquals(0, counter.incrementPlain());
        assertEquals(1, counter.getPlain());

        counter.setPlain(42);
        assertEquals(42, counter.getPlain());

        assertEquals(42, counter.decrementPlain());
        assertEquals(41, counter.getPlain());

        assertEquals(41, counter.getAndAddPlain(10));
        assertEquals(51, counter.getPlain());
    }

    @Test
    public void testVolatile()
    {
        final AtomicBuffer buffer = new UnsafeBuffer(new byte[8]);
        final AtomicCounter counter = new AtomicCounter(buffer, 0);

        assertEquals(0, counter.get());

        assertEquals(0, counter.increment());
        assertEquals(1, counter.get());

        counter.set(42);
        assertEquals(42, counter.get());

        assertEquals(42, counter.decrement());
        assertEquals(41, counter.get());

        assertEquals(41, counter.getAndAdd(10));
        assertEquals(51, counter.get());
    }

    @Test
    public void testOpaque()
    {
        final AtomicBuffer buffer = new UnsafeBuffer(new byte[8]);
        final AtomicCounter counter = new AtomicCounter(buffer, 0);

        assertEquals(0, counter.getOpaque());

        assertEquals(0, counter.incrementOpaque());
        assertEquals(1, counter.getOpaque());

        counter.setOpaque(42);
        assertEquals(42, counter.getOpaque());

        assertEquals(42, counter.decrementOpaque());
        assertEquals(41, counter.getOpaque());

        assertEquals(41, counter.getAndAddOpaque(10));
        assertEquals(51, counter.getOpaque());
    }

    @Test
    public void testReleaseAcquire()
    {
        final AtomicBuffer buffer = new UnsafeBuffer(new byte[8]);
        final AtomicCounter counter = new AtomicCounter(buffer, 0);

        assertEquals(0, counter.getAcquire());

        assertEquals(0, counter.incrementRelease());
        assertEquals(1, counter.getAcquire());

        counter.setRelease(42);
        assertEquals(42, counter.getAcquire());

        assertEquals(42, counter.decrementRelease());
        assertEquals(41, counter.getAcquire());

        assertEquals(41, counter.getAndAddRelease(10));
        assertEquals(51, counter.getAcquire());
    }
}
