package com.aldebaran.qi.sdk.builder;

import android.support.annotation.RawRes;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.Topic;
import com.aldebaran.qi.sdk.util.FutureUtils;
import com.aldebaran.qi.sdk.util.IOUtils;

/**
 * Build a new {@link Topic}
 */
public class TopicBuilder {

    private final QiContext qiContext;
    private String text = null;
    private Integer resId = null;
    private String assetName = null;

    private TopicBuilder(QiContext context) {
        qiContext = context;
    }

    /**
     * Create a new topic builder
     *
     * @param context the robot context
     * @return the builder
     */
    public static TopicBuilder with(QiContext context) {
        return new TopicBuilder(context);
    }

    /**
     * Set a topic content from asset
     *
     * @param assetName the asset file name
     * @return the builder
     */
    public TopicBuilder withAsset(String assetName) {
        if (text == null && resId == null) {
            this.assetName = assetName;
        }
        return this;
    }

    /**
     * Set a topic content from resource file
     *
     * @param resId the resource id
     * @return the builder
     */
    public TopicBuilder withResource(@RawRes Integer resId) {
        if (text == null && assetName == null) {
            this.resId = resId;
        }
        return this;
    }

    /**
     * Set a topic content from text
     *
     * @param text the text
     * @return the builder
     */
    public TopicBuilder withText(String text) {
        if (assetName == null && resId == null) {
            this.text = text;
        }
        return this;
    }

    /**
     * Create the {@link Topic} instance using configured values
     *
     * @return the Topic
     */
    public Topic build() {
        return FutureUtils.get(buildAsync());
    }


    /**
     * Create the {@link Topic} instance using configured values
     *
     * @return the Topic
     */
    public Future<Topic> buildAsync() {
        String topicContent = null;

        if (text != null) {
            topicContent = text;
        } else if (resId != null) {
            topicContent = IOUtils.fromRaw(qiContext, resId);
        } else if (assetName != null) {
            topicContent = IOUtils.fromAsset(qiContext, assetName);
        }

        if (topicContent == null) {
            throw new IllegalStateException("Topic required.");
        }

        String finalTopicContent = topicContent;
        return qiContext.getConversationAsync()
                .andThenCompose(service -> service.async().makeTopic(finalTopicContent));
    }
}
