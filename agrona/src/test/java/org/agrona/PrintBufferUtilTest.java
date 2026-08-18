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
package org.agrona;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

class PrintBufferUtilTest
{
    @Test
    void shouldPrettyPrintHex()
    {
        final String contents = "Hello World!\nThis is a test String\nto print out.";
        final ExpandableArrayBuffer buffer = new ExpandableArrayBuffer();

        buffer.putStringAscii(0, contents);

        final StringBuilder builder = new StringBuilder();
        PrintBufferUtil.appendPrettyHexDump(builder, buffer);
        assertThat(builder.toString(), containsString("0...Hello World!"));
    }

    @Test
    void shouldAppendHex()
    {
        final byte[] bytes = new byte[255];
        final Random r = ThreadLocalRandom.current();
        r.nextBytes(bytes);

        final StringBuilder sb = new StringBuilder();
        for (final byte b : bytes)
        {
            final String hexString = Integer.toHexString((0xFF) & b);
            sb.append(hexString.length() < 2 ? "0" + hexString : hexString);
        }
        final String expected = sb.toString();

        assertEquals(expected, PrintBufferUtil.hexDump(bytes));
        assertEquals(expected, PrintBufferUtil.hexDump(bytes, 0, bytes.length));
        assertEquals(expected, PrintBufferUtil.hexDump(new UnsafeBuffer(bytes)));
        assertEquals(expected, PrintBufferUtil.hexDump(new UnsafeBuffer(bytes), 0, bytes.length));

        final StringBuilder input = new StringBuilder();
        PrintBufferUtil.appendHexDump(input, new UnsafeBuffer(bytes), 0, bytes.length);
        assertEquals(expected, input.toString());

        input.setLength(0);
        PrintBufferUtil.appendHexDump(input, new UnsafeBuffer(bytes));
        assertEquals(expected, input.toString());

        input.setLength(0);
        PrintBufferUtil.appendHexDump(input, bytes, 0, bytes.length);
        assertEquals(expected, input.toString());

        input.setLength(0);
        PrintBufferUtil.appendHexDump(input, bytes);
        assertEquals(expected, input.toString());
    }
}
