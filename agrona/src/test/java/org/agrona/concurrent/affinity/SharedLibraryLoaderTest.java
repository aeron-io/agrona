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

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SharedLibraryLoaderTest
{
    @ParameterizedTest
    @ValueSource(strings = { "amd64", "x86_64", "x64" })
    @EnabledOnOs(OS.LINUX)
    void resolvesX64Architectures(final String osArch)
    {
        assertEquals(
            "/native/libagrona-x86_64-linux.so",
            SharedLibraryLoader.resolveNativeLibraryResourcePath(true, osArch));
    }

    @ParameterizedTest
    @ValueSource(strings = { "aarch64", "ppc64le" })
    @EnabledOnOs(OS.LINUX)
    void resolvesNonX64ArchitecturesToRawOsArchDirectory(final String osArch)
    {
        assertEquals(
            "/native/libagrona-" + osArch + "-linux.so",
            SharedLibraryLoader.resolveNativeLibraryResourcePath(false, osArch));
    }
}
