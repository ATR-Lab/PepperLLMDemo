package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.EnforceTabletReachability;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link EnforceTabletReachability}
 */
public class EnforceTabletReachabilityBuilder {

    private QiContext qiContext;

    private EnforceTabletReachabilityBuilder(QiContext context) {
        this.qiContext = context;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static EnforceTabletReachabilityBuilder with(QiContext context) {
        return new EnforceTabletReachabilityBuilder(context);
    }

    /**
     * Return a configured instance of EnforceTabletReachability
     *
     * @return the EnforceTabletReachability
     */
    public EnforceTabletReachability build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of EnforceTabletReachability
     *
     * @return the EnforceTabletReachability
     */
    public Future<EnforceTabletReachability> buildAsync() {
        return qiContext.getActuationAsync()
                .andThenCompose(service -> service.async().makeEnforceTabletReachability(qiContext.getRobotContext()));
    }

}
