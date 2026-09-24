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
    private static boolean isLoaded = false;
    static
    {
        isLoaded = SharedLibraryLoader.load(
            SharedLibraryLoader.resolveNativeLibraryResourcePath("/native/linux", "libagrona-native-lib.so"));
    }

    private ThreadAffinity()
    {
    }

    /**
     * Sets the CPU affinity of the calling thread.
     *
     * @param cpu the id of the CPU core to pin the calling thread to.
     */
    public static void setAffinity(final int cpu)
    {
        if (isLoaded)
        {
            nativeSetAffinity(cpu);
        }
    }

    private static native void nativeSetAffinity(int cpu);

    /**
     * Gets the CPU affinity of the calling thread.
     *
     * @return the number of CPU cores written into {@code cpus}.
     */
    public static int getAffinity()
    {
        if (isLoaded)
        {
            return nativeGetAffinity();
        }
        return NO_AFFINITY;
    }

    private static native int nativeGetAffinity();
}
