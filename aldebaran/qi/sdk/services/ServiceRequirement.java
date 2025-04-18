package com.aldebaran.qi.sdk.services;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.Promise;
import com.aldebaran.qi.QiConversionException;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.serialization.QiSerializer;

public class ServiceRequirement<T> extends Requirement<T> {
    private final Requirement<Session> sessionRequirement;
    private final String serviceName;
    private Class<T> clazz;
    private final QiSerializer serializer;

    public ServiceRequirement(Requirement<Session> sessionRequirement, QiSerializer serializer, String serviceName, Class<T> clazz) {
        this.sessionRequirement = sessionRequirement;
        this.serviceName = serviceName;
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    protected Future<T> create() {
        return sessionRequirement.satisfy()
                .andThenCompose(this::retrieveService)
                .andThenApply(this::serializeService);
    }

    private Future<AnyObject> retrieveService(Session session) {
        //FIXME: remove useless promise when libqi fix the waitForService crash on cancel
        Promise<Void> p = new Promise<>();
        p.setOnCancel(aVoid -> {
            try {
                p.setCancelled();
            } catch (Exception e) {
                // Just in case
            }
        });

        // waitForService has a default timeout (5min)
        session.waitForService(serviceName).thenConsume(future -> {
            try {
                if (future.hasError()) {
                    p.setError(future.getErrorMessage());
                } else if (future.isCancelled()) {
                    p.setCancelled();
                } else {
                    p.setValue(null);
                }
            } catch (Exception e) {
                // If the future is already cancelled this block can throw
            }
        });
        return p.getFuture().andThenCompose(aVoid -> session.service(serviceName));
    }

    private T serializeService(AnyObject service) throws QiConversionException {
        return (T) serializer.deserialize(service, clazz);
    }
}
