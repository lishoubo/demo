package com.learn.lishoubo.frame.utils;

import com.vdian.middleware.qps.core.utils.LongMaxUpdater;
import com.vdian.middleware.qps.core.utils.VdianRollingNumberEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by lishoubo on 17/6/22.
 */

public class VdianRollingNumber implements Serializable {
    private static final long serialVersionUID = 1437880145080150735L;

    private final int timeInMilliseconds;
    private final int numberOfBuckets;
    private final int millisecondsInBucket;

    private final BucketCircularArray buckets;
    private final Time time;

    private ReentrantLock newBucketLock = new ReentrantLock();

    public VdianRollingNumber(int timeInMilliseconds, int numberOfBuckets) {
        this(new ActualTime(), timeInMilliseconds, numberOfBuckets);
    }

    public VdianRollingNumber(Time time, int timeInMilliseconds, int numberOfBuckets) {
        this.timeInMilliseconds = timeInMilliseconds;
        this.numberOfBuckets = numberOfBuckets;
        this.time = time;

        if (timeInMilliseconds % numberOfBuckets != 0) {
            throw new IllegalArgumentException(
                    "The timeInMilliseconds must divide equally into numberOfBuckets. For example 1000/10 is ok, 1000/11 is not.");
        }

        this.millisecondsInBucket = timeInMilliseconds / numberOfBuckets;
        buckets = new BucketCircularArray(numberOfBuckets);
    }

    /**
     * Increment the counter in the current bucket by one for the given {@link VdianRollingNumberEvent} type.
     * <p/>
     * The {@link VdianRollingNumberEvent} must be a "counter" type <code>VdianRollingNumberEvent.isCounter() == true</code>.
     *
     * @param type HystrixRollingNumberEvent defining which counter to increment
     */
    public void increment(VdianRollingNumberEvent type) {
        getCurrentBucket().getAdder(type).increment();
    }

    /**
     * Add to the counter in the current bucket for the given {@link VdianRollingNumberEvent} type.
     * <p/>
     * The {@link VdianRollingNumberEvent} must be a "counter" type <code>VdianRollingNumberEvent.isCounter() == true</code>.
     *
     * @param type  VdianRollingNumberEvent defining which counter to add to
     * @param value long value to be added to the current bucket
     */
    public void add(VdianRollingNumberEvent type, long value) {
        getCurrentBucket().getAdder(type).add(value);
    }

    /**
     * Update a value and retain the max value.
     * <p/>
     * The {@link VdianRollingNumberEvent} must be a "max updater" type <code>HystrixRollingNumberEvent.isMaxUpdater() == true</code>.
     *
     * @param type  VdianRollingNumberEvent defining which counter to retrieve values from
     * @param value long value to be given to the max updater
     */
    public void updateRollingMax(VdianRollingNumberEvent type, long value) {
        getCurrentBucket().getMaxUpdater(type).update(value);
    }


    /**
     * Get the sum of all buckets in the rolling counter for the given {@link VdianRollingNumberEvent} type.
     * <p/>
     * The {@link VdianRollingNumberEvent} must be a "counter" type <code>VdianRollingNumberEvent.isCounter() == true</code>.
     *
     * @param type VdianRollingNumberEvent defining which counter to retrieve values from
     * @return value from the given {@link VdianRollingNumberEvent} counter type
     */
    public long getRollingSum(VdianRollingNumberEvent type) {
        Bucket lastBucket = getCurrentBucket();
        if (lastBucket == null)
            return 0;

        long sum = 0;
        for (Bucket b : buckets) {
            sum += b.getAdder(type).sum();
        }
        return sum;
    }

    /**
     * Get the value of the latest (current) bucket in the rolling counter for the given {@link VdianRollingNumberEvent} type.
     * <p/>
     * The {@link VdianRollingNumberEvent} must be a "counter" type <code>VdianRollingNumberEvent.isCounter() == true</code>.
     *
     * @param type VdianRollingNumberEvent defining which counter to retrieve value from
     * @return value from latest bucket for given {@link VdianRollingNumberEvent} counter type
     */
    public long getValueOfLatestBucket(VdianRollingNumberEvent type) {
        Bucket lastBucket = getCurrentBucket();
        if (lastBucket == null)
            return 0;
        // we have bucket data so we'll return the lastBucket
        return lastBucket.get(type);
    }

