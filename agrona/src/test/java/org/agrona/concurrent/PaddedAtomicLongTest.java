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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaddedAtomicLongTest
{
    private PaddedAtomicLong counter;

    @BeforeEach
    void setUp()
    {
        counter = new PaddedAtomicLong();
    }

    // =========================================================================
    // Construction
    // =========================================================================

    @Test
    void defaultConstructorInitialisesToZero()
    {
        assertEquals(0L, counter.get());
    }

    @Test
    void initialValueConstructor()
    {
        assertEquals(42L, new PaddedAtomicLong(42L).get());
    }

    @Test
    void initialValueConstructorNegative()
    {
        assertEquals(-1L, new PaddedAtomicLong(-1L).get());
    }

    @Test
    void initialValueConstructorLongMinValue()
    {
        assertEquals(Long.MIN_VALUE, new PaddedAtomicLong(Long.MIN_VALUE).get());
    }

    @Test
    void initialValueConstructorLongMaxValue()
    {
        assertEquals(Long.MAX_VALUE, new PaddedAtomicLong(Long.MAX_VALUE).get());
    }

    // =========================================================================
    // Number implementation
    // =========================================================================

    @Test
    void intValue()
    {
        counter.set(7L);
        assertEquals(7, counter.intValue());
    }

    @Test
    void longValue()
    {
        counter.set(7L);
        assertEquals(7L, counter.longValue());
    }

    @Test
    void floatValue()
    {
        counter.set(7L);
        assertEquals(7.0f, counter.floatValue());
    }

    @Test
    void doubleValue()
    {
        counter.set(7L);
        assertEquals(7.0, counter.doubleValue());
    }

    @Test
    void toStringReturnsCurrentValue()
    {
        counter.set(123L);
        assertEquals("123", counter.toString());
    }

    // =========================================================================
    // Plain access
    // =========================================================================

    @Nested
    class Plain
    {
        @Test
        void getAndSet()
        {
            counter.setPlain(10L);
            assertEquals(10L, counter.getPlain());
        }

        @Test
        void setOverwritesPreviousValue()
        {
            counter.setPlain(10L);
            counter.setPlain(20L);
            assertEquals(20L, counter.getPlain());
        }

        @Test
        void getAndIncrement()
        {
            counter.setPlain(5L);
            assertEquals(5L, counter.getAndIncrementPlain());
            assertEquals(6L, counter.getPlain());
        }

        @Test
        void incrementAndGet()
        {
            counter.setPlain(5L);
            assertEquals(6L, counter.incrementAndGetPlain());
            assertEquals(6L, counter.getPlain());
        }

        @Test
        void getAndDecrement()
        {
            counter.setPlain(5L);
            assertEquals(5L, counter.getAndDecrementPlain());
            assertEquals(4L, counter.getPlain());
        }

        @Test
        void decrementAndGet()
        {
            counter.setPlain(5L);
            assertEquals(4L, counter.decrementAndGetPlain());
            assertEquals(4L, counter.getPlain());
        }

        @Test
        void getAndAdd()
        {
            counter.setPlain(5L);
            assertEquals(5L, counter.getAndAddPlain(3L));
            assertEquals(8L, counter.getPlain());
        }

        @Test
        void addAndGet()
        {
            counter.setPlain(5L);
            assertEquals(8L, counter.addAndGetPlain(3L));
            assertEquals(8L, counter.getPlain());
        }

        @Test
        void getAndAddNegativeDelta()
        {
            counter.setPlain(5L);
            assertEquals(5L, counter.getAndAddPlain(-3L));
            assertEquals(2L, counter.getPlain());
        }
    }

    // =========================================================================
    // Opaque access
    // =========================================================================

    @Nested
    class Opaque
    {
        @Test
        void getAndSet()
        {
            counter.setOpaque(10L);
            assertEquals(10L, counter.getOpaque());
        }

        @Test
        void getAndIncrement()
        {
            counter.setOpaque(5L);
            assertEquals(5L, counter.getAndIncrementOpaque());
            assertEquals(6L, counter.getOpaque());
        }

        @Test
        void incrementAndGet()
        {
            counter.setOpaque(5L);
            assertEquals(6L, counter.incrementAndGetOpaque());
            assertEquals(6L, counter.getOpaque());
        }

        @Test
        void getAndDecrement()
        {
            counter.setOpaque(5L);
            assertEquals(5L, counter.getAndDecrementOpaque());
            assertEquals(4L, counter.getOpaque());
        }

        @Test
        void decrementAndGet()
        {
            counter.setOpaque(5L);
            assertEquals(4L, counter.decrementAndGetOpaque());
            assertEquals(4L, counter.getOpaque());
        }

        @Test
        void getAndAdd()
        {
            counter.setOpaque(5L);
            assertEquals(5L, counter.getAndAddOpaque(3L));
            assertEquals(8L, counter.getOpaque());
        }

        @Test
        void addAndGet()
        {
            counter.setOpaque(5L);
            assertEquals(8L, counter.addAndGetOpaque(3L));
            assertEquals(8L, counter.getOpaque());
        }

        @Test
        void getAndAddNegativeDelta()
        {
            counter.setOpaque(5L);
            assertEquals(5L, counter.getAndAddOpaque(-3L));
            assertEquals(2L, counter.getOpaque());
        }
    }

    // =========================================================================
    // Acquire / Release access
    // =========================================================================

    @Nested
    class AcquireRelease
    {
        @Test
        void getAcquireReadsValueWrittenWithSetRelease()
        {
            counter.setRelease(99L);
            assertEquals(99L, counter.getAcquire());
        }
    }

    // =========================================================================
    // Volatile access
    // =========================================================================

    @Nested
    class Volatile
    {
        @Test
        void getAndSet()
        {
            counter.set(10L);
            assertEquals(10L, counter.get());
        }

        @Test
        void getAndSetExchange()
        {
            counter.set(10L);
            assertEquals(10L, counter.getAndSet(20L));
            assertEquals(20L, counter.get());
        }
    }

    // =========================================================================
    // Compare-and-exchange
    // =========================================================================

    @Nested
    class CompareAndExchange
    {
        @Test
        void successReturnsExpectedValue()
        {
            counter.set(10L);
            assertEquals(10L, counter.compareAndExchange(10L, 20L));
            assertEquals(20L, counter.get());
        }

        @Test
        void failureReturnsWitnessValue()
        {
            counter.set(10L);
            assertEquals(10L, counter.compareAndExchange(99L, 20L));
            assertEquals(10L, counter.get());
        }

        @Test
        void acquireSucceeds()
        {
            counter.set(10L);
            assertEquals(10L, counter.compareAndExchangeAcquire(10L, 20L));
            assertEquals(20L, counter.get());
        }

        @Test
        void acquireFails()
        {
            counter.set(10L);
            assertEquals(10L, counter.compareAndExchangeAcquire(99L, 20L));
            assertEquals(10L, counter.get());
        }

        @Test
        void releaseSucceeds()
        {
            counter.set(10L);
            assertEquals(10L, counter.compareAndExchangeRelease(10L, 20L));
            assertEquals(20L, counter.get());
        }

        @Test
        void releaseFails()
        {
            counter.set(10L);
            assertEquals(10L, counter.compareAndExchangeRelease(99L, 20L));
            assertEquals(10L, counter.get());
        }
    }

    // =========================================================================
    // Compare-and-set
    // =========================================================================

    @Nested
    class CompareAndSet
    {
        @Test
        void successReturnsTrueAndUpdatesValue()
        {
            counter.set(10L);
            assertTrue(counter.compareAndSet(10L, 20L));
            assertEquals(20L, counter.get());
        }

        @Test
        void failureReturnsFalseAndLeavesValueUnchanged()
        {
            counter.set(10L);
            assertFalse(counter.compareAndSet(99L, 20L));
            assertEquals(10L, counter.get());
        }

        @Test
        void weakVolatileEventuallySucceeds()
        {
            counter.set(10L);
            //noinspection StatementWithEmptyBody
            while (!counter.weakCompareAndSet(10L, 20L))
            {
            }
            assertEquals(20L, counter.get());
        }

        @Test
        void weakAcquireEventuallySucceeds()
        {
            counter.set(10L);
            //noinspection StatementWithEmptyBody
            while (!counter.weakCompareAndSetAcquire(10L, 20L))
            {
            }
            assertEquals(20L, counter.get());
        }

        @Test
        void weakReleaseEventuallySucceeds()
        {
            counter.set(10L);
            //noinspection StatementWithEmptyBody
            while (!counter.weakCompareAndSetRelease(10L, 20L))
            {
            }
            assertEquals(20L, counter.get());
        }

        @Test
        void weakPlainEventuallySucceeds()
        {
            counter.set(10L);
            //noinspection StatementWithEmptyBody
            while (!counter.weakCompareAndSetPlain(10L, 20L))
            {
            }
            assertEquals(20L, counter.get());
        }
    }

    // =========================================================================
    // Add
    // =========================================================================

    @Nested
    class Add
    {
        @Test
        void getAndAdd()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndAdd(3L));
            assertEquals(8L, counter.get());
        }

        @Test
        void addAndGet()
        {
            counter.set(5L);
            assertEquals(8L, counter.addAndGet(3L));
            assertEquals(8L, counter.get());
        }

        @Test
        void getAndAddNegativeDelta()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndAdd(-3L));
            assertEquals(2L, counter.get());
        }

        @Test
        void getAndAddAcquire()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndAddAcquire(3L));
            assertEquals(8L, counter.get());
        }

        @Test
        void addAndGetAcquire()
        {
            counter.set(5L);
            assertEquals(8L, counter.addAndGetAcquire(3L));
            assertEquals(8L, counter.get());
        }

        @Test
        void getAndAddRelease()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndAddRelease(3L));
            assertEquals(8L, counter.get());
        }

        @Test
        void addAndGetRelease()
        {
            counter.set(5L);
            assertEquals(8L, counter.addAndGetRelease(3L));
            assertEquals(8L, counter.get());
        }
    }

    // =========================================================================
    // Increment
    // =========================================================================

    @Nested
    class Increment
    {
        @Test
        void getAndIncrement()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndIncrement());
            assertEquals(6L, counter.get());
        }

        @Test
        void incrementAndGet()
        {
            counter.set(5L);
            assertEquals(6L, counter.incrementAndGet());
            assertEquals(6L, counter.get());
        }

        @Test
        void getAndIncrementAcquire()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndIncrementAcquire());
            assertEquals(6L, counter.get());
        }

        @Test
        void incrementAndGetAcquire()
        {
            counter.set(5L);
            assertEquals(6L, counter.incrementAndGetAcquire());
            assertEquals(6L, counter.get());
        }

        @Test
        void getAndIncrementRelease()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndIncrementRelease());
            assertEquals(6L, counter.get());
        }

        @Test
        void incrementAndGetRelease()
        {
            counter.set(5L);
            assertEquals(6L, counter.incrementAndGetRelease());
            assertEquals(6L, counter.get());
        }
    }

    // =========================================================================
    // Decrement
    // =========================================================================

    @Nested
    class Decrement
    {
        @Test
        void getAndDecrement()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndDecrement());
            assertEquals(4L, counter.get());
        }

        @Test
        void decrementAndGet()
        {
            counter.set(5L);
            assertEquals(4L, counter.decrementAndGet());
            assertEquals(4L, counter.get());
        }

        @Test
        void getAndDecrementAcquire()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndDecrementAcquire());
            assertEquals(4L, counter.get());
        }

        @Test
        void decrementAndGetAcquire()
        {
            counter.set(5L);
            assertEquals(4L, counter.decrementAndGetAcquire());
            assertEquals(4L, counter.get());
        }

        @Test
        void getAndDecrementRelease()
        {
            counter.set(5L);
            assertEquals(5L, counter.getAndDecrementRelease());
            assertEquals(4L, counter.get());
        }

        @Test
        void decrementAndGetRelease()
        {
            counter.set(5L);
            assertEquals(4L, counter.decrementAndGetRelease());
            assertEquals(4L, counter.get());
        }
    }
}