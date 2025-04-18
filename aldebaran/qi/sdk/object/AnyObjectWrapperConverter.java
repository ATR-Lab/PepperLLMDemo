package com.aldebaran.qi.sdk.object;

import com.aldebaran.qi.QiConversionException;
import com.aldebaran.qi.sdk.BuildConfig;
import com.aldebaran.qi.serialization.QiSerializer;

import java.lang.reflect.Type;

/**
 * Converter for AnyObjectWrapper objects.
 */
public class AnyObjectWrapperConverter implements QiSerializer.Converter {

    @Override
    public boolean canSerialize(final Object o) {
        return o instanceof AnyObjectWrapper;
    }

    @Override
    public Object serialize(final QiSerializer qiSerializer, final Object o) throws QiConversionException {
        return ((AnyObjectWrapper) o).getAnyObject();
    }

    @Override
    public boolean canDeserialize(final Object o, final Type type) {
        // Default : a AnyObjectWrapper cannot be retrieved from the head
        return false;
    }

    @Override
    public Object deserialize(final QiSerializer qiSerializer, final Object o, final Type type) throws QiConversionException {
        if (BuildConfig.DEBUG) throw new AssertionError();
        return null;
    }
}
