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
package org.agrona.collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LongArrayListTest
{
    @ParameterizedTest
    @ValueSource(longs = { Long.MIN_VALUE, -1, 1099511627776L })
    void removeShouldDistinguishNullFromItsSentinel(final long nullValue)
    {
        final LongArrayList target = new LongArrayList(4, nullValue);
        assertFalse(target.remove(Long.valueOf(nullValue)));
        assertFalse(target.remove(null));

        target.addLong(7L);
        target.add(null);
        target.addLong(8L);
        target.add(null);

        assertFalse(target.remove(Long.valueOf(nullValue)));
        assertEquals(Arrays.asList(7L, null, 8L, null), target);

        assertTrue(target.remove(null));
        assertEquals(Arrays.asList(7L, 8L, null), target);

        assertTrue(target.removeLong(nullValue));
        assertEquals(Arrays.asList(7L, 8L), target);

        assertTrue(target.remove(Long.valueOf(7L)));
        assertFalse(target.remove(Long.valueOf(9L)));
        assertEquals(Arrays.asList(8L), target);
    }

    @ParameterizedTest
    @ValueSource(longs = { Long.MIN_VALUE, -1, 1099511627776L })
    void containsShouldDistinguishNullFromItsSentinel(final long nullValue)
    {
        final LongArrayList target = new LongArrayList(2, nullValue);
        assertFalse(target.contains(null));
        assertFalse(target.contains(nullValue));

        target.addLong(nullValue);
        target.addLong(7);

        assertTrue(target.contains(null));
        assertFalse(target.contains(nullValue));
        assertTrue(target.containsLong(nullValue));
        assertTrue(target.contains(7L));
        assertFalse(target.contains(8L));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void containsAllOverloadsShouldAgreeForDifferentNullValues(final boolean reverse)
    {
        final LongArrayList nulls = new LongArrayList(new long[]{ -1 }, 1, -1);
        final LongArrayList values = new LongArrayList(new long[]{ -1 }, 1, -2);
        final LongArrayList target = reverse ? values : nulls;
        final LongArrayList source = reverse ? nulls : values;

        assertFalse(target.containsAll(source));
        assertFalse(target.containsAll((Collection<Long>)source));
        assertFalse(target.containsAll(new ArrayList<>(source)));

        values.clear();
        values.add(null);

        assertTrue(target.containsAll(source));
        assertTrue(target.containsAll((Collection<Long>)source));
        assertTrue(target.containsAll(new ArrayList<>(source)));
    }
}