    /**
     * Get an array of values for all buckets in the rolling counter for the given {@link VdianRollingNumberEvent} type.
     * <p/>
     * Index 0 is the oldest bucket.
     * <p/>
     * The {@link VdianRollingNumberEvent} must be a "counter" type <code>VdianRollingNumberEvent.isCounter() == true</code>.
     *
     * @param type VdianRollingNumberEvent defining which counter to retrieve values from
     * @return array of values from each of the rolling buckets for given {@link VdianRollingNumberEvent} counter type
     */
    public long[] getValues(VdianRollingNumberEvent type) {
        Bucket lastBucket = getCurrentBucket();
        if (lastBucket == null)
            return new long[0];

        // get buckets as an array (which is a copy of the current state at this point in time)
        Bucket[] bucketArray = buckets.getArray();

        // we have bucket data so we'll return an array of values for all buckets
        long values[] = new long[bucketArray.length];
        int i = 0;
        for (Bucket bucket : bucketArray) {
            if (type.isCounter()) {
                values[i++] = bucket.getAdder(type).sum();
            } else if (type.isMaxUpdater()) {
                values[i++] = bucket.getMaxUpdater(type).max();
            }
        }
        return values;
    }

    /**
     * Get the max value of values in all buckets for the given {@link VdianRollingNumberEvent} type.
     * <p/>
     * The {@link VdianRollingNumberEvent} must be a "max updater" type <code>VdianRollingNumberEvent.isMaxUpdater() == true</code>.
     *
     * @param type VdianRollingNumberEvent defining which "max updater" to retrieve values from
     * @return max value for given {@link VdianRollingNumberEvent} type during rolling window
     */
    public long getRollingMaxValue(VdianRollingNumberEvent type) {
        long values[] = getValues(type);
        if (values.length == 0) {
            return 0;
        } else {
            Arrays.sort(values);
            return values[values.length - 1];
        }
    }

    /**
     * Force a reset of all rolling counters (clear all buckets) so that statistics start being gathered from scratch.
     */
    public void reset() {
        // clear buckets so we start over again
        buckets.clear();
    }


    private Bucket getCurrentBucket() {

        long currentTime = time.getCurrentTimeInMillis();

        /* a shortcut to try and get the most common result of immediately finding the current bucket */

        /**
         * Retrieve the latest bucket if the given time is BEFORE the end of the bucket window, otherwise it returns
         * NULL. buckets是线程安全的
         */
        Bucket currentBucket = buckets.peekLast();
        if (currentBucket != null && currentTime < currentBucket.windowStart + this.millisecondsInBucket) {
            // if we're within the bucket 'window of time' return the current one
            // NOTE: We do not worry if we are BEFORE the window in a weird case of where thread scheduling causes that
            // to occur,
            // we'll just use the latest as long as we're not AFTER the window
            return currentBucket;
        }

        if (newBucketLock.tryLock()) {
            try {
                /**
                 * vguard里面的rollingnumber这个地方有问题, vguard里面直接使用的currentBucket
                 */
                if (buckets.peekLast() == null) {
                    Bucket newBucket = new Bucket(currentTime);
                    buckets.addLast(newBucket);
                    return newBucket;
                } else {
                    // We go into a loop so that it will create as many buckets as needed to catch up to the current
                    // time
                    // as we want the buckets complete even if we don't have transactions during a period of time.
                    for (int i = 0; i < numberOfBuckets; i++) {
                        // we have at least 1 bucket so retrieve it
                        Bucket lastBucket = buckets.peekLast();
                        if (currentTime < lastBucket.windowStart + this.millisecondsInBucket) {
                            // if we're within the bucket 'window of time' return the current one
                            // NOTE: We do not worry if we are BEFORE the window in a weird case of where thread
                            // scheduling causes that to occur,
                            // we'll just use the latest as long as we're not AFTER the window
                            return lastBucket;
                        } else if (currentTime - (lastBucket.windowStart + this.millisecondsInBucket) > timeInMilliseconds) {
                            reset();
                            // recursively call getCurrentBucket which will create a new bucket and return it
                            return getCurrentBucket();
                        } else {
                            // we're past the window so we need to create a new bucket
                            // create a new bucket and add it as the new 'last'
                            buckets.addLast(new Bucket(lastBucket.windowStart + this.millisecondsInBucket));
                        }
                    }
                    // we have finished the for-loop and created all of the buckets, so return the lastBucket now
                    return buckets.peekLast();
                }

            } finally {
                newBucketLock.unlock();
            }
        } else {
            // 否则，让别的线程继续通行
            currentBucket = buckets.peekLast();
            if (currentBucket != null) {
                // 当有线程正在往尾部添加bucket的时候
                return currentBucket;
            } else {
                // 存在一个或者多个线程都在竞争创建第一个锁。则等待，重试
                try {
                    Thread.sleep(5);
                } catch (Exception e) {
                    // ignore
                }
                return getCurrentBucket();
            }
        }

    }

