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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtomicLongPositionTest
{

    @Test
    void testPlain()
    {
        final AtomicLongPosition position = new AtomicLongPosition();
        position.set(10);
        assertEquals(10, position.get());

        assertFalse(position.proposeMax(5));
        assertEquals(10, position.get());
        assertTrue(position.proposeMax(100));
        assertEquals(100, position.get());

    }

    @Test
    void testVolatile()
    {
        final AtomicLongPosition position = new AtomicLongPosition();

        position.setVolatile(10);
        assertEquals(10, position.getVolatile());
    }

    @Test
    void testAcquireRelease()
    {
        final AtomicLongPosition position = new AtomicLongPosition();

        position.setRelease(10);
        assertEquals(10, position.getAcquire());

        assertFalse(position.proposeMaxRelease(5));
        assertEquals(10, position.getOpaque());
        assertTrue(position.proposeMaxRelease(100));
        assertEquals(100, position.getAcquire());
    }

    @Test
    void testOpaque()
    {
        final AtomicLongPosition position = new AtomicLongPosition();

        position.setOpaque(10);
        assertEquals(10, position.getOpaque());

        assertFalse(position.proposeMaxOpaque(5));
        assertEquals(10, position.getOpaque());
        assertTrue(position.proposeMaxOpaque(100));
        assertEquals(100, position.getOpaque());
    }
}
