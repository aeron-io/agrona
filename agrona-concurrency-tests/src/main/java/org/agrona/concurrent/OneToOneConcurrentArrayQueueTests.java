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

import org.agrona.collections.MutableInteger;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.IIII_Result;

import java.util.function.Consumer;

/**
 * JCStressTest for the {@link OneToOneConcurrentArrayQueue}.
 */
public final class OneToOneConcurrentArrayQueueTests
{
    private OneToOneConcurrentArrayQueueTests()
    {
    }

    private static <E> void offerSpin(final OneToOneConcurrentArrayQueue<E> queue, final E value)
    {
        while (!queue.offer(value))
        {
            Thread.onSpinWait();
        }
    }

    private static <E> E pollSpin(final OneToOneConcurrentArrayQueue<E> queue)
    {
        E value;
        while ((value = queue.poll()) == null)
        {
            Thread.onSpinWait();
        }
        return value;
    }

    /**
     * FIFO order, no loss, no duplicates across a wraparound, plus the
     * release/acquire edge on the slot: the value field is written immediately
     * before offer() and read after poll().
     */
    @JCStressTest
    @Outcome(id = "1, 2, 3, 4", expect = Expect.ACCEPTABLE, desc = "FIFO preserved across wraparound")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Reordered, lost, duplicated, or stale field")
    @State
    public static class FifoWithWraparound
    {
        private final OneToOneConcurrentArrayQueue<MutableInteger> queue =
            new OneToOneConcurrentArrayQueue<>(2);
        private final MutableInteger p1 = new MutableInteger();
        private final MutableInteger p2 = new MutableInteger();
        private final MutableInteger p3 = new MutableInteger();
        private final MutableInteger p4 = new MutableInteger();

        /**
         * Producer.
         */
        @Actor
        public void producer()
        {
            p1.value = 1;
            offerSpin(queue, p1);
            p2.value = 2;
            offerSpin(queue, p2);
            p3.value = 3;
            offerSpin(queue, p3);
            p4.value = 4;
            offerSpin(queue, p4);
        }

        /**
         * Consumer.
         *
         * @param r the result
         */
        @Actor
        public void consumer(final IIII_Result r)
        {
            r.r1 = pollSpin(queue).value;
            r.r2 = pollSpin(queue).value;
            r.r3 = pollSpin(queue).value;
            r.r4 = pollSpin(queue).value;
        }
    }

    /**
     * Similar test as FifoWithWraparound, but drained on the consumer side.
     * The release/acquire edge is tested by writing the value field
     * immediately before each offer() and reading it inside accept().
     */
    @JCStressTest
    @Outcome(id = "1, 2, 3, 4", expect = Expect.ACCEPTABLE, desc = "FIFO preserved across wraparound via drain")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Reordered, lost, duplicated, or stale field")
    @State
    public static class FifoWithWraparoundDrain implements Consumer<MutableInteger>
    {
        private final OneToOneConcurrentArrayQueue<MutableInteger> queue =
            new OneToOneConcurrentArrayQueue<>(2);
        private final MutableInteger p1 = new MutableInteger();
        private final MutableInteger p2 = new MutableInteger();
        private final MutableInteger p3 = new MutableInteger();
        private final MutableInteger p4 = new MutableInteger();
        private final int[] received = new int[4];
        private int receivedCount;

        /**
         * Consumes an item and stores it in the received array.
         *
         * @param value the input argument the item to consume.
         */
        public void accept(final MutableInteger value)
        {
            received[receivedCount++] = value.value;
        }

        /**
         * Producer of items.
         */
        @Actor
        public void producer()
        {
            p1.value = 1;
            offerSpin(queue, p1);
            p2.value = 2;
            offerSpin(queue, p2);
            p3.value = 3;
            offerSpin(queue, p3);
            p4.value = 4;
            offerSpin(queue, p4);
        }

        /**
         * Consumer of items.
         *
         * @param r the result.
         */
        @Actor
        public void consumer(final IIII_Result r)
        {
            while (receivedCount < 4)
            {
                queue.drain(this, 4 - receivedCount);
                if (receivedCount < 4)
                {
                    Thread.onSpinWait();
                }
            }
            r.r1 = received[0];
            r.r2 = received[1];
            r.r3 = received[2];
            r.r4 = received[3];
        }
    }
}