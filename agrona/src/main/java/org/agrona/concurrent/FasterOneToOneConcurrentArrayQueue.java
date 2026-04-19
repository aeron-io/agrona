package org.agrona.concurrent;

import org.agrona.UnsafeApi;

import java.util.Collection;
import java.util.function.Consumer;

public class FasterOneToOneConcurrentArrayQueue<E> extends AbstractConcurrentArrayQueue<E>
{
    /**
     * Constructs queue with the requested capacity.
     *
     * @param requestedCapacity of the queue.
     */
    public FasterOneToOneConcurrentArrayQueue(final int requestedCapacity)
    {
        super(requestedCapacity);
    }

    /**
     * {@inheritDoc}
     */
    public boolean offer(final E e)
    {
        if (null == e)
        {
            throw new NullPointerException("Null is not a valid element");
        }

        final long currentTail = UnsafeApi.getLongOpaque(this, TAIL_OFFSET);
        final long elementOffset = sequenceToBufferOffset(currentTail, capacity - 1);

        if (null != UnsafeApi.getReferenceOpaque(buffer, elementOffset))
        {
            return false;
        }

        UnsafeApi.putReferenceRelease(buffer, elementOffset, e);
        UnsafeApi.putLongOpaque(this, TAIL_OFFSET, currentTail + 1);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public E poll()
    {
        final Object[] buffer = this.buffer;
        final long currentHead = UnsafeApi.getLongOpaque(this, HEAD_OFFSET);
        final long elementOffset = sequenceToBufferOffset(currentHead, capacity - 1);

        final Object e = UnsafeApi.getReferenceAcquire(buffer, elementOffset);
        if (null != e)
        {
            UnsafeApi.putReferenceOpaque(buffer, elementOffset, null);
            UnsafeApi.putLongOpaque(this, HEAD_OFFSET, currentHead + 1);
        }

        return (E)e;
    }

    /**
     * {@inheritDoc}
     */
    public int drain(final Consumer<E> elementConsumer)
    {
        return drain(elementConsumer, (int)(tail - head));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public int drain(final Consumer<E> elementConsumer, final int limit)
    {
        final Object[] buffer = this.buffer;
        final long mask = this.capacity - 1;
        final long currentHead = head;
        long nextSequence = currentHead;
        final long limitSequence = nextSequence + limit;

        while (nextSequence < limitSequence)
        {
            final long elementOffset = sequenceToBufferOffset(nextSequence, mask);
            final Object item = UnsafeApi.getReferenceAcquire(buffer, elementOffset);

            if (null == item)
            {
                break;
            }

            UnsafeApi.putReferenceOpaque(buffer, elementOffset, null);
            nextSequence++;
            UnsafeApi.putLongOpaque(this, HEAD_OFFSET, nextSequence);
            elementConsumer.accept((E)item);
        }

        return (int)(nextSequence - currentHead);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public int drainTo(final Collection<? super E> target, final int limit)
    {
        final Object[] buffer = this.buffer;
        final long mask = this.capacity - 1;
        long nextSequence = head;
        int count = 0;

        while (count < limit)
        {
            final long elementOffset = sequenceToBufferOffset(nextSequence, mask);
            final Object item = UnsafeApi.getReferenceAcquire(buffer, elementOffset);
            if (null == item)
            {
                break;
            }

            UnsafeApi.putReferenceOpaque(buffer, elementOffset, null);
            nextSequence++;
            UnsafeApi.putLongOpaque(this, HEAD_OFFSET, nextSequence);
            count++;
            target.add((E)item);
        }

        return count;
    }
}
