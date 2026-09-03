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
package org.agrona.affinity;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory that sets the affinity of the created threads.
 */
public class AffinedThreadFactory implements ThreadFactory
{
    private final int affinity;

    /**
     * Creates a thread factory that will set the affinity of the thread to the specified value.
     * @param affinity of the created threads.
     */
    public AffinedThreadFactory(final int affinity)
    {
        this.affinity = affinity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread newThread(final Runnable r)
    {
        return new Thread(() ->
        {
            if (ThreadAffinity.NO_AFFINITY != affinity)
            {
                ThreadAffinity.setAffinity(affinity);
            }
            r.run();
        });
    }
}
