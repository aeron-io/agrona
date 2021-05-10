/*
 * Copyright 2014-2021 Real Logic Limited.
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

import org.agrona.hints.ThreadHints;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

abstract class AbstractSnowflakeIdGeneratorPadding
{
    byte p000, p001, p002, p003, p004, p005, p006, p007, p008, p009, p010, p011, p012, p013, p014, p015;
    byte p016, p017, p018, p019, p020, p021, p022, p023, p024, p025, p026, p027, p028, p029, p030, p031;
    byte p032, p033, p034, p035, p036, p037, p038, p039, p040, p041, p042, p043, p044, p045, p046, p047;
    byte p048, p049, p050, p051, p052, p053, p054, p055, p056, p057, p058, p059, p060, p061, p062, p063;
}

abstract class AbstractSnowflakeIdGeneratorValue extends AbstractSnowflakeIdGeneratorPadding
{
    static final AtomicLongFieldUpdater<AbstractSnowflakeIdGeneratorValue> TIMESTAMP_SEQUENCE_UPDATER =
        AtomicLongFieldUpdater.newUpdater(AbstractSnowflakeIdGeneratorValue.class, "timestampSequence");

    volatile long timestampSequence;
}

/**
 * Generate unique identifiers based on the Twitter
 * <a href="https://github.com/twitter-archive/snowflake/tree/snowflake-2010">Snowflake</a> algorithm.
 * <p>
 * This implementation is lock-less resulting in greater throughput plus less contention and latency jitter.
 * <p>
 * <b>Note:</b> ntpd, or alternative clock source, should be setup correctly to ensure the clock does not go backwards.
 */
public final class SnowflakeIdGenerator extends AbstractSnowflakeIdGeneratorValue implements IdGenerator
{
    byte p000, p001, p002, p003, p004, p005, p006, p007, p008, p009, p010, p011, p012, p013, p014, p015;
    byte p016, p017, p018, p019, p020, p021, p022, p023, p024, p025, p026, p027, p028, p029, p030, p031;
    byte p032, p033, p034, p035, p036, p037, p038, p039, p040, p041, p042, p043, p044, p045, p046, p047;
    byte p048, p049, p050, p051, p052, p053, p054, p055, p056, p057, p058, p059, p060, p061, p062, p063;

    /**
     * High order 2's compliment bit which is unused.
     */
    public static final int UNUSED_BITS = 1;

    /**
     * Number of bits used for the timestamp giving 69 years from {@link #timestampOffsetMs()}.
     */
    public static final int EPOCH_BITS = 41;

    /**
     * Total number of bits used to represent the distributed node and the sequence within a millisecond.
     */
    public static final int NODE_ID_AND_SEQUENCE_BITS = 22;

    /**
     * Name of the system property that can be used to set number of bits used to represent the distributed node or
     * application. Default value is {@code 10} bits allowing for 1024 nodes (0-1023). Minimal value is {@code 0}
     * allowing a single node (0).
     *
     * <p>
     * Note: The total number of bits defined by this property and {@link #SEQUENCE_BITS_PROP_NAME} cannot exceed
     * {@link #NODE_ID_AND_SEQUENCE_BITS}.
     * </p>
     *
     * @see #SEQUENCE_BITS_PROP_NAME
     */
    public static final String NODE_ID_BITS_PROP_NAME = "agrona.snowflake.nodeIdBits";

    /**
     * Number of bits used to represent the distributed node or application.
     *
     * @see #NODE_ID_BITS_PROP_NAME
     */
    public static final int NODE_ID_BITS;

    /**
     * Name of the system property that can be used to set number of bits used to represent the sequence within a
     * millisecond. Default value is {@code 12} bits supporting 4,096,000 ids per second per node. Minimal value
     * is {@code 0} which supports only 1 id per second per node.
     *
     * <p>
     * Note: The total number of bits defined by this property and {@link #NODE_ID_BITS_PROP_NAME} cannot exceed
     * {@link #NODE_ID_AND_SEQUENCE_BITS}.
     * </p>
     *
     * @see #NODE_ID_BITS_PROP_NAME
     */
    public static final String SEQUENCE_BITS_PROP_NAME = "agrona.snowflake.sequenceBits";

    /**
     * Number of bits used to represent the sequence within a millisecond.
     *
     * @see #SEQUENCE_BITS_PROP_NAME
     */
    public static final int SEQUENCE_BITS;

    static
    {
        final int nodeIdBits = Integer.getInteger(NODE_ID_BITS_PROP_NAME, 10);
        if (nodeIdBits < 0)
        {
            throw new IllegalArgumentException("must be >= 0: " + NODE_ID_BITS_PROP_NAME + "=" + nodeIdBits);
        }

        final int sequenceBits = Integer.getInteger(SEQUENCE_BITS_PROP_NAME, 12);
        if (sequenceBits < 0)
        {
            throw new IllegalArgumentException("must be >= 0: " + SEQUENCE_BITS_PROP_NAME + "=" + sequenceBits);
        }

        if ((nodeIdBits + sequenceBits) > NODE_ID_AND_SEQUENCE_BITS)
        {
            throw new IllegalArgumentException("too many bits used, must not exceed " + NODE_ID_AND_SEQUENCE_BITS +
                ": " + NODE_ID_BITS_PROP_NAME + "=" + nodeIdBits + ", " + SEQUENCE_BITS_PROP_NAME + "=" + sequenceBits);
        }

        NODE_ID_BITS = nodeIdBits;
        SEQUENCE_BITS = sequenceBits;
    }

