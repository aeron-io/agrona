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
package org.agrona.concurrent;

import org.agrona.ErrorHandler;
import org.agrona.LangUtil;
import org.agrona.SystemUtil;
import org.agrona.concurrent.affinity.ThreadAffinity;
import org.agrona.concurrent.affinity.ThreadAffinityException;
import org.agrona.collections.MutableInteger;
import org.agrona.concurrent.status.AtomicCounter;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentRunnerTest
{
    private final AtomicCounter mockAtomicCounter = mock(AtomicCounter.class);

    private final ErrorHandler mockErrorHandler = mock(ErrorHandler.class);
    private final Agent mockAgent = mock(Agent.class);
    private final IdleStrategy idleStrategy = new NoOpIdleStrategy();
    private final AgentRunner runner = new AgentRunner(idleStrategy, mockErrorHandler, mockAtomicCounter, mockAgent);

    @Test
    void shouldReturnAgent()
    {
        assertThat(runner.agent(), is(mockAgent));
    }

    @Test
    void shouldNotDoWorkOnClosedRunner() throws Exception
    {
        runner.close();
        runner.run();

        verify(mockAgent, never()).onStart();
        verify(mockAgent, never()).doWork();
        verify(mockErrorHandler, never()).onError(any());
        verify(mockAtomicCounter, never()).increment();
        verify(mockAgent, times(1)).onClose();
        assertTrue(runner.isClosed());
    }

    @Test
    void shouldHandleAgentTerminationExceptionThrownByAgent() throws Exception
    {
        final RuntimeException terminationException = new AgentTerminationException();
        when(mockAgent.doWork()).thenThrow(terminationException);

        runner.run();

        verify(mockAgent).doWork();
        verify(mockErrorHandler).onError(terminationException);
        verify(mockAtomicCounter, never()).increment();
        verify(mockAgent, times(1)).onClose();
        assertTrue(runner.isClosed());
    }

    @Test
    void shouldNotErrorWhenAgentTerminationExceptionThrownByAgentAsExpected() throws Exception
    {
        final RuntimeException terminationException = new AgentTerminationException(true);
        when(mockAgent.doWork()).thenThrow(terminationException);

        runner.run();

        verify(mockAgent).doWork();
        verify(mockErrorHandler, never()).onError(any());
        verify(mockAtomicCounter, never()).increment();
        verify(mockAgent, times(1)).onClose();
        assertTrue(runner.isClosed());
    }

    @Test
    void shouldReportExceptionThrownByAgent() throws Exception
    {
        final CountDownLatch latch = new CountDownLatch(1);
        final RuntimeException expectedException = new RuntimeException();
        when(mockAgent.doWork()).thenThrow(expectedException);

        doAnswer(
            (invocation) ->
            {
                latch.countDown();
                return null;
            })
            .when(mockErrorHandler).onError(expectedException);

        new Thread(runner).start();

        if (!latch.await(3, TimeUnit.SECONDS))
        {
            fail("Should have called error handler");
        }

        verify(mockAgent, times(1)).onStart();
        verify(mockAgent, atLeastOnce()).doWork();
        verify(mockErrorHandler, atLeastOnce()).onError(expectedException);
        verify(mockAtomicCounter, atLeastOnce()).increment();

        runner.close();
    }

    @Test
    void shouldNotReportClosedByInterruptException() throws Exception
    {
        assertExceptionNotReported(() -> new ClosedByInterruptException());
    }

    @Test
    void shouldNotReportRethrownClosedByInterruptException() throws Exception
    {
        assertExceptionNotReported(() ->
        {
            try
            {
                throw new ClosedByInterruptException();
            }
            catch (final ClosedByInterruptException ex)
            {
                LangUtil.rethrowUnchecked(ex);
            }
        });
    }

    @Test
    void shouldInterruptRunnerThreadIfCloseCallIsItselfInterruped() throws Exception
    {
        final CountDownLatch runLatch = new CountDownLatch(1);
        when(mockAgent.doWork()).then((Answer<Void>)invocation ->
        {
            runLatch.countDown();
            while (true)
            {
                try
                {
                    Thread.sleep(10);
                }
                catch (final InterruptedException ex)
                {
                    break;
                }
            }
            return null;
        });

        final Thread runnerThread = new Thread(runner);
        runnerThread.start();

        runLatch.await();

        final CountDownLatch closeLatch = new CountDownLatch(1);
        final CopyOnWriteArrayList<Thread> closeActions = new CopyOnWriteArrayList<>();
        final Thread closerThread = new Thread(() ->
        {
            closeLatch.countDown();
            runner.close(Integer.MAX_VALUE, closeActions::add);
        });
        closerThread.start();

        closeLatch.await();

        closerThread.interrupt();
        closerThread.join();

        assertFalse(closerThread.isAlive());
        assertFalse(runnerThread.isAlive());
        assertEquals(1, closeActions.size());
        assertSame(runnerThread, closeActions.get(0));
    }

    @Test
    void shouldNotInterruptRunnerThreadIfCloseCompletesOnTime() throws Exception
    {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean finished = new AtomicBoolean(false);
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        when(mockAgent.doWork()).then((Answer<Void>)invocation ->
        {
            startLatch.countDown();
            try
            {
                Thread.sleep(50);
            }
            catch (final InterruptedException ex)
            {
                interrupted.set(true);
            }

            finished.set(true);
            return null;
        });

        final Thread runnerThread = new Thread(runner);
        runnerThread.start();

        startLatch.await();

        final MutableInteger failCount = new MutableInteger();
        runner.close(300, (t) -> failCount.increment());

        assertFalse(Thread.interrupted());
        assertFalse(runnerThread.isAlive());
        assertTrue(finished.get());
        assertFalse(interrupted.get());
        assertEquals(0, failCount.get());
    }

    @Test
    void shouldInterruptRunnerThreadIfCloseDoesNotCompleteWithinCloseBudget() throws Exception
    {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean finished = new AtomicBoolean(false);
        final AtomicInteger interrupted = new AtomicInteger();
        when(mockAgent.doWork()).then((Answer<Void>)invocation ->
        {
            startLatch.countDown();

            try
            {
                Thread.sleep(Integer.MAX_VALUE);
            }
            catch (final InterruptedException ex)
            {
                interrupted.getAndIncrement();
            }

            try
            {
                Thread.sleep(Integer.MAX_VALUE);
            }
            catch (final InterruptedException ex)
            {
                interrupted.getAndIncrement();
            }

            finished.set(true);
            return null;
        });

        final Thread runnerThread = new Thread(runner);
        runnerThread.start();

        startLatch.await();

        final MutableInteger failCount = new MutableInteger();
        runner.close(50, (t) -> failCount.increment());

        assertFalse(Thread.interrupted());
        assertFalse(runnerThread.isAlive());
        assertTrue(finished.get());
        assertEquals(2, interrupted.get());
        assertEquals(2, failCount.get());
    }

    @Test
    void shouldInterruptRunnerThreadIfCloseCompletesOnTimeWhenMainThreadIsInterruptedBeforeTheCloseCall()
        throws Exception
    {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final AtomicBoolean finished = new AtomicBoolean(false);
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        when(mockAgent.doWork()).then((Answer<Void>)invocation ->
        {
            startLatch.countDown();
            try
            {
                Thread.sleep(50);
            }
            catch (final InterruptedException ex)
            {
                interrupted.set(true);
            }

            finished.set(true);
            return null;
        });

        final Thread runnerThread = new Thread(runner);
        runnerThread.start();

        startLatch.await();

        Thread.currentThread().interrupt();
        final MutableInteger failCount = new MutableInteger();
        runner.close(300, (t) -> failCount.increment());

        assertTrue(Thread.interrupted());
        assertFalse(runnerThread.isAlive());
        assertTrue(finished.get());
        assertTrue(interrupted.get());
        assertEquals(1, failCount.get());
    }

    @Test
    void shouldApplyAffinityPassedToConstructorAfterOnStart() throws Exception
    {
        assumeTrue(SystemUtil.isLinux());
        final int affinity = pickAvailableCpu();
        final int[] available = ThreadAffinity.getAffinity(ThreadAffinity.CURRENT_THREAD);
        final AtomicReference<int[]> affinitiesOnStart = new AtomicReference<>();
        final AtomicReference<int[]> affinityInDoWork = new AtomicReference<>();
        doAnswer(invocation ->
        {
            affinitiesOnStart.set(ThreadAffinity.getAffinity(ThreadAffinity.CURRENT_THREAD));
            return null;
        }).when(mockAgent).onStart();

        doAnswer(invocation ->
        {
            affinityInDoWork.set(ThreadAffinity.getAffinity(ThreadAffinity.CURRENT_THREAD));
            // Kill the runner
            throw new AgentTerminationException();
        }).when(mockAgent).doWork();

        when(mockAgent.roleName()).thenReturn("test");

        final AgentRunner runner = new AgentRunner(
            idleStrategy, mockErrorHandler, mockAtomicCounter, mockAgent, affinity);
        AgentRunner.startOnThread(runner).join();

        verify(mockAgent).onStart();
        // Affinity is applied after onStart, so the thread is not yet pinned there
        assertArrayEquals(available, affinitiesOnStart.get());
        assertArrayEquals(new int[]{ affinity }, affinityInDoWork.get());

        affinitiesOnStart.set(null);
        affinityInDoWork.set(null);

        final AgentRunner runnerWithFactory = new AgentRunner(
            idleStrategy, mockErrorHandler, mockAtomicCounter, mockAgent, affinity);
        AgentRunner.startOnThread(runnerWithFactory, Thread::new).join();

        verify(mockAgent, times(2)).onStart();
        assertArrayEquals(available, affinitiesOnStart.get());
        assertArrayEquals(new int[]{ affinity }, affinityInDoWork.get());
    }

    @Test
    void shouldReportErrorAndCloseAgentWhenAffinityCannotBeSet() throws Exception
    {
        final Class<? extends Throwable> expectedException = SystemUtil.isLinux() ?
            ThreadAffinityException.class : IllegalStateException.class;
        when(mockAgent.roleName()).thenReturn("test");

        final AgentRunner runner = new AgentRunner(
            idleStrategy, mockErrorHandler, mockAtomicCounter, mockAgent, Integer.MAX_VALUE);
        AgentRunner.startOnThread(runner).join();

        verify(mockAgent).onStart();
        verify(mockAgent, never()).doWork();
        verify(mockAgent).onClose();
        verify(mockErrorHandler).onError(isA(expectedException));
        assertTrue(runner.isClosed());
    }

    private static int pickAvailableCpu()
    {
        final int[] available = ThreadAffinity.getAffinity(ThreadAffinity.CURRENT_THREAD);
        assumeTrue(available.length >= 2, "requires at least 2 available CPUs");
        return available[available.length - 1];
    }

    private void assertExceptionNotReported(final Runnable task) throws Exception
    {
        final CountDownLatch latch = new CountDownLatch(1);
        when(mockAgent.doWork()).thenAnswer(
            (invocation) ->
            {
                latch.countDown();
                task.run();
                return null;
            }
        );

        new Thread(runner).start();

        latch.await();

        runner.close();

        verify(mockAgent).onStart();
        verify(mockAgent, atLeastOnce()).doWork();
        verify(mockErrorHandler, never()).onError(any());
        verify(mockAtomicCounter, never()).increment();
    }
}
