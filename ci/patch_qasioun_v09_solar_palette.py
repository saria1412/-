#!/usr/bin/env python3
"""V09: genuinely transparent weather/clock widget, sunrise-to-Maghrib day/night palettes."""
from pathlib import Path
from xml.etree import ElementTree as ET
import sys

root=Path(sys.argv[1]).resolve()
res=root/"app/src/main/res"
java=root/"app/src/main/java/com/sarf/qasioun/prayer"
layouts=res/"layout"
def write(rel,data):
    p=root/rel
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(data.strip()+"\n",encoding="utf-8")

def once(source,old,new,label):
    matches=source.count(old)
    if matches!=1:
        raise AssertionError(f"{label}: found {matches} exact instances")
    return source.replace(old,new,1)

# Solar phase in pure Java, for offline use and for scheduling the NEXT sunset/sunrise.
write("app/src/main/java/com/sarf/qasioun/prayer/SolarThemeMath.java",r'''
package com.sarf.qasioun.prayer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/** Offline solar fallback; cached Umm Al-Qura sunrise/Maghrib always takes priority. */
public final class SolarThemeMath {
    private SolarThemeMath() {}
    public static boolean daylight(long nowMs,long sunriseMs,long maghribMs) {
        return nowMs >= sunriseMs && nowMs < maghribMs && sunriseMs < maghribMs;
    }
    private static double norm(double x,double range) {
        double n=x%range; return n<0?n+range:n;
    }
    private static double sin(double degrees){return Math.sin(Math.toRadians(degrees));}
    private static double cos(double degrees){return Math.cos(Math.toRadians(degrees));}

    private static double utcHour(LocalDate date,double latitude,double longitude,boolean rise) {
        double t=date.getDayOfYear()+(((rise?6.0:18.0)-longitude/15.0)/24.0);
        double mean=0.9856*t-3.289;
        double longitudeSun=norm(mean+1.916*sin(mean)+0.020*sin(2*mean)+282.634,360);
        double right=norm(Math.toDegrees(Math.atan(0.91764*Math.tan(Math.toRadians(longitudeSun)))),360);
        right+=Math.floor(longitudeSun/90.0)*90.0-Math.floor(right/90.0)*90.0;
        right/=15.0;
        double declinationSin=0.39782*sin(longitudeSun);
        double declinationCos=Math.cos(Math.asin(declinationSin));
        double hourCos=(cos(90.833)-declinationSin*sin(latitude))/(declinationCos*cos(latitude));
        if(hourCos>1.0||hourCos< -1.0) return Double.NaN;
        double localHour=(rise ? 360.0-Math.toDegrees(Math.acos(hourCos))
                : Math.toDegrees(Math.acos(hourCos)))/15.0+right-(0.06571*t)-6.622;
        return norm(localHour-longitude/15.0,24);
    }
    public static long eventMillis(LocalDate date, double latitude, double longitude,
                                   ZoneId zone, boolean sunrise) {
        if(!Double.isFinite(latitude)||!Double.isFinite(longitude)||
           latitude< -90||latitude>90||longitude< -180||longitude>180)
            return 0L;
        double hour=utcHour(date,latitude,longitude,sunrise);
        if(Double.isNaN(hour)) return 0L;
        Instant noonUTC=date.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(Math.round(hour*3600));
        for(int offset=-1;offset<=1;offset++){
            Instant timestamp=noonUTC.plusSeconds(offset*86400L);
            if(timestamp.atZone(zone).toLocalDate().equals(date))return timestamp.toEpochMilli();
        }
        return noonUTC.toEpochMilli();
    }
    public static boolean daylightNow(long nowMs,double lat,double lon,ZoneId zone) {
        LocalDate day=Instant.ofEpochMilli(nowMs).atZone(zone).toLocalDate();
        long sunrise=eventMillis(day,lat,lon,zone,true);
        long sunset=eventMillis(day,lat,lon,zone,false);
        return sunrise>0L && sunset>0L && daylight(nowMs,sunrise,sunset);
    }
    /** First future sunrise/sunset in the current GPS city, zero if coordinates unavailable. */
    public static long nextBoundary(long nowMs,double lat,double lon,ZoneId zone) {
        LocalDate day=Instant.ofEpochMilli(nowMs).atZone(zone).toLocalDate();
        long earliest=Long.MAX_VALUE;
        for(int delta=0;delta<=2;delta++) {
            LocalDate d=day.plusDays(delta);
            for(boolean sunrise:new boolean[]{true,false}){
                long ts=eventMillis(d,lat,lon,zone,sunrise);
                if(ts>nowMs && ts<earliest)earliest=ts;
            }
        }
        return earliest==Long.MAX_VALUE?0L:earliest;
    }
}
''')

