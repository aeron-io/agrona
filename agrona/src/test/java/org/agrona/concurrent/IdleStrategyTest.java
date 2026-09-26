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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdleStrategyTest
{
    @Test
    void shouldExerciseNoOpIdleStrategy()
    {
        final IdleStrategy strategy = NoOpIdleStrategy.INSTANCE;
        strategy.idle(1);
        strategy.idle(0);
        strategy.reset();
        assertEquals("noop", strategy.alias());
    }

    @Test
    void shouldExerciseYieldingIdleStrategy()
    {
        final IdleStrategy strategy = YieldingIdleStrategy.INSTANCE;
        strategy.idle(1);
        strategy.idle(0);
        strategy.reset();
        assertEquals("yield", strategy.alias());
    }

    @Test
    void shouldExerciseBusySpinIdleStrategy()
    {
        final IdleStrategy strategy = BusySpinIdleStrategy.INSTANCE;
        strategy.idle(1);
        strategy.idle(0);
        strategy.reset();
        assertEquals("spin", strategy.alias());
    }

    @Test
    void shouldExerciseSleepingIdleStrategy()
    {
        final IdleStrategy strategy = new SleepingIdleStrategy(1L);
        strategy.idle(1);
        strategy.idle(0);
        strategy.reset();
        assertEquals("sleep-ns", strategy.alias());
    }

    @Test
    void shouldExerciseSleepingMillisIdleStrategy()
    {
        final IdleStrategy strategy = new SleepingMillisIdleStrategy(1L);
        strategy.idle(1);
        strategy.idle(0);
        strategy.reset();
        assertEquals("sleep-ms", strategy.alias());
    }

    @Test
    void shouldExerciseBackoffIdleStrategy()
    {
        final IdleStrategy strategy = new BackoffIdleStrategy(1, 1, 1, 1);
        strategy.idle(1);
        strategy.idle(0);
        strategy.reset();
        assertEquals("backoff", strategy.alias());
    }
}
