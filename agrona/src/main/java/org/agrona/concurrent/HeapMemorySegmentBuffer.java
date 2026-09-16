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

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;

import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED;
import static java.lang.foreign.ValueLayout.JAVA_LONG_UNALIGNED;

@SuppressWarnings({"checkstyle:MethodName", "checkstyle:MissingJavadocType", "checkstyle:MissingJavadocMethod"})
public class HeapMemorySegmentBuffer implements AtomicBuffer
{
    private final byte[] byteArray;

    public HeapMemorySegmentBuffer(final byte[] buffer)
    {
        this.byteArray = buffer;
    }

    private MemorySegment segment()
    {
        return MemorySegment.ofArray(byteArray);
    }

    public void wrap(final byte[] arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void wrap(final byte[] arg0, final int arg1, final int arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void wrap(final ByteBuffer arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void wrap(final ByteBuffer arg0, final int arg1, final int arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void wrap(final DirectBuffer arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void wrap(final DirectBuffer arg0, final int arg1, final int arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void wrap(final long arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public long addressOffset()
    {
        throw new UnsupportedOperationException();
    }

    public byte[] byteArray()
    {
        throw new UnsupportedOperationException();
    }

    public ByteBuffer byteBuffer()
    {
        throw new UnsupportedOperationException();
    }

    public int capacity()
    {
        throw new UnsupportedOperationException();
    }

    public void checkLimit(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public long getLong(final int arg0, final ByteOrder arg1)
    {
        throw new UnsupportedOperationException();
    }

    public long getLong(final int index)
    {
        return segment().get(JAVA_LONG_UNALIGNED, index);
    }

    public int getInt(final int arg0, final ByteOrder arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int getInt(final int index)
    {
        return segment().get(JAVA_INT_UNALIGNED, index);
    }

    public int parseNaturalIntAscii(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public long parseNaturalLongAscii(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int parseIntAscii(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public long parseLongAscii(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public double getDouble(final int arg0, final ByteOrder arg1)
    {
        throw new UnsupportedOperationException();
    }

    public double getDouble(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public float getFloat(final int arg0, final ByteOrder arg1)
    {
        throw new UnsupportedOperationException();
    }

    public float getFloat(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public short getShort(final int arg0, final ByteOrder arg1)
    {
        throw new UnsupportedOperationException();
    }

    public short getShort(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public char getChar(final int arg0, final ByteOrder arg1)
    {
        throw new UnsupportedOperationException();
    }

    public char getChar(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public byte getByte(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void getBytes(final int arg0, final byte[] arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void getBytes(final int arg0, final byte[] arg1, final int arg2, final int arg3)
    {
        throw new UnsupportedOperationException();
    }

    public void getBytes(final int arg0, final MutableDirectBuffer arg1, final int arg2, final int arg3)
    {
        throw new UnsupportedOperationException();
    }

    public void getBytes(final int arg0, final ByteBuffer arg1, final int arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void getBytes(final int arg0, final ByteBuffer arg1, final int arg2, final int arg3)
    {
        throw new UnsupportedOperationException();
    }

    public String getStringAscii(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public int getStringAscii(final int arg0, final Appendable arg1)
    {
        throw new UnsupportedOperationException();
    }

    public String getStringAscii(final int arg0, final ByteOrder arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int getStringAscii(final int arg0, final Appendable arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public String getStringAscii(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int getStringAscii(final int arg0, final int arg1, final Appendable arg2)
    {
        throw new UnsupportedOperationException();
    }

    public String getStringWithoutLengthAscii(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int getStringWithoutLengthAscii(final int arg0, final int arg1, final Appendable arg2)
    {
        throw new UnsupportedOperationException();
    }

    public String getStringUtf8(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public String getStringUtf8(final int arg0, final ByteOrder arg1)
    {
        throw new UnsupportedOperationException();
    }

    public String getStringUtf8(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public String getStringWithoutLengthUtf8(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void boundsCheck(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int wrapAdjustment()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isExpandable()
    {
        throw new UnsupportedOperationException();
    }

    public void setMemory(final int arg0, final int arg1, final byte arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void putLong(final int arg0, final long arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void putLong(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putInt(final int arg0, final int arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void putInt(final int index, final int value)
    {
        segment().set(JAVA_INT_UNALIGNED, index, value);
    }

    public int putIntAscii(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int putNaturalIntAscii(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int putNaturalIntAsciiFromEnd(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int putNaturalLongAscii(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int putLongAscii(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putDouble(final int arg0, final double arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void putDouble(final int arg0, final double arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putFloat(final int arg0, final float arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void putFloat(final int arg0, final float arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putShort(final int arg0, final short arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void putShort(final int arg0, final short arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putChar(final int arg0, final char arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void putChar(final int arg0, final char arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putByte(final int arg0, final byte arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putBytes(final int arg0, final byte[] arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putBytes(final int arg0, final byte[] arg1, final int arg2, final int arg3)
    {
        throw new UnsupportedOperationException();
    }

    public void putBytes(final int arg0, final ByteBuffer arg1, final int arg2)
    {
        throw new UnsupportedOperationException();
    }

    public void putBytes(final int arg0, final ByteBuffer arg1, final int arg2, final int arg3)
    {
        throw new UnsupportedOperationException();
    }

    public void putBytes(final int arg0, final DirectBuffer arg1, final int arg2, final int arg3)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringAscii(final int arg0, final String arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringAscii(final int arg0, final CharSequence arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringAscii(final int arg0, final String arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringAscii(final int arg0, final CharSequence arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringWithoutLengthAscii(final int arg0, final String arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringWithoutLengthAscii(final int arg0, final CharSequence arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringWithoutLengthAscii(final int arg0, final String arg1, final int arg2, final int arg3)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringWithoutLengthAscii(final int arg0, final CharSequence arg1, final int arg2, final int arg3)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringUtf8(final int arg0, final String arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringUtf8(final int arg0, final String arg1, final ByteOrder arg2)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringUtf8(final int arg0, final String arg1, final int arg2)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringUtf8(final int arg0, final String arg1, final ByteOrder arg2, final int arg3)
    {
        throw new UnsupportedOperationException();
    }

    public int putStringWithoutLengthUtf8(final int arg0, final String arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void verifyAlignment()
    {
        throw new UnsupportedOperationException();
    }

    public long getLongVolatile(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public long getLongAcquire(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void putLongVolatile(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putLongOrdered(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putLongRelease(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public long addLongOrdered(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putLongOpaque(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public long getLongOpaque(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public long addLongOpaque(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public long addLongRelease(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public boolean compareAndSetLong(final int arg0, final long arg1, final long arg2)
    {
        throw new UnsupportedOperationException();
    }

    public long compareAndExchangeLong(final int arg0, final long arg1, final long arg2)
    {
        throw new UnsupportedOperationException();
    }

    public long getAndSetLong(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public long getAndAddLong(final int arg0, final long arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int getIntVolatile(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void putIntVolatile(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int getIntAcquire(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void putIntOrdered(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putIntRelease(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int addIntOrdered(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int addIntRelease(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putIntOpaque(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int getIntOpaque(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public int addIntOpaque(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public boolean compareAndSetInt(final int arg0, final int arg1, final int arg2)
    {
        throw new UnsupportedOperationException();
    }

    public int compareAndExchangeInt(final int arg0, final int arg1, final int arg2)
    {
        throw new UnsupportedOperationException();
    }

    public int getAndSetInt(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public int getAndAddInt(final int arg0, final int arg1)
    {
        throw new UnsupportedOperationException();
    }

    public short getShortVolatile(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void putShortVolatile(final int arg0, final short arg1)
    {
        throw new UnsupportedOperationException();
    }

    public char getCharVolatile(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void putCharVolatile(final int arg0, final char arg1)
    {
        throw new UnsupportedOperationException();
    }

    public byte getByteVolatile(final int arg0)
    {
        throw new UnsupportedOperationException();
    }

    public void putByteVolatile(final int arg0, final byte arg1)
    {
        throw new UnsupportedOperationException();
    }

    public void putNaturalPaddedIntAscii(final int arg0, final int arg1, final int arg2)
    {
        throw new UnsupportedOperationException();
    }

    public int compareTo(final DirectBuffer arg0)
    {
        throw new UnsupportedOperationException();
    }
}
