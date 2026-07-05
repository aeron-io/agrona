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

import java.io.Serial;

/**
 * Thrown to terminate the work/duty cycle of an {@link Agent}.
 *
 * @see Agent
 * @see AgentInvoker
 * @see AgentRunner
 */
public class AgentTerminationException extends RuntimeException
{
    @Serial
    private static final long serialVersionUID = 1196193506427752388L;

    /**
     * Whether this termination is expected as part of a graceful shutdown.
     *
     * @serial
     */
    private final boolean isExpected;

    /**
     * Default constructor.
     */
    public AgentTerminationException()
    {
        this(false);
    }

    /**
     * Create exception with expected status.
     *
     * @param isExpected if the termination is for a graceful shutdown.
     */
    public AgentTerminationException(final boolean isExpected)
    {
        this.isExpected = isExpected;
    }

    /**
     * Create an exception with the given message.
     *
     * @param message to assign.
     */
    public AgentTerminationException(final String message)
    {
        this(message, false);
    }

    /**
     * Create an exception with the given message.
     *
     * @param message to assign.
     * @param isExpected if the termination is for a graceful shutdown.
     */
    public AgentTerminationException(final String message, final boolean isExpected)
    {
        super(message);
        this.isExpected = isExpected;
    }

    /**
     * Create an exception with the given message and a cause.
     *
     * @param message to assign.
     * @param cause   of the error.
     */
    public AgentTerminationException(final String message, final Throwable cause)
    {
        this(message, cause, false);
    }

    /**
     * Create an exception with the given message and a cause.
     *
     * @param message    to assign.
     * @param cause      of the error.
     * @param isExpected if the termination is for a graceful shutdown.
     */
    public AgentTerminationException(final String message, final Throwable cause, final boolean isExpected)
    {
        super(message, cause);
        this.isExpected = isExpected;
    }

    /**
     * Create an exception with the given cause.
     *
     * @param cause of the error.
     */
    public AgentTerminationException(final Throwable cause)
    {
        this(cause, false);
    }

    /**
     * Create an exception with the given cause.
     *
     * @param cause of the error.
     * @param isExpected if the termination is for a graceful shutdown.
     */
    public AgentTerminationException(final Throwable cause, final boolean isExpected)
    {
        super(cause);
        this.isExpected = isExpected;
    }

    /**
     * Create an exception with the given message and a cause.
     *
     * @param message            to assign.
     * @param cause              of the error.
     * @param enableSuppression  true to enable suppression.
     * @param writableStackTrace true to enable writing a full stack trace.
     */
    public AgentTerminationException(
        final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
    {
        this(message, cause, enableSuppression, writableStackTrace, false);
    }

    /**
     * Create an exception with the given message and a cause.
     *
     * @param message            to assign.
     * @param cause              of the error.
     * @param enableSuppression  true to enable suppression.
     * @param writableStackTrace true to enable writing a full stack trace.
     * @param isExpected         if the termination is for a graceful shutdown.
     */
    public AgentTerminationException(
        final String message,
        final Throwable cause,
        final boolean enableSuppression,
        final boolean writableStackTrace,
        final boolean isExpected)
    {
        super(message, cause, enableSuppression, writableStackTrace);
        this.isExpected = isExpected;
    }

    /**
     * {@return if this terminate is expected as part of a graceful shutdown}
     */
    public boolean isExpected()
    {
        return isExpected;
    }
}
