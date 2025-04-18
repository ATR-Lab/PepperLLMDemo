package com.aldebaran.qi.sdk.object.conversation;


import android.util.Log;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.DynamicObjectBuilder;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.Property;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.AnyObjectWrapper;
import com.aldebaran.qi.sdk.util.FutureUtils;

import java.util.List;

/**
 * Parent class for QiChatExecutor implementations.
 *
 * @since 3
 */
public abstract class BaseQiChatExecutor extends AnyObjectWrapper implements QiChatExecutor {

    private static final String AUTONOMOUS_SUGGESTION = "autonomousReaction";
    private static final String TAG = "BaseQiChatExecutor";
    private Property<AnyObject> anyObjectProperty;

    protected BaseQiChatExecutor(final QiContext context) {
        super(context);
    }

    @Override
    protected AnyObject advertise() {
        DynamicObjectBuilder builder = new DynamicObjectBuilder();
        try {
            builder.advertiseMethods(getSerializer(), QiChatExecutor.class, this);
            builder.advertiseProperty(AUTONOMOUS_SUGGESTION, getAutonomousReactionProperty());
        } catch (Exception e) {
            Log.e(TAG, "Advertise error", e);
        }
        return builder.object();
    }

    private Property<AnyObject> getAutonomousReactionProperty() {
        if (anyObjectProperty == null) {
            anyObjectProperty = new Property<>(AnyObject.class);
        }
        return anyObjectProperty;
    }

    @Override
    public Async async() {
        return new Async() {

            @Override
            public Future<Void> runWith(final List<String> params) {
                return FutureUtils.futureOf(voidFuture -> {
                    BaseQiChatExecutor.this.runWith(params);
                });
            }

            @Override
            public Future<Void> stop() {
                return FutureUtils.futureOf(voidFuture -> {
                    BaseQiChatExecutor.this.stop();
                });
            }

        };
    }

    @Override
    public abstract void runWith(List<String> params);

    @Override
    public abstract void stop();

    @Override
    public String toString() {
        return "BaseQiChatExecutor{ " + getAnyObject() + " }";
    }

}
