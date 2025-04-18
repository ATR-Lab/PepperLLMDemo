package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.Optional;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.actuation.Frame;
import com.aldebaran.qi.sdk.object.actuation.GoTo;
import com.aldebaran.qi.sdk.object.actuation.GoToConfig;
import com.aldebaran.qi.sdk.object.actuation.OrientationPolicy;
import com.aldebaran.qi.sdk.object.actuation.PathPlanningPolicy;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link GoTo}
 */
public class GoToBuilder {

    private QiContext qiContext;
    private Frame frame;
    private GoToConfig config = null;

    private GoToBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static GoToBuilder with(QiContext context) {
        return new GoToBuilder(context);
    }

    /**
     * Configure the frame used by GoTo
     *
     * @param frame the target frame
     * @return the builder
     */
    public GoToBuilder withFrame(Frame frame) {
        this.frame = frame;
        return this;
    }

    /**
     * Configure the max speed used by GoTo
     *
     * @param maxSpeed the max speed
     * @return the builder
     */
    public GoToBuilder withMaxSpeed(Float maxSpeed) {
        if (this.config == null) {
            this.config = new GoToConfig();
        }
        this.config.setMaxSpeed(Optional.of(maxSpeed));
        return this;
    }

    /**
     * Configure the path planning policy used by GoTo
     *
     * @param pathPlanningPolicy the path planning policy
     * @return the builder
     */
    public GoToBuilder withPathPlanningPolicy(PathPlanningPolicy pathPlanningPolicy) {
        if (this.config == null) {
            this.config = new GoToConfig();
        }
        this.config.setPathPlanningPolicy(Optional.of(pathPlanningPolicy));
        return this;
    }

    /**
     * Configure the final orientation policy used by GoTo
     *
     * @param finalOrientationPolicy the final orientation policy
     * @return the builder
     */
    public GoToBuilder withFinalOrientationPolicy(OrientationPolicy finalOrientationPolicy) {
        if (this.config == null) {
            this.config = new GoToConfig();
        }
        this.config.setFinalOrientationPolicy(Optional.of(finalOrientationPolicy));
        return this;
    }

    /**
     * Return a configured instance of GoTo
     *
     * @return the GoTo
     */
    public GoTo build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of GoTo
     *
     * @return the GoTo
     */
    public Future<GoTo> buildAsync() {
        if (frame == null) {
            throw new IllegalStateException("Frame required.");
        }
        if (config == null) {
            return qiContext.getActuationAsync()
                    .andThenCompose(service -> service.async().makeGoTo(qiContext.getRobotContext(), frame));
        }
        return qiContext.getActuationAsync()
                .andThenCompose(service -> service.async().makeGoTo(qiContext.getRobotContext(), frame, config));
    }
}