    /**
     * Maximum number of nodes given {@link #NODE_ID_BITS}.
     */
    public static final long MAX_NODE_ID = (long)(Math.pow(2, NODE_ID_BITS) - 1);

    /**
     * Maximum sequence within a given millisecond given {@link #SEQUENCE_BITS}.
     */
    public static final long MAX_SEQUENCE = (long)(Math.pow(2, SEQUENCE_BITS) - 1);

    private static final long SEQUENCE_MASK = MAX_SEQUENCE;

    private final long nodeBits;
    private final long timestampOffsetMs;
    private final EpochClock clock;

    /**
     * Construct a new Snowflake id generator for a given node with a provided offset and {@link EpochClock}.
     *
     * @param nodeId            for the node generating ids.
     * @param timestampOffsetMs to adjust the base offset from 1 Jan 1970 UTC to extend the 69 year range.
     * @param clock             to provide timestamps.
     */
    public SnowflakeIdGenerator(final long nodeId, final long timestampOffsetMs, final EpochClock clock)
    {
        if (nodeId < 0 || nodeId > MAX_NODE_ID)
        {
            throw new IllegalArgumentException("must be >= 0 && <= " + MAX_NODE_ID + ": nodeId=" + nodeId);
        }

        if (timestampOffsetMs < 0)
        {
            throw new IllegalArgumentException("must be >= 0: timestampOffsetMs=" + timestampOffsetMs);
        }

        final long nowMs = clock.time();
        if (timestampOffsetMs > nowMs)
        {
            throw new IllegalArgumentException("timestampOffsetMs=" + timestampOffsetMs + " > nowMs=" + nowMs);
        }

        this.nodeBits = nodeId << SEQUENCE_BITS;
        this.timestampOffsetMs = timestampOffsetMs;
        this.clock = clock;
    }

    /**
     * Construct a new Snowflake id generator for a given node with a 0 offset from 1 Jan 1970 UTC and use
     * {@link SystemEpochClock#INSTANCE}.
     *
     * @param nodeId for the node generating ids.
     */
    public SnowflakeIdGenerator(final long nodeId)
    {
        this(nodeId, 0, SystemEpochClock.INSTANCE);
    }

    /**
     * Node identity which scopes the id generation. This is limited to {@link #MAX_NODE_ID}.
     *
     * @return the node identity which scopes the id generation.
     */
    public long nodeId()
    {
        return nodeBits >>> SEQUENCE_BITS;
    }

    /**
     * Offset in milliseconds from Epoch of 1 Jan 1970 UTC which is subtracted to give 69 years of ids.
     * <p>
     * To offset from 1 Jan 2015 UTC then 1420070400000 can be used.
     *
     * @return offset in milliseconds from Epoch of 1 Jan 1970 UTC which is subtracted to give 69 years of ids.
     */
    public long timestampOffsetMs()
    {
        return timestampOffsetMs;
    }

    /**
     * Generate the next id in sequence. If {@link #MAX_SEQUENCE} is reached within the same millisecond then this
     * implementation will busy spin until the next millisecond using {@link ThreadHints#onSpinWait()}.
     *
     * @return the next unique id for this node.
     */
    public long nextId()
    {
        while (true)
        {
            final long oldTimestampSequence = timestampSequence;
            final long timestampMs = clock.time() - timestampOffsetMs;
            final long oldTimestampMs = oldTimestampSequence >>> (NODE_ID_BITS + SEQUENCE_BITS);

            if (timestampMs > oldTimestampMs)
            {
                final long newTimestampSequence = timestampMs << (NODE_ID_BITS + SEQUENCE_BITS);
                if (TIMESTAMP_SEQUENCE_UPDATER.compareAndSet(this, oldTimestampSequence, newTimestampSequence))
                {
                    return newTimestampSequence | nodeBits;
                }
            }
            else if (timestampMs == oldTimestampMs)
            {
                final long oldSequence = oldTimestampSequence & SEQUENCE_MASK;
                if (oldSequence < MAX_SEQUENCE)
                {
                    final long newTimestampSequence = oldTimestampSequence + 1;
                    if (TIMESTAMP_SEQUENCE_UPDATER.compareAndSet(this, oldTimestampSequence, newTimestampSequence))
                    {
                        return newTimestampSequence | nodeBits;
                    }
                }
            }
            else
            {
                throw new IllegalStateException(
                    "clock has gone backwards: timestampMs=" + timestampMs + " < oldTimestampMs=" + oldTimestampMs);
            }

            if (Thread.currentThread().isInterrupted())
            {
                throw new IllegalStateException("unexpected thread interrupt");
            }

            ThreadHints.onSpinWait();
        }
    }
}
