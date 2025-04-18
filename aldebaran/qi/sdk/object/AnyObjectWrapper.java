package com.aldebaran.qi.sdk.object;

import android.support.annotation.NonNull;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.QiService;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.core.SignalManager;
import com.aldebaran.qi.serialization.QiSerializer;

import static com.aldebaran.qi.sdk.core.BuildConfig.DEBUG;

/**
 * Parent class for QiService objects that run on the tablet.
 */
public abstract class AnyObjectWrapper extends QiService implements AnyObjectProvider {

    private static final String TAG = "AnyObjectWrapper";

    private QiContext qiContext;

    private SignalManager signalManager;

    protected AnyObjectWrapper(final QiContext context) {
        super();
        qiContext = context;

        init(advertise());

        if (DEBUG && (getAnyObject() == null || getQiContext() == null || getSerializer() == null)) {
            throw new AssertionError();
        }
    }


    @Override
    public AnyObject getAnyObject() {
        return self;
    }

    /**
     * @return the QiContext this object is associated to.
     */
    public QiContext getQiContext() {
        return qiContext;
    }


    protected QiSerializer getSerializer() {
        return getQiContext().getSerializer();
    }

    /**
     * Method for advertising methods and properties associated to this QiService.
     *
     * @return the AnyObject that results from the advertising
     */
    protected abstract AnyObject advertise();

    /**
     * This method only to provide a default implementation to deprecated setXxxListener methods.
     */
    protected void setListener(@NonNull final String signalName, final Object listener, final String method) {
        if (listener != null) {
            addListener(signalName, listener, method);
        } else {
            if (DEBUG && (method != null)) {
                throw new AssertionError();
            }
            removeAllListeners(signalName);
        }
    }

    protected void addListener(@NonNull final String propertyName, @NonNull final Object listener, @NonNull final String method) {
        getSignalManager().connect(propertyName, listener, method).getValue();
    }

    protected void removeListener(@NonNull final String propertyName, @NonNull final Object listener) {
        getSignalManager().disconnect(propertyName, listener);
    }

    protected void removeAllListeners(@NonNull final String signalName) {
        getSignalManager().disconnect(signalName);
    }

    private SignalManager getSignalManager() {
        if (signalManager == null) {
            signalManager = new SignalManager(getSerializer(), getAnyObject());
        }
        return signalManager;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnyObjectWrapper that = (AnyObjectWrapper) o;
        return getAnyObject().equals(that.getAnyObject());
    }

    @Override
    public int hashCode() {
        return getAnyObject().hashCode();
    }

    @Override
    public String toString() {
        return "AnyObjectWrapper{ " + getAnyObject() + " }";
    }

}
