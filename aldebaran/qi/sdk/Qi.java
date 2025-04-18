package com.aldebaran.qi.sdk;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Function;
import com.aldebaran.qi.Promise;
import com.aldebaran.qi.QiRuntimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Qi {

    private static final String TAG = "QiSDK";

    private static final AtomicBoolean autoLogFutureErrors = new AtomicBoolean(true);

    private Qi() {
        // not instantiable
    }

    public static boolean getAutoLogFutureErrors() {
        return autoLogFutureErrors.get();
    }

    public static void setAutoLogFutureErrors(boolean autoLogFutureErrors) {
        Qi.autoLogFutureErrors.set(autoLogFutureErrors);
    }

    /**
     * Wrap the {@link Function} so that its callbacks are executed on the {@link Handler}.
     *
     * @param function the function to wrap
     * @param handler  the handler
     * @param <Ret>    the output future type
     * @param <Arg>    the input future type
     * @return a {@link Function} executed on the {@link Handler}
     */
    public static <Arg, Ret> Function<Arg, Ret> onHandler(final Function<Arg, Ret> function, final Handler handler) {
        return arg -> {
            final Promise<Ret> promise = new Promise<>();
            handler.post(() -> {
                try {
                    Ret resultFuture = function.execute(arg);
                    promise.setValue(resultFuture);
                } catch (CancellationException c) {
                    promise.setCancelled();
                } catch (Throwable e) {
                    promise.setError(e.getMessage());
                }
            });
            return promise.getFuture().get();
        };
    }

    /**
     * Wrap the {@link Consumer} so that its callbacks are executed on the {@link Handler}.
     *
     * @param consumer the function to wrap
     * @param handler  the handler
     * @param <Arg>    the input future type
     * @return a {@link Consumer} executed on the {@link Handler}
     */
    public static <Arg> Consumer<Arg> onHandler(final Consumer<Arg> consumer, final Handler handler) {
        return arg -> {
            final Promise<Void> promise = new Promise<>();
            handler.post(() -> {
                try {
                    consumer.consume(arg);
                    promise.setValue(null);
                } catch (CancellationException c) {
                    promise.setCancelled();
                } catch (Throwable e) {
                    promise.setError(e.getMessage());
                }
            });
            promise.getFuture().get();
        };
    }

    /**
     * Wrap the {@link QiDisconnectionListener} so that its callbacks are executed on the {@link Handler}.
     *
     * @param listener the listener to wrap
     * @param handler  the handler
     * @return a {@link QiDisconnectionListener} executed on the {@link Handler}
     */
    public static QiDisconnectionListener onHandler(final QiDisconnectionListener listener, final Handler handler) {
        return reason -> handler.post(() -> listener.onQiDisconnected(reason));
    }

    /**
     * Wrap the interface implementation so that its callbacks are executed on the {@link Handler}.
     *
     * @param interf   the interface to implement
     * @param callback the callback to wrap
     * @param handler  the handler
     * @param <T>      the output type
     * @return an instance of the interface that will call the callback on the{@link Handler}
     */
    public static <T> T onHandler(Class<T> interf, final Object callback, final Handler handler) {
        Class<?>[] interfaces = {interf};
        @SuppressWarnings("unchecked")
        T result = (T) Proxy.newProxyInstance(Qi.class.getClassLoader(), interfaces, (proxy, method, args) -> {
            if (method.getReturnType() != void.class)
                throw new QiRuntimeException("Cannot execute a non-void method on another thread");
            handler.post(() -> {
                try {
                    method.invoke(callback, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Log.e(TAG, "Cannot call method", e);
                }
            });
            return null;
        });
        return result;
    }

    /**
     * Wrap the {@link Function} so that its callbacks are executed on the UI thread.
     *
     * @param function the function to wrap
     * @param <Ret>    the output future type
     * @param <Arg>    the input future type
     * @return a {@link Function} executed on the UI thread
     */
    public static <Arg, Ret> Function<Arg, Ret> onUiThread(Function<Arg, Ret> function) {
        return onHandler(function, new Handler(Looper.getMainLooper()));
    }


    /**
     * Wrap the {@link Consumer} so that its callbacks are executed on the UI thread.
     *
     * @param consumer the function to wrap
     * @param <Arg>    the input future type
     * @return a {@link Function} executed on the UI thread
     */
    public static <Arg> Consumer<Arg> onUiThread(Consumer<Arg> consumer) {
        return onHandler(consumer, new Handler(Looper.getMainLooper()));
    }

    /**
     * Wrap the {@link QiDisconnectionListener} so that its callbacks are executed on the UI thread.
     *
     * @param listener the listener to wrap
     * @return a {@link QiDisconnectionListener} executed on the UI thread
     */
    public static QiDisconnectionListener onUiThread(QiDisconnectionListener listener) {
        return onHandler(listener, new Handler(Looper.getMainLooper()));
    }


    /**
     * Wrap the interface implementation so that its callbacks are executed on the UI thread.
     *
     * @param interf   the interface to implement
     * @param callback the callback to wrap
     * @param <T>      the output type
     * @return an instance of the interface that will call the callback on the UI thread
     */
    public static <T> T onUiThread(Class<T> interf, Object callback) {
        return onHandler(interf, callback, new Handler(Looper.getMainLooper()));
    }
}
