package com.aldebaran.qi.sdk.object.conversation;

import android.support.annotation.NonNull;
import android.util.Log;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.DynamicObjectBuilder;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.Property;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.AnyObjectWrapper;

import static com.aldebaran.qi.sdk.BuildConfig.DEBUG;

/**
 * Default implementation of ReplyReaction.
 *
 * @since 3
 */
public class StandardReplyReaction extends AnyObjectWrapper implements ReplyReaction {

    private static final String TAG = "StandardReplyReaction";

    private static final String CHATBOT_REACTION = "chatbotReaction";
    private static final String REPLY_PRIORITY = "replyPriority";

    private Property<ChatbotReaction> chatbotReactionProperty;
    private Property<ReplyPriority> replyPriorityProperty;

    public StandardReplyReaction(final @NonNull BaseChatbotReaction chatbotReaction,
                                 final @NonNull ReplyPriority replyPriority) {
        this(chatbotReaction.getQiContext(), replyPriority);

        getChatbotReactionProperty().setValue(chatbotReaction).getValue();

        if (DEBUG && (getReplyPriority() == null || getChatbotReaction() == null)) {
            throw new AssertionError();
        }

    }

    // Package-only constructor, for delayed ChatbotReaction assignment
    StandardReplyReaction(final @NonNull QiContext context, final @NonNull ReplyPriority replyPriority) {
        super(context);

        getReplyPriorityProperty().setValue(replyPriority).getValue();

        if (DEBUG && getReplyPriority() == null) {
            throw new AssertionError();
        }

    }

    @Override
    public Async async() {
        return new Async() {
            @Override
            public Future<ChatbotReaction> getChatbotReaction() {
                return Future.of(StandardReplyReaction.this.getChatbotReaction());
            }

            @Override
            public Future<ReplyPriority> getReplyPriority() {
                return Future.of(StandardReplyReaction.this.getReplyPriority());
            }
        };
    }

    @Override
    public ChatbotReaction getChatbotReaction() {
        return getChatbotReactionProperty().getValue().getValue();
    }

    Property<ChatbotReaction> getChatbotReactionProperty() {
        if (chatbotReactionProperty == null) {
            chatbotReactionProperty = new Property<>(ChatbotReaction.class);
        }
        return chatbotReactionProperty;
    }

    @Override
    public ReplyPriority getReplyPriority() {
        return getReplyPriorityProperty().getValue().getValue();
    }

    Property<ReplyPriority> getReplyPriorityProperty() {
        if (replyPriorityProperty == null) {
            replyPriorityProperty = new Property<>(ReplyPriority.class);
        }
        return replyPriorityProperty;
    }

    @Override
    protected AnyObject advertise() {

        DynamicObjectBuilder builder = new DynamicObjectBuilder();

        try {
            builder.advertiseMethods(getQiContext().getSerializer(), ReplyReaction.class, this);

            builder.advertiseProperty(CHATBOT_REACTION, getChatbotReactionProperty());
            builder.advertiseProperty(REPLY_PRIORITY, getReplyPriorityProperty());

        } catch (Exception e) {
            Log.e(TAG, "Advertise error", e);
        }

        return builder.object();
    }

    @Override
    public String toString() {
        return "StandardReplyReaction{ " + getAnyObject() + " }";
    }

}
