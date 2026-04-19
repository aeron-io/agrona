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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.Control;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

@Fork(value = 3, jvmArgsPrepend = {
    "-Dagrona.disable.bounds.checks=true",
    "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
    "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED",
//    "-XX:+UnlockDiagnosticVMOptions",
//    "-XX:+PreserveFramePointer"
//    "-XX:+DebugNonSafepoints"
})
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
//@Measurement(iterations = 10, time = 1)
@Measurement(iterations = 1, time = 600)
@State(Scope.Group)
public class OneToOneConcurrentArrayQueueBenchmark
{
    private static final int CAPACITY = 64 * 1024;
    private static final Integer ELEMENT = 42;

    @Param({ "new"})
    private String impl;

    private Queue<Integer> queue;

    public OneToOneConcurrentArrayQueueBenchmark()
    {
    }

    @Setup(Level.Iteration)
    public void setup()
    {
        switch (impl)
        {
            case "old":
                queue = new OneToOneConcurrentArrayQueue<>(CAPACITY);
                break;
            case "new":
                queue = new FasterOneToOneConcurrentArrayQueue<>(CAPACITY);
                break;
            default:
                throw new IllegalArgumentException("unknown impl: " + impl);
        }
    }

    @TearDown(Level.Iteration)
    public void drain()
    {
        while (null != queue.poll())
        {
        }
    }

    @Benchmark
    @Group("spsc")
    @GroupThreads(1)
    public void offer(final Control ctl)
    {
        while (!ctl.stopMeasurement && !queue.offer(ELEMENT))
        {
            Thread.onSpinWait();
        }
    }

    @Benchmark
    @Group("spsc")
    @GroupThreads(1)
    public void poll(final Control ctl, final Blackhole bh)
    {
        Integer e = null;
        while (!ctl.stopMeasurement && null == (e = queue.poll()))
        {
            Thread.onSpinWait();
        }
        bh.consume(e);
    }
//
//    public static void main(String[] args) throws Exception
//    {
//        System.out.println("foobar");
//        org.openjdk.jmh.Main.main(new String[] {
//            OneToOneConcurrentArrayQueueBenchmark.class.getName(),
//            "-prof", "perfasm:intelSyntax=true;hotThreshold=0.05"
//        });
//    }
}