package com.aldebaran.qi.sdk;

import android.util.Log;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.autonomousabilities.AutonomousAbilities;
import com.aldebaran.qi.sdk.object.camera.Camera;
import com.aldebaran.qi.sdk.object.context.RobotContextFactory;
import com.aldebaran.qi.sdk.object.conversation.Conversation;
import com.aldebaran.qi.sdk.object.focus.Focus;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.knowledge.Knowledge;
import com.aldebaran.qi.sdk.object.power.Power;
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.sdk.services.Requirement;
import com.aldebaran.qi.sdk.services.ServiceRequirement;
import com.aldebaran.qi.sdk.services.SessionRequirement;
import com.aldebaran.qi.serialization.QiSerializer;

/**
 * HandleHolder for robot services.
 * <p>
 * In general, users don't need to manipulate it directly.
 * Instead, they can directly retrieve a service from an Android context ({@code this}). For instance:
 */
final class QiServices {
    private static final String TAG = "QiServices";

    private SessionRequirement sessionRequirement = new SessionRequirement();
    private Requirement<Actuation> actuation;
    private Requirement<AutonomousAbilities> autonomousAbilities;
    private Requirement<Focus> focus;
    private Requirement<Conversation> conversation;
    private Requirement<Camera> camera;
    private Requirement<Mapping> mapping;
    private Requirement<RobotContextFactory> contextFactory;
    private Requirement<Touch> touch;
    private Requirement<Knowledge> knowledge;
    private Requirement<HumanAwareness> humanAwareness;
    private Requirement<Power> power;

    QiServices(QiSerializer serializer) {
        this.actuation = new ServiceRequirement<>(sessionRequirement, serializer, "Actuation", Actuation.class);
        this.autonomousAbilities = new ServiceRequirement<>(sessionRequirement, serializer, "AutonomousAbilities", AutonomousAbilities.class);
        this.focus = new ServiceRequirement<>(sessionRequirement, serializer, "Focus", Focus.class);
        this.conversation = new ServiceRequirement<>(sessionRequirement, serializer, "Conversation", Conversation.class);
        this.camera = new ServiceRequirement<>(sessionRequirement, serializer, "Camera", Camera.class);
        this.mapping = new ServiceRequirement<>(sessionRequirement, serializer, "Mapping", Mapping.class);
        this.contextFactory = new ServiceRequirement<>(sessionRequirement, serializer, "ContextFactory", RobotContextFactory.class);
        this.touch = new ServiceRequirement<>(sessionRequirement, serializer, "Touch", Touch.class);
        this.knowledge = new ServiceRequirement<>(sessionRequirement, serializer, "Knowledge", Knowledge.class);
        this.humanAwareness = new ServiceRequirement<>(sessionRequirement, serializer, "HumanAwareness", HumanAwareness.class);
        this.power = new ServiceRequirement<>(sessionRequirement, serializer, "Power", Power.class);
    }

    public Future<Conversation> getConversation() {
        return conversation.satisfy();
    }

    public Future<Actuation> getActuation() {
        return actuation.satisfy();
    }

    public Future<Focus> getFocus() {
        return focus.satisfy();
    }

    public Future<Mapping> getMapping() {
        return mapping.satisfy();
    }

    public Future<Touch> getTouch() {
        return touch.satisfy();
    }

    public Future<Knowledge> getKnowledge() {
        return knowledge.satisfy();
    }

    public Future<RobotContextFactory> getContextFactory() {
        return contextFactory.satisfy();
    }

    public Future<AutonomousAbilities> getAutonomousAbilities() {
        return autonomousAbilities.satisfy();
    }

    public Future<HumanAwareness> getHumanAwareness() {
        return humanAwareness.satisfy();
    }

    public Future<Camera> getCamera() {
        return camera.satisfy();
    }

    public Future<Power> getPower() {
        return power.satisfy();
    }

    void retrieveAllFrom(Session session) {
        if (session == null || !session.isConnected()) {
            Log.w(TAG, "Session is not connected to retrieve services");
            return;
        }

        sessionRequirement.setSession(session);
        sessionRequirement.satisfy();

        actuation.satisfy();
        autonomousAbilities.satisfy();
        focus.satisfy();
        conversation.satisfy();
        mapping.satisfy();
        contextFactory.satisfy();
        touch.satisfy();
        humanAwareness.satisfy();
        knowledge.satisfy();
        camera.satisfy();
        power.satisfy();
    }

    void invalidateAll() {
        sessionRequirement.invalidate();

        actuation.invalidate();
        autonomousAbilities.invalidate();
        focus.invalidate();
        conversation.invalidate();
        mapping.invalidate();
        contextFactory.invalidate();
        touch.invalidate();
        humanAwareness.invalidate();
        knowledge.invalidate();
        camera.invalidate();
        power.invalidate();
    }

}
