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

    @Test
    void shouldDumpPrettyHex()
    {
        final byte[] bytes = new byte[300];

        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = (byte)i;
        }

        final DirectBuffer buffer = new UnsafeBuffer(bytes);

        String expected =
            """
                         +-------------------------------------------------+
                         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
                +--------+-------------------------------------------------+----------------+
                |00000000| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
                |00000010| 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f |................|
                |00000020| 20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f | !"#$%&'()*+,-./|
                |00000030| 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f |0123456789:;<=>?|
                |00000040| 40 41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f |@ABCDEFGHIJKLMNO|
                |00000050| 50 51 52 53 54 55 56 57 58 59 5a 5b 5c 5d 5e 5f |PQRSTUVWXYZ[\\]^_|
                |00000060| 60 61 62 63 64 65 66 67 68 69 6a 6b 6c 6d 6e 6f |`abcdefghijklmno|
                |00000070| 70 71 72 73 74 75 76 77 78 79 7a 7b 7c 7d 7e 7f |pqrstuvwxyz{|}~.|
                |00000080| 80 81 82 83 84 85 86 87 88 89 8a 8b 8c 8d 8e 8f |................|
                |00000090| 90 91 92 93 94 95 96 97 98 99 9a 9b 9c 9d 9e 9f |................|
                |000000a0| a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa ab ac ad ae af |................|
                |000000b0| b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd be bf |................|
                |000000c0| c0 c1 c2 c3 c4 c5 c6 c7 c8 c9 ca cb cc cd ce cf |................|
                |000000d0| d0 d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df |................|
                |000000e0| e0 e1 e2 e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef |................|
                |000000f0| f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc fd fe ff |................|
                |00000100| 00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f |................|
                |00000110| 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f |................|
                |00000120| 20 21 22 23 24 25 26 27 28 29 2a 2b             | !"#$%&'()*+    |
                +--------+-------------------------------------------------+----------------+""";

        expected = expected.replace("\n", System.lineSeparator());
        assertEquals(expected, PrintBufferUtil.prettyHexDump(buffer));
    }
}
