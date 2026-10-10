package com.sarf.qasioun.wallpaper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Offline solar sunrise/sunset calculation based on the standard USNO approximation.
 * Zenith 90.833 degrees includes standard atmospheric refraction.
 * Result is tied to the user's current timezone and GPS latitude/longitude.
 */
public final class SolarClock {
    private SolarClock() { }
    public static final class Events {
        public final ZonedDateTime sunrise;
        public final ZonedDateTime sunset;
        Events(ZonedDateTime rise, ZonedDateTime set) {sunrise=rise; sunset=set;}
    }
    private static double normalized(double x, double period) {
        double y = x % period;
        return y < 0 ? y + period : y;
    }
    private static double sin(double deg) {return Math.sin(Math.toRadians(deg));}
    private static double cos(double deg) {return Math.cos(Math.toRadians(deg));}
    private static double atan(double x) {return Math.toDegrees(Math.atan(x));}
    private static double acos(double x) {return Math.toDegrees(Math.acos(x));}

    static double utcHour(LocalDate date, double lat, double lon, boolean sunrise) {
        int n=date.getDayOfYear();
        double lngHour=lon/15.0;
        double t=n+(((sunrise?6.0:18.0)-lngHour)/24.0);
        double m=0.9856*t-3.289;
        double l=normalized(m+1.916*sin(m)+0.020*sin(2*m)+282.634,360.0);
        double ra=normalized(atan(0.91764*Math.tan(Math.toRadians(l))),360.0);
        ra=ra+(Math.floor(l/90.0)*90.0-Math.floor(ra/90.0)*90.0);
        ra/=15.0;
        double sinDec=0.39782*sin(l);
        double cosDec=Math.cos(Math.asin(sinDec));
        double cosH=(cos(90.833)-(sinDec*sin(lat)))/(cosDec*cos(lat));
        if (cosH > 1.0 || cosH < -1.0) return Double.NaN;
        double h=sunrise?(360.0-acos(cosH)):acos(cosH);
        h/=15.0;
        double local=h+ra-(0.06571*t)-6.622;
        return normalized(local-lngHour,24.0);
    }
    static ZonedDateTime solar(LocalDate date,double lat,double lon,ZoneId zone,boolean sunrise) {
        if (!Double.isFinite(lat) || !Double.isFinite(lon) ||
                lat < -90 || lat > 90 || lon < -180 || lon > 180)
            throw new IllegalArgumentException("Invalid coordinates");
        double hour=utcHour(date,lat,lon,sunrise);
        if (Double.isNaN(hour)) {
            // Polar day/night fallback; not intended for Saudi Arabia.
            return date.atTime(sunrise?6:18,0).atZone(zone);
        }
        long seconds=Math.round(hour*3600.0);
        Instant utc=date.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(seconds);
        for (int delta=-1;delta<=1;delta++) {
            ZonedDateTime candidate=utc.plusSeconds(delta*86400L).atZone(zone);
            if (candidate.toLocalDate().equals(date)) return candidate;
        }
        return utc.atZone(zone);
    }
    public static Events forDate(LocalDate date,double lat,double lon,ZoneId zone) {
        return new Events(solar(date,lat,lon,zone,true),solar(date,lat,lon,zone,false));
    }
    public static boolean isNight(ZonedDateTime now,double lat,double lon) {
        Events today=forDate(now.toLocalDate(),lat,lon,now.getZone());
        return now.isBefore(today.sunrise) || !now.isBefore(today.sunset);
    }
}
