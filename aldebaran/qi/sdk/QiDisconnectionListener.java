package com.aldebaran.qi.sdk;

/**
 * Session disconnection listener.
 */
public interface QiDisconnectionListener {

    /**
     * Method called when the session has been disconnected.
     * <p>
     * In that case, every robot objects created from this session become invalid.
     *
     * @param reason the reason
     */
    void onQiDisconnected(String reason);
}
