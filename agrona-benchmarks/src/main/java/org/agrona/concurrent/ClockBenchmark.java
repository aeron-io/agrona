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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks for the different clock implementations.
 */
@Fork(value = 3, jvmArgsPrepend = "-Dagrona.disable.bounds.checks=true")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@State(Scope.Benchmark)
public class ClockBenchmark
{
    private static final OffsetEpochNanoClock OFFSET_EPOCH_NANO_CLOCK = new OffsetEpochNanoClock();

    /**
     * Default constructor.
     */
    public ClockBenchmark()
    {
    }

    /**
     * Benchmark {@link System#nanoTime()} method.
     *
     * @return time in nanoseconds.
     */
    @Benchmark
    public long nanoTime()
    {
        return System.nanoTime();
    }

    /**
     * Benchmark {@link System#currentTimeMillis()} method.
     *
     * @return time in nanoseconds.
     */
    @Benchmark
    public long currentTimeMillis()
    {
        return System.currentTimeMillis();
    }

    /**
     * Benchmark {@link SystemNanoClock#nanoTime()} method.
     *
     * @return time in nanoseconds.
     */
    @Benchmark
    public long nanoClock()
    {
        return SystemNanoClock.INSTANCE.nanoTime();
    }

    /**
     * Benchmark {@link SystemEpochClock#time()} method.
     *
     * @return time in milliseconds.
     */
    @Benchmark
    public long epochClock()
    {
        return SystemEpochClock.INSTANCE.time();
    }

    /**
     * Benchmark {@link SystemEpochNanoClock#nanoTime()} method.
     *
     * @return time in nanoseconds.
     */
    @Benchmark
    public long epochNanoClock()
    {
        return SystemEpochNanoClock.INSTANCE.nanoTime();
    }

    /**
     * Benchmark {@link OffsetEpochNanoClock#nanoTime()} method.
     *
     * @return time in nanoseconds.
     */
    @Benchmark
    public long offsetEpochNanoClock()
    {
        return OFFSET_EPOCH_NANO_CLOCK.nanoTime();
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
            .include(ClockBenchmark.class.getName())
            .shouldFailOnError(true)
            .build())
            .run();
    }
}
