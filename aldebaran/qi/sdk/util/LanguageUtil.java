package com.aldebaran.qi.sdk.util;

import android.util.Log;

import com.aldebaran.qi.sdk.object.locale.Language;

/**
 * Language utility class.
 */

public final class LanguageUtil {

    private static final String TAG = "LanguageUtil";

    // A cache for the last requested region
    private static Language lastLanguage;


    private LanguageUtil() {
        // not instantiable
    }

    /**
     * Retrieve a Language enum value from its associated qiValue.
     *
     * @param qiValue the internal value of the requested Region
     * @return the Language associated to the value
     */
    public static synchronized Language languageFrom(final int qiValue) {

        // Try the cached language first
        if (lastLanguage != null && lastLanguage.getQiValue() == qiValue) {
            return lastLanguage;
        }

        for (Language language : Language.values()) {
            if (language.getQiValue() == qiValue) {
                lastLanguage = language;
                return language;
            }
        }
        Log.e(TAG, "Unknown Language qiValue: " + qiValue + ". Returning UNKNOWN language.");
        return Language.UNKNOWN;
    }

}
