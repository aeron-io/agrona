/*
 * Copyright 2014-2026 Real Logic Limited.
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

import org.agrona.LangUtil;
import org.agrona.SystemUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ThreadAffinityTest
{
    @Test
    void setAndGetAffinity() throws InterruptedException
    {
        assumeTrue(SystemUtil.isLinux());
        runOnNewThread(() ->
        {
            final int[] available = ThreadAffinity.getAffinitiesFor(0);
            assumeTrue(available.length >= 2, "requires at least 2 available CPUs");
            final int cpu = available[available.length - 1];
            ThreadAffinity.setAffinity(cpu);
            assertEquals(cpu, ThreadAffinity.getAffinity());
        });
    }

    @Test
    void setAndGetAffinityExplicitlyForCurrentThread() throws InterruptedException
    {
        assumeTrue(SystemUtil.isLinux());
        runOnNewThread(() ->
        {
            final int[] available = ThreadAffinity.getAffinitiesFor(0);
            assumeTrue(available.length >= 2, "requires at least 2 available CPUs");
            final int cpu = available[available.length - 1];
            ThreadAffinity.setAffinityFor(0, cpu);
            assertEquals(cpu, ThreadAffinity.getAffinityFor(0));
        });
    }

    @Test
    void setAndGetMultipleAffinities() throws InterruptedException
    {
        assumeTrue(SystemUtil.isLinux());
        runOnNewThread(() ->
        {
            final int[] available = ThreadAffinity.getAffinitiesFor(0);
            assumeTrue(available.length >= 3, "requires at least 3 available CPUs");

            final int[] cpus = new int[(available.length + 1) / 2];
            for (int i = 0; i < cpus.length; i++)
            {
                cpus[i] = available[i * 2];
            }

            ThreadAffinity.setAffinitiesFor(0, cpus);
            assertArrayEquals(cpus, ThreadAffinity.getAffinitiesFor(0));
        });
    }

    @Test
    void shouldFailOnLibraryLoadingIfNotLinux()
    {
        assumeFalse(SystemUtil.isLinux());
        assertThrows(IllegalStateException.class, () -> ThreadAffinity.setAffinity(5));
        assertThrows(IllegalStateException.class, ThreadAffinity::getAffinity);
    }

    @Test
    void shouldRejectInvalidArguments()
    {
        assumeTrue(SystemUtil.isLinux());
        assertThrows(IllegalArgumentException.class, () -> ThreadAffinity.setAffinity(-1));
        assertThrows(IllegalArgumentException.class, () -> ThreadAffinity.setAffinityFor(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> ThreadAffinity.setAffinityFor(0, -1));
        assertThrows(IllegalArgumentException.class, () -> ThreadAffinity.setAffinitiesFor(-1, new int[]{ 0 }));
        assertThrows(IllegalArgumentException.class, () -> ThreadAffinity.setAffinitiesFor(0, null));
        assertThrows(IllegalArgumentException.class, () -> ThreadAffinity.setAffinitiesFor(0, new int[0]));
        assertThrows(IllegalArgumentException.class, () -> ThreadAffinity.setAffinitiesFor(0, new int[]{ 0, -1 }));
        assertThrows(IllegalArgumentException.class, () -> ThreadAffinity.getAffinityFor(-1));
        assertThrows(IllegalArgumentException.class, () -> ThreadAffinity.getAffinitiesFor(-1));
    }

    @Test
    void shouldThrowWhenThreadDoesNotExist() throws InterruptedException
    {
        assumeTrue(SystemUtil.isLinux());
        runOnNewThread(() ->
        {
            final int badTid = Integer.MAX_VALUE;
            assertThrows(ThreadAffinityException.class, () -> ThreadAffinity.setAffinityFor(badTid, 0));
            assertThrows(
                ThreadAffinityException.class, () -> ThreadAffinity.setAffinitiesFor(badTid, new int[]{ 0 }));
            assertThrows(ThreadAffinityException.class, () -> ThreadAffinity.getAffinityFor(badTid));
            assertThrows(ThreadAffinityException.class, () -> ThreadAffinity.getAffinitiesFor(badTid));
        });
    }

    @Test
    void shouldThrowWhenCpuIsOutOfRange() throws InterruptedException
    {
        assumeTrue(SystemUtil.isLinux());
        runOnNewThread(() ->
        {
            assertThrows(ThreadAffinityException.class, () -> ThreadAffinity.setAffinity(Integer.MAX_VALUE));
            assertThrows(
                ThreadAffinityException.class,
                () -> ThreadAffinity.setAffinitiesFor(0, new int[]{ Integer.MAX_VALUE }));
        });
    }

    // This is to prevent the affinity from leaking to the rest of the test
    private static void runOnNewThread(final Runnable task) throws InterruptedException
    {
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final Thread thread = new Thread(() ->
        {
            try
            {
                task.run();
            }
            catch (final Throwable ex)
            {
                error.set(ex);
            }
        });

        thread.start();
        thread.join();

        final Throwable ex = error.get();
        if (null != ex)
        {
            LangUtil.rethrowUnchecked(ex);
        }
    }
}