    private static class Bucket implements Serializable {
        final long windowStart;
        final LongAdder[] adderForCounterType;
        final com.vdian.middleware.qps.core.utils.LongMaxUpdater[] updaterForCounterType;

        Bucket(long startTime) {
            this.windowStart = startTime;

            /*
             * We support both LongAdder and LongMaxUpdater in a bucket but don't want the memory allocation of all
             * types for each so we only allocate the objects if the HystrixRollingNumberEvent matches the correct type
             * - though we still have the allocation of empty arrays to the given length as we want to keep using the
             * type.ordinal() value for fast random access.
             */

            // initialize the array of LongAdders
            adderForCounterType = new LongAdder[VdianRollingNumberEvent.values().length];
            for (VdianRollingNumberEvent type : VdianRollingNumberEvent.values()) {
                if (type.isCounter()) {
                    adderForCounterType[type.ordinal()] = new LongAdder();
                }
            }

            updaterForCounterType = new com.vdian.middleware.qps.core.utils.LongMaxUpdater[VdianRollingNumberEvent.values().length];
            for (VdianRollingNumberEvent type : VdianRollingNumberEvent.values()) {
                if (type.isMaxUpdater()) {
                    updaterForCounterType[type.ordinal()] = new com.vdian.middleware.qps.core.utils.LongMaxUpdater();
                    updaterForCounterType[type.ordinal()].update(0);
                }
            }
        }

        long get(VdianRollingNumberEvent type) {
            if (type.isCounter()) {
                return adderForCounterType[type.ordinal()].sum();
            }
            if (type.isMaxUpdater()) {
                return updaterForCounterType[type.ordinal()].max();
            }
            throw new IllegalStateException("Unknown type of event: " + type.name());
        }

        LongAdder getAdder(VdianRollingNumberEvent type) {
            if (!type.isCounter()) {
                throw new IllegalStateException("Type is not a Counter: " + type.name());
            }
            return adderForCounterType[type.ordinal()];
        }

        LongMaxUpdater getMaxUpdater(VdianRollingNumberEvent type) {
            if (!type.isMaxUpdater()) {
                throw new IllegalStateException("Type is not a MaxUpdater: " + type.name());
            }
            return updaterForCounterType[type.ordinal()];
        }
    }


    /**
     * This is a circular array acting as a FIFO queue.
     * <p/>
     * It purposefully does NOT implement Deque or some other Collection interface as it only implements functionality
     * necessary for this RollingNumber use case.
     * <p/>
     * Important Thread-Safety Note: This is ONLY thread-safe within the context of RollingNumber and the protection it
     * gives in the <code>getCurrentBucket</code> method. It uses AtomicReference objects to ensure anything done
     * outside of <code>getCurrentBucket</code> is thread-safe, and to ensure visibility of changes across threads (ie.
     * volatility) but the addLast and removeFirst methods are NOT thread-safe for external access they depend upon the
     * lock.tryLock() protection in <code>getCurrentBucket</code> which ensures only a single thread will access them at
     * at time.
     * <p/>
     * benjchristensen => This implementation was chosen based on performance testing I did and documented at:
     * http://benjchristensen.com/2011/10/08/atomiccirculararray/
     */
    private class BucketCircularArray implements Iterable<Bucket>, Serializable {

        /**
         *
         */
        private static final long serialVersionUID = -8973969734912754681L;

        private final AtomicReference<ListState> state;
        private final int dataLength; // we don't resize, we always stay the same, so remember
        // this
        private final int numBuckets;

        /**
         * Immutable object that is atomically set every time the state of the BucketCircularArray changes
         * <p/>
         * This handles the compound operations
         */
        private class ListState {
            /*
             * this is an AtomicReferenceArray and not a normal Array because we're copying the reference
             * between ListState objects and multiple threads could maintain references across these
             * compound operations so I want the visibility/concurrency guarantees
             */
            private final AtomicReferenceArray<Bucket> data;
            private final int size;
            private final int tail;
            private final int head;

            private ListState(AtomicReferenceArray<Bucket> data, int head, int tail) {
                this.head = head;
                this.tail = tail;
                if (head == 0 && tail == 0) {
                    size = 0;
                } else {
                    this.size = (tail + dataLength - head) % dataLength;
                }
                this.data = data;
            }

