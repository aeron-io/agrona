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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class MutableReferenceTest
{
    @ParameterizedTest
    @ValueSource(strings = { "", "abc", "Hello World" })
    void shouldSetAndGet(final String value)
    {
        final MutableReference<String> mutableReference = new MutableReference<>();
        assertEquals(value, mutableReference.setAndGet(value));
        assertSame(value, mutableReference.get());
    }

    @Test
    void shouldSetAndGetNull()
    {
        final MutableReference<String> mutableReference = new MutableReference<>("initial");
        assertNull(mutableReference.setAndGet(null));
        assertNull(mutableReference.get());
    }
}
