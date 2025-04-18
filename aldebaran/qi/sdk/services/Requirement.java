package com.aldebaran.qi.sdk.services;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;

import java.util.ArrayList;
import java.util.List;

/**
 * A requirement creates and holds a {@link Future}, which represents the value once satisfied.
 * <p/>
 * If a requirement is already satisfied (i.e. a future not finished with an error is present), {@link #satisfy()}
 * returns the existing future. Otherwise, it creates a new one.
 * <p/>
 * An existing future can be invalidated ({@link #invalidate()}, so that a future call to {@link #satisfy()} will
 * create a new one.
 *
 * @param <T> the type of the resulting value
 */
public abstract class Requirement<T> {

    /**
     * Listener on value availability.
     * <p/>
     * A value is available if and only if a call to {@code satisfy().get()} returns the value immediately.
     */
    public interface AvailableListener {

        /**
         * Method called when the availability changes.
         *
         * @param available the new availability state
         */
        void onAvailableChanged(boolean available);
    }

    private Future<T> cache;
    private boolean available;
    private int generation;
    private final List<AvailableListener> availableListeners = new ArrayList<>();

    protected abstract Future<T> create();

    public synchronized final Future<T> satisfy() {
        if (hasInvalidCache()) {
            invalidate();
        }
        if (cache == null || hasAlreadyFailed(cache)) {
            cache = create();
            registerAvailability();
        }
        return cache;
    }

    public synchronized void invalidate() {
        if (cache != null) {
            cache.requestCancellation();
            cache = null;
            invalidateAvailability();
        }
    }

    public synchronized void addAvailableListener(AvailableListener availableListener) {
        availableListeners.add(availableListener);
    }

    public synchronized void removeAvailableListener(AvailableListener availableListener) {
        availableListeners.remove(availableListener);
    }

    private void registerAvailability() {
        final int currentGeneration = ++generation;
        cache.thenConsume(new Consumer<Future<T>>() {
            @Override
            public void consume(Future<T> tFuture) throws Throwable {
                onAvailable(currentGeneration);
            }
        });
    }

    private void invalidateAvailability() {
        ++generation;
        setAvailable(false);
    }

    private synchronized void onAvailable(int availableGeneration) {
        if (generation != availableGeneration)
            return;
        setAvailable(true);
    }

    public synchronized boolean isAvailable() {
        return available;
    }

    protected boolean isStillValid(T result) {
        return true;
    }

    private boolean hasInvalidCache() {
        // already synchronized on this
        if (cache == null)
            return false;

        boolean succeeded;
        T value = null;
        synchronized (cache) {
            // lock the future mutex to avoid an asynchronous future.cancel() between hasAlreadySucceeded() and
            // future.getValue()
            succeeded = hasAlreadySucceeded(cache);
            if (succeeded)
                value = cache.getValue();
        }
        return !succeeded || !isStillValid(value);
    }

    private void setAvailable(boolean available) {
        if (this.available != available) {
            this.available = available;
            fireOnAvailableChanged(available);
        }
    }

    private void fireOnAvailableChanged(boolean available) {
        for (AvailableListener availableListener : availableListeners)
            availableListener.onAvailableChanged(available);
    }

    private static boolean hasAlreadyFailed(Future<?> future) {
        return future.isDone() && (future.hasError() || future.isCancelled());
    }

    private static boolean hasAlreadySucceeded(Future<?> future) {
        return future.isDone() && !future.hasError() && !future.isCancelled();
    }
}
