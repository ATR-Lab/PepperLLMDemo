package com.aldebaran.qi.sdk.services;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.Promise;
import com.aldebaran.qi.Session;

public class SessionRequirement extends Requirement<Session> {

    private Promise<Session> promise;

    public SessionRequirement() {
        createPromise();
    }

    private void createPromise() {
        promise = new Promise<>();
        promise.setOnCancel(p -> {
            try {
                p.setCancelled();
            } catch (Exception e) {
                // Just in case
            }
        });
    }

    @Override
    protected Future<Session> create() {
        return promise.getFuture();
    }

    public void setSession(Session session) {
        promise.setValue(session);
    }

    @Override
    public synchronized void invalidate() {
        super.invalidate();
        createPromise();
    }
}
