package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link Animate}
 */
public class AnimateBuilder {
    private QiContext qiContext;
    private Animation animation;

    private AnimateBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static AnimateBuilder with(QiContext context) {
        return new AnimateBuilder(context);
    }

    /**
     * Configure animation used by Animate
     *
     * @param animation the animation
     * @return the builder
     */
    public AnimateBuilder withAnimation(Animation animation) {
        this.animation = animation;
        return this;
    }

    /**
     * Return a configured instance of Animate
     *
     * @return the AnimateAction
     */
    public Animate build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of Animate
     *
     * @return the AnimateAction
     */
    public Future<Animate> buildAsync() {
        if (animation == null) {
            throw new IllegalStateException("Animation required.");
        }

        return qiContext.getActuationAsync()
                .andThenCompose(service -> service.async().makeAnimate(qiContext.getRobotContext(), animation));
    }

}
