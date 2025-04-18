package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.LookAt;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link LookAt}
 */
public class LookAtBuilder {
    private final QiContext qiContext;
    private Frame frame;

    private LookAtBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static LookAtBuilder with(QiContext context) {
        return new LookAtBuilder(context);
    }

    /**
     * Create the {@link LookAt} instance using configured values
     *
     * @return the LookAt
     */
    public LookAt build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Configure the frame used by LookAt
     *
     * @param frame the target frame
     * @return the builder
     */
    public LookAtBuilder withFrame(Frame frame) {
        this.frame = frame;
        return this;
    }

    /**
     * Create the {@link LookAt} instance using configured values
     *
     * @return the LookAt
     */
    public Future<LookAt> buildAsync() {
        if (frame == null) {
            throw new IllegalStateException("Frame required.");
        }

        return qiContext.getActuationAsync()
                .andThenCompose(service -> service.async().makeLookAt(qiContext.getRobotContext(), frame));
    }
}