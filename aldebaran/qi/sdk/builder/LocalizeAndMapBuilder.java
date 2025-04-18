package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.actuation.LocalizeAndMap;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link LocalizeAndMap}
 */
public class LocalizeAndMapBuilder {
    private QiContext qiContext;

    private ExplorationMap explorationMap;

    private LocalizeAndMapBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static LocalizeAndMapBuilder with(QiContext context) {
        return new LocalizeAndMapBuilder(context);
    }

    /**
     * Configure the explorationMap used by LocalizeAndMap
     *
     * @param explorationMap the exploration map
     * @return the builder
     */
    public LocalizeAndMapBuilder withMap(ExplorationMap explorationMap) {
        this.explorationMap = explorationMap;
        return this;
    }

    /**
     * Return a configured instance of LocalizeAndMap
     *
     * @return the LocalizeAndMap
     */
    public LocalizeAndMap build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of LocalizeAndMap
     *
     * @return the LocalizeAndMap
     */
    public Future<LocalizeAndMap> buildAsync() {
        if (explorationMap == null) {
            return qiContext.getMappingAsync()
                    .andThenCompose(service -> service.async().makeLocalizeAndMap(qiContext.getRobotContext()));
        } else {
            return qiContext.getMappingAsync()
                    .andThenCompose(service -> service.async().makeLocalizeAndMap(qiContext.getRobotContext(), explorationMap));
        }
    }
}
