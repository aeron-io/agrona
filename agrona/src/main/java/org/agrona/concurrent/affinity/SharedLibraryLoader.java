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
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

final class SharedLibraryLoader
{
    private SharedLibraryLoader()
    {
    }

    static String resolveNativeLibraryResourcePath(final String baseDir, final String libraryFileName)
    {
        return resolveNativeLibraryResourcePath(baseDir, libraryFileName, SystemUtil.isX64Arch(), SystemUtil.osArch());
    }

    static String resolveNativeLibraryResourcePath(
        final String baseDir, final String libraryFileName, final boolean isX64Arch, final String osArch)
    {
        final String archDir = isX64Arch ? "x86_64" : osArch;
        return baseDir + "/" + archDir + "/" + libraryFileName;
    }

    static boolean load(final String resourcePath)
    {
        try (InputStream in = SharedLibraryLoader.class.getResourceAsStream(resourcePath))
        {
            if (null == in)
            {
                return false;
            }

            final String suffix = resourcePath.substring(resourcePath.lastIndexOf('.'));
            final Path tempFile = Files.createTempFile("agrona-native-lib", suffix);
            tempFile.toFile().deleteOnExit();

            Files.copy(in, tempFile, REPLACE_EXISTING);

            System.load(tempFile.toAbsolutePath().toString());
            // Can be safely deleted since the file has been loaded to memory
            Files.deleteIfExists(tempFile);
            return true;
        }
        catch (final IOException ex)
        {
            ex.printStackTrace(System.err);
            return false;
        }
    }
}
