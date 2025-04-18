package com.aldebaran.qi.sdk.util;

import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.QiException;

import java.util.concurrent.ExecutionException;

/**
 * A {@link Consumer} use to automatically log future error or cancel
 *
 * @param <T> the result type
 */
public class FutureLogger<T> implements Consumer<Future<T>> {

    private static final String TAG = "QiSDK_FutureLogger";

    private String message;

    public FutureLogger(String message) {
        this.message = message;
    }

    @Override
    public void consume(Future<T> future) throws Throwable {
        if (future.hasError()) {
            ExecutionException error = future.getError();
            if (error instanceof QiException) {
                // for now, libqi errors are always a simple string wrapped in a QiException
                // do not print the useless stack trace
                Log.e(TAG, "Future [" + message + "] finished with an error: " + error.getMessage());
            } else {
                Log.e(TAG, "Future [" + message + "] finished with an error", error);
            }
        } else if (future.isCancelled()) {
            Log.w(TAG, "Future [" + message + "] cancelled");
        }
    }
}
