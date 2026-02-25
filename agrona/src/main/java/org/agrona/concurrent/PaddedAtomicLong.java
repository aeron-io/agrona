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

/*
 * PaddedAtomicLong - a cache-line padded drop-in replacement for AtomicLong.
 *
 * Pads the hot field with 128 bytes on each side to eliminate false sharing.
 * 128 bytes is chosen because Intel's adjacent cache line prefetcher fetches
 * pairs of 64-byte lines, so 64-byte padding alone is insufficient on x86.
 *
 * Intentionally omitted (legacy / not useful in low-latency code):
 *   - lazySet()                          — use setRelease() directly
 *   - weakCompareAndSet()                — deprecated alias for weakCompareAndSetPlain()
 *   - updateAndGet / getAndUpdate        — retry loop around a lambda; write it explicitly
 *   - accumulateAndGet / getAndAccumulate
 */

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

@SuppressWarnings("serial")
abstract class PaddedAtomicLongLhsPadding extends Number
{
    boolean p000, p001, p002, p003, p004, p005, p006, p007, p008, p009, p010, p011, p012, p013, p014, p015;
    boolean p016, p017, p018, p019, p020, p021, p022, p023, p024, p025, p026, p027, p028, p029, p030, p031;
    boolean p032, p033, p034, p035, p036, p037, p038, p039, p040, p041, p042, p043, p044, p045, p046, p047;
    boolean p048, p049, p050, p051, p052, p053, p054, p055, p056, p057, p058, p059, p060, p061, p062, p063;
    boolean p064, p065, p066, p067, p068, p069, p070, p071, p072, p073, p074, p075, p076, p077, p078, p079;
    boolean p080, p081, p082, p083, p084, p085, p086, p087, p088, p089, p090, p091, p092, p093, p094, p095;
    boolean p096, p097, p098, p099, p100, p101, p102, p103, p104, p105, p106, p107, p108, p109, p110, p111;
    boolean p112, p113, p114, p115, p116, p117, p118, p119, p120, p121, p122, p123, p124, p125, p126, p127;
}

@SuppressWarnings("serial")
abstract class PaddedAtomicLongHotFields extends PaddedAtomicLongLhsPadding
{
    long value;
}

@SuppressWarnings("serial")
abstract class PaddedAtomicLongRhsPadding extends PaddedAtomicLongHotFields
{
    boolean p000, p001, p002, p003, p004, p005, p006, p007, p008, p009, p010, p011, p012, p013, p014, p015;
    boolean p016, p017, p018, p019, p020, p021, p022, p023, p024, p025, p026, p027, p028, p029, p030, p031;
    boolean p032, p033, p034, p035, p036, p037, p038, p039, p040, p041, p042, p043, p044, p045, p046, p047;
    boolean p048, p049, p050, p051, p052, p053, p054, p055, p056, p057, p058, p059, p060, p061, p062, p063;
    boolean p064, p065, p066, p067, p068, p069, p070, p071, p072, p073, p074, p075, p076, p077, p078, p079;
    boolean p080, p081, p082, p083, p084, p085, p086, p087, p088, p089, p090, p091, p092, p093, p094, p095;
    boolean p096, p097, p098, p099, p100, p101, p102, p103, p104, p105, p106, p107, p108, p109, p110, p111;
    boolean p112, p113, p114, p115, p116, p117, p118, p119, p120, p121, p122, p123, p124, p125, p126, p127;
}

