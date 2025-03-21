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

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.JJ_Result;
import org.openjdk.jcstress.infra.results.Z_Result;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Set of concurrency tests for the {@link SnowflakeIdGenerator} class.
 */
public class SnowflakeIdGeneratorTests
{
    SnowflakeIdGeneratorTests()
    {
    }

    /**
     * Test to ensure that the generated id is unique.
     */
    @JCStressTest
    @Outcome(id = "true", expect = Expect.ACCEPTABLE, desc = "Ids are unique")
    @State
    public static class UniquenessTest
    {
        private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(3);

        private long v1;
        private long v2;

        UniquenessTest()
        {
        }

        /**
         * First thread calling increment.
         */
        @Actor
        public void actor1()
        {
            v1 = idGenerator.nextId();
        }

        /**
         * Second thread calling increment.
         */
        @Actor
        public void actor2()
        {
            v2 = idGenerator.nextId();
        }

        /**
         * Arbiter thread checking results of both threads.
         *
         * @param result object.
         */
        @Arbiter
        public void arbiter(final Z_Result result)
        {
            result.r1 = v1 != v2;
        }
    }

    /**
     * Test that verifies atomicity of the id generation when the clock is advanced on every call.
     */
    @JCStressTest
    @Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "T1 before T2")
    @Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "T2 before T1")
    @Outcome(id = "1, 3", expect = Expect.ACCEPTABLE, desc = "T1 wins a CAS with a timestamp of 1")
    @Outcome(id = "3, 1", expect = Expect.ACCEPTABLE, desc = "T2 wins a CAS with a timestamp of 1")
    @Outcome(id = "2, 3", expect = Expect.ACCEPTABLE, desc = "T1 wins a CAS with timestamp of 2")
    @Outcome(id = "3, 2", expect = Expect.ACCEPTABLE, desc = "T2 wins a CAS with timestamp of 2")
    @State
    public static class AdvanceClockTest
    {
        private final AtomicLong clock = new AtomicLong();
        private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(0, 0, 0, 0, clock::getAndIncrement);

        AdvanceClockTest()
        {
        }

        /**
         * First thread calling increment.
         *
         * @param result object.
         */
        @Actor
        public void actor1(final JJ_Result result)
        {
            result.r1 = idGenerator.nextId();
        }

        /**
         * Second thread calling increment.
         *
         * @param result object.
         */
        @Actor
        public void actor2(final JJ_Result result)
        {
            result.r2 = idGenerator.nextId();
        }
    }

    /**
     * Test verifying that incrementing a sequence part of the id is atomic.
     */
    @JCStressTest
    @Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "T1 before T2")
    @Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "T2 before T1")
    @State
    public static class IncrementSequenceTest
    {
        private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(0, 2, 0, 0, () -> 0);

        IncrementSequenceTest()
        {
        }

        /**
         * First thread calling increment.
         *
         * @param result object.
         */
        @Actor
        public void actor1(final JJ_Result result)
        {
            result.r1 = idGenerator.nextId();
        }

        /**
         * Second thread calling increment.
         *
         * @param result object.
         */
        @Actor
        public void actor2(final JJ_Result result)
        {
            result.r2 = idGenerator.nextId();
        }
    }
}
