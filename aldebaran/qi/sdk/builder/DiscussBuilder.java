package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.Discuss;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.util.FutureUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Build a new {@link Discuss}
 *
 * @deprecated Since API level 3. Please use {@link ChatBuilder} with {@link QiChatbotBuilder} instead.
 */
@Deprecated
public class DiscussBuilder {

    private List<Topic> topics;
    private QiContext qiContext;
    private Locale locale;

    private DiscussBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
        this.topics = new ArrayList<>();
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static DiscussBuilder with(QiContext context) {
        return new DiscussBuilder(context);
    }

    /**
     * Add a topic to discuss
     *
     * @param topics the topic
     * @return the builder
     */
    public DiscussBuilder withTopic(Topic... topics) {
        if (topics != null) {
            this.topics.addAll(Arrays.asList(topics));
        }

        return this;
    }

    /**
     * Add topics list to discuss
     *
     * @param topics the topic list
     * @return the builder
     */
    public DiscussBuilder withTopics(List<Topic> topics) {
        if (topics != null) {
            this.topics.addAll(topics);
        }

        return this;
    }

    /**
     * Add locale to discuss
     *
     * @param locale the locale
     * @return the builder
     */
    public DiscussBuilder withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Return a configured instance of Discuss
     *
     * @return the Discuss
     */
    public Discuss build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of Discuss
     *
     * @return the Discuss
     */
    public Future<Discuss> buildAsync() {
        if (topics.isEmpty()) {
            throw new IllegalStateException("Topics required.");
        }

        if (locale == null) {
            return qiContext.getConversationAsync()
                    .andThenCompose(service -> service.async().makeDiscuss(qiContext.getRobotContext(), topics));
        } else {
            return qiContext.getConversationAsync()
                    .andThenCompose(service -> service.async().makeDiscuss(qiContext.getRobotContext(), topics, locale));
        }

    }
}