package com.aldebaran.qi.sdk.builder;

import com.aldebaran.qi.sdk.object.geometry.Quaternion;
import com.aldebaran.qi.sdk.object.geometry.Transform;
import com.aldebaran.qi.sdk.object.geometry.Vector3;

/**
 * Build a new {@link Transform}
 */
public class TransformBuilder {

    /**
     * Create a new builder
     *
     * @return the builder
     */
    public static TransformBuilder create() {
        return new TransformBuilder();
    }

    /**
     * Create a {@link Transform} from rotation
     *
     * @param rotation the rotation
     * @return the Transform
     */
    public Transform fromRotation(Quaternion rotation) {
        return new Transform(rotation, new Vector3(0d, 0d, 0d));
    }

    /**
     * Create a {@link Transform} from translation
     *
     * @param translation the translation
     * @return the Transform
     */
    public Transform fromTranslation(Vector3 translation) {
        return new Transform(new Quaternion(0d, 0d, 0d, 1d), translation);
    }

    /**
     * Create a {@link Transform} from X translation
     *
     * @param x the X translation
     * @return the Transform
     */
    public Transform fromXTranslation(double x) {
        return new Transform(new Quaternion(0d, 0d, 0d, 1d), new Vector3(x, 0d, 0d));
    }

    /**
     * Create a {@link Transform} from 2D translation
     *
     * @param x the X translation
     * @param y the Y translation
     * @return the Transform
     */
    public Transform from2DTranslation(double x, double y) {
        return new Transform(new Quaternion(0d, 0d, 0d, 1d), new Vector3(x, y, 0d));
    }

    /**
     * Create a {@link Transform} from 2D transform
     *
     * @param x        the X translation
     * @param y        the Y translation
     * @param thetaRad the radian
     * @return the Transform
     */
    public Transform from2DTransform(double x, double y, double thetaRad) {
        return new Transform(new Quaternion(0d, 0d, Math.sin(thetaRad * 0.5), Math.cos(thetaRad * 0.5)), new Vector3(x, y, 0d));
    }
}
