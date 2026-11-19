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
package org.agrona.io;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectBufferInputStreamTest
{
    private static final int END_OF_STREAM_MARKER = -1;

    @Test
    void shouldCorrectlyConvertBytesToPositiveIntegers()
    {
        final byte[] data = { (byte)-1, 0 };
        final DirectBuffer buffer = new UnsafeBuffer(data);
        final DirectBufferInputStream inputStream = new DirectBufferInputStream(buffer);

        assertEquals(255, inputStream.read());
    }

    @Test
    void shouldReturnMinusOneOnEndOfStream()
    {
        final byte[] data = { 1, 2 };

        final DirectBuffer buffer = new UnsafeBuffer(data);
        final DirectBufferInputStream inputStream = new DirectBufferInputStream(buffer);

        assertEquals(1, inputStream.read());
        assertEquals(2, inputStream.read());
        assertEquals(END_OF_STREAM_MARKER, inputStream.read());
    }

    @Test
    void shouldClampSkippedBytes()
    {
        final byte[] data = { 1, 2, 3, 4, 5 };

        final DirectBuffer buffer = new UnsafeBuffer(data);
        final DirectBufferInputStream inputStream = new DirectBufferInputStream(buffer);

        assertEquals(0, inputStream.skip(-1));
        assertEquals(5, inputStream.skip(10));
        assertEquals(-2, inputStream.skip(-2));
        assertEquals(-3, inputStream.skip(-3));
        assertEquals(0, inputStream.skip(-4));
        assertEquals(4, inputStream.skip(4));
        assertEquals(1, inputStream.skip(5));
        assertEquals(0, inputStream.skip(Integer.MAX_VALUE));
    }
}
