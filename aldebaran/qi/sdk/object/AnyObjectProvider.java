package com.aldebaran.qi.sdk.object;

import com.aldebaran.qi.AnyObject;

/**
 * Interface for objects that can provide an AnyObject.
 */
public interface AnyObjectProvider {

    /**
     * @return the instance of AnyObject associated to this.
     */
    AnyObject getAnyObject();

}