            public Bucket tail() {
                if (size == 0) {
                    return null;
                } else {
                    // we want to get the last item, so size()-1
                    return data.get(convert(size - 1));
                }
            }

            private Bucket[] getArray() {
                /*
                 * this isn't technically thread-safe since it requires multiple reads on something that can change
                 * but since we never clear the data directly, only increment/decrement head/tail we would never get a NULL
                 * just potentially return stale data which we are okay with doing
                 */
                ArrayList<Bucket> array = new ArrayList<Bucket>();
                for (int i = 0; i < size; i++) {
                    array.add(data.get(convert(i)));
                }
                return array.toArray(new Bucket[array.size()]);
            }

            private ListState incrementTail() {
                /* if incrementing results in growing larger than 'length' which is the max we should be at, then also increment head (equivalent of removeFirst but done atomically) */
                if (size == numBuckets) {
                    // increment tail and head
                    return new ListState(data, (head + 1) % dataLength, (tail + 1) % dataLength);
                } else {
                    // increment only tail
                    return new ListState(data, head, (tail + 1) % dataLength);
                }
            }

            public ListState clear() {
                return new ListState(new AtomicReferenceArray<Bucket>(dataLength), 0, 0);
            }

            public ListState addBucket(Bucket b) {
                /*
                 * We could in theory have 2 threads addBucket concurrently and this compound operation would interleave.
                 * <p>
                 * This should NOT happen since getCurrentBucket is supposed to be executed by a single thread.
                 * <p>
                 * If it does happen, it's not a huge deal as incrementTail() will be protected by compareAndSet and one of the two addBucket calls will succeed with one of the Buckets.
                 * <p>
                 * In either case, a single Bucket will be returned as "last" and data loss should not occur and everything keeps in sync for head/tail.
                 * <p>
                 * Also, it's fine to set it before incrementTail because nothing else should be referencing that index position until incrementTail occurs.
                 */
                data.set(tail, b);
                return incrementTail();
            }

            // The convert() method takes a logical index (as if head was
            // always 0) and calculates the index within elementData
            private int convert(int index) {
                return (index + head) % dataLength;
            }
        }

        public BucketCircularArray(int size) {

            AtomicReferenceArray<Bucket> _buckets = new AtomicReferenceArray<Bucket>(size + 1); // + 1 as extra room for
            // the add/remove;
            state = new AtomicReference<ListState>(new ListState(_buckets, 0, 0));
            dataLength = _buckets.length();
            numBuckets = size;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<Bucket> iterator() {
            return Collections.unmodifiableList(Arrays.asList(getArray())).iterator();
        }

        public void clear() {
            while (true) {
                /*
                 * it should be very hard to not succeed the first pass thru since this is typically is only called from
                 * a single thread protected by a tryLock, but there is at least 1 other place (at time of writing this
                 * comment) where reset can be called from (CircuitBreaker.markSuccess after circuit was tripped) so it
                 * can in an edge-case conflict. Instead of trying to determine if someone already successfully called
                 * clear() and we should skip we will have both calls reset the circuit, even if that means losing data
                 * added in between the two depending on thread scheduling. The rare scenario in which that would occur,
                 * we'll accept the possible data loss while clearing it since the code has stated its desire to clear()
                 * anyways.
                 */
                ListState current = state.get();
                ListState newState = current.clear();
                if (state.compareAndSet(current, newState)) {
                    return;
                }
            }
        }

        public void addLast(Bucket o) {
            ListState currentState = state.get();
            // create new version of state (what we want it to become)
            ListState newState = currentState.addBucket(o);

            /*
             * use compareAndSet to set in case multiple threads are attempting (which shouldn't be the case because
             * since addLast will ONLY be called by a single thread at a time due to protection provided in
             * <code>getCurrentBucket</code>)
             */
            if (state.compareAndSet(currentState, newState)) {
                // we succeeded
                return;
            } else {
                // we failed, someone else was adding or removing
                // instead of trying again and risking multiple addLast concurrently (which shouldn't be the case)
                // we'll just return and let the other thread 'win' and if the timing is off the next call to
                // getCurrentBucket will fix things
                return;
            }
        }

        public Bucket getLast() {
            return peekLast();
        }

        public int size() {
            // the size can also be worked out each time as:
            // return (tail + data.length() - head) % data.length();
            return state.get().size;
        }

        public Bucket peekLast() {
            return state.get().tail();
        }

        private Bucket[] getArray() {
            return state.get().getArray();
        }

    }
}