package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.ApproachHuman;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link ApproachHuman}
 */
public class ApproachHumanBuilder {

    private final QiContext qiContext;
    private Human human;

    private ApproachHumanBuilder(QiContext context) {
        this.qiContext = context;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static ApproachHumanBuilder with(QiContext context) {
        return new ApproachHumanBuilder(context);
    }

    /**
     * Configure human used by ApproachHuman
     *
     * @param human the human
     * @return the builder
     */
    public ApproachHumanBuilder withHuman(Human human) {
        this.human = human;
        return this;
    }

    /**
     * Return a configured instance of ApproachHuman
     *
     * @return the ApproachHuman
     */
    public ApproachHuman build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of ApproachHuman
     *
     * @return the ApproachHuman
     */
    public Future<ApproachHuman> buildAsync() {
        if (human == null) {
            throw new IllegalStateException("Human required.");
        }

        return qiContext.getHumanAwarenessAsync()
                .andThenCompose(service -> service.async().makeApproachHuman(qiContext.getRobotContext(), human));
    }

}
