package com.aldebaran.qi.sdk;

import android.app.Activity;
import android.content.Context;

import com.aldebaran.qi.Session;
import com.aldebaran.qi.sdk.core.SessionManager;
import com.aldebaran.qi.serialization.QiSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a connection to a robot.
 */
public class QiRobot implements SessionManager.Callback {

    private SessionManager sessionManager;

    private final QiServices services;
    private final QiSerializer serializer;
    private final Map<Integer, QiContext> qiContexts;

    QiRobot(Context context, QiSerializer serializer) {
        this.serializer = serializer;
        this.sessionManager = new SessionManager(false);
        this.services = new QiServices(serializer);
        this.qiContexts = new HashMap<>();

        sessionManager.register(context.getApplicationContext(), this);
    }


    QiContext retrieveQiContext(Activity activity) {
        int activityHasCode = System.identityHashCode(activity);

        if (!qiContexts.containsKey(activityHasCode)) {
            qiContexts.put(activityHasCode, new QiContext(activity, serializer, services, sessionManager));
        }

        return qiContexts.get(activityHasCode);
    }

    void removeQiContext(Activity activity) {
        int activityHasCode = System.identityHashCode(activity);
        qiContexts.remove(activityHasCode);

        if (qiContexts.isEmpty()) {
            services.invalidateAll();
        }
    }

    @Override
    public void onRobotReady(Session session) {
        services.retrieveAllFrom(session);
    }

    @Override
    public void onRobotLost() {
        services.invalidateAll();

        for (QiContext qiContext : qiContexts.values()) {
            qiContext.fireOnQiDisconnected("Robot is disconnected");
        }
    }

    @Override
    public void onRobotAbsent() {

    }
}
