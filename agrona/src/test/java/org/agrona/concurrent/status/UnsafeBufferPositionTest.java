/*
 * Copyright 2014-2022 Real Logic Limited.
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
package org.agrona.concurrent.status;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static org.agrona.concurrent.status.CountersReader.COUNTER_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UnsafeBufferPositionTest
{
    @ParameterizedTest
    @MethodSource("buffers")
    void canWrapDifferentKindsOfBuffers(final UnsafeBuffer buffer)
    {
        final long value = 12362863812378L;
        final int counterId = 2;
        final UnsafeBufferPosition position = new UnsafeBufferPosition(buffer, counterId, null);

        position.set(value);
        position.proposeMax(value + 42);
        assertEquals(value + 42, position.get());
    }

    private static List<UnsafeBuffer> buffers()
    {
        return Collections.singletonList(
            new UnsafeBuffer(ByteBuffer.allocateDirect(5 * COUNTER_LENGTH)));
    }
}
