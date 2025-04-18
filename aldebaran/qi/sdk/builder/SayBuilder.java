package com.aldebaran.qi.sdk.builder;

import android.support.annotation.StringRes;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.aldebaran.qi.sdk.object.locale.Locale;
import com.aldebaran.qi.sdk.util.FutureUtils;

/**
 * Build a new {@link Say}
 */
public class SayBuilder {
    private final QiContext qiContext;
    private final SpeechEngine speechEngine;

    private Phrase phrase;
    private BodyLanguageOption bodyLanguageOption;
    private Locale locale;

    private SayBuilder(QiContext qiContext) {
        this.qiContext = qiContext;
        this.speechEngine = null;
    }

    private SayBuilder(SpeechEngine speechEngine) {
        this.qiContext = null;
        this.speechEngine = speechEngine;
    }

    /**
     * Create a new builder from the {@link QiContext}
     *
     * @param context the Android context
     * @return the builder
     */
    public static SayBuilder with(QiContext context) {
        return new SayBuilder(context);
    }

    /**
     * Create a new builder from the {@link SpeechEngine}
     *
     * @param speechEngine the SpeechEngine
     * @return the builder
     */
    public static SayBuilder with(SpeechEngine speechEngine) {
        return new SayBuilder(speechEngine);
    }

    /**
     * Configure the text used by Say
     *
     * @param text the text
     * @return the builder
     */
    public SayBuilder withText(String text) {
        this.phrase = new Phrase(text);
        return this;
    }

    /**
     * Configure the phrase used by Say
     *
     * @param phrase the phrase
     * @return the builder
     */
    public SayBuilder withPhrase(Phrase phrase) {
        this.phrase = phrase;
        return this;
    }

    /**
     * Configure the text used by Say
     *
     * @param resId      the resource id of string
     * @param formatArgs The format arguments that will be used for substitution.
     * @return the builder
     */
    public SayBuilder withResource(@StringRes int resId, Object... formatArgs) {
        this.phrase = new Phrase(qiContext.getString(resId, formatArgs));
        return this;
    }

    /**
     * Configure the bodyLanguageOption used by Say
     *
     * @param bodyLanguageOption the bodyLanguageOption
     * @return the builder
     */
    public SayBuilder withBodyLanguageOption(BodyLanguageOption bodyLanguageOption) {
        this.bodyLanguageOption = bodyLanguageOption;
        return this;
    }

    /**
     * Configure the locale used by Say
     *
     * @param locale the locale
     * @return the builder
     */
    public SayBuilder withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Return a configured instance of Say
     *
     * @return the Say
     */
    public Say build() {
        return FutureUtils.get(buildAsync());
    }

    /**
     * Return a configured instance of Say
     *
     * @return the Say
     */
    public Future<Say> buildAsync() {
        if (qiContext == null && speechEngine == null) {
            throw new IllegalStateException("QiContext or SpeechEngine required.");
        }

        if (phrase == null) {
            throw new IllegalStateException("Phrase required.");
        }

        if (bodyLanguageOption == null) {
            if (locale == null) {
                if (speechEngine == null) {
                    return qiContext.getConversationAsync()
                            .andThenCompose(service -> service.async().makeSay(qiContext.getRobotContext(), phrase));
                } else {
                    return speechEngine.async().makeSay(phrase);
                }
            } else {
                if (speechEngine == null) {
                    return qiContext.getConversationAsync()
                            .andThenCompose(service -> service.async().makeSay(qiContext.getRobotContext(), phrase, BodyLanguageOption.NEUTRAL, locale));
                } else {
                    return speechEngine.async().makeSay(phrase, BodyLanguageOption.NEUTRAL, locale);
                }
            }
        } else {
            if (locale == null) {
                if (speechEngine == null) {
                    return qiContext.getConversationAsync()
                            .andThenCompose(service -> service.async().makeSay(qiContext.getRobotContext(), phrase, bodyLanguageOption));
                } else {
                    return speechEngine.async().makeSay(phrase, bodyLanguageOption);
                }
            } else {
                if (speechEngine == null) {
                    return qiContext.getConversationAsync()
                            .andThenCompose(service -> service.async().makeSay(qiContext.getRobotContext(), phrase, bodyLanguageOption, locale));
                } else {
                    return speechEngine.async().makeSay(phrase, bodyLanguageOption, locale);
                }
            }

        }
    }
}