/**
 * A padded drop-in replacement for {@link java.util.concurrent.atomic.AtomicLong}.
 *
 * <p>Pads the {@code long} value with 128 bytes on each side to prevent false sharing
 * with adjacent fields, array elements, or other {@code PaddedAtomicLong} instances.
 * 128 bytes is used rather than 64 because Intel's adjacent cache line prefetcher
 * fetches pairs of 64-byte lines, making 64-byte padding alone insufficient on x86.
 *
 * <p>The padding costs 256 bytes of heap per instance. When many counters are needed,
 * an array of {@code PaddedAtomicLong} objects also incurs per-element JVM object overhead
 * on top of the padding; prefer a structure that eliminates that cost by packing values
 * with an explicit stride, such as {@link org.agrona.concurrent.AtomicBuffer}.
 *
 * <h2>Access modes — weakest to strongest</h2>
 * <table border="1"><caption>Access modes</caption>
 *   <tr><th>Mode</th><th>Load</th><th>Store</th><th>Typical use</th></tr>
 *   <tr><td><b>plain</b></td>
 *       <td>{@link #getPlain()}</td><td>{@link #setPlain(long)}</td>
 *       <td>Single-threaded or externally synchronized</td></tr>
 *   <tr><td><b>opaque</b></td>
 *       <td>{@link #getOpaque()}</td><td>{@link #setOpaque(long)}</td>
 *       <td>Atomic + coherent; no cross-variable ordering. Progress indicators, statistics.</td></tr>
 *   <tr><td><b>acquire/release</b></td>
 *       <td>{@link #getAcquire()}</td><td>{@link #setRelease(long)}</td>
 *       <td>Publish/consume handoff — always use as a matched pair.</td></tr>
 *   <tr><td><b>volatile</b></td>
 *       <td>{@link #get()}</td><td>{@link #set(long)}</td>
 *       <td>Full sequential consistency. Default; matches {@code AtomicLong}.</td></tr>
 * </table>
 */
public final class PaddedAtomicLong extends PaddedAtomicLongRhsPadding implements java.io.Serializable
{
    private static final long serialVersionUID = 1927816293512124184L;

    /**
     * {@link VarHandle} for the {@code value} field, exposed for callers that need access modes
     * not covered by the methods of this class (e.g. bitwise atomics such as
     * {@link VarHandle#getAndBitwiseOr}, or custom retry loops using
     * {@link VarHandle#compareAndExchange} directly).
     *
     * <p>The coordinate type is {@code (PaddedAtomicLong)} and the value type is {@code long}.
     * All standard {@link VarHandle} access modes are supported.
     */
    public static final VarHandle VALUE_HANDLE;

