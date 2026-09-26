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
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CloseHelperTest
{
    @Test
    void quietCloseShouldIgnoreNull()
    {
        CloseHelper.quietClose(null);
    }

    @Test
    void quietCloseShouldClose() throws Exception
    {
        final AutoCloseable closeable = mock(AutoCloseable.class);
        CloseHelper.quietClose(closeable);
        verify(closeable).close();
    }

    @Test
    void quietCloseShouldSuppressException() throws Exception
    {
        final AutoCloseable closeable = mock(AutoCloseable.class);
        doThrow(new Exception("test")).when(closeable).close();
        CloseHelper.quietClose(closeable);
        verify(closeable).close();
    }

    @Test
    void quietCloseAllCollectionShouldIgnoreNull()
    {
        CloseHelper.quietCloseAll((List<AutoCloseable>)null);
    }

    @Test
    void quietCloseAllCollection() throws Exception
    {
        final AutoCloseable c1 = mock(AutoCloseable.class);
        final AutoCloseable c2 = mock(AutoCloseable.class);
        CloseHelper.quietCloseAll(Arrays.asList(c1, c2));

        final InOrder inOrder = inOrder(c1, c2);
        inOrder.verify(c1).close();
        inOrder.verify(c2).close();
    }

    @Test
    void quietCloseAllArray() throws Exception
    {
        final AutoCloseable c1 = mock(AutoCloseable.class);
        final AutoCloseable c2 = mock(AutoCloseable.class);
        CloseHelper.quietCloseAll(c1, c2);

        final InOrder inOrder = inOrder(c1, c2);
        inOrder.verify(c1).close();
        inOrder.verify(c2).close();
    }

    @Test
    void closeShouldThrowException() throws Exception
    {
        final AutoCloseable closeable = mock(AutoCloseable.class);
        final Exception ex = new Exception("test");
        doThrow(ex).when(closeable).close();

        assertThrows(Exception.class, () -> CloseHelper.close(closeable));
    }

    @Test
    void closeAllCollectionShouldThrowException() throws Exception
    {
        final AutoCloseable c1 = mock(AutoCloseable.class);
        final AutoCloseable c2 = mock(AutoCloseable.class);
        final Exception ex = new Exception("test");
        doThrow(ex).when(c1).close();

        assertThrows(Exception.class, () -> CloseHelper.closeAll(Arrays.asList(c1, c2)));
        verify(c2).close();
    }

    @Test
    void closeWithErrorHandler() throws Exception
    {
        final ErrorHandler errorHandler = mock(ErrorHandler.class);
        final AutoCloseable closeable = mock(AutoCloseable.class);
        final Exception ex = new Exception("test");
        doThrow(ex).when(closeable).close();

        CloseHelper.close(errorHandler, closeable);

        verify(errorHandler).onError(ex);
    }

    @Test
    void closeAllWithErrorHandler() throws Exception
    {
        final ErrorHandler errorHandler = mock(ErrorHandler.class);
        final AutoCloseable c1 = mock(AutoCloseable.class);
        final AutoCloseable c2 = mock(AutoCloseable.class);
        final Exception ex1 = new Exception("test1");
        final Exception ex2 = new Exception("test2");
        doThrow(ex1).when(c1).close();
        doThrow(ex2).when(c2).close();

        CloseHelper.closeAll(errorHandler, c1, c2);

        verify(errorHandler).onError(ex1);
        verify(errorHandler).onError(ex2);
    }

    @Test
    void quietCloseAllCollectionWithNulls() throws Exception
    {
        final AutoCloseable c1 = mock(AutoCloseable.class);
        CloseHelper.quietCloseAll(Arrays.asList(c1, null));
        verify(c1).close();
    }
}
