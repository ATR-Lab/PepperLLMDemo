package com.aldebaran.qi.sdk;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import com.aldebaran.qi.sdk.object.AnyObjectWrapperConverter;
import com.aldebaran.qi.sdk.object.actuation.ActuationConverter;
import com.aldebaran.qi.sdk.object.autonomousabilities.AutonomousabilitiesConverter;
import com.aldebaran.qi.sdk.object.camera.CameraConverter;
import com.aldebaran.qi.sdk.object.context.ContextConverter;
import com.aldebaran.qi.sdk.object.conversation.ConversationConverter;
import com.aldebaran.qi.sdk.object.focus.FocusConverter;
import com.aldebaran.qi.sdk.object.human.HumanConverter;
import com.aldebaran.qi.sdk.object.humanawareness.HumanawarenessConverter;
import com.aldebaran.qi.sdk.object.image.ImageConverter;
import com.aldebaran.qi.sdk.object.knowledge.KnowledgeConverter;
import com.aldebaran.qi.sdk.object.power.PowerConverter;
import com.aldebaran.qi.sdk.object.streamablebuffer.StreamablebufferConverter;
import com.aldebaran.qi.sdk.object.touch.TouchConverter;
import com.aldebaran.qi.sdk.serialization.EnumConverter;
import com.aldebaran.qi.serialization.QiSerializer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper to initialize Qi SDK.
 */
public class QiSDK {

    private static AtomicBoolean alreadyInitialized = new AtomicBoolean(false);
    private static QiRobot qiRobot;

    // The QiSDK API level.
    public static final String VERSION = BuildConfig.QISDK_VERSION;

    public static void init(Application application) {
        if (!alreadyInitialized.get()) {
            alreadyInitialized.set(true);
            qiRobot = new QiRobot(application, QiSerializerHolder.SERIALIZER);
        }
    }

    /**
     * Add a new RobotLifecycleCallbacks for default QiRobot
     *
     * @param activity  the activity
     * @param callbacks the callbacks
     */
    public static void register(Activity activity, RobotLifecycleCallbacks callbacks) {
        init(activity.getApplication());

        if (!checkSdkInitialized()) {
            return;
        }

        QiContext qiContext = qiRobot.retrieveQiContext(activity);
        qiContext.register(callbacks);
    }

    /**
     * Remove a RobotLifecycleCallbacks for default QiRobot
     *
     * @param activity  the activity
     * @param callbacks the callbacks
     */
    public static void unregister(Activity activity, RobotLifecycleCallbacks callbacks) {
        if (!checkSdkInitialized()) {
            return;
        }

        QiContext qiContext = qiRobot.retrieveQiContext(activity);
        qiContext.unregister(callbacks);


        if (!qiContext.hasCallbacks()) {
            qiRobot.removeQiContext(activity);
        }
    }

    /**
     * Unregister all RobotLifecycleCallbacks for default QiRobot
     *
     * @param activity the activity
     */
    public static void unregister(Activity activity) {
        if (!checkSdkInitialized()) {
            return;
        }

        QiContext qiContext = qiRobot.retrieveQiContext(activity);
        qiContext.unregister();

        qiRobot.removeQiContext(activity);
    }

    private static boolean checkSdkInitialized() {
        if (qiRobot == null) {
            Log.w("QiSDK", "QiSDK not initialized. Call QiSDK.initIfNeeded(application) method.");
            return false;
        }
        return true;
    }

    public static QiSerializer getSerializer() {
        return QiSerializerHolder.SERIALIZER;
    }

    // Used for thread-safe lazy instantiation of the serializer.
    private static class QiSerializerHolder {
        private static final QiSerializer SERIALIZER = setupDefaultSerializer();

        private static QiSerializer setupDefaultSerializer() {
            QiSerializer qiSerializer = QiSerializer.getDefault(); // TODO Needed for Chat API, to be improved.

            qiSerializer.addConverter(new EnumConverter());
            qiSerializer.addConverter(new AnyObjectWrapperConverter());
            qiSerializer.addConverter(new ContextConverter());
            qiSerializer.addConverter(new FocusConverter());

            qiSerializer.addConverter(new ActuationConverter());
            qiSerializer.addConverter(new AutonomousabilitiesConverter());
            qiSerializer.addConverter(new ConversationConverter());
            qiSerializer.addConverter(new HumanConverter());
            qiSerializer.addConverter(new TouchConverter());
            qiSerializer.addConverter(new KnowledgeConverter());
            qiSerializer.addConverter(new HumanawarenessConverter());
            qiSerializer.addConverter(new CameraConverter());
            qiSerializer.addConverter(new ImageConverter());
            qiSerializer.addConverter(new PowerConverter());
            qiSerializer.addConverter(new StreamablebufferConverter());

            return qiSerializer;
        }
    }
}
