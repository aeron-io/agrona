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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Benchmark for the {@link IntArrayList}.
 */
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@State(Scope.Benchmark)
public class IntArrayListBenchmark
{
    @Param({ "16", "1024", "65536" })
    private int size;

    private IntArrayList list;

    /**
     * Default constructor.
     */
    public IntArrayListBenchmark()
    {
    }

    /**
     * Setup.
     */
    @Setup
    public void setup()
    {
        list = new IntArrayList(size, Integer.MIN_VALUE);
    }

    /**
     * Add {@code size} elements to a new list created with the default arguments.
     *
     * @return the list.
     */
    @Benchmark
    public IntArrayList addInt()
    {
        final IntArrayList list = this.list;
        list.clear();

        for (int i = 0; i < size; i++)
        {
            list.addInt(i);
        }
        return list;
    }

    /**
     * Runner method that allows starting benchmark directly.
     *
     * @param args for the main method.
     * @throws RunnerException in case if JMH throws while starting the benchmark.
     */
    public static void main(final String[] args) throws RunnerException
    {
        new Runner(new OptionsBuilder()
            .include(IntArrayListBenchmark.class.getName()).shouldFailOnError(true).build())
            .run();
    }
}
