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

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import static java.nio.ByteBuffer.allocateDirect;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.agrona.concurrent.status.CountersReader.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentCountersManagerTest
{
    private static final int NUMBER_OF_COUNTERS = 4;
    private final UnsafeBuffer metadataBuffer =
        new UnsafeBuffer(allocateDirect(NUMBER_OF_COUNTERS * METADATA_LENGTH));
    private final UnsafeBuffer valuesBuffer =
        new UnsafeBuffer(allocateDirect(NUMBER_OF_COUNTERS * COUNTER_LENGTH));
    private final ConcurrentCountersManager manager =
        new ConcurrentCountersManager(metadataBuffer, valuesBuffer, US_ASCII);
    private final CountersReader reader = manager;

    @Test
    void shouldAllocateAndFree()
    {
        final int id1 = manager.allocate("label1");
        final int id2 = manager.allocate("label2");

        assertEquals(0, id1);
        assertEquals(1, id2);

        manager.free(id1);
        final int id3 = manager.allocate("label3");

        assertEquals(0, id3);
    }

    @Test
    void shouldSetCounterValue()
    {
        final int id = manager.allocate("label");
        manager.setCounterValue(id, 42L);

        assertEquals(42L, manager.getCounterValue(id));
    }

    @Test
    void shouldSetMetadataFields()
    {
        final int id = manager.allocate("label");

        manager.setCounterRegistrationId(id, 100L);
        manager.setCounterOwnerId(id, 200L);
        manager.setCounterReferenceId(id, 300L);

        assertEquals(100L, manager.getCounterRegistrationId(id));
        assertEquals(200L, manager.getCounterOwnerId(id));
        assertEquals(300L, manager.getCounterReferenceId(id));
    }

    @Test
    void shouldUpdateLabel()
    {
        final int id = manager.allocate("label");
        manager.setCounterLabel(id, "new label");

        assertEquals("new label", manager.getCounterLabel(id));
    }

    @Test
    void shouldAppendToLabel()
    {
        final int id = manager.allocate("label");
        manager.appendToLabel(id, " append");

        assertEquals("label append", manager.getCounterLabel(id));
    }

    @Test
    void shouldSetKey()
    {
        final int id = manager.allocate("label");
        final UnsafeBuffer key = new UnsafeBuffer(new byte[8]);
        key.putLong(0, 777L);

        manager.setCounterKey(id, key, 0, 8);

        final int metaDataOffset = CountersReader.metaDataOffset(id);
        assertEquals(777L, metadataBuffer.getLong(metaDataOffset + KEY_OFFSET));
    }
}
