package com.sarf.qasioun.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;

/** Only a user-approved location is used for solar sunrise/sunset. */
public final class GeoPreferences {
    private static final String PREFERENCES="qasioun_solar_location_v2";
    private GeoPreferences(){}
    private static SharedPreferences get(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }
    public static boolean has(Context c) { return get(c).getBoolean("approved", false); }
    public static double latitude(Context c) {
        return Double.longBitsToDouble(get(c).getLong("latitude",
          Double.doubleToRawLongBits(Double.NaN)));
    }
    public static double longitude(Context c) {
        return Double.longBitsToDouble(get(c).getLong("longitude",
          Double.doubleToRawLongBits(Double.NaN)));
    }
    public static String source(Context c) { return get(c).getString("source", "غير محدد"); }
    public static float accuracy(Context c) { return get(c).getFloat("accuracy",-1f); }
    public static long updated(Context c) { return get(c).getLong("updated",0L); }

    public static void save(Context c,double lat,double lon,String source,float accuracy) {
        if(!Double.isFinite(lat) || !Double.isFinite(lon) ||
          lat < -90 || lat > 90 || lon < -180 || lon > 180)
            throw new IllegalArgumentException("إحداثيات غير صالحة");
        get(c).edit().putBoolean("approved",true)
            .putLong("latitude",Double.doubleToRawLongBits(lat))
            .putLong("longitude",Double.doubleToRawLongBits(lon))
            .putString("source",source)
            .putFloat("accuracy",accuracy)
            .putLong("updated",System.currentTimeMillis()).apply();
    }
}
