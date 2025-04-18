package com.aldebaran.qi.sdk.object.conversation;

import android.util.Log;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.DynamicObjectBuilder;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.Property;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.AnyObjectWrapper;
import com.aldebaran.qi.sdk.object.locale.Locale;

import java.util.concurrent.ExecutionException;

import static com.aldebaran.qi.sdk.util.FutureUtils.futureOf;

/**
 * Parent class for ChatBot implementations.
 *
 * @since 3
 */
public abstract class BaseChatbot extends AnyObjectWrapper implements Chatbot {

    private static final String TAG = "BaseChatbot";

    private static final String AUTONOMOUS_REACTION = "autonomousReaction";
    private static final String ON_AUTONOMOUS_REACTION_CHANGED = "onAutonomousReactionChanged";

    private static final String MAX_HYPOTHESES_PER_UTTERANCE = "maxHypothesesPerUtterance";
    private static final String ON_MAXHYPOTHESES_PER_UTTERANCE_CHANGED = "onMaxHypothesesPerUtteranceChanged";
    private static final int MAX_HYPOTHESES_PER_UTTERANCE_DEFAULT_VALUE = 2;

    private static final String IS_AVAILABLE_TO_REPLY = "isAvailableToReply";
    private static final String IS_AVAILABLE_TO_REPLY_CHANGED = "onIsAvailableToReplyChanged";
    private static final boolean IS_AVAILABLE_TO_REPLY_DEFAULT_VALUE = true;

    private Property<AutonomousReaction> autonomousReactionProperty;

    private Property<Integer> maxHypothesesPerUtteranceProperty;

    private Property<Boolean> isAvailableToReplyProperty;

    protected BaseChatbot(final QiContext context) {
        super(context);
    }

    @Override
    protected final AnyObject advertise() {
        DynamicObjectBuilder builder = new DynamicObjectBuilder();
        try {
            builder.advertiseMethods(getSerializer(), Chatbot.class, this);
            builder.advertiseProperty(AUTONOMOUS_REACTION, getAutonomousReactionProperty());
            builder.advertiseProperty(MAX_HYPOTHESES_PER_UTTERANCE, getMaxHypothesesPerUtteranceProperty());
            builder.advertiseProperty(IS_AVAILABLE_TO_REPLY, getIsAvailableToReplyProperty());
        } catch (Exception e) {
            Log.e(TAG, "Advertise error", e);
        }

        AnyObject anyObject = builder.object();
        try {
            anyObject.setProperty(MAX_HYPOTHESES_PER_UTTERANCE, MAX_HYPOTHESES_PER_UTTERANCE_DEFAULT_VALUE).get();
            anyObject.setProperty(IS_AVAILABLE_TO_REPLY, IS_AVAILABLE_TO_REPLY_DEFAULT_VALUE).get();
        } catch (ExecutionException e) {
            Log.e(TAG, "Set default value error", e);
        }

        return anyObject;
    }