write("app/src/main/java/com/sarf/qasioun/prayer/PrayerVisualTheme.java",r'''
package com.sarf.qasioun.prayer;
import android.content.Context;
import android.content.SharedPreferences;
import java.time.ZoneId;

/** Same GPS solar timing for prayer widgets, app, and transparent clock text. */
public final class PrayerVisualTheme {
    private static final String PREF="qasioun_prayer_palette_v09";
    private PrayerVisualTheme() {}
    public static boolean isDay(Context context,PrayerEngine.Snapshot snapshot) {
        long now=System.currentTimeMillis();
        if(snapshot!=null && snapshot.sunrise()!=null && snapshot.today()!=null) {
            long sunset=0L;
            for(PrayerEngine.Event event:snapshot.today()){
                if(event.index()==3) { sunset=event.epochMs(); break; }
            }
            long sunrise=snapshot.sunrise().epochMs();
            if(sunrise>0L && sunset>sunrise) {
                boolean day=SolarThemeMath.daylight(now,sunrise,sunset);
                context.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit()
                    .putBoolean("last_day",day).apply();
                return day;
            }
        }
        if(PrayerStore.hasLocation(context)) {
            boolean day=SolarThemeMath.daylightNow(now,PrayerStore.lat(context),
                    PrayerStore.lon(context),ZoneId.systemDefault());
            context.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit()
                .putBoolean("last_day",day).apply();
            return day;
        }
        return context.getSharedPreferences(PREF,Context.MODE_PRIVATE)
            .getBoolean("last_day",false);
    }
    public static long nextBoundary(Context context,PrayerEngine.Snapshot snapshot,long now) {
        long first=Long.MAX_VALUE;
        if(snapshot!=null && snapshot.sunrise()!=null) {
            long sunrise=snapshot.sunrise().epochMs();
            if(sunrise>now)first=Math.min(first,sunrise);
            for(PrayerEngine.Event event:snapshot.today()) {
                if(event.index()==3 && event.epochMs()>now)
                    first=Math.min(first,event.epochMs());
            }
        }
        if(PrayerStore.hasLocation(context)) {
            long fallback=SolarThemeMath.nextBoundary(now,PrayerStore.lat(context),
                    PrayerStore.lon(context),ZoneId.systemDefault());
            // While today's exact Umm Al-Qura data exists, don't use an
            // approximate boundary minutes before the official Maghrib/Sunrise.
            if(first==Long.MAX_VALUE && fallback>now)first=fallback;
        }
        return first==Long.MAX_VALUE?0L:first;
    }
}
''')

write("app/src/main/res/drawable/widget_background_day.xml",r'''
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#E8F9F3E7"/>
    <corners android:radius="22dp"/>
    <stroke android:width="1dp" android:color="#B0B48E50"/>
</shape>
''')
write("app/src/main/res/drawable/widget_next_panel_day.xml",r'''
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="#66E8D9C1"/>
    <corners android:radius="12dp"/>
    <stroke android:width="0.5dp" android:color="#55936E40"/>
</shape>
''')

colors=res/"values/colors.xml"
t=colors.read_text(encoding="utf-8")
t=once(t,"</resources>",'''    <color name="widget_day_primary">#0A352D</color>
    <color name="widget_day_muted">#48645A</color>
    <color name="widget_day_accent">#916324</color>
    <color name="widget_day_cream">#F9F3E7</color>
</resources>''',"light palette values")
colors.write_text(t,encoding="utf-8")

