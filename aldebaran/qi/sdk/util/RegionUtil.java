package com.aldebaran.qi.sdk.util;

import android.util.Log;

import com.aldebaran.qi.sdk.object.locale.Region;

/**
 * Region utility class.
 */
public final class RegionUtil {

    private static final String TAG = "RegionUtil";

    // A cache for the last requested region
    private static Region lastRegion;


    private RegionUtil() {
        // not instantiable
    }

    /**
     * Retrieve a Region enum value from its associated qiValue.
     *
     * @param qiValue the internal value of the requested Region
     * @return the Region associated with the value
     */
    public static synchronized Region regionFrom(final int qiValue) {

        // Try the cached region first
        if (lastRegion != null && lastRegion.getQiValue() == qiValue) {
            return lastRegion;
        }
        for (Region region : Region.values()) {
            if (region.getQiValue() == qiValue) {
                lastRegion = region;
                return region;
            }
        }
        Log.e(TAG, "Unknown Region qiValue: " + qiValue + ". Returning UNKNOWN value.");
        return Region.UNKNOWN;
    }

}
