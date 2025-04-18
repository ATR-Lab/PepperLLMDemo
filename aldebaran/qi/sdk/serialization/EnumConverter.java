package com.aldebaran.qi.sdk.serialization;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.QiConversionException;
import com.aldebaran.qi.sdk.object.QiEnum;
import com.aldebaran.qi.serialization.QiSerializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Convert {@link QiEnum} from and to raw {@link AnyObject}.
 */
public class EnumConverter implements QiSerializer.Converter {

    private static QiEnum getQiEnum(QiEnum[] qiEnums, long qiValue) {
        QiEnum unsupportedValue = null;
        for (QiEnum qiEnum : qiEnums) {
            if (qiEnum.getQiValue() == -1) {
                unsupportedValue = qiEnum;
            }

            if (qiEnum.getQiValue() == qiValue)
                return qiEnum;
        }

        return unsupportedValue;
    }

    @Override
    public boolean canSerialize(Object object) {
        return object instanceof QiEnum;
    }

    @Override
    public Integer serialize(QiSerializer serializer, Object object) throws QiConversionException {
        QiEnum qiEnum = (QiEnum) object;
        return qiEnum.getQiValue();
    }

    @Override
    public boolean canDeserialize(Object object, Type type) {
        if (!(type instanceof Class))
            return false;

        Class<?> cls = (Class<?>) type;
        return Enum.class.isAssignableFrom(cls) && QiEnum.class.isAssignableFrom(cls);
    }

    @Override
    public QiEnum deserialize(QiSerializer serializer, Object object, Type targetType) throws QiConversionException {
        if (!(object instanceof Number)) {
            throw new QiConversionException("Cannot convert instance of " + object.getClass() + " to " + targetType);
        }
        Class<? extends Enum> enumClass = (Class<? extends Enum>) targetType;
        try {
            int qiValue = ((Number) object).intValue();
            Method method = enumClass.getDeclaredMethod("values");
            method.setAccessible(true);
            QiEnum[] values = (QiEnum[]) method.invoke(null);
            return getQiEnum(values, qiValue);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassCastException e) {
            throw new QiConversionException(e);
        }
    }
}