# Use separate Android RemoteViews layouts: no unsupported dynamic View background methods.
for base in ("prayer_widget_compact","prayer_widget"):
    src=(layouts/(base+".xml")).read_text(encoding="utf-8")
    variant=src.replace('android:background="@drawable/widget_background"',
                        'android:background="@drawable/widget_background_day"')
    variant=variant.replace('android:background="@drawable/widget_next_panel"',
                            'android:background="@drawable/widget_next_panel_day"')
    for old,new in (
        ('@color/ivory','@color/widget_day_primary'),
        ('@color/muted','@color/widget_day_muted'),
        ('@color/gold_soft','@color/widget_day_accent'),
        ('@color/gold','@color/widget_day_accent')):
        variant=variant.replace(old,new)
    assert variant!=src
    write("app/src/main/res/layout/"+base+"_day.xml",variant)

clock=layouts/"clock_weather_widget.xml"
clock_source=clock.read_text(encoding="utf-8")
assert 'android:background="@drawable/clock_weather_background"' in clock_source
clock_night=clock_source.replace('android:background="@drawable/clock_weather_background"',
                                 'android:background="@android:color/transparent"')
# Faint divider; transparent root means the theme no longer covers the sky/flag.
clock_night=clock_night.replace('android:background="#78C7B787"',
                                'android:background="#55E6C98E"')
clock.write_text(clock_night,encoding="utf-8")
clock_day=clock_night.replace('android:background="#55E6C98E"',
                              'android:background="#703C594D"')
for old,new in (
    ('@color/ivory','@color/widget_day_primary'),
    ('@color/muted','@color/widget_day_muted'),
    ('@color/gold','@color/widget_day_accent')):
    clock_day=clock_day.replace(old,new)
write("app/src/main/res/layout/clock_weather_widget_day.xml",clock_day)

widget=java/"WidgetProvider.java"
text=widget.read_text(encoding="utf-8")
text=once(text,
    '''        updateGroup(context, manager, snapshot, WidgetProvider.class, R.layout.prayer_widget, true);
        updateGroup(context, manager, snapshot, WidgetCompactProvider.class, R.layout.prayer_widget_compact, false);''',
    '''        boolean day = PrayerVisualTheme.isDay(context, snapshot);
        updateGroup(context, manager, snapshot, WidgetProvider.class,
                day ? R.layout.prayer_widget_day : R.layout.prayer_widget, true);
        updateGroup(context, manager, snapshot, WidgetCompactProvider.class,
                day ? R.layout.prayer_widget_compact_day : R.layout.prayer_widget_compact, false);''',
    "widgets day and night resource selector")
text=once(text,
    '''        int expandedCount = manager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class)).length;
        if (compactCount + expandedCount == 0) return;
        PrayerEngine.Event displayed = displayedEvent(snapshot, System.currentTimeMillis());
        if (displayed == null) return;
        long when = Math.max(System.currentTimeMillis()+1500L, displayed.epochMs()+1500L);''',
    '''        int expandedCount = manager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class)).length;
        int clockCount = manager.getAppWidgetIds(new ComponentName(context, ClockWeatherWidgetProvider.class)).length;
        if (compactCount + expandedCount + clockCount == 0) return;
        long now = System.currentTimeMillis();
        PrayerEngine.Event displayed = displayedEvent(snapshot, now);
        long nextEvent = displayed == null ? 0L : displayed.epochMs();
        long nextTheme = PrayerVisualTheme.nextBoundary(context, snapshot, now);
        long deadline = 0L;
        if (nextEvent > now) deadline = nextEvent;
        if (nextTheme > now && (deadline==0L || nextTheme < deadline)) deadline = nextTheme;
        if (deadline==0L) return;
        long when = Math.max(now + 1500L, deadline + 1500L);''',
    "clock-only schedule & solar boundary")
widget.write_text(text,encoding="utf-8")

clock_provider=java/"ClockWeatherWidgetProvider.java"
s=clock_provider.read_text(encoding="utf-8")
# Widget addition and time changes schedule both widgets regardless of prayer widget presence.
s=once(s,'''    @Override public void onUpdate(Context c,AppWidgetManager manager,int[] ids) {
        renderAll(c);''','''    @Override public void onUpdate(Context c,AppWidgetManager manager,int[] ids) {
        WidgetProvider.renderAll(c);''',"weather widget first install")
s=once(s,'''    @Override public void onEnabled(Context c) {
        renderAll(c);''','''    @Override public void onEnabled(Context c) {
        WidgetProvider.renderAll(c);''',"weather widget activation")
