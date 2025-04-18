package com.aldebaran.qi.sdk.object.conversation;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.ChatbotReaction.OnChatbotReactionHandlingStatusChangedListener;

import static com.aldebaran.qi.sdk.BuildConfig.DEBUG;

/**
 * Alternative ReplyReaction that allows the implementation of its execution by heirs instead of delegating it to a ChatbotReaction.
 */
public abstract class RunnableReplyReaction extends StandardReplyReaction
        implements WithSpeechEngineRunnable, OnChatbotReactionHandlingStatusChangedListener {

    public RunnableReplyReaction(final QiContext context, final ReplyPriority replyPriority) {
        super(context, replyPriority);

        // Use a dedicated chatbotReaction that redirects calls to this
        BaseChatbotReaction chatbotReaction = new BaseChatbotReaction(context) {

            @Override
            public void runWith(final SpeechEngine speechEngine) {
                RunnableReplyReaction.this.runWith(speechEngine);
            }

            @Override
            public void stop() {
                RunnableReplyReaction.this.stop();
            }
        };

        chatbotReaction.addOnChatbotReactionHandlingStatusChangedListener(this);
        getChatbotReactionProperty().setValue(chatbotReaction).getValue();

        if (DEBUG && !(getChatbotReaction() instanceof BaseChatbotReaction)) {
            throw new AssertionError();
        }
    }

    @Override
    public abstract void runWith(SpeechEngine speechEngine);

    @Override
    public abstract void stop();

    @Override
    public void onChatbotReactionHandlingStatusChanged(final ChatbotReactionHandlingStatus updateStatus) {
        // default, do nothing
    }

    @Override
    public String toString() {
        return "RunnableReplyReaction{ " + getAnyObject() + " }";
    }

}
