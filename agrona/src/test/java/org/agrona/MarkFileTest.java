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
package org.agrona;

import org.agrona.concurrent.EpochClock;
import org.agrona.concurrent.SystemEpochClock;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MarkFileTest
{
    @TempDir
    File tempDir;
    @TempDir
    File otherTempDir;

    @Test
    @SuppressWarnings("unchecked")
    void throwsNullPointerExceptionIfFileIsNull()
    {
        assertThrowsExactly(NullPointerException.class, () -> new MarkFile(
            null,
            true,
            0,
            8,
            16,
            100,
            SystemEpochClock.INSTANCE,
            mock(IntConsumer.class),
            mock(Consumer.class)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void throwsNullPointerExceptionIfClockIsNull(@TempDir final File tempDir)
    {
        assertThrowsExactly(NullPointerException.class, () -> new MarkFile(
            new File(tempDir, "test.txt"),
            false,
            0,
            8,
            16,
            100,
            null,
            mock(IntConsumer.class),
            mock(Consumer.class)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void throwsNullPointerExceptionIfVersionCheckIsNull(@TempDir final File tempDir)
    {
        assertThrowsExactly(NullPointerException.class, () -> new MarkFile(
            new File(tempDir, "test.txt"),
            false,
            0,
            8,
            16,
            100,
            SystemEpochClock.INSTANCE,
            null,
            mock(Consumer.class)));
    }

    @Test
    void shouldWaitForMarkFileToContainEnoughDataForVersionCheck() throws IOException
    {
        final String filename = "markfile.dat";
        final Path markFilePath = tempDir.toPath().resolve(filename);
        Files.createFile(markFilePath);

        try (FileChannel channel = FileChannel.open(markFilePath, StandardOpenOption.WRITE))
        {
            channel.write(ByteBuffer.allocate(1));
        }

        assertThrows(IllegalStateException.class,
            () -> new MarkFile(tempDir, filename, 0, 16, 10, new SystemEpochClock(), (v) -> {}, (msg) -> {}));
    }

    @Test
    void shouldCreateLinkFileIfFileInDifferentLocation() throws IOException
    {
        final String linkFilename = "markfile.lnk";
        final File markFileLocation = new File(otherTempDir, "markfile.dat");

        MarkFile.ensureMarkFileLink(tempDir, markFileLocation, linkFilename);
        final File linkFileLocation = new File(tempDir, linkFilename);
        assertTrue(linkFileLocation.exists());
        final List<String> strings = Files.readAllLines(linkFileLocation.toPath());
        assertEquals(1, strings.size());
        assertEquals(markFileLocation.getCanonicalFile().getParent(), strings.get(0));
    }

    @Test
    void shouldRemoveLinkFileIfMarkFileIsInServiceDirectory() throws IOException
    {
        final String linkFilename = "markfile.lnk";
        final File markFileLocation = new File(tempDir, "markfile.dat");
        final File linkFileLocation = new File(tempDir, linkFilename);

        assertTrue(linkFileLocation.createNewFile());
        assertTrue(linkFileLocation.exists());

        MarkFile.ensureMarkFileLink(tempDir, markFileLocation, linkFilename);
        assertFalse(linkFileLocation.exists());
    }

    @Test
    void shouldMapNewFile()
    {
        final File file = new File(tempDir, "new.file");

        final int totalFileLength = 123000;
        final MappedByteBuffer mappedByteBuffer = MarkFile.mapNewOrExistingMarkFile(
            file,
            false,
            0,
            8,
            totalFileLength,
            1000,
            SystemEpochClock.INSTANCE,
            (version) -> {},
            (msg) -> {});

        assertNotNull(mappedByteBuffer);
        assertTrue(file.exists());
        assertEquals(totalFileLength, file.length());
        BufferUtil.free(mappedByteBuffer);
    }

    @Test
    void shouldMapAndResizeExistingFile() throws IOException
    {
        final File file = new File(tempDir, "existing.file");
        Files.createFile(file.toPath());
        assertTrue(file.exists());
        assertEquals(0, file.length());

        final int totalFileLength = 256 * 1024;
        final MappedByteBuffer mappedByteBuffer = MarkFile.mapNewOrExistingMarkFile(
            file,
            true,
            0,
            8,
            totalFileLength,
            1000,
            SystemEpochClock.INSTANCE,
            (version) -> {},
            (msg) -> {});

        assertNotNull(mappedByteBuffer);
        assertTrue(file.exists());
        assertEquals(totalFileLength, file.length());
        BufferUtil.free(mappedByteBuffer);
    }

    @Test
    @SuppressWarnings("indentation")
    void shouldFailWithExceptionIfFileAlreadyExistsAndCreationFails() throws IOException
    {
        final File file = new File(tempDir, "existing.file");
        Files.createFile(file.toPath());
        assertTrue(file.exists());
        assertEquals(0, file.length());

        final UncheckedIOException exception = assertThrowsExactly(
            UncheckedIOException.class,
            () -> MarkFile.mapNewOrExistingMarkFile(
                file,
                false,
                0,
                8,
                1024,
                1000,
                SystemEpochClock.INSTANCE,
                (version) -> {},
                (msg) -> {}));
        final IOException cause = exception.getCause();
        assertInstanceOf(FileAlreadyExistsException.class, cause);
    }

    @Test
    void testDirectoryCreatingConstructor()
    {
        final File directory = new File(tempDir, "foo");
        final Queue<Integer> versions = new ArrayDeque<>();
        final EpochClock clock = SystemEpochClock.INSTANCE;
        final Supplier<MarkFile> supplier = () -> new MarkFile(
            directory,
            "mark",
            false,
            false,
            8,
            16,
            4096,
            5_000,
            clock,
            versions::add,
            null);

        final MarkFile markFile1 = supplier.get();
        markFile1.timestampOrdered(clock.time());
        markFile1.signalReady(123);

        assertNull(versions.poll());

        final IllegalStateException ise = assertThrows(IllegalStateException.class, supplier::get);
        assertEquals("active mark file detected: " + markFile1.markFile(), ise.getMessage());
        assertEquals(123, versions.poll());

        markFile1.timestampOrdered(-1);
        markFile1.close();

        try (MarkFile markFile2 = supplier.get())
        {
            assertEquals(123, versions.poll());

            markFile2.timestampOrdered(clock.time());
            markFile2.signalReady(123);
        }
    }

    @Test
    void shouldPreventConcurrentActivationFromMultipleThreads() throws Exception
    {
        for (int i = 0; i < 100; i++)
        {
            testConcurrentActivation();
        }
    }

    @SuppressWarnings("MethodLength")
    private void testConcurrentActivation() throws Exception
    {
        final Path path = tempDir.toPath().resolve("existing.file");
        Files.deleteIfExists(path);
        final int fileLength = 64;
        Files.write(path, new byte[fileLength], StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);

        final File file = path.toFile();

        final int versionFieldOffset = 0;
        final int timestampFieldOffset = 8;
        final int numThreads = Math.min(Runtime.getRuntime().availableProcessors(), 4);
        final CountDownLatch startLatch = new CountDownLatch(numThreads + 1);
        final CountDownLatch endLatch = new CountDownLatch(numThreads);
        final Thread[] threads = new Thread[numThreads];
        final Throwable[] exceptions = new Throwable[numThreads];
        final long[] times = new long[numThreads];
        final AtomicInteger ok = new AtomicInteger();
        final AtomicInteger error = new AtomicInteger();
        final long baseTimeMs = SystemEpochClock.INSTANCE.time();
        for (int i = 0; i < numThreads; i++)
        {
            final int index = i;
            threads[i] = new Thread(() ->
            {
                startLatch.countDown();

                try
                {
                    startLatch.await();

                    try (MarkFile markFile = new MarkFile(
                        file,
                        true,
                        versionFieldOffset,
                        timestampFieldOffset,
                        fileLength,
                        TimeUnit.SECONDS.toMillis(15),
                        SystemEpochClock.INSTANCE,
                        (v) -> assertThat(v, allOf(lessThan(numThreads), greaterThanOrEqualTo(0))),
                        null))
                    {
                        times[index] = ThreadLocalRandom.current().nextLong(baseTimeMs + 1 + index, Long.MAX_VALUE);
                        markFile.timestampRelease(times[index]);
                        markFile.signalReady(index);
                        ok.incrementAndGet();
                    }
                }
                catch (final Exception ex)
                {
                    exceptions[index] = ex;
                    error.incrementAndGet();
                }
                finally
                {
                    endLatch.countDown();
                }
            });
        }

        for (final Thread thread : threads)
        {
            thread.start();
        }

        startLatch.countDown();
        endLatch.await();

        for (final Thread thread : threads)
        {
            thread.join();
        }

        assertEquals(1, ok.get(), "multiple threads succeeded");
        assertEquals(numThreads - 1, error.get(), "multiple threads succeeded");
        for (int i = 0; i < numThreads; i++)
        {
            final Throwable exception = exceptions[i];
            if (null != exception)
            {
                assertInstanceOf(IllegalStateException.class, exception);
                assertThat(exception.getMessage(), anyOf(
                    containsString("active mark file detected: "),
                    containsString("concurrent mark file activation: ")));
            }
            else
            {
                final MappedByteBuffer byteBuffer = IoUtil.mapExistingFile(file, file.toString(), 0, fileLength);
                try
                {
                    final UnsafeBuffer buffer = new UnsafeBuffer(byteBuffer);
                    assertEquals(i, buffer.getIntVolatile(versionFieldOffset));
                    final long timestampMs = buffer.getLongVolatile(timestampFieldOffset);
                    assertNotEquals(MarkFile.ACTIVATION_IN_PROGRESS_TIMESTAMP, timestampMs);
                    assertEquals(times[i], timestampMs);
                }
                finally
                {
                    IoUtil.unmap(byteBuffer);
                }
            }
        }
    }
}
