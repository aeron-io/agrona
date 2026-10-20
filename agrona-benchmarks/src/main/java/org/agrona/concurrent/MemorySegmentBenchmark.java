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

import org.openjdk.jmh.annotations.*;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

@Fork(value = 1, jvmArgsAppend = {
    "--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED", "--enable-native-access=ALL-UNNAMED" })
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@State(Scope.Benchmark)
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:MissingJavadocType", "checkstyle:MissingJavadocMethod"})
public class MemorySegmentBenchmark
{
    private static final int CAPACITY = 8 * 1024;

    private OffheapMemorySegmentBuffer segmentOffheap;
    private HeapMemorySegmentBuffer segmentHeap;
    private UnsafeBuffer unsafeOffheap;
    private UnsafeBuffer unsafeHeap;
    private int index;

    @Setup(Level.Trial)
    public void setup()
    {
        segmentOffheap = new OffheapMemorySegmentBuffer(ByteBuffer.allocateDirect(CAPACITY));
        segmentHeap = new HeapMemorySegmentBuffer(new byte[CAPACITY]);
        unsafeOffheap = new UnsafeBuffer(ByteBuffer.allocateDirect(CAPACITY));
        unsafeHeap = new UnsafeBuffer(new byte[CAPACITY]);
        index = 64;
    }

    @Benchmark
    public int getInt_OffheapMemorySegment()
    {
        return segmentOffheap.getInt(index);
    }

    @Benchmark
    public int getInt_OffheapUnsafeBuffer()
    {
        return unsafeOffheap.getInt(index);
    }

    @Benchmark
    public int getInt_HeapMemorySegment()
    {
        return segmentHeap.getInt(index);
    }

    @Benchmark
    public int getInt_HeapUnsafeBuffer()
    {
        return unsafeHeap.getInt(index);
    }

    @Benchmark
    public void putInt_OffheapMemorySegment()
    {
        segmentOffheap.putInt(index, 42);
    }

    @Benchmark
    public void putInt_OffheapUnsafeBuffer()
    {
        unsafeOffheap.putInt(index, 42);
    }

    @Benchmark
    public void putInt_HeapMemorySegment()
    {
        segmentHeap.putInt(index, 42);
    }

    @Benchmark
    public void putInt_HeapUnsafeBuffer()
    {
        unsafeHeap.putInt(index, 42);
    }

    @Benchmark
    public long getLong_OffheapMemorySegment()
    {
        return segmentOffheap.getLong(index);
    }

    @Benchmark
    public long getLong_OffheapUnsafeBuffer()
    {
        return unsafeOffheap.getLong(index);
    }

    @Benchmark
    public long getLong_HeapMemorySegment()
    {
        return segmentHeap.getLong(index);
    }

    @Benchmark
    public long getLong_HeapUnsafeBuffer()
    {
        return unsafeHeap.getLong(index);
    }
}
