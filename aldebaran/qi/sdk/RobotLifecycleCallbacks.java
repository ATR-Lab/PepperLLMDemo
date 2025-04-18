package com.aldebaran.qi.sdk;

/**
 * Robot Lifecycle Callback.
 */
public interface RobotLifecycleCallbacks {
    /**
     * Called when focus is gained
     *
     * @param qiContext the robot context
     */
    void onRobotFocusGained(QiContext qiContext);

    /**
     * Called when focus is lost
     */
    void onRobotFocusLost();

    /**
     * Called when focus is refused
     *
     * @param reason the reason
     */
    void onRobotFocusRefused(String reason);
}