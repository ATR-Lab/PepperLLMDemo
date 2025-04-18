package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.Optional;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenOptions;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.util.FutureUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Build a new {@link Listen}
 */
public class ListenBuilder {
    private QiContext qiContext;
    private List<PhraseSet> phraseSets;
    private BodyLanguageOption bodyLanguageOption;
    private Locale locale;
    private Map<String, String> aSRParameters;
    private ListenOptions listenOptions = new ListenOptions();

    private ListenBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
        this.phraseSets = new ArrayList<>();
    }

    /**
     * Create a new builder from the qiContext
     *
     * @param context the Android context
     * @return the builder
     */
    public static ListenBuilder with(QiContext context) {
        return new ListenBuilder(context);
    }

    /**
     * Configure the phrase set used by Listen
     *
     * @param phraseSet the phrase set
     * @return the builder
     */
    public ListenBuilder withPhraseSet(PhraseSet phraseSet) {
        this.phraseSets.add(phraseSet);
        return this;
    }

    /**
     * Configure the phrase sets used by Listen
     *
     * @param phraseSets the phrase sets
     * @return the builder
     */
    public ListenBuilder withPhraseSets(PhraseSet... phraseSets) {
        if (phraseSets != null) {
            this.phraseSets.addAll(Arrays.asList(phraseSets));
        }
        return this;
    }

    /**
     * Configure the phrase sets used by Listen
     *
     * @param phraseSets the phrase sets list
     * @return the builder
     */
    public ListenBuilder withPhraseSets(List<PhraseSet> phraseSets) {
        if (phraseSets != null) {
            this.phraseSets.addAll(phraseSets);
        }
        return this;
    }

    /**
     * Configure the bodyLanguageOption used by Listen
     *
     * @param bodyLanguageOption the bodyLanguageOption
     * @return the builder
     */
    public ListenBuilder withBodyLanguageOption(BodyLanguageOption bodyLanguageOption) {
        this.bodyLanguageOption = bodyLanguageOption;
        return this;
    }

    /**
     * Configure the locale used by Listen
     *
     * @param locale the locale
     * @return the builder
     */
    public ListenBuilder withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }


    /**
     * Configure the AsrDriverParameters used by Listen
     *
     * @param aSRParameters the AsrDriverParameters
     * @return the builder
     */
    public ListenBuilder withAsrDriverParameters(Map<String, String> aSRParameters) {
        this.aSRParameters = aSRParameters;
        return this;
    }

    /**
     * Return a configured instance of Listen
     *
     * @return the Listen
     */
    public Listen build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of Listen
     *
     * @return the Listen
     */
    public Future<Listen> buildAsync() {
        if (phraseSets.isEmpty()) {
            throw new IllegalStateException("Phrase sets required.");
        }

        if (aSRParameters == null) {
            if (bodyLanguageOption == null) {
                if (locale == null) {
                    return qiContext.getConversationAsync()
                            .andThenCompose(service -> service.async().makeListen(qiContext.getRobotContext(), phraseSets));
                } else {
                    return qiContext.getConversationAsync()
                            .andThenCompose(service -> service.async().makeListen(qiContext.getRobotContext(), phraseSets, BodyLanguageOption.NEUTRAL, locale));
                }
            } else {
                if (locale == null) {
                    return qiContext.getConversationAsync()
                            .andThenCompose(service -> service.async().makeListen(qiContext.getRobotContext(), phraseSets, bodyLanguageOption));
                } else {
                    return qiContext.getConversationAsync()
                            .andThenCompose(service -> service.async().makeListen(qiContext.getRobotContext(), phraseSets, bodyLanguageOption, locale));
                }
            }
        } else {
            listenOptions.setASRParameters(Optional.of(aSRParameters));

            if (bodyLanguageOption != null) {
                listenOptions.setBodyLanguageOption(Optional.of(bodyLanguageOption));
            } else {
                listenOptions.setBodyLanguageOption(Optional.<BodyLanguageOption>empty());
            }

            if (locale != null) {
                listenOptions.setLocale(Optional.of(locale));
            } else {
                listenOptions.setLocale(Optional.<Locale>empty());
            }

            return qiContext.getConversationAsync()
                    .andThenCompose(service -> service.async().makeListen(qiContext.getRobotContext(), phraseSets, listenOptions));
        }
    }
}
