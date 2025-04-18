package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.ExplorationMap;
import com.aldebaran.qi.sdk.object.streamablebuffer.StreamableBuffer;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link ExplorationMap}
 */
public class ExplorationMapBuilder {
    private QiContext qiContext;
    private String mapString;
    private StreamableBuffer streamableBuffer;

    private ExplorationMapBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the QiContext
     * @return the builder
     */
    public static ExplorationMapBuilder with(QiContext context) {
        return new ExplorationMapBuilder(context);
    }

    /**
     * Configure the data used by the ExplorationMap
     *
     * @param mapString the map data
     * @return the builder
     */
    public ExplorationMapBuilder withMapString(String mapString) {
        this.mapString = mapString;
        this.streamableBuffer = null;
        return this;
    }

    /**
     * Configure the data used by the ExplorationMap
     *
     * @param streamableBuffer the streamable buffer
     * @return the builder
     */
    public ExplorationMapBuilder withStreamableBuffer(StreamableBuffer streamableBuffer) {
        this.streamableBuffer = streamableBuffer;
        this.mapString = null;
        return this;
    }

    /**
     * Return a configured instance of ExplorationMap
     *
     * @return the ExplorationMap
     */
    public ExplorationMap build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of ExplorationMap
     *
     * @return the ExplorationMap
     */
    public Future<ExplorationMap> buildAsync() {
        if (streamableBuffer == null) {
            if (mapString == null || mapString.isEmpty()) {
                throw new IllegalStateException("mapString or streamableBuffer required.");
            }
            return qiContext.getMappingAsync()
                    .andThenCompose(service -> service.async().makeMap(mapString));
        }

        return qiContext.getMappingAsync()
                .andThenCompose(service -> service.async().makeMap(streamableBuffer));
    }
}
