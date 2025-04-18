package com.aldebaran.qi.sdk.util;

import android.os.Looper;
import android.os.NetworkOnMainThreadException;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.QiStruct;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiThreadPool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Utility methods used for work with futures
 */
public class FutureUtils {

    /**
     * Returns a future that transform a list of future into a future of list
     *
     * @param futures the list of futures
     * @param <T>     the type of return
     * @return a future of all elements
     */
    public static <T> Future<List<T>> zip(final Future<T>... futures) {
        return Future.waitAll(futures)
                .andThenApply(aVoid -> {
                    List<T> results = new ArrayList<>();
                    for (Future<T> future : futures) {
                        results.add(future.get());
                    }
                    return results;
                });
    }

    /**
     * Returns a future that transform a list of future into a future of list
     *
     * @param futures the list of futures
     * @param <T>     the type of return
     * @return a future of all elements
     */
    public static <T> Future<List<T>> zip(final List<Future<T>> futures) {
        return zip((Future<T>[]) futures.toArray(new Future[futures.size()]));
    }

    /**
     * Returns a future that wait a delay to finish
     *
     * @param delay    the delay to wait
     * @param timeUnit the time unit
     * @return a future of the execution
     */
    public static Future<Void> wait(long delay, TimeUnit timeUnit) {
        return QiThreadPool.schedule(() -> null, delay, timeUnit);
    }

    /**
     * Returns a future that log errors and cancel
     *
     * @param future        the original future
     * @param futureMessage the future message
     * @param <T>           the result type
     * @return a future of the execution
     */
    public static <T> Future<T> autoLog(Future<T> future, String futureMessage) {
        if (Qi.getAutoLogFutureErrors())
            future.thenConsume(new FutureLogger<T>(futureMessage));
        return future;
    }

    /**
     * Check if a future has already failed
     *
     * @param future the future
     * @return true if future has already failed
     */
    public static boolean hasAlreadyFailed(Future<?> future) {
        synchronized (future) {
            return future.isDone() && (future.hasError() || future.isCancelled());
        }
    }

    private static Object getValue(Field field, Object o) {
        try {
            field.setAccessible(true);
            return field.get(o);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e); // cannot occur
        }
    }

    private static boolean isQiStruct(Class<?> cls) {
        return cls.getAnnotation(QiStruct.class) != null;
    }

    private static boolean isTransient(Field field) {
        return (field.getModifiers() & Modifier.TRANSIENT) != 0;
    }

    public static <T> T get(Future<T> future) {
        if (!future.isCancelled() &&
                !future.isDone() &&
                Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new NetworkOnMainThreadException();
        }

        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return a Future of a Function
     *
     * @param function the function to be asynchronously executed
     * @param <R>      the type returned by the future
     * @return the future of the function
     */
    public static <R> Future<R> futureOf(final Function<Future<Void>, Future<R>> function) {
        return Future.<Void>of(null).thenCompose(function);
    }

    /**
     * Return a Future of a Consumer
     *
     * @param consumer the consumer to be asynchronously executed
     * @return the future of the consumer
     */
    public static Future<Void> futureOf(final Consumer<Future<Void>> consumer) {
        return Future.<Void>of(null).thenConsume(consumer);
    }

}
