package com.aldebaran.qi.sdk;

import android.app.Activity;
import android.content.ContextWrapper;
import android.util.Log;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.QiConversionException;
import com.aldebaran.qi.sdk.core.FocusManager;
import com.aldebaran.qi.sdk.core.QiThreadPool;
import com.aldebaran.qi.sdk.core.SessionManager;
import com.aldebaran.qi.sdk.exceptions.ServiceUnavailableException;
import com.aldebaran.qi.sdk.object.actuation.Actuation;
import com.aldebaran.qi.sdk.object.actuation.Mapping;
import com.aldebaran.qi.sdk.object.autonomousabilities.AutonomousAbilities;
import com.aldebaran.qi.sdk.object.camera.Camera;
import com.aldebaran.qi.sdk.object.context.RobotContext;
import com.aldebaran.qi.sdk.object.context.RobotContextFactory;
import com.aldebaran.qi.sdk.object.conversation.Conversation;
import com.aldebaran.qi.sdk.object.focus.Focus;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;
import com.aldebaran.qi.sdk.object.knowledge.Knowledge;
import com.aldebaran.qi.sdk.object.power.Power;
import com.aldebaran.qi.sdk.object.touch.Touch;
import com.aldebaran.qi.serialization.QiSerializer;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QiContext extends ContextWrapper implements FocusManager.Callback {

    private static final String TAG = "QiContext";

    private final QiSerializer serializer;
    private final QiServices services;
    private final FocusManager focusManager;
    private final CopyOnWriteArrayList<WeakReference<RobotLifecycleCallbacks>> robotLifecycleCallbacksList;

    private QiDisconnectionListener disconnectionListener;
    private RobotContext robotContext;
    private AtomicBoolean hasFocus = new AtomicBoolean(false);

    QiContext(Activity activity, QiSerializer serializer, QiServices services, SessionManager sessionManager) {
        super(activity);

        this.serializer = serializer;
        this.services = services;
        this.focusManager = new FocusManager(activity, sessionManager);
        this.robotLifecycleCallbacksList = new CopyOnWriteArrayList<>();
    }

    public void setDisconnectionListener(QiDisconnectionListener disconnectionListener) {
        this.disconnectionListener = disconnectionListener;
    }

    void fireOnQiDisconnected(String reason) {
        robotContext = null;
        hasFocus.set(false);

        if (disconnectionListener != null) {
            disconnectionListener.onQiDisconnected(reason);
        }
    }

    void addCallback(final RobotLifecycleCallbacks callbacks) {
        if (callbacks == null) {
            return;
        }

        if (!contains(robotLifecycleCallbacksList, callbacks)) {
            robotLifecycleCallbacksList.add(new WeakReference<>(callbacks));
        }

        RobotContext currentRobotContext = robotContext;
        if (hasFocus.get() && currentRobotContext != null) {

            QiThreadPool.run(() -> callbacks.onRobotFocusGained(QiContext.this));
        }
    }

    void removeCallback(RobotLifecycleCallbacks callbacks) {
        remove(robotLifecycleCallbacksList, callbacks);
    }

    void removeAllCallback() {
        robotLifecycleCallbacksList.clear();
    }

    /**
     * Return the robot "Conversation" service.
     *
     * @return the robot "Conversation" service
     * @throws ServiceUnavailableException if service is unavailable
     */
    public Conversation getConversation() {
        try {
            return services.getConversation().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Conversation");
        }
    }

    /**
     * Return the robot "Conversation" service.
     *
     * @return future of the robot "Conversation" service
     */
    public Future<Conversation> getConversationAsync() {
        return services.getConversation();
    }

    /**
     * Return the robot "Actuation" service.
     *
     * @return the robot "Actuation" service
     * @throws ServiceUnavailableException if service is unavailable
     */
    public Actuation getActuation() {
        try {
            return services.getActuation().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Actuation");
        }
    }

    /**
     * Return the robot "Actuation" service.
     *
     * @return future of the robot "Actuation" service
     */
    public Future<Actuation> getActuationAsync() {
        return services.getActuation();
    }

    /**
     * Return the robot "Focus" service.
     *
     * @return the robot "Focus" service.
     * @throws ServiceUnavailableException if service is unavailable
     */
    public Focus getFocus() {
        try {
            return services.getFocus().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Focus");
        }
    }

    /**
     * Return the robot "Focus" service.
     *
     * @return future of the robot "Focus" service
     */
    public Future<Focus> getFocusAsync() {
        return services.getFocus();
    }

    /**
     * Return the robot "Mapping" service.
     *
     * @return the robot "Mapping" service.
     * @throws ServiceUnavailableException if service is unavailable
     */
    public Mapping getMapping() {
        try {
            return services.getMapping().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Mapping");
        }
    }

    /**
     * Return the robot "Mapping" service.
     *
     * @return future of the robot "Mapping" service
     */
    public Future<Mapping> getMappingAsync() {
        return services.getMapping();
    }

    /**
     * Return the robot "Touch" service.
     *
     * @return the robot "Touch" service.
     * @throws ServiceUnavailableException if service is unavailable
     */
    public Touch getTouch() {
        try {
            return services.getTouch().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Touch");
        }
    }

    /**
     * Return the robot "Touch" service.
     *
     * @return future of the robot "Touch" service
     */
    public Future<Touch> getTouchAsync() {
        return services.getTouch();
    }

    /**
     * Return the robot "Knowledge" service.
     *
     * @return the robot "Knowledge" service.
     * @throws ServiceUnavailableException if service is unavailable
     */
    public Knowledge getKnowledge() {
        try {
            return services.getKnowledge().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Knowledge");
        }
    }

    /**
     * Return the robot "Knowledge" service.
     *
     * @return future of the robot "Knowledge" service
     */
    public Future<Knowledge> getKnowledgeAsync() {
        return services.getKnowledge();
    }

    /**
     * Return the robot "ContextFactory" service.
     *
     * @return the robot "ContextFactory" service.
     * @throws ServiceUnavailableException if service is unavailable
     */
    public RobotContextFactory getContextFactory() {
        try {
            return services.getContextFactory().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("ContextFactory");
        }
    }

    /**
     * Return the robot "ContextFactory" service.
     *
     * @return future of the robot "ContextFactory" service
     */
    public Future<RobotContextFactory> getContextFactoryAsync() {
        return services.getContextFactory();
    }

    /**
     * Return the robot "HumanAwareness" service.
     *
     * @return the robot "HumanAwareness" service.
     * @throws ServiceUnavailableException if service is unavailable
     */
    public HumanAwareness getHumanAwareness() {
        try {
            return services.getHumanAwareness().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("HumanAwareness");
        }
    }

    /**
     * Return the robot "HumanAwareness" service.
     *
     * @return future of the robot "HumanAwareness" service
     */
    public Future<HumanAwareness> getHumanAwarenessAsync() {
        return services.getHumanAwareness();
    }

    /**
     * Return the robot "AutonomousAbilities" service.
     *
     * @return the robot "AutonomousAbilities" service.
     * @throws ServiceUnavailableException if service is unavailable
     */
    public AutonomousAbilities getAutonomousAbilities() {
        try {
            return services.getAutonomousAbilities().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("AutonomousAbilities");
        }
    }

    /**
     * Return the robot "AutonomousAbilities" service.
     *
     * @return future of the robot "AutonomousAbilities" service
     */
    public Future<AutonomousAbilities> getAutonomousAbilitiesAsync() {
        return services.getAutonomousAbilities();
    }

    /**
     * Return the robot "Camera" service.
     *
     * @return the robot "Camera" service.
     * @throws ServiceUnavailableException if service is unavailable
     */
    public Camera getCamera() {
        try {
            return services.getCamera().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Camera");
        }
    }

    /**
     * Return the robot "Camera" service.
     *
     * @return future of the robot "Camera" service
     */
    public Future<Camera> getCameraAsync() {
        return services.getCamera();
    }

    /**
     * Return the robot "Power" service.
     *
     * @return the robot "Power" service.
     * @throws ServiceUnavailableException if service is unavailable
     */
    public Power getPower() {
        try {
            return services.getPower().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new ServiceUnavailableException("Power");
        }
    }

    /**
     * Return the robot "Power" service.
     *
     * @return future of the robot "Power" service
     */
    public Future<Power> getPowerAsync() {
        return services.getPower();
    }

    private int indexOf(List<WeakReference<RobotLifecycleCallbacks>> list, RobotLifecycleCallbacks robotLifecycleCallbacks) {
        for (int i = 0; i < list.size(); i++) {
            WeakReference<RobotLifecycleCallbacks> robotLifecycleCallbacksWeakReference = list.get(i);

            RobotLifecycleCallbacks callbacks = robotLifecycleCallbacksWeakReference.get();

            if (callbacks != null && callbacks.equals(robotLifecycleCallbacks)) {
                return i;
            }
        }
        return -1;
    }

    private boolean contains(List<WeakReference<RobotLifecycleCallbacks>> list, RobotLifecycleCallbacks robotLifecycleCallbacks) {
        return indexOf(list, robotLifecycleCallbacks) >= 0;
    }

    private void remove(List<WeakReference<RobotLifecycleCallbacks>> list, RobotLifecycleCallbacks robotLifecycleCallbacks) {
        int index = indexOf(list, robotLifecycleCallbacks);

        if (index >= 0) {
            list.remove(index);
        }
    }

    public void register(RobotLifecycleCallbacks callbacks) {
        boolean isFirstCallback = robotLifecycleCallbacksList.isEmpty();

        addCallback(callbacks);

        if (isFirstCallback) {
            focusManager.register(this);
        }
    }

    public void unregister(RobotLifecycleCallbacks callbacks) {
        removeCallback(callbacks);

        boolean isLastCallback = robotLifecycleCallbacksList.isEmpty();
        if (isLastCallback) {
            focusManager.unregister();
        }
    }

    public void unregister() {
        removeAllCallback();

        focusManager.unregister();
    }

    public boolean hasCallbacks() {
        return !robotLifecycleCallbacksList.isEmpty();
    }

    public <T> T convert(Object o, Type type) {
        try {
            return (T) serializer.deserialize(o, type);
        } catch (Exception e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    @Override
    public void onFocusGained(AnyObject robotContext) {
        this.robotContext = deserializeRobotContext(robotContext);
        this.hasFocus.set(true);

        try {
            for (WeakReference<RobotLifecycleCallbacks> weakRobotLifecycleCallbacks : robotLifecycleCallbacksList) {
                RobotLifecycleCallbacks robotLifecycleCallbacks = weakRobotLifecycleCallbacks.get();
                if (robotLifecycleCallbacks != null) {
                    robotLifecycleCallbacks.onRobotFocusGained(QiContext.this);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onFocusGained: Error", e);
        }
    }

    private RobotContext deserializeRobotContext(AnyObject robotContext) {
        try {
            Object deserialize = serializer.deserialize(robotContext, RobotContext.class);
            return (RobotContext) deserialize;
        } catch (QiConversionException e) {
            Log.e("QiContext", "Error when deserialize robot context");
            return null;
        }
    }

    @Override
    public void onFocusLost() {
        this.hasFocus.set(false);

        final List<WeakReference<RobotLifecycleCallbacks>> local;

        local = new ArrayList<>(robotLifecycleCallbacksList);

        try {
            for (WeakReference<RobotLifecycleCallbacks> weakRobotLifecycleCallbacks : local) {
                RobotLifecycleCallbacks robotLifecycleCallbacks = weakRobotLifecycleCallbacks.get();

                if (robotLifecycleCallbacks != null) {
                    robotLifecycleCallbacks.onRobotFocusLost();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onFocusLost: Error", e);
        }
    }

    @Override
    public void onFocusRefused(String reason) {
        try {
            for (WeakReference<RobotLifecycleCallbacks> weakRobotLifecycleCallbacks : robotLifecycleCallbacksList) {
                RobotLifecycleCallbacks robotLifecycleCallbacks = weakRobotLifecycleCallbacks.get();
                if (robotLifecycleCallbacks != null) {
                    robotLifecycleCallbacks.onRobotFocusRefused(reason);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onFocusRefused: Error", e);
        }
    }

    public RobotContext getRobotContext() {
        return robotContext;
    }

    public QiSerializer getSerializer() {
        return serializer;
    }

}
