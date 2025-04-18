package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.EngageHuman;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link EngageHuman}
 */
public class EngageHumanBuilder {

    private final QiContext qiContext;
    private Human human;

    private EngageHumanBuilder(QiContext context) {
        this.qiContext = context;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static EngageHumanBuilder with(QiContext context) {
        return new EngageHumanBuilder(context);
    }

    /**
     * Configure human used by EngageHuman
     *
     * @param human the human
     * @return the builder
     */
    public EngageHumanBuilder withHuman(Human human) {
        this.human = human;
        return this;
    }

    /**
     * Return a configured instance of EngageHuman
     *
     * @return the EngageHuman
     */
    public EngageHuman build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of EngageHuman
     *
     * @return the EngageHuman
     */
    public Future<EngageHuman> buildAsync() {
        if (human == null) {
            throw new IllegalStateException("Human required.");
        }

        return qiContext.getHumanAwarenessAsync()
                .andThenCompose(service -> service.async().makeEngageHuman(qiContext.getRobotContext(), human));
    }

}
