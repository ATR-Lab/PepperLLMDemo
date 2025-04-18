package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link TakePicture}
 */
public class TakePictureBuilder {
    private QiContext qiContext;

    private TakePictureBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static TakePictureBuilder with(QiContext context) {
        return new TakePictureBuilder(context);
    }

    /**
     * Return a configured instance of TakePicture
     *
     * @return the TakePicture
     */
    public TakePicture build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of TakePicture
     *
     * @return the TakePicture
     */
    public Future<TakePicture> buildAsync() {
        return qiContext.getCameraAsync()
                .andThenCompose(service -> service.async().makeTakePicture(qiContext.getRobotContext()));
    }
}
