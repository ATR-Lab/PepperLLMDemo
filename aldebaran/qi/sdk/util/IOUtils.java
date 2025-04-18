package com.aldebaran.qi.sdk.util;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiThreadPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * Utility methods used for work raw files and assets
 */
public final class IOUtils {

    private IOUtils() {
        // not instantiable
    }

    /**
     * Returns a string that represent the content of a stream
     *
     * @param is      the input stream
     * @param charset the output chatset
     * @return the content of the stream
     * @throws IOException if file is not found
     */
    public static String readAllStream(InputStream is, String charset) throws IOException {
        return new Scanner(is, charset).useDelimiter("\\A").next();
    }

    /**
     * Returns a byte array that represent the content of the stream
     *
     * @param is the input stream
     * @return the content of the stream
     * @throws IOException if file is not found
     */
    public static byte[] toByteArray(InputStream is) throws IOException {
        try {
            ByteArrayOutputStream bis = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int nRead;
            while ((nRead = is.read(buf)) != -1)
                bis.write(buf, 0, nRead);
            return bis.toByteArray();
        } finally {
            is.close();
        }
    }

    /**
     * Returns a string from a raw file
     *
     * @param context the Android context
     * @param resId   the resource id
     * @return the content of the raw file
     */
    public static String fromRaw(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
    }

    /**
     * Returns a string from a asset file
     *
     * @param context   the Android context
     * @param assetName the asset file
     * @return the content of the asset file
     * @throws NotFoundException if file not found
     */
    public static String fromAsset(Context context, String assetName) throws NotFoundException {
        try {
            InputStream inputStream = context.getAssets().open(assetName);
            return new Scanner(inputStream, "UTF-8").useDelimiter("\\A").next();
        } catch (IOException e) {
            throw new NotFoundException();
        }
    }

    private static Future<String> from(final Context context, Callable<String> contentRetriever) {
        return QiThreadPool.execute(contentRetriever);
    }
}
