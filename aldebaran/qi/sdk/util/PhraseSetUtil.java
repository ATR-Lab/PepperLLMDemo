package com.aldebaran.qi.sdk.util;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;

import java.util.List;

/**
 * PhraseSet Utility class
 */
public class PhraseSetUtil {

    public static Boolean equals(PhraseSet phraseSet1, PhraseSet phraseSet2) {
        return FutureUtils.get(Async.equals(phraseSet1, phraseSet2));
    }

    public static class Async {
        public static Future<Boolean> equals(final PhraseSet phraseSet1, final PhraseSet phraseSet2) {

            return phraseSet1.async().getPhrases().andThenApply(phrases1 -> {
                List<Phrase> phrases2 = phraseSet2.getPhrases();
                return phrases1.containsAll(phrases2) && phrases2.containsAll(phrases1);
            });
        }
    }

}
