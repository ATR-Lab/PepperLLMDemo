package com.aldebaran.qi.sdk.object.conversation;

/**
 * Interface for objects that can be run with a SpeechEngine.
 */

interface WithSpeechEngineRunnable {

    /**
     * @param speechEngine the speech engine to be used in this method
     */
    void runWith(SpeechEngine speechEngine);

    /**
     * Stop running.
     */
    void stop();

}
