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
package org.agrona.concurrent.affinity;

/**
 * JNI bindings for pinning the calling thread to a CPU core via the underlying OS thread affinity APIs.
 * Backed by a native library that is currently only built for Linux.
 */
public final class ThreadAffinity
{
    /**
     * Identifier for no affinity.
     */
    public static final int NO_AFFINITY = -1;
    private static final boolean IS_LOADED;
    static
    {
        IS_LOADED = SharedLibraryLoader.load("/native/linux", "libagrona-native-lib.so");
    }

    private ThreadAffinity()
    {
    }

    /**
     * Sets the CPU affinity of the calling thread.
     *
     * @param cpu the id of the CPU core to pin the calling thread to.
     * @throws IllegalStateException    if the native library is not loaded.
     * @throws IllegalArgumentException if {@code cpu} is negative.
     * @throws ThreadAffinityException  if the OS fails to set the affinity.
     */
    public static void setAffinity(final int cpu)
    {
        checkLoaded();
        if (cpu < 0)
        {
            throw new IllegalArgumentException("cpu must be non-negative: cpu=" + cpu);
        }
        nativeSetAffinity(cpu);
    }

    private static native void nativeSetAffinity(int cpu);

    /**
     * Sets the CPU affinity of the thread with id {@code tid}.
     *
     * @param tid the id of the thread to pin to the CPU core, or 0 for the calling thread.
     * @param cpu the id of the CPU core to pin the thread to.
     * @throws IllegalStateException    if the native library is not loaded.
     * @throws IllegalArgumentException if {@code tid} or {@code cpu} is negative.
     * @throws ThreadAffinityException  if the OS fails to set the affinity.
     */
    public static void setAffinityFor(final int tid, final int cpu)
    {
        checkLoaded();
        if (tid < 0)
        {
            throw new IllegalArgumentException("tid must be non-negative: tid=" + tid);
        }
        if (cpu < 0)
        {
            throw new IllegalArgumentException("cpu must be non-negative: cpu=" + cpu);
        }
        nativeSetAffinityFor(tid, cpu);
    }

    private static native void nativeSetAffinityFor(int tid, int cpu);

    /**
     * Sets the CPU affinity of the thread with id {@code tid} to the given set of CPU cores.
     *
     * @param tid  the id of the thread to pin, or 0 for the calling thread.
     * @param cpus the ids of the CPU cores the thread may run on.
     * @throws IllegalStateException    if the native library is not loaded.
     * @throws IllegalArgumentException if {@code tid} is negative, {@code cpus} is null or empty, or contains a
     *                                  negative CPU id.
     * @throws ThreadAffinityException  if the OS fails to set the affinity.
     */
    public static void setAffinitiesFor(final int tid, final int[] cpus)
    {
        checkLoaded();
        if (tid < 0)
        {
            throw new IllegalArgumentException("tid must be non-negative: tid=" + tid);
        }
        if (null == cpus || 0 == cpus.length)
        {
            throw new IllegalArgumentException("cpus must not be null or empty");
        }
        for (int i = 0; i < cpus.length; i++)
        {
            if (cpus[i] < 0)
            {
                throw new IllegalArgumentException("cpu must be non-negative: cpus[" + i + "]=" + cpus[i]);
            }
        }
        nativeSetAffinitiesFor(tid, cpus);
    }

    private static native void nativeSetAffinitiesFor(int tid, int[] cpus);

    /**
     * Gets the CPU affinity of the calling thread.
     *
     * @return the id of the lowest numbered CPU core in the calling thread's affinity mask.
     * @throws IllegalStateException   if the native library is not loaded.
     * @throws ThreadAffinityException if the OS fails to get the affinity.
     */
    public static int getAffinity()
    {
        checkLoaded();
        return nativeGetAffinity();
    }

    private static native int nativeGetAffinity();

    /**
     * Gets the CPU affinity for a specific thread with id {@code tid}.
     *
     * @param tid the id of the thread, or 0 for the calling thread.
     * @return the id of the lowest numbered CPU core in the thread's affinity mask.
     * @throws IllegalStateException    if the native library is not loaded.
     * @throws IllegalArgumentException if {@code tid} is negative.
     * @throws ThreadAffinityException  if the OS fails to get the affinity.
     */
    public static int getAffinityFor(final int tid)
    {
        checkLoaded();
        if (tid < 0)
        {
            throw new IllegalArgumentException("tid must be non-negative: tid=" + tid);
        }
        return nativeGetAffinityFor(tid);
    }

    private static native int nativeGetAffinityFor(int tid);

    /**
     * Gets the set of CPU cores the thread with id {@code tid} is allowed to run on.
     *
     * @param tid the id of the thread, or 0 for the calling thread.
     * @return the ids of the CPU cores in the thread's affinity mask.
     * @throws IllegalStateException    if the native library is not loaded.
     * @throws IllegalArgumentException if {@code tid} is negative.
     * @throws ThreadAffinityException  if the OS fails to get the affinity.
     */
    public static int[] getAffinitiesFor(final int tid)
    {
        checkLoaded();
        if (tid < 0)
        {
            throw new IllegalArgumentException("tid must be non-negative: tid=" + tid);
        }
        return nativeGetAffinitiesFor(tid);
    }

    private static void checkLoaded()
    {
        if (!IS_LOADED)
        {
            throw new IllegalStateException("Failed to load native library");
        }
    }

    private static native int[] nativeGetAffinitiesFor(int tid);
}
