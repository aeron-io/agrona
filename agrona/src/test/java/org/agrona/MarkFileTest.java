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

import org.agrona.concurrent.SystemEpochClock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkFileTest
{
    @TempDir
    File serviceDirectory;
    @TempDir
    File alternativeDirectory;

    @Test
    void shouldWaitForMarkFileToContainEnoughDataForVersionCheck() throws IOException
    {
        final String filename = "markfile.dat";
        final Path markFilePath = serviceDirectory.toPath().resolve(filename);
        Files.createFile(markFilePath);

        try (FileChannel channel = FileChannel.open(markFilePath, StandardOpenOption.WRITE))
        {
            channel.write(ByteBuffer.allocate(1));
        }

        assertThrows(IllegalStateException.class,
            () -> new MarkFile(serviceDirectory, filename, 0, 16, 10, new SystemEpochClock(), (v) -> {}, (msg) -> {}));
    }

    @Test
    void shouldCreateLinkFileIfFileInDifferentLocation() throws IOException
    {
        final String linkFilename = "markfile.lnk";
        final File markFileLocation = new File(alternativeDirectory, "markfile.dat");

        MarkFile.ensureMarkFileLink(serviceDirectory, markFileLocation, linkFilename);
        final File linkFileLocation = new File(serviceDirectory, linkFilename);
        assertTrue(linkFileLocation.exists());
        final List<String> strings = Files.readAllLines(linkFileLocation.toPath());
        assertEquals(1, strings.size());
        assertEquals(markFileLocation.getCanonicalFile().getParent(), strings.get(0));
    }

    @Test
    void shouldRemoveLinkFileIfMarkFileIsInServiceDirectory() throws IOException
    {
        final String linkFilename = "markfile.lnk";
        final File markFileLocation = new File(serviceDirectory, "markfile.dat");
        final File linkFileLocation = new File(serviceDirectory, linkFilename);

        assertTrue(linkFileLocation.createNewFile());
        assertTrue(linkFileLocation.exists());

        MarkFile.ensureMarkFileLink(serviceDirectory, markFileLocation, linkFilename);
        assertFalse(linkFileLocation.exists());
    }
}
