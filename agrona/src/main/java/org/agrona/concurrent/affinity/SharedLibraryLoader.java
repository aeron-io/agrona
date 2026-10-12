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

import org.agrona.SystemUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

final class SharedLibraryLoader
{
    private SharedLibraryLoader()
    {
    }

    static String resolveNativeLibraryResourcePath(
        final boolean isX64Arch, final String osArch)
    {
        final String arch = isX64Arch ? "x86_64" : osArch;
        return "/native/libagrona-" + arch + "-" + SystemUtil.osName() + ".so";
    }

    static boolean load()
    {
        // The native library is currently only built for Linux
        if (!SystemUtil.isLinux())
        {
            return false;
        }

        // This will be made more dynamic if we start adding stuff from other OSes
        final String resourcePath = resolveNativeLibraryResourcePath(
            SystemUtil.isX64Arch(), SystemUtil.osArch());
        try (InputStream in = SharedLibraryLoader.class.getResourceAsStream(resourcePath))
        {
            if (null == in)
            {
                return false;
            }

            final String suffix = resourcePath.substring(resourcePath.lastIndexOf('.'));
            final Path tempFile = Files.createTempFile("agrona-native-lib", suffix);
            tempFile.toFile().deleteOnExit();

            // Write into the file createTempFile made, keeping its owner-only permissions
            try (OutputStream out = Files.newOutputStream(tempFile, WRITE, TRUNCATE_EXISTING))
            {
                in.transferTo(out);
            }

            System.load(tempFile.toAbsolutePath().toString());
            // Can be safely deleted since the file has been loaded to memory
            tempFile.toFile().delete();
            return true;
        }
        catch (final IOException ex)
        {
            ex.printStackTrace(System.err);
            return false;
        }
    }
}