    @Override
    public Async async() {

        return new Async() {
            @Override
            public Future<ReplyReaction> replyTo(final Phrase phrase, final Locale locale) {
                return futureOf(notUsed -> {
                    return Future.<ReplyReaction>of(BaseChatbot.this.replyTo(phrase, locale));
                });
            }

            @Override
            public Future<Void> acknowledgeHeard(final Phrase phrase, final Locale locale) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.acknowledgeHeard(phrase, locale);
                });
            }

            @Override
            public Future<Void> acknowledgeSaid(final Phrase phrase, final Locale locale) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.acknowledgeSaid(phrase, locale);
                });
            }

            @Override
            public Future<AutonomousReaction> getAutonomousReaction() {
                return futureOf(notUsed -> {
                    return Future.of(BaseChatbot.this.getAutonomousReaction());
                });
            }

            @Override
            @Deprecated
            public Future<Void> setOnAutonomousReactionChangedListener(final OnAutonomousReactionChangedListener listener) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.setOnAutonomousReactionChangedListener(listener);
                });
            }

            @Override
            public Future<Void> addOnAutonomousReactionChangedListener(final OnAutonomousReactionChangedListener onAutonomousReactionChangedListener) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.addOnAutonomousReactionChangedListener(onAutonomousReactionChangedListener);
                });
            }

            @Override
            public Future<Void> removeOnAutonomousReactionChangedListener(final OnAutonomousReactionChangedListener onAutonomousReactionChangedListener) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.removeOnAutonomousReactionChangedListener(onAutonomousReactionChangedListener);
                });
            }

            @Override
            public Future<Void> removeAllOnAutonomousReactionChangedListeners() {
                return futureOf(notUsed -> {
                    BaseChatbot.this.removeAllOnAutonomousReactionChangedListeners();
                });
            }

            @Override
            public Future<Integer> getMaxHypothesesPerUtterance() {
                return futureOf(notUsed -> {
                    return Future.of(BaseChatbot.this.getMaxHypothesesPerUtterance());
                });
            }

            @Override
            public Future<Void> setMaxHypothesesPerUtterance(final Integer maxHypothesesPerUtterance) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.setMaxHypothesesPerUtterance(maxHypothesesPerUtterance);
                });
            }

            @Override
            @Deprecated
            public Future<Void> setOnMaxHypothesesPerUtteranceChangedListener(final OnMaxHypothesesPerUtteranceChangedListener onMaxHypothesesPerUtteranceChangedListener) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.setOnMaxHypothesesPerUtteranceChangedListener(onMaxHypothesesPerUtteranceChangedListener);
                });
            }

            @Override
            public Future<Void> addOnMaxHypothesesPerUtteranceChangedListener(final OnMaxHypothesesPerUtteranceChangedListener onMaxHypothesesPerUtteranceChangedListener) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.addOnMaxHypothesesPerUtteranceChangedListener(onMaxHypothesesPerUtteranceChangedListener);
                });
            }

            @Override
            public Future<Void> removeOnMaxHypothesesPerUtteranceChangedListener(final OnMaxHypothesesPerUtteranceChangedListener onMaxHypothesesPerUtteranceChangedListener) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.removeOnMaxHypothesesPerUtteranceChangedListener(onMaxHypothesesPerUtteranceChangedListener);
                });
            }

            @Override
            public Future<Void> removeAllOnMaxHypothesesPerUtteranceChangedListeners() {
                return futureOf(notUsed -> {
                    BaseChatbot.this.removeAllOnMaxHypothesesPerUtteranceChangedListeners();
                });
            }

            @Override
            public Future<Boolean> getIsAvailableToReply() {
                return futureOf(notUsed -> {
                    return Future.of(BaseChatbot.this.getIsAvailableToReply());
                });
            }

            protected Future<Void> setIsAvailableToReply(final Boolean isAvailableToReply) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.setIsAvailableToReply(isAvailableToReply);
                });
            }


            @Override
            public Future<Void> addOnIsAvailableToReplyChangedListener(final OnIsAvailableToReplyChangedListener onIsAvailableToReplyChangedListener) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.addOnIsAvailableToReplyChangedListener(onIsAvailableToReplyChangedListener);
                });
            }

            @Override
            public Future<Void> removeOnIsAvailableToReplyChangedListener(final OnIsAvailableToReplyChangedListener onIsAvailableToReplyChangedListener) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.removeOnIsAvailableToReplyChangedListener(onIsAvailableToReplyChangedListener);
                });
            }

            @Override
            @Deprecated
            public Future<Void> setOnIsAvailableToReplyChangedListener(final OnIsAvailableToReplyChangedListener onIsAvailableToReplyChangedListener) {
                return futureOf(notUsed -> {
                    BaseChatbot.this.setOnIsAvailableToReplyChangedListener(onIsAvailableToReplyChangedListener);
                });
            }

            @Override
            public Future<Void> removeAllOnIsAvailableToReplyChangedListeners() {
                return futureOf(notUsed -> {
                    BaseChatbot.this.removeAllOnIsAvailableToReplyChangedListeners();
                });
            }

        };
    }

    @Override
    public abstract StandardReplyReaction replyTo(Phrase phrase, Locale locale);

    @Override
    public void acknowledgeHeard(final Phrase phrase, final Locale locale) {
        // default: do nothing
    }

    @Override
    public void acknowledgeSaid(final Phrase phrase, final Locale locale) {
        // default: do nothing
    }

    @Override
    public final AutonomousReaction getAutonomousReaction() {
        return getAutonomousReactionProperty().getValue().getValue();
    }

    /**
     * Setter for AutonomousReaction.
     *
     * @param autonomousReaction an autonomous suggestion from this chatbot.
     */
    protected void setAutonomousReaction(final StandardAutonomousReaction autonomousReaction) {
        getAutonomousReactionProperty().setValue(autonomousReaction).getValue();
    }

    Property<AutonomousReaction> getAutonomousReactionProperty() {
        if (autonomousReactionProperty == null) {
            autonomousReactionProperty = new Property<>(AutonomousReaction.class);
        }
        return autonomousReactionProperty;
    }

    Property<Integer> getMaxHypothesesPerUtteranceProperty() {
        if (maxHypothesesPerUtteranceProperty == null) {
            maxHypothesesPerUtteranceProperty = new Property<>(Integer.class);
        }
        return maxHypothesesPerUtteranceProperty;
    }

    Property<Boolean> getIsAvailableToReplyProperty() {
        if (isAvailableToReplyProperty == null) {
            isAvailableToReplyProperty = new Property<>(Boolean.class);
        }
        return isAvailableToReplyProperty;
    }

    @Override
    public void setOnAutonomousReactionChangedListener(final OnAutonomousReactionChangedListener listener) {
        setListener(AUTONOMOUS_REACTION, listener, ON_AUTONOMOUS_REACTION_CHANGED);
    }

    @Override
    public void addOnAutonomousReactionChangedListener(final OnAutonomousReactionChangedListener listener) {
        addListener(AUTONOMOUS_REACTION, listener, ON_AUTONOMOUS_REACTION_CHANGED);
    }

    @Override
    public void removeOnAutonomousReactionChangedListener(final OnAutonomousReactionChangedListener listener) {
        removeListener(AUTONOMOUS_REACTION, listener);
    }

    @Override
    public void removeAllOnAutonomousReactionChangedListeners() {
        removeAllListeners(AUTONOMOUS_REACTION);
    }

    @Override
    public Integer getMaxHypothesesPerUtterance() {
        return getMaxHypothesesPerUtteranceProperty().getValue().getValue();
    }

    @Override
    public void setMaxHypothesesPerUtterance(Integer maxHypothesesPerUtterance) {
        getMaxHypothesesPerUtteranceProperty().setValue(maxHypothesesPerUtterance).getValue();
    }

    @Override
    public void setOnMaxHypothesesPerUtteranceChangedListener(OnMaxHypothesesPerUtteranceChangedListener listener) {
        setListener(MAX_HYPOTHESES_PER_UTTERANCE, listener, ON_MAXHYPOTHESES_PER_UTTERANCE_CHANGED);
    }

    @Override
    public void addOnMaxHypothesesPerUtteranceChangedListener(OnMaxHypothesesPerUtteranceChangedListener listener) {
        addListener(MAX_HYPOTHESES_PER_UTTERANCE, listener, ON_MAXHYPOTHESES_PER_UTTERANCE_CHANGED);
    }

    @Override
    public void removeOnMaxHypothesesPerUtteranceChangedListener(OnMaxHypothesesPerUtteranceChangedListener listener) {
        removeListener(MAX_HYPOTHESES_PER_UTTERANCE, listener);
    }

    @Override
    public void removeAllOnMaxHypothesesPerUtteranceChangedListeners() {
        removeAllListeners(MAX_HYPOTHESES_PER_UTTERANCE);
    }

    @Override
    public Boolean getIsAvailableToReply() {
        return getIsAvailableToReplyProperty().getValue().getValue();
    }

    protected void setIsAvailableToReply(Boolean isAvailableToReply) {
        getIsAvailableToReplyProperty().setValue(isAvailableToReply).getValue();
    }

    @Override
    @Deprecated
    public void setOnIsAvailableToReplyChangedListener(OnIsAvailableToReplyChangedListener listener) {
        setListener(IS_AVAILABLE_TO_REPLY, listener, IS_AVAILABLE_TO_REPLY_CHANGED);
    }

    @Override
    public void addOnIsAvailableToReplyChangedListener(OnIsAvailableToReplyChangedListener listener) {
        addListener(IS_AVAILABLE_TO_REPLY, listener, IS_AVAILABLE_TO_REPLY_CHANGED);
    }

    @Override
    public void removeOnIsAvailableToReplyChangedListener(OnIsAvailableToReplyChangedListener listener) {
        removeListener(IS_AVAILABLE_TO_REPLY, listener);
    }

    @Override
    public void removeAllOnIsAvailableToReplyChangedListeners() {
        removeAllListeners(IS_AVAILABLE_TO_REPLY);
    }

    @Override
    public String toString() {
        return "BaseChatbot{ " + getAnyObject() + " }";
    }

}
