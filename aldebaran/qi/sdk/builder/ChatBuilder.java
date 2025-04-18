package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.Optional;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.ChatOptions;
import com.aldebaran.qi.sdk.object.conversation.Chatbot;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.util.FutureUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Build a new {@link Chat}
 */
public class ChatBuilder {
    private final QiContext context;
    private List<Chatbot> chatbots;
    private Locale locale;
    private SpeechEngine speechEngine;
    private Map<String, String> aSRParameters;
    private ChatOptions chatOptions = new ChatOptions();

    private ChatBuilder(QiContext context) {
        this.context = context;
        this.chatbots = new ArrayList<>();
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static ChatBuilder with(QiContext context) {
        return new ChatBuilder(context);
    }

    /**
     * Configure the locale used by Chat
     *
     * @param locale the locale
     * @return the builder
     */
    public ChatBuilder withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Configure the speechEngine used by Chat
     *
     * @param speechEngine the speechEngine
     * @return the builder
     */
    public ChatBuilder withSpeechEngine(SpeechEngine speechEngine) {
        this.speechEngine = speechEngine;
        return this;
    }

    /**
     * Configure the chatbots sets used by Chat
     *
     * @param chatbots the phrase sets list
     * @return the builder
     */
    public ChatBuilder withChatbot(Chatbot... chatbots) {
        if (chatbots != null) {
            this.chatbots.addAll(Arrays.asList(chatbots));
        }
        return this;
    }

    /**
     * Configure the chatbots sets used by Chat
     *
     * @param chatbots the phrase sets list
     * @return the builder
     */
    public ChatBuilder withChatbots(List<? extends Chatbot> chatbots) {
        if (chatbots != null) {
            this.chatbots.addAll(chatbots);
        }
        return this;
    }

    /**
     * Configure the AsrDriverParameters used by Chat
     *
     * @param aSRParameters the AsrDriverParameters
     * @return the builder
     */
    public ChatBuilder withAsrDriverParameters(Map<String, String> aSRParameters) {
        this.aSRParameters = aSRParameters;
        return this;
    }

    /**
     * Return a configured instance of Chat
     *
     * @return the Chat
     */
    public Chat build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of Chat
     *
     * @return the Chat
     */
    public Future<Chat> buildAsync() {
        if (aSRParameters == null) {
            if (speechEngine == null) {
                if (locale == null) {
                    return context.getConversation()
                            .async()
                            .makeChat(context.getRobotContext(), chatbots);
                } else {
                    return context.getConversation()
                            .async()
                            .makeChat(context.getRobotContext(), chatbots, locale);
                }
            } else {
                if (locale == null) {
                    return context.getConversation()
                            .async()
                            .makeChat(context.getRobotContext(), chatbots, speechEngine);
                } else {
                    return context.getConversation()
                            .async()
                            .makeChat(context.getRobotContext(), chatbots, speechEngine, locale);
                }
            }
        } else {
            chatOptions.setASRParameters(Optional.of(aSRParameters));

            if (speechEngine != null) {
                chatOptions.setSpeechEngine(Optional.of(speechEngine));
            } else {
                chatOptions.setSpeechEngine(Optional.<SpeechEngine>empty());
            }

            if (locale != null) {
                chatOptions.setLocale(Optional.of(locale));
            } else {
                chatOptions.setLocale(Optional.<Locale>empty());
            }

            return context.getConversationAsync()
                    .andThenCompose(service -> service.async().makeChat(context.getRobotContext(), chatbots, chatOptions));
        }
    }
}

