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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.agrona.SystemUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.*;

class SystemUtilTest
{
    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "42, 42",
        "5k, 5120",
        "2K, 2048",
        "1m, 1048576",
        "10M, 10485760",
        "4g, 4294967296",
        "10G, 10737418240",
        "8589934591g, 9223372035781033984",
        "8796093022207m, 9223372036853727232",
        "9007199254740991k, 9223372036854774784",
    })
    void shouldParseSizesWithSuffix(final String value, final long expected)
    {
        assertEquals(expected, parseSize("", value));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldRejectEmptySizeValue(final String value)
    {
        final NumberFormatException exception =
            assertThrowsExactly(NumberFormatException.class, () -> parseSize("abc", value));
        assertEquals("abc must be non-empty: " + value, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1024", "-5g", "-3K", "-0K", "-2m", "-1M", "-5G"})
    void shouldRejectEmptyNegativeSizeValue(final String value)
    {
        final NumberFormatException exception =
            assertThrowsExactly(NumberFormatException.class, () -> parseSize("abc", value));
        assertEquals("abc must be positive: " + value, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "9007199254740992k", "8796093022208m", "8589934592g" })
    void shouldThrowWhenParseSizeOverflows(final String value)
    {
        final NumberFormatException exception =
            assertThrows(NumberFormatException.class, () -> parseSize("test", value));
        assertEquals("test would overflow a long: " + value, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "0ns, 0",
        "0us, 0",
        "0ms, 0",
        "0s, 0",
        "1, 1",
        "1ns, 1",
        "4NS, 4",
        "44444nS, 44444",
        "1024, 1024",
        "1us, 1000",
        "5us, 5000",
        "7US, 7000",
        "9uS, 9000",
        "12Us, 12000",
        "1ms, 1000000",
        "123ms, 123000000",
        "456MS, 456000000",
        "789mS, 789000000",
        "2Ms, 2000000",
        "1s, 1000000000",
        "1S, 1000000000",
        "42s, 42000000000",
        "9223372036854775807, 9223372036854775807",
        "2147483647, 2147483647",
        "9223372036854775807ns, 9223372036854775807",
        "9223372036854775us, 9223372036854775000",
        "9223372036854ms, 9223372036854000000",
        "9223372036s, 9223372036000000000" })
    void shouldParseTimesWithSuffix(final String value, final long expected)
    {
        assertEquals(expected, parseDuration("", value));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "9223372036854775807ns",
        "9223372036854775807us",
        "9223372036854775807ms",
        "9223372036854775807s",
        "9223372036854776us",
        "9223372036855ms",
        "9223372037s" })
    void shouldReturnLongMaxValueIfExceedsForSuffix(final String value)
    {
        assertEquals(Long.MAX_VALUE, parseDuration("", value));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldRejectEmptyDurationValue(final String value)
    {
        final NumberFormatException exception =
            assertThrowsExactly(NumberFormatException.class, () -> parseDuration("x", value));
        assertEquals("x must be non-empty: " + value, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "-4", "-1s", "-5S", "-2us", "-9ms", "-11MS", "-1US", "-8NS", "-4444444ns" })
    void shouldRejectNegativeDurations(final String value)
    {
        final NumberFormatException exception =
            assertThrowsExactly(NumberFormatException.class, () -> parseDuration("x", value));
        assertEquals("x must be positive: " + value, exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "123x", "1p", "0xs", "42xxl", "s", "S", "2ps", "5px", "4u", "5n", "3m", "us", "ms", "ns" })
    void shouldRejectInvalidDurationSuffix(final String value)
    {
        final NumberFormatException exception =
            assertThrows(NumberFormatException.class, () -> parseDuration("x", value));
        assertEquals("x: " + value + " should end with: s, ms, us, or ns.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(longs = { Long.MIN_VALUE, -100 })
    void formatDurationShouldRejectNegativeValues(final long value)
    {
        final IllegalArgumentException exception =
            assertThrowsExactly(IllegalArgumentException.class, () -> formatDuration(value));
        assertEquals("duration must be positive: " + value, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "999, 999",
        "1000, 1us",
        "5429, 5429",
        "999999, 999999",
        "1000000, 1ms",
        "560000000, 560ms",
        "560000001, 560000001",
        "1000000000, 1s",
        "120000000000, 120s",
        "9223372035781033984, 9223372035781033984"
    })
    void shouldFormatDuration(final long value, final String expected)
    {
        assertEquals(expected, SystemUtil.formatDuration(value));
    }

    @Test
    void shouldDoNothingToSystemPropsWhenLoadingFileWhichDoesNotExist()
    {
        final int originalSystemPropSize = System.getProperties().size();

        loadPropertiesFile("$unknown-file$");
        assertEquals(originalSystemPropSize, System.getProperties().size());
    }

    @Test
    void shouldMergeMultiplePropFilesTogether()
    {
        assertThat(System.getProperty("TestFileA.foo"), is(emptyOrNullString()));
        assertThat(System.getProperty("TestFileB.foo"), is(emptyOrNullString()));

        try
        {
            loadPropertiesFiles("TestFileA.properties", "TestFileB.properties");
            assertEquals("AAA", System.getProperty("TestFileA.foo"));
            assertEquals("BBB", System.getProperty("TestFileB.foo"));
        }
        finally
        {
            System.clearProperty("TestFileA.foo");
            System.clearProperty("TestFileB.foo");
        }
    }

    @Test
    void shouldOverrideSystemPropertiesWithConfigFromPropFile()
    {
        System.setProperty("TestFileA.foo", "ToBeOverridden");
        assertEquals("ToBeOverridden", System.getProperty("TestFileA.foo"));

        try
        {
            loadPropertiesFiles("TestFileA.properties");
            assertEquals("AAA", System.getProperty("TestFileA.foo"));
        }
        finally
        {
            System.clearProperty("TestFileA.foo");
        }
    }

    @Test
    void shouldNotOverrideSystemPropertiesWithConfigFromPropFile()
    {
        System.setProperty("TestFileA.foo", "ToBeNotOverridden");
        assertEquals("ToBeNotOverridden", System.getProperty("TestFileA.foo"));

        try
        {
            loadPropertiesFile(PropertyAction.PRESERVE, "TestFileA.properties");
            assertEquals("ToBeNotOverridden", System.getProperty("TestFileA.foo"));
        }
        finally
        {
            System.clearProperty("TestFileA.foo");
        }
    }

    @Test
    void shouldReturnPid()
    {
        assertNotEquals(PID_NOT_FOUND, getPid());
    }

    @Test
    void shouldGetNormalProperty()
    {
        final String key = "org.agrona.test.case";
        final String value = "wibble";

        System.setProperty(key, value);

        try
        {
            assertEquals(value, SystemUtil.getProperty(key));
        }
        finally
        {
            System.clearProperty(key);
        }
    }

    @Test
    void shouldGetNullProperty()
    {
        final String key = "org.agrona.test.case";
        final String value = "@null";

        System.setProperty(key, value);

        try
        {
            assertNull(SystemUtil.getProperty(key));
        }
        finally
        {
            System.clearProperty(key);
        }
    }

    @Test
    void shouldGetNullPropertyWithDefault()
    {
        final String key = "org.agrona.test.case";
        final String value = "@null";

        System.setProperty(key, value);

        try
        {
            assertNull(SystemUtil.getProperty(key, "default"));
        }
        finally
        {
            System.clearProperty(key);
        }
    }

    @Test
    void shouldGetDefaultProperty()
    {
        final String key = "org.agrona.test.case";
        final String defaultValue = "default";

        assertEquals(defaultValue, SystemUtil.getProperty(key, defaultValue));
    }

    @ParameterizedTest
    @ValueSource(strings = { "x64", "x86_64", "amd64" })
    void isX64ArchShouldDetectProperOsArch(final String arch)
    {
        assertTrue(SystemUtil.isX64Arch(arch));
    }

    @ParameterizedTest
    @ValueSource(strings = { "aarch64", "ppc64", "ppc64le", "unknown", "", "x86", "i386" })
    void isX64ArchShouldReturnFalse(final String arch)
    {
        assertFalse(SystemUtil.isX64Arch(arch));
    }

    @Test
    @EnabledOnOs(architectures = { "x64", "x86_64", "amd64" })
    void isX64ArchSystemTest()
    {
        assertTrue(SystemUtil.isX64Arch());
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void isLinuxReturnsTrueForLinuxBasedSystems()
    {
        assertTrue(SystemUtil.isLinux());
        assertFalse(SystemUtil.isWindows());
        assertFalse(SystemUtil.isMac());
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void isWindowsReturnsTrueForWindows()
    {
        assertTrue(SystemUtil.isWindows());
        assertFalse(SystemUtil.isLinux());
        assertFalse(SystemUtil.isMac());
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void isMacOsReturnsTrueForMacBasedSystems()
    {
        assertTrue(SystemUtil.isMac());
        assertFalse(SystemUtil.isLinux());
        assertFalse(SystemUtil.isWindows());
    }
}
