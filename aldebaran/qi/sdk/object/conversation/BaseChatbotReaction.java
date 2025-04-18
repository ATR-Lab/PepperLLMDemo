package com.aldebaran.qi.sdk.object.conversation;


import android.support.annotation.NonNull;
import android.util.Log;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.DynamicObjectBuilder;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.Property;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.AnyObjectWrapper;

import static com.aldebaran.qi.sdk.util.FutureUtils.futureOf;

/**
 * Parent class for ChatBotReaction implementations.
 *
 * @since 3
 */

public abstract class BaseChatbotReaction extends AnyObjectWrapper implements ChatbotReaction {

    private static final String TAG = "BaseChatbotReaction";

    private static final String CHATBOT_REACTION_HANDLING_STATUS = "chatbotReactionHandlingStatus";
    private static final String ON_CHATBOT_REACTION_HANDLING_STATUS_CHANGED = "onChatbotReactionHandlingStatusChanged";

    private Property<ChatbotReactionHandlingStatus> handlingStatusProperty;

    protected BaseChatbotReaction(final QiContext context) {
        super(context);
    }

    protected final AnyObject advertise() {

        DynamicObjectBuilder builder = new DynamicObjectBuilder();
        try {
            builder.advertiseMethods(getSerializer(), ChatbotReaction.class, this);
            builder.advertiseProperty(CHATBOT_REACTION_HANDLING_STATUS, getHandlingStatusProperty());
        } catch (Exception e) {
            Log.e(TAG, "Advertise error", e);
        }
        return builder.object();
    }

    @Override
    public final ChatbotReaction.Async async() {

        return new Async() {
            @Override
            public Future<Void> runWith(final SpeechEngine speechEngine) {
                return futureOf(notUsed -> {
                    BaseChatbotReaction.this.runWith(speechEngine);
                    return Future.of(null);
                });
            }

            @Override
            public Future<Void> stop() {
                return futureOf(notUsed -> {
                    BaseChatbotReaction.this.stop();
                });
            }

            @Override
            public Future<ChatbotReactionHandlingStatus> getChatbotReactionHandlingStatus() {
                return futureOf(notUsed -> {
                    return Future.of(BaseChatbotReaction.this.getChatbotReactionHandlingStatus());
                });
            }

            @Override
            public Future<Void> setChatbotReactionHandlingStatus(final ChatbotReactionHandlingStatus updateStatus) {
                return futureOf(notUsed -> {
                    BaseChatbotReaction.this.setChatbotReactionHandlingStatus(updateStatus);
                });
            }

            @Override
            public Future<Void> setOnChatbotReactionHandlingStatusChangedListener(final OnChatbotReactionHandlingStatusChangedListener listener) {
                return futureOf(notUsed -> {
                    BaseChatbotReaction.this.setOnChatbotReactionHandlingStatusChangedListener(listener);
                });
            }

            @Override
            public Future<Void> addOnChatbotReactionHandlingStatusChangedListener(final OnChatbotReactionHandlingStatusChangedListener listener) {
                return futureOf(notUsed -> {
                    BaseChatbotReaction.this.addOnChatbotReactionHandlingStatusChangedListener(listener);
                });
            }

            @Override
            public Future<Void> removeOnChatbotReactionHandlingStatusChangedListener(final OnChatbotReactionHandlingStatusChangedListener listener) {
                return futureOf(notUsed -> {
                    BaseChatbotReaction.this.removeOnChatbotReactionHandlingStatusChangedListener(listener);
                });
            }

            @Override
            public Future<Void> removeAllOnChatbotReactionHandlingStatusChangedListeners() {
                return futureOf(notUsed -> {
                    BaseChatbotReaction.this.removeAllOnChatbotReactionHandlingStatusChangedListeners();
                });
            }
        };
    }

    @Override
    public ChatbotReactionHandlingStatus getChatbotReactionHandlingStatus() {
        return getHandlingStatusProperty().getValue().getValue();
    }

    @Override
    public final void setChatbotReactionHandlingStatus(final ChatbotReactionHandlingStatus updateStatus) {
        getHandlingStatusProperty().setValue(updateStatus).getValue();
    }

    private Property<ChatbotReactionHandlingStatus> getHandlingStatusProperty() {
        if (handlingStatusProperty == null) {
            handlingStatusProperty = new Property<>(ChatbotReactionHandlingStatus.class);
        }
        return handlingStatusProperty;
    }

    @Override
    @Deprecated
    public void setOnChatbotReactionHandlingStatusChangedListener(final OnChatbotReactionHandlingStatusChangedListener listener) {
        setListener(CHATBOT_REACTION_HANDLING_STATUS, listener, ON_CHATBOT_REACTION_HANDLING_STATUS_CHANGED);
    }

    @Override
    public void addOnChatbotReactionHandlingStatusChangedListener(@NonNull final OnChatbotReactionHandlingStatusChangedListener listener) {
        addListener(CHATBOT_REACTION_HANDLING_STATUS, listener, ON_CHATBOT_REACTION_HANDLING_STATUS_CHANGED);
    }

    @Override
    public void removeOnChatbotReactionHandlingStatusChangedListener(@NonNull final OnChatbotReactionHandlingStatusChangedListener listener) {
        removeListener(CHATBOT_REACTION_HANDLING_STATUS, listener);
    }

    @Override
    public void removeAllOnChatbotReactionHandlingStatusChangedListeners() {
        removeAllListeners(CHATBOT_REACTION_HANDLING_STATUS);
    }

    @Override
    public String toString() {
        return "BaseChatbotReaction{ " + getAnyObject() + " }";
    }
}
