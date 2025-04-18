package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.util.FutureUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Build a new {@link QiChatbot}
 */
public class QiChatbotBuilder {

    private List<Topic> topics;
    private QiContext qiContext;
    private Locale locale;

    private QiChatbotBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
        this.topics = new ArrayList<>();
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static QiChatbotBuilder with(QiContext context) {
        return new QiChatbotBuilder(context);
    }

    /**
     * Add a topic to QiChatbot
     *
     * @param topics the topic
     * @return the builder
     */
    public QiChatbotBuilder withTopic(Topic... topics) {
        if (topics != null) {
            this.topics.addAll(Arrays.asList(topics));
        }

        return this;
    }

    /**
     * Add topics list to QiChatbot
     *
     * @param topics the topic list
     * @return the builder
     */
    public QiChatbotBuilder withTopics(List<? extends Topic> topics) {
        if (topics != null) {
            this.topics.addAll(topics);
        }

        return this;
    }

    /**
     * Add locale to QiChatbot
     *
     * @param locale the locale
     * @return the builder
     */
    public QiChatbotBuilder withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Return a configured instance of QiChatbot
     *
     * @return the QiChatbot
     */
    public QiChatbot build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of QiChatbot
     *
     * @return the QiChatbot
     */
    public Future<QiChatbot> buildAsync() {
        if (topics.isEmpty()) {
            throw new IllegalStateException("Topics required.");
        }

        if (locale == null) {
            return qiContext.getConversationAsync()
                    .andThenCompose(service -> service.async().makeQiChatbot(qiContext.getRobotContext(), topics));
        } else {
            return qiContext.getConversationAsync()
                    .andThenCompose(service -> service.async().makeQiChatbot(qiContext.getRobotContext(), topics, locale));
        }

    }
}