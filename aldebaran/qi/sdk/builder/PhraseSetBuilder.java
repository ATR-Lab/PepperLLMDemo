package com.aldebaran.qi.sdk.builder;

import android.support.annotation.StringRes;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.util.FutureUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Build a new {@link PhraseSet}
 */
public class PhraseSetBuilder {
    private final QiContext qiContext;
    private final List<String> texts;
    private final List<Integer> resourceIds;
    private final List<Phrase> phrases;

    private PhraseSetBuilder(QiContext context) {
        this.qiContext = context;
        this.texts = new ArrayList<>();
        this.resourceIds = new ArrayList<>();
        this.phrases = new ArrayList<>();
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static PhraseSetBuilder with(QiContext context) {
        return new PhraseSetBuilder(context);
    }

    /**
     * Add phrases from texts
     *
     * @param texts the texts
     * @return the builder
     */
    public PhraseSetBuilder withTexts(String... texts) {
        if (texts != null) {
            this.texts.addAll(Arrays.asList(texts));
        }
        return this;
    }

    /**
     * Add phrases from string resources
     *
     * @param resIds the resources ids
     * @return the builder
     */
    public PhraseSetBuilder withResources(@StringRes Integer... resIds) {
        if (resIds != null) {
            this.resourceIds.addAll(Arrays.asList(resIds));
        }
        return this;
    }

    /**
     * Add phrases from string resources
     *
     * @param phrases the phrases
     * @return the builder
     */
    public PhraseSetBuilder withPhrases(Phrase... phrases) {
        if (phrases != null) {
            this.phrases.addAll(Arrays.asList(phrases));
        }
        return this;
    }

    /**
     * Add phrases from string resources
     *
     * @param phrases the phrases
     * @return the builder
     */
    public PhraseSetBuilder withPhrases(List<Phrase> phrases) {
        if (phrases != null) {
            this.phrases.addAll(phrases);
        }
        return this;
    }

    /**
     * Create the {@link PhraseSet} instance using configured values
     *
     * @return the phraseSet
     */
    public PhraseSet build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Create the {@link PhraseSet} instance using configured values
     *
     * @return the phraseSet
     */
    public Future<PhraseSet> buildAsync() {
        List<Phrase> localPhrases = new ArrayList<>();

        for (String text : texts) {
            localPhrases.add(new Phrase(text));
        }

        for (Integer resId : resourceIds) {
            localPhrases.add(new Phrase(qiContext.getString(resId)));
        }

        for (Phrase phrase : phrases) {
            localPhrases.add(phrase);
        }

        if (localPhrases.isEmpty()) {
            throw new IllegalStateException("Phrases required.");
        }

        return qiContext.getConversationAsync()
                .andThenCompose(service -> service.async().makePhraseSet(localPhrases));
    }
}
