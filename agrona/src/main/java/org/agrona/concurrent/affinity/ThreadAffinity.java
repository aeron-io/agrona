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
 * JNI bindings for pinning threads to CPU cores via the underlying OS thread affinity APIs.
 * Backed by a native library that is currently only built for Linux.
 */
public final class ThreadAffinity
{
    /**
     * Identifier for no affinity.
     */
    public static final int NO_AFFINITY = -1;

    /**
     * Thread id which identifies the calling thread.
     */
    public static final int CURRENT_THREAD = 0;

    private static final boolean IS_LOADED;
    static
    {
        IS_LOADED = SharedLibraryLoader.load();
    }

    private ThreadAffinity()
    {
    }

    /**
     * Sets the CPU affinity of the thread with id {@code tid} to the given set of CPU cores.
     *
     * @param tid  the OS id of the thread to pin, or {@link #CURRENT_THREAD} for the calling thread.
     * @param cpus the ids of the CPU cores the thread may run on.
     * @throws IllegalStateException    if the native library is not loaded.
     * @throws IllegalArgumentException if {@code tid} is negative, {@code cpus} is null or empty, or contains a
     *                                  negative CPU id.
     * @throws ThreadAffinityException  if the OS fails to set the affinity.
     */
    public static void setAffinity(final int tid, final int[] cpus)
    {
        checkLoaded();
        validateTid(tid);
        validateCpus(cpus);
        nativeSetAffinity(tid, cpus);
    }

    /**
     * Gets the set of CPU cores the thread with id {@code tid} is allowed to run on.
     *
     * @param tid the OS id of the thread, or {@link #CURRENT_THREAD} for the calling thread.
     * @return the ids of the CPU cores in the thread's affinity mask.
     * @throws IllegalStateException    if the native library is not loaded.
     * @throws IllegalArgumentException if {@code tid} is negative.
     * @throws ThreadAffinityException  if the OS fails to get the affinity.
     */
    public static int[] getAffinity(final int tid)
    {
        checkLoaded();
        validateTid(tid);
        return nativeGetAffinity(tid);
    }

    private static void checkLoaded()
    {
        if (!IS_LOADED)
        {
            throw new IllegalStateException("Failed to load native library");
        }
    }

    private static void validateTid(final int tid)
    {
        if (tid < 0)
        {
            throw new IllegalArgumentException("tid must be non-negative: tid=" + tid);
        }
    }

    private static void validateCpus(final int[] cpus)
    {
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
    }

    private static native void nativeSetAffinity(int tid, int[] cpus);

    private static native int[] nativeGetAffinity(int tid);
}
