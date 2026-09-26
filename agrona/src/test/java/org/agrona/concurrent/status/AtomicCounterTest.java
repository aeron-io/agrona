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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;

import static java.nio.ByteBuffer.allocateDirect;
import static org.agrona.concurrent.status.CountersReader.COUNTER_LENGTH;
import static org.junit.jupiter.api.Assertions.*;

class AtomicCounterTest
{
    @ParameterizedTest
    @MethodSource("buffers")
    void canWrapDifferentKindsOfBuffers(final AtomicBuffer buffer)
    {
        final long value = 42;
        final int counterId = 5;
        final AtomicCounter counter = new AtomicCounter(buffer, counterId);

        counter.set(value);
        counter.increment();
        assertEquals(value + 1, counter.get());
    }

    private static List<AtomicBuffer> buffers()
    {
        return Collections.singletonList(
            new UnsafeBuffer(allocateDirect(10 * COUNTER_LENGTH)));
    }

    @ParameterizedTest
    @MethodSource("buffers")
    void shouldProvideBasicOperations(final AtomicBuffer buffer)
    {
        final int counterId = 1;
        final AtomicCounter counter = new AtomicCounter(buffer, counterId);

        counter.set(10L);
        assertEquals(10L, counter.get());

        assertEquals(10L, counter.getAndAdd(5L));
        assertEquals(15L, counter.get());

        assertEquals(15L, counter.increment());
        assertEquals(16L, counter.decrement());

        counter.setOrdered(20L);
        assertEquals(20L, counter.get());

        assertTrue(counter.compareAndSet(20L, 30L));
        assertEquals(30L, counter.get());
        assertFalse(counter.compareAndSet(20L, 40L));
        assertEquals(30L, counter.get());
    }

    @ParameterizedTest
    @MethodSource("buffers")
    void shouldProposeMax(final AtomicBuffer buffer)
    {
        final int counterId = 1;
        final AtomicCounter counter = new AtomicCounter(buffer, counterId);

        counter.set(10L);
        assertTrue(counter.proposeMax(20L));
        assertEquals(20L, counter.get());

        assertFalse(counter.proposeMax(15L));
        assertEquals(20L, counter.get());
    }

    @ParameterizedTest
    @MethodSource("buffers")
    void shouldHandleMemoryOrderingVariants(final AtomicBuffer buffer)
    {
        final int counterId = 1;
        final AtomicCounter counter = new AtomicCounter(buffer, counterId);

        counter.setPlain(10L);
        assertEquals(10L, counter.getPlain());

        counter.setRelease(11L);
        assertEquals(11L, counter.getAcquire());

        counter.setOpaque(12L);
        assertEquals(12L, counter.getOpaque());

        assertEquals(12L, counter.incrementPlain());
        assertEquals(13L, counter.incrementOrdered());

        assertEquals(14L, counter.decrementPlain());
        assertEquals(13L, counter.decrementOrdered());
    }
}
