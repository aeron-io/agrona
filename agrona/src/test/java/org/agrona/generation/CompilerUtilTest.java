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
package org.agrona.generation;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CompilerUtilTest
{
    private static final String CLASS_NAME = "org.agrona.generation.test.Person";

    private static final String TEST_CLASS = """
package org.agrona.generation.test;

public record Person(int age){}""";

    @Test
    void compileInMemory() throws ClassNotFoundException, NoSuchFieldException
    {
        final Class<?> klass = CompilerUtil.compileInMemory(CLASS_NAME, Map.of(CLASS_NAME, TEST_CLASS));
        assertNotNull(klass);
        assertEquals(CLASS_NAME, klass.getName());
        assertNotNull(klass.getDeclaredField("age"));
    }
}