    static
    {
        try
        {
            // Lookup must be performed in the declaring class, not this subclass.
            VALUE_HANDLE = MethodHandles.lookup()
                .in(PaddedAtomicLongHotFields.class)
                .findVarHandle(PaddedAtomicLongHotFields.class, "value", long.class);
        }
        catch (final ReflectiveOperationException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@code PaddedAtomicLong} with initial value {@code 0}.
     */
    public PaddedAtomicLong()
    {
        this(0L);
    }

    /**
     * Creates a new {@code PaddedAtomicLong} with the given initial value.
     *
     * @param initialValue the initial value
     */
    public PaddedAtomicLong(final long initialValue)
    {
        VALUE_HANDLE.set(this, initialValue);
    }

    // =========================================================================
    // Plain — no ordering guarantees
    // =========================================================================

    /**
     * Returns the current value, with memory semantics of reading as if the variable
     * was declared non-{@code volatile}.
     *
     * @return the value
     */
    public long getPlain()
    {
        return (long)VALUE_HANDLE.get(this);
    }

    /**
     * Sets the value to {@code newValue}, with memory semantics of setting as if the
     * variable was declared non-{@code volatile} and non-{@code final}.
     *
     * @param newValue the new value
     */
    public void setPlain(final long newValue)
    {
        VALUE_HANDLE.set(this, newValue);
    }

    /**
     * Increments the current value, with memory semantics of reading and writing as if
     * the variable was declared non-{@code volatile} and non-{@code final}.
     * Equivalent to {@code getAndAddPlain(1)}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @return the previous value
     */
    public long getAndIncrementPlain()
    {
        final long current = (long)VALUE_HANDLE.get(this);
        VALUE_HANDLE.set(this, current + 1L);
        return current;
    }

    /**
     * Increments the current value, with memory semantics of reading and writing as if
     * the variable was declared non-{@code volatile} and non-{@code final}.
     * Equivalent to {@code addAndGetPlain(1)}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @return the updated value
     */
    public long incrementAndGetPlain()
    {
        final long next = (long)VALUE_HANDLE.get(this) + 1L;
        VALUE_HANDLE.set(this, next);
        return next;
    }

    /**
     * Adds the given value to the current value, with memory semantics of reading and
     * writing as if the variable was declared non-{@code volatile} and non-{@code final}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @param delta the value to add
     * @return the previous value
     */
    public long getAndAddPlain(final long delta)
    {
        final long current = (long)VALUE_HANDLE.get(this);
        VALUE_HANDLE.set(this, current + delta);
        return current;
    }

    /**
     * Adds the given value to the current value, with memory semantics of reading and
     * writing as if the variable was declared non-{@code volatile} and non-{@code final}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @param delta the value to add
     * @return the updated value
     */
    public long addAndGetPlain(final long delta)
    {
        final long next = (long)VALUE_HANDLE.get(this) + delta;
        VALUE_HANDLE.set(this, next);
        return next;
    }

    /**
     * Decrements the current value, with memory semantics of reading and writing as if
     * the variable was declared non-{@code volatile} and non-{@code final}.
     * Equivalent to {@code getAndAddPlain(-1)}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @return the previous value
     */
    public long getAndDecrementPlain()
    {
        final long current = (long)VALUE_HANDLE.get(this);
        VALUE_HANDLE.set(this, current - 1L);
        return current;
    }

    /**
     * Decrements the current value, with memory semantics of reading and writing as if
     * the variable was declared non-{@code volatile} and non-{@code final}.
     * Equivalent to {@code addAndGetPlain(-1)}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @return the updated value
     */
    public long decrementAndGetPlain()
    {
        final long next = (long)VALUE_HANDLE.get(this) - 1L;
        VALUE_HANDLE.set(this, next);
        return next;
    }

    // =========================================================================
    // Opaque — atomic + coherent, no cross-variable ordering
    // =========================================================================

    /**
     * Returns the current value, with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getOpaque VarHandle.getOpaque}.
     *
     * @return the value
     */
    public long getOpaque()
    {
        return (long)VALUE_HANDLE.getOpaque(this);
    }

    /**
     * Sets the value to {@code newValue}, with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#setOpaque VarHandle.setOpaque}.
     *
     * @param newValue the new value
     */
    public void setOpaque(final long newValue)
    {
        VALUE_HANDLE.setOpaque(this, newValue);
    }

    /**
     * Increments the current value using opaque loads and stores.
     * Equivalent to {@code getAndAddOpaque(1)}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @return the previous value
     */
    public long getAndIncrementOpaque()
    {
        final long current = (long)VALUE_HANDLE.getOpaque(this);
        VALUE_HANDLE.setOpaque(this, current + 1L);
        return current;
    }

    /**
     * Increments the current value using opaque loads and stores.
     * Equivalent to {@code addAndGetOpaque(1)}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @return the updated value
     */
    public long incrementAndGetOpaque()
    {
        final long next = (long)VALUE_HANDLE.getOpaque(this) + 1L;
        VALUE_HANDLE.setOpaque(this, next);
        return next;
    }

    /**
     * Decrements the current value using opaque loads and stores.
     * Equivalent to {@code getAndAddOpaque(-1)}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @return the previous value
     */
    public long getAndDecrementOpaque()
    {
        final long current = (long)VALUE_HANDLE.getOpaque(this);
        VALUE_HANDLE.setOpaque(this, current - 1L);
        return current;
    }

    /**
     * Decrements the current value using opaque loads and stores.
     * Equivalent to {@code addAndGetOpaque(-1)}.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @return the updated value
     */
    public long decrementAndGetOpaque()
    {
        final long next = (long)VALUE_HANDLE.getOpaque(this) - 1L;
        VALUE_HANDLE.setOpaque(this, next);
        return next;
    }

    /**
     * Adds the given value to the current value using opaque loads and stores.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @param delta the value to add
     * @return the previous value
     */
    public long getAndAddOpaque(final long delta)
    {
        final long current = (long)VALUE_HANDLE.getOpaque(this);
        VALUE_HANDLE.setOpaque(this, current + delta);
        return current;
    }

    /**
     * Adds the given value to the current value using opaque loads and stores.
     * <p><strong>Not atomic:</strong> intended for single-writer/multiple-reader use only.</p>
     *
     * @param delta the value to add
     * @return the updated value
     */
    public long addAndGetOpaque(final long delta)
    {
        final long next = (long)VALUE_HANDLE.getOpaque(this) + delta;
        VALUE_HANDLE.setOpaque(this, next);
        return next;
    }

    // =========================================================================
    // Acquire / Release — one-way barriers; always use as a matched pair
    // =========================================================================

    /**
     * Returns the current value, with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAcquire VarHandle.getAcquire}.
     * Must be paired with {@link #setRelease(long)} on the writer side.
     *
     * @return the value
     */
    public long getAcquire()
    {
        return (long)VALUE_HANDLE.getAcquire(this);
    }

    /**
     * Sets the value to {@code newValue}, with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#setRelease VarHandle.setRelease}.
     * Must be paired with {@link #getAcquire()} on the reader side.
     *
     * @param newValue the new value
     */
    public void setRelease(final long newValue)
    {
        VALUE_HANDLE.setRelease(this, newValue);
    }

    // =========================================================================
    // Volatile — full sequential consistency  (AtomicLong-equivalent default)
    // =========================================================================

    /**
     * Returns the current value, with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getVolatile VarHandle.getVolatile}.
     *
     * @return the current value
     */
    public long get()
    {
        return (long)VALUE_HANDLE.getVolatile(this);
    }

    /**
     * Sets the value to {@code newValue}, with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#setVolatile VarHandle.setVolatile}.
     *
     * @param newValue the new value
     */
    public void set(final long newValue)
    {
        VALUE_HANDLE.setVolatile(this, newValue);
    }

    // =========================================================================
    // Atomic exchange
    // =========================================================================

    /**
     * Atomically sets the value to {@code newValue} and returns the old value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndSet VarHandle.getAndSet}.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public long getAndSet(final long newValue)
    {
        return (long)VALUE_HANDLE.getAndSet(this, newValue);
    }

    // =========================================================================
    // Compare-and-exchange — returns the witness value (not a boolean)
    //
    // Prefer these over compare-and-set in tight retry loops: the witness value
    // tells you exactly what was seen, avoiding an extra load on failure.
    // =========================================================================

    /**
     * Atomically sets the value to {@code newValue} if the current value,
     * referred to as the <em>witness value</em>, {@code == expectedValue},
     * with memory effects as specified by {@link java.lang.invoke.VarHandle#compareAndExchange}.
     *
     * @param expectedValue the expected value
     * @param newValue      the new value
     * @return the <em>witness value</em>, which will be the same as {@code expectedValue} if successful
     */
    public long compareAndExchange(final long expectedValue, final long newValue)
    {
        return (long)VALUE_HANDLE.compareAndExchange(this, expectedValue, newValue);
    }

    /**
     * Atomically sets the value to {@code newValue} if the current value,
     * referred to as the <em>witness value</em>, {@code == expectedValue},
     * with memory effects as specified by {@link java.lang.invoke.VarHandle#compareAndExchangeAcquire}.
     *
     * @param expectedValue the expected value
     * @param newValue      the new value
     * @return the <em>witness value</em>, which will be the same as {@code expectedValue} if successful
     */
    public long compareAndExchangeAcquire(final long expectedValue, final long newValue)
    {
        return (long)VALUE_HANDLE.compareAndExchangeAcquire(this, expectedValue, newValue);
    }

    /**
     * Atomically sets the value to {@code newValue} if the current value,
     * referred to as the <em>witness value</em>, {@code == expectedValue},
     * with memory effects as specified by {@link java.lang.invoke.VarHandle#compareAndExchangeRelease}.
     *
     * @param expectedValue the expected value
     * @param newValue      the new value
     * @return the <em>witness value</em>, which will be the same as {@code expectedValue} if successful
     */
    public long compareAndExchangeRelease(final long expectedValue, final long newValue)
    {
        return (long)VALUE_HANDLE.compareAndExchangeRelease(this, expectedValue, newValue);
    }

    // =========================================================================
    // Compare-and-set — boolean result
    // =========================================================================

    /**
     * Atomically sets the value to {@code newValue} if the current value {@code == expectedValue},
     * with memory effects as specified by {@link java.lang.invoke.VarHandle#compareAndSet}.
     *
     * @param expectedValue the expected value
     * @param newValue      the new value
     * @return {@code true} if successful; {@code false} indicates the actual value was not equal to the expected value
     */
    public boolean compareAndSet(final long expectedValue, final long newValue)
    {
        return VALUE_HANDLE.compareAndSet(this, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the value to {@code newValue} if the current value {@code == expectedValue},
     * with memory effects as specified by {@link java.lang.invoke.VarHandle#weakCompareAndSet}.
     * May spuriously fail — must be used in a retry loop.
     *
     * @param expectedValue the expected value
     * @param newValue      the new value
     * @return {@code true} if successful
     */
    public boolean weakCompareAndSet(final long expectedValue, final long newValue)
    {
        return VALUE_HANDLE.weakCompareAndSet(this, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the value to {@code newValue} if the current value {@code == expectedValue},
     * with memory effects as specified by {@link java.lang.invoke.VarHandle#weakCompareAndSetAcquire}.
     * May spuriously fail — must be used in a retry loop.
     *
     * @param expectedValue the expected value
     * @param newValue      the new value
     * @return {@code true} if successful
     */
    public boolean weakCompareAndSetAcquire(final long expectedValue, final long newValue)
    {
        return VALUE_HANDLE.weakCompareAndSetAcquire(this, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the value to {@code newValue} if the current value {@code == expectedValue},
     * with memory effects as specified by {@link java.lang.invoke.VarHandle#weakCompareAndSetRelease}.
     * May spuriously fail — must be used in a retry loop.
     *
     * @param expectedValue the expected value
     * @param newValue      the new value
     * @return {@code true} if successful
     */
    public boolean weakCompareAndSetRelease(final long expectedValue, final long newValue)
    {
        return VALUE_HANDLE.weakCompareAndSetRelease(this, expectedValue, newValue);
    }

    /**
     * Possibly atomically sets the value to {@code newValue} if the current value {@code == expectedValue},
     * with memory effects as specified by {@link java.lang.invoke.VarHandle#weakCompareAndSetPlain}.
     * May spuriously fail — must be used in a retry loop.
     *
     * @param expectedValue the expected value
     * @param newValue      the new value
     * @return {@code true} if successful
     */
    public boolean weakCompareAndSetPlain(final long expectedValue, final long newValue)
    {
        return VALUE_HANDLE.weakCompareAndSetPlain(this, expectedValue, newValue);
    }

    // =========================================================================
    // Atomic add — volatile and acquire/release variants
    // =========================================================================

    /**
     * Atomically adds the given value to the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAdd VarHandle.getAndAdd}.
     *
     * @param delta the value to add
     * @return the previous value
     */
    public long getAndAdd(final long delta)
    {
        return (long)VALUE_HANDLE.getAndAdd(this, delta);
    }

    /**
     * Atomically adds the given value to the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAdd VarHandle.getAndAdd}.
     *
     * @param delta the value to add
     * @return the updated value
     */
    public long addAndGet(final long delta)
    {
        return (long)VALUE_HANDLE.getAndAdd(this, delta) + delta;
    }

    /**
     * Atomically adds the given value to the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddAcquire VarHandle.getAndAddAcquire}.
     *
     * @param delta the value to add
     * @return the previous value
     */
    public long getAndAddAcquire(final long delta)
    {
        return (long)VALUE_HANDLE.getAndAddAcquire(this, delta);
    }

    /**
     * Atomically adds the given value to the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddAcquire VarHandle.getAndAddAcquire}.
     *
     * @param delta the value to add
     * @return the updated value
     */
    public long addAndGetAcquire(final long delta)
    {
        return (long)VALUE_HANDLE.getAndAddAcquire(this, delta) + delta;
    }

    /**
     * Atomically adds the given value to the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddRelease VarHandle.getAndAddRelease}.
     *
     * @param delta the value to add
     * @return the previous value
     */
    public long getAndAddRelease(final long delta)
    {
        return (long)VALUE_HANDLE.getAndAddRelease(this, delta);
    }

    /**
     * Atomically adds the given value to the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddRelease VarHandle.getAndAddRelease}.
     *
     * @param delta the value to add
     * @return the updated value
     */
    public long addAndGetRelease(final long delta)
    {
        return (long)VALUE_HANDLE.getAndAddRelease(this, delta) + delta;
    }

    // =========================================================================
    // Increment — volatile and acquire/release variants
    // =========================================================================

    /**
     * Atomically increments the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAdd VarHandle.getAndAdd}.
     * Equivalent to {@code getAndAdd(1)}.
     *
     * @return the previous value
     */
    public long getAndIncrement()
    {
        return getAndAdd(1L);
    }

    /**
     * Atomically increments the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAdd VarHandle.getAndAdd}.
     * Equivalent to {@code addAndGet(1)}.
     *
     * @return the updated value
     */
    public long incrementAndGet()
    {
        return addAndGet(1L);
    }

    /**
     * Atomically increments the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddAcquire VarHandle.getAndAddAcquire}.
     * Equivalent to {@code getAndAddAcquire(1)}.
     *
     * @return the previous value
     */
    public long getAndIncrementAcquire()
    {
        return getAndAddAcquire(1L);
    }

    /**
     * Atomically increments the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddAcquire VarHandle.getAndAddAcquire}.
     * Equivalent to {@code addAndGetAcquire(1)}.
     *
     * @return the updated value
     */
    public long incrementAndGetAcquire()
    {
        return addAndGetAcquire(1L);
    }

    /**
     * Atomically increments the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddRelease VarHandle.getAndAddRelease}.
     * Equivalent to {@code getAndAddRelease(1)}.
     *
     * @return the previous value
     */
    public long getAndIncrementRelease()
    {
        return getAndAddRelease(1L);
    }

    /**
     * Atomically increments the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddRelease VarHandle.getAndAddRelease}.
     * Equivalent to {@code addAndGetRelease(1)}.
     *
     * @return the updated value
     */
    public long incrementAndGetRelease()
    {
        return addAndGetRelease(1L);
    }

    // =========================================================================
    // Decrement — volatile and acquire/release variants
    // =========================================================================

    /**
     * Atomically decrements the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAdd VarHandle.getAndAdd}.
     * Equivalent to {@code getAndAdd(-1)}.
     *
     * @return the previous value
     */
    public long getAndDecrement()
    {
        return getAndAdd(-1L);
    }

    /**
     * Atomically decrements the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAdd VarHandle.getAndAdd}.
     * Equivalent to {@code addAndGet(-1)}.
     *
     * @return the updated value
     */
    public long decrementAndGet()
    {
        return addAndGet(-1L);
    }

    /**
     * Atomically decrements the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddAcquire VarHandle.getAndAddAcquire}.
     * Equivalent to {@code getAndAddAcquire(-1)}.
     *
     * @return the previous value
     */
    public long getAndDecrementAcquire()
    {
        return getAndAddAcquire(-1L);
    }

    /**
     * Atomically decrements the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddAcquire VarHandle.getAndAddAcquire}.
     * Equivalent to {@code addAndGetAcquire(-1)}.
     *
     * @return the updated value
     */
    public long decrementAndGetAcquire()
    {
        return addAndGetAcquire(-1L);
    }

    /**
     * Atomically decrements the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddRelease VarHandle.getAndAddRelease}.
     * Equivalent to {@code getAndAddRelease(-1)}.
     *
     * @return the previous value
     */
    public long getAndDecrementRelease()
    {
        return getAndAddRelease(-1L);
    }

    /**
     * Atomically decrements the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getAndAddRelease VarHandle.getAndAddRelease}.
     * Equivalent to {@code addAndGetRelease(-1)}.
     *
     * @return the updated value
     */
    public long decrementAndGetRelease()
    {
        return addAndGetRelease(-1L);
    }

    // =========================================================================
    // Number implementation — all reads use volatile for safety
    // =========================================================================

    /**
     * Returns the current value of this {@code PaddedAtomicLong} as an {@code int}
     * after a narrowing primitive conversion,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getVolatile VarHandle.getVolatile}.
     *
     * @return the numeric value represented by this object after conversion to type {@code int}
     */
    @Override
    public int intValue()
    {
        return (int)get();
    }

    /**
     * Returns the current value of this {@code PaddedAtomicLong} as a {@code long},
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getVolatile VarHandle.getVolatile}.
     * Equivalent to {@link #get()}.
     *
     * @return the numeric value represented by this object after conversion to type {@code long}
     */
    @Override
    public long longValue()
    {
        return get();
    }

    /**
     * Returns the current value of this {@code PaddedAtomicLong} as a {@code float}
     * after a widening primitive conversion,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getVolatile VarHandle.getVolatile}.
     *
     * @return the numeric value represented by this object after conversion to type {@code float}
     */
    @Override
    public float floatValue()
    {
        return (float)get();
    }

    /**
     * Returns the current value of this {@code PaddedAtomicLong} as a {@code double}
     * after a widening primitive conversion,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getVolatile VarHandle.getVolatile}.
     *
     * @return the numeric value represented by this object after conversion to type {@code double}
     */
    @Override
    public double doubleValue()
    {
        return (double)get();
    }

    /**
     * Returns the String representation of the current value,
     * with memory effects as specified by
     * {@link java.lang.invoke.VarHandle#getVolatile VarHandle.getVolatile}.
     *
     * @return the String representation of the current value
     */
    @Override
    public String toString()
    {
        return Long.toString(get());
    }
}