s=once(s,
    '''            RemoteViews rv=new RemoteViews(c.getPackageName(),R.layout.clock_weather_widget);''',
    '''            boolean day = PrayerVisualTheme.isDay(c, PrayerEngine.snapshot(c));
            RemoteViews rv=new RemoteViews(c.getPackageName(),
                    day ? R.layout.clock_weather_widget_day : R.layout.clock_weather_widget);''',
    "transparent clock color contrast")
clock_provider.write_text(s,encoding="utf-8")

activity=java/"MainActivity.java"
s=activity.read_text(encoding="utf-8")
s=once(s,"private int gold, ivory, muted, green, cardGreen;",
          "private int gold, ivory, muted, green, cardGreen;\n    private boolean dayPalette;","screen palette state")
s=once(s,'''        cardGreen = getColor(R.color.green2);
        kufiRegular =''','''        cardGreen = getColor(R.color.green2);
        applyVisualPalette(PrayerVisualTheme.isDay(this, PrayerEngine.snapshot(this)));
        kufiRegular =''',"main palette initialized")
anchor='    private int dp(float n) { return Math.round(n * getResources().getDisplayMetrics().density); }'
new_method='''    private void applyVisualPalette(boolean day) {
        dayPalette=day;
        gold=day ? 0xFF916324 : getColor(R.color.gold);
        ivory=day ? 0xFF0A352D : getColor(R.color.ivory);
        muted=day ? 0xFF48645A : getColor(R.color.muted);
        green=day ? 0xFFF6F0E4 : getColor(R.color.damascus_green);
        cardGreen=day ? 0xFFF9F4EA : getColor(R.color.green2);
        getWindow().setStatusBarColor(green);
        getWindow().setNavigationBarColor(green);
        int flags=day ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : 0;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
'''
s=once(s,anchor,new_method+anchor,"apply day/night app palette")
s=once(s,
    '''        View v = new View(this); v.setBackgroundColor(Color.argb(35, 255, 255, 255));''',
    '''        View v = new View(this); v.setBackgroundColor(dayPalette
                ? Color.argb(36, 10, 53, 45) : Color.argb(35, 255, 255, 255));''',
    "separator contrast")
s=once(s,
    '''cityRow.setBackground(shape(Color.rgb(13, 67, 56), Color.TRANSPARENT, 14));''',
    '''cityRow.setBackground(shape(dayPalette ? 0xFFEADDC8 :
                    Color.rgb(13, 67, 56), Color.TRANSPARENT, 14));''',
    "app light city strip")
s=once(s,
    '''        city.setText(PrayerStore.city(this));
        PrayerEngine.Snapshot sn = PrayerEngine.snapshot(this);''',
    '''        PrayerEngine.Snapshot sn = PrayerEngine.snapshot(this);
        boolean shouldBeDay=PrayerVisualTheme.isDay(this,sn);
        if(shouldBeDay!=dayPalette){
            applyVisualPalette(shouldBeDay);
            createScreen();
        }
        city.setText(PrayerStore.city(this));''',
    "switch app palette at sunrise and sunset")
activity.write_text(s,encoding="utf-8")

build=root/"app/build.gradle"
s=build.read_text(encoding="utf-8")
s=once(s,"versionCode 8","versionCode 9","version")
s=once(s,"versionName '0.8.0-portrait-weather-beta'",
         "versionName '0.9.0-solar-day-night-beta'","version name")
build.write_text(s,encoding="utf-8")

for p in [*layouts.glob("*.xml"),*res.glob("drawable/*.xml"),res/"values/colors.xml"]:
    ET.parse(p)

a="{http://schemas.android.com/apk/res/android}"
for name in ("prayer_widget_compact_day.xml","prayer_widget_day.xml","clock_weather_widget_day.xml"):
    xml=ET.parse(layouts/name).getroot()
    assert xml.attrib[a+"background"] in ("@drawable/widget_background_day",
                                           "@android:color/transparent"),name
for name in ("clock_weather_widget_day.xml","clock_weather_widget.xml"):
    assert '@android:color/transparent' in (layouts/name).read_text()
assert "clockCount" in text and "PrayerVisualTheme.nextBoundary" in text
assert "setBackgroundResource" not in text
assert "PrayerVisualTheme.isDay" in s or "PrayerVisualTheme.isDay" in activity.read_text()
print("PASS: V09 transparent clock, 2 pairs of widget palettes, app day/night UI, scheduled transitions")
