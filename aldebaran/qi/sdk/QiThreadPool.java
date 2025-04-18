package com.aldebaran.qi.sdk;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.Promise;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Shared thread pool.
 * <p>
 * Mainly for internal purpose.
 */
public final class QiThreadPool {

    private static final String TAG = "QiThreadPool";

    // for now, use a java thread pool; later, could use the native libqi thread pool
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    private QiThreadPool() {
        // not instantiable
    }

    /**
     * Execute a callable on the thread pool.
     *
     * @param callable the callable
     * @param <V>      the value type
     * @return a future of the computed result
     */
    public static <V> Future<V> execute(final Callable<V> callable) {
        final Promise<V> promise = new Promise<>();
        executor.execute(() -> {
            try {
                V value = callable.call();
                promise.setValue(value);
            } catch (Throwable t) {
                Log.e(TAG, "Execution error", t);
                promise.setError(t.getMessage());
            }
        });
        return promise.getFuture();
    }

    /**
     * Execute a callable with delay on the thread pool.
     *
     * @param callable the callable
     * @param delay    the delay
     * @param timeUnit the delay time unit
     * @param <V>      the value type
     * @return a future of the computed result
     */
    public static <V> Future<V> schedule(final Callable<V> callable, long delay, TimeUnit timeUnit) {
        final Promise<V> promise = new Promise<>();

        final ScheduledFuture<?> scheduledFuture = executor.schedule(() -> {
            try {
                V value = callable.call();
                promise.setValue(value);
            } catch (Throwable t) {
                Log.e(TAG, "Execution error", t);
                promise.setError(t.getMessage());
            }
        }, delay, timeUnit);

        promise.setOnCancel(ignored -> {
                    scheduledFuture.cancel(true);
                    promise.setCancelled();
                }
        );

        return promise.getFuture();
    }
}
