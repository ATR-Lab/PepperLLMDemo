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
 * Default implementation of AutonomousReaction.
 *
 * @since 3
 */
public class StandardAutonomousReaction extends AnyObjectWrapper implements AutonomousReaction {

    private static final String TAG = "StandardAutonomousReact";

    private static final String CHATBOT_REACTION = "chatbotReaction";
    private static final String IMPORTANCE = "importance";
    private static final String VALIDITY = "validity";

    private Property<ChatbotReaction> chatbotReactionProperty;
    private Property<AutonomousReactionImportance> importanceProperty;
    private Property<AutonomousReactionValidity> validityProperty;

    public StandardAutonomousReaction(
            final @NonNull BaseChatbotReaction chatbotReaction,
            final @NonNull AutonomousReactionImportance importance,
            final @NonNull AutonomousReactionValidity validity) {

        this(chatbotReaction.getQiContext(), importance, validity);

        getChatbotReactionProperty().setValue(chatbotReaction).getValue();

        if (DEBUG && (getImportance() == null || getValidity() == null || getChatbotReaction() == null)) {
            throw new AssertionError();
        }
    }

    // Package-only constructor, for delayed ChatbotReaction assignment
    StandardAutonomousReaction(
            final @NonNull QiContext context,
            final @NonNull AutonomousReactionImportance importance,
            final @NonNull AutonomousReactionValidity validity) {

        super(context);

        getImportanceProperty().setValue(importance).getValue();
        getValidityProperty().setValue(validity).getValue();

        if (DEBUG && (getImportance() == null || getValidity() == null)) {
            throw new AssertionError();
        }
    }

    @Override
    public Async async() {
        return new Async() {
            @Override
            public Future<ChatbotReaction> getChatbotReaction() {
                return Future.of(StandardAutonomousReaction.this.getChatbotReaction());
            }

            @Override
            public Future<AutonomousReactionImportance> getImportance() {
                return Future.of(StandardAutonomousReaction.this.getImportance());
            }

            @Override
            public Future<AutonomousReactionValidity> getValidity() {
                return Future.of(StandardAutonomousReaction.this.getValidity());
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
    public AutonomousReactionImportance getImportance() {
        return importanceProperty.getValue().getValue();
    }

    Property<AutonomousReactionImportance> getImportanceProperty() {
        if (importanceProperty == null) {
            importanceProperty = new Property<>(AutonomousReactionImportance.class);
        }
        return importanceProperty;
    }

    @Override
    public AutonomousReactionValidity getValidity() {
        return validityProperty.getValue().getValue();
    }

    private Property<AutonomousReactionValidity> getValidityProperty() {
        if (validityProperty == null) {
            validityProperty = new Property<>(AutonomousReactionValidity.class);
        }
        return validityProperty;
    }

    @Override
    protected final AnyObject advertise() {
        DynamicObjectBuilder builder = new DynamicObjectBuilder();
        try {
            builder.advertiseMethods(getQiContext().getSerializer(), AutonomousReaction.class, this);

            builder.advertiseProperty(CHATBOT_REACTION, getChatbotReactionProperty());
            builder.advertiseProperty(IMPORTANCE, getImportanceProperty());
            builder.advertiseProperty(VALIDITY, getValidityProperty());

        } catch (Exception e) {
            Log.e(TAG, "Advertise error", e);
        }
        return builder.object();
    }

    @Override
    public String toString() {
        return "StandardAutonomousReaction{ " + getAnyObject() + " }";
    }

}
