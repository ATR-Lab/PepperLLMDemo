package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.Localize;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link Localize}
 */
public class LocalizeBuilder {
    private QiContext qiContext;

    private ExplorationMap explorationMap;

    private LocalizeBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static LocalizeBuilder with(QiContext context) {
        return new LocalizeBuilder(context);
    }

    /**
     * Configure the explorationMap used by Localize
     *
     * @param explorationMap the exploration map
     * @return the builder
     */
    public LocalizeBuilder withMap(ExplorationMap explorationMap) {
        this.explorationMap = explorationMap;
        return this;
    }

    /**
     * Return a configured instance of Localize
     *
     * @return the Localize
     */
    public Localize build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of Localize
     *
     * @return the Localize
     */
    public Future<Localize> buildAsync() {
        if (explorationMap == null) {
            throw new IllegalStateException("Exploration map required.");
        }

        return qiContext.getMappingAsync()
                .andThenCompose(service -> service.async().makeLocalize(qiContext.getRobotContext(), explorationMap));
    }
}
