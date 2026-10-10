#!/usr/bin/env python3
"""Add a portrait clock + live weather widget to the verified Qasioun prayer app."""
from pathlib import Path
from xml.etree import ElementTree as ET
import sys

root=Path(sys.argv[1]).resolve()
java=root/"app/src/main/java/com/sarf/qasioun/prayer"
res=root/"app/src/main/res"
java.mkdir(parents=True,exist_ok=True)

def write(p,data):
    p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text(data.strip()+"\n",encoding="utf-8")

write(res/"drawable/clock_weather_background.xml",r'''
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <gradient
        android:startColor="#D20A382F"
        android:endColor="#E5083029"
        android:angle="270"/>
    <corners android:radius="23dp"/>
    <stroke android:width="1dp" android:color="#B8E6C98E"/>
</shape>
''')

write(res/"layout/clock_weather_widget.xml",r'''
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/cw_root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layoutDirection="rtl"
    android:orientation="vertical"
    android:gravity="center_horizontal"
    android:paddingStart="10dp"
    android:paddingEnd="10dp"
    android:paddingTop="9dp"
    android:paddingBottom="9dp"
    android:background="@drawable/clock_weather_background">
    <LinearLayout android:orientation="horizontal"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center_vertical"
        android:layoutDirection="rtl">
        <TextView android:id="@+id/cw_city"
            android:layout_width="0dp" android:layout_weight="1"
            android:layout_height="wrap_content"
            android:text="المدينة"
            android:singleLine="true" android:ellipsize="end"
            android:fontFamily="@font/noto_kufi_arabic_bold"
            android:textColor="@color/gold"
            android:textSize="11sp"
            android:includeFontPadding="false"/>
        <TextView android:id="@+id/cw_refresh"
            android:layout_width="26dp" android:layout_height="28dp"
            android:gravity="center" android:text="↻"
            android:textSize="19sp"
            android:textColor="@color/gold"
            android:contentDescription="تحديث الطقس"/>
    </LinearLayout>
    <TextClock
        android:id="@+id/cw_clock"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:format12Hour="h:mm"
        android:format24Hour="HH:mm"
        android:fontFamily="sans-serif-light"
        android:textSize="40sp"
        android:textColor="@color/ivory"
        android:includeFontPadding="false"
        android:singleLine="true"/>
    <TextView android:layout_width="match_parent" android:layout_height="1dp"
        android:layout_marginTop="4dp"
        android:layout_marginBottom="6dp"
        android:background="#78C7B787"/>
    <TextView android:id="@+id/cw_gregorian"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="التاريخ الميلادي"
        android:textColor="@color/ivory"
        android:textSize="10sp"
        android:fontFamily="@font/noto_kufi_arabic_medium"
        android:maxLines="2" android:ellipsize="end"
        android:includeFontPadding="false"/>
    <TextView android:id="@+id/cw_hijri"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:gravity="center"
        android:text="التاريخ الهجري"
        android:textColor="@color/gold"
        android:textSize="10sp"
        android:fontFamily="@font/noto_kufi_arabic_regular"
        android:maxLines="2" android:ellipsize="end"
        android:includeFontPadding="false"/>
    <TextView android:layout_width="match_parent" android:layout_height="1dp"
        android:layout_marginTop="7dp"
        android:layout_marginBottom="5dp"
        android:background="#78C7B787"/>
    <TextView android:id="@+id/cw_weather_icon"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="☀"
        android:textSize="33sp"
        android:fontFamily="sans-serif"
        android:textColor="@color/gold"
        android:includeFontPadding="false"/>
    <TextView android:id="@+id/cw_temperature"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="--°"
        android:textDirection="ltr"
        android:fontFamily="sans-serif-medium"
        android:textSize="31sp"
        android:textColor="@color/ivory"
        android:includeFontPadding="false"/>
    <TextView android:id="@+id/cw_condition"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="بانتظار الطقس"
        android:textSize="11sp"
        android:fontFamily="@font/noto_kufi_arabic_medium"
        android:textColor="@color/ivory"
        android:singleLine="true" android:ellipsize="end"
        android:includeFontPadding="false"/>
    <TextView android:id="@+id/cw_status"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:gravity="center"
        android:text="طقس حسب الموقع"
        android:textSize="8sp"
        android:fontFamily="@font/noto_kufi_arabic_regular"
        android:textColor="@color/muted"
        android:singleLine="true" android:ellipsize="end"
        android:includeFontPadding="false"/>
</LinearLayout>
''')

write(res/"xml/clock_weather_widget.xml",r'''
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="145dp"
    android:minHeight="190dp"
    android:minResizeWidth="145dp"
    android:minResizeHeight="190dp"
    android:targetCellWidth="2"
    android:targetCellHeight="3"
    android:initialLayout="@layout/clock_weather_widget"
    android:previewLayout="@layout/clock_weather_widget"
    android:previewImage="@drawable/clock_weather_preview"
    android:updatePeriodMillis="1800000"
    android:widgetCategory="home_screen"
    android:resizeMode="horizontal|vertical"/>
''')

write(java/"WeatherCodes.java",r'''
package com.sarf.qasioun.prayer;

/** WMO interpretation for Open-Meteo current weather, without misleading icons. */
public final class WeatherCodes {
    private WeatherCodes() {}

    public static String label(int code) {
        switch (code) {
            case 0: return "صحو";
            case 1: return "غالبًا صافٍ";
            case 2: return "غائم جزئيًا";
            case 3: return "غائم";
            case 45: case 48: return "ضباب";
            case 51: case 53: case 55: case 56: case 57: return "رذاذ";
            case 61: case 63: case 65: case 66: case 67: return "أمطار";
            case 71: case 73: case 75: case 77: return "ثلوج";
            case 80: case 81: case 82: return "زخات مطر";
            case 85: case 86: return "زخات ثلج";
            case 95: case 96: case 99: return "عواصف رعدية";
            default: return "حالة الطقس";
        }
    }

    public static String glyph(int code, boolean day) {
        if (code == 0) return day ? "☀" : "☾";
        if (code >= 1 && code <= 3) return "☁";
        if (code == 45 || code == 48) return "≋";
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) return "☂";
        if (code >= 71 && code <= 77 || code == 85 || code == 86) return "❄";
        if (code >= 95) return "⚡";
        return "◌";
    }
}
''')

write(java/"WeatherStore.java",r'''
package com.sarf.qasioun.prayer;

import android.content.Context;
import android.content.SharedPreferences;

/** Shared cached current conditions; coordinate matching prevents showing weather for an old city. */
public final class WeatherStore {
    private static final String PREFS = "qasioun_weather_v1";
    public record State(double temperature, int weatherCode, boolean day, long fetchedAt) { }
    private WeatherStore() {}
    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static State get(Context c) {
        SharedPreferences p=prefs(c);
        if (!PrayerStore.hasLocation(c) || !p.contains("temp") || !p.contains("fetched_at")) return null;
        double lat = Double.longBitsToDouble(p.getLong("lat_bits", 0L));
        double lon = Double.longBitsToDouble(p.getLong("lon_bits", 0L));
        if (PrayerMath.distanceKm(lat, lon, PrayerStore.lat(c), PrayerStore.lon(c)) > 10.0) return null;
        long timestamp=p.getLong("fetched_at",0L);
        if (timestamp<=0 || timestamp > System.currentTimeMillis()+10*60_000L) return null;
        return new State(Double.longBitsToDouble(p.getLong("temp",0L)),
                p.getInt("code",-1), p.getBoolean("day",true),timestamp);
    }
    public static boolean needsRefresh(Context c) {
        State x=get(c);
        return x==null || System.currentTimeMillis()-x.fetchedAt()>=55*60_000L;
    }
    public static void save(Context c,double lat,double lon,State value) {
        if (!PrayerStore.hasLocation(c) ||
                PrayerMath.distanceKm(lat,lon,PrayerStore.lat(c),PrayerStore.lon(c))>1.5) return;
        prefs(c).edit()
                .putLong("lat_bits",Double.doubleToRawLongBits(lat))
                .putLong("lon_bits",Double.doubleToRawLongBits(lon))
                .putLong("temp",Double.doubleToRawLongBits(value.temperature()))
                .putInt("code",value.weatherCode())
                .putBoolean("day",value.day())
                .putLong("fetched_at",value.fetchedAt()).apply();
    }
}
''')

write(java/"WeatherApi.java",r'''
package com.sarf.qasioun.prayer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.json.JSONObject;

public final class WeatherApi {
    private WeatherApi() { }
    public static WeatherStore.State current(double lat,double lon) throws Exception {
        if (!Double.isFinite(lat) || !Double.isFinite(lon) ||
                lat < -90 || lat > 90 || lon < -180 || lon > 180)
            throw new IllegalArgumentException("invalid coordinates");
        String path = String.format(Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.5f&longitude=%.5f&current=temperature_2m,weather_code,is_day&timezone=auto",
                lat, lon);
        HttpURLConnection con=(HttpURLConnection)new URL(path).openConnection();
        con.setConnectTimeout(9000);
        con.setReadTimeout(9000);
        con.setRequestProperty("Accept","application/json");
        con.setRequestProperty("Accept-Encoding","identity");
        con.setRequestProperty("User-Agent","Qasioun-Syria-Weather/0.8 (Android)");
        try {
            if (con.getResponseCode()!=200)
                throw new IllegalStateException("Weather HTTP "+con.getResponseCode());
            try (InputStream in=con.getInputStream();
                 ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] buffer=new byte[4096];
                int n;
                while ((n=in.read(buffer))!=-1) {
                    out.write(buffer,0,n);
                    if (out.size()>128_000) throw new IllegalStateException("weather result too large");
                }
                JSONObject curr=new JSONObject(new String(out.toByteArray(), StandardCharsets.UTF_8))
                        .getJSONObject("current");
                double temp=curr.getDouble("temperature_2m");
                int code=curr.getInt("weather_code");
                int isDay=curr.getInt("is_day");
                if (!Double.isFinite(temp) || temp< -100 || temp>65 ||
                        code<0 || code>99 || (isDay!=0 && isDay!=1))
                    throw new IllegalStateException("invalid current conditions");
                return new WeatherStore.State(temp,code,isDay==1,System.currentTimeMillis());
            }
        } finally {
            con.disconnect();
        }
    }
}
''')

write(java/"WeatherRefreshJobService.java",r'''
package com.sarf.qasioun.prayer;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;

/** Weather refresh is independent from prayer alarms; an offline failure leaves cached weather intact. */
public final class WeatherRefreshJobService extends JobService {
    private static final int JOB_NOW=41881, JOB_HOURLY=41882;
    private volatile boolean cancelled;

    public static void start(Context c) {
        AppWidgetManager widgets=AppWidgetManager.getInstance(c);
        int[] ids=widgets.getAppWidgetIds(new ComponentName(c,ClockWeatherWidgetProvider.class));
        if (ids==null || ids.length==0 || !PrayerStore.hasLocation(c)) return;
        JobScheduler scheduler=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler==null) return;
        ComponentName component=new ComponentName(c,WeatherRefreshJobService.class);
        JobInfo recurring = new JobInfo.Builder(JOB_HOURLY, component)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true).setPeriodic(60*60_000L).build();
        scheduler.schedule(recurring);
        if (!WeatherStore.needsRefresh(c)) return;
        JobInfo immediate=new JobInfo.Builder(JOB_NOW, component)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setOverrideDeadline(75_000L).build();
        scheduler.schedule(immediate);
    }

    @Override public boolean onStartJob(JobParameters params) {
        cancelled=false;
        new Thread(() -> {
            try {
                Context c=getApplicationContext();
                if (PrayerStore.hasLocation(c) && WeatherStore.needsRefresh(c)) {
                    double lat=PrayerStore.lat(c), lon=PrayerStore.lon(c);
                    try {
                        WeatherStore.State state=WeatherApi.current(lat,lon);
                        WeatherStore.save(c,lat,lon,state);
                    } catch (Exception failure) {
                        // The screen will report a cached or unavailable reading, not an invented temperature.
                    }
                }
                ClockWeatherWidgetProvider.renderAll(c);
            } finally {
                if (!cancelled) jobFinished(params,false);
            }
        },"QasiounWeatherUpdate").start();
        return true;
    }
    @Override public boolean onStopJob(JobParameters params) {
        cancelled=true;
        return true;
    }
}
''')

write(java/"ClockWeatherWidgetProvider.java",r'''
package com.sarf.qasioun.prayer;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Tall 2x3 Damascus glass clock and live weather; position is chosen in the HONOR launcher. */
public final class ClockWeatherWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_REFRESH="com.sarf.qasioun.prayer.WEATHER_REFRESH";
    private static final Locale AR=new Locale("ar","SA");

    @Override public void onUpdate(Context c,AppWidgetManager manager,int[] ids) {
        renderAll(c);
        WeatherRefreshJobService.start(c);
    }
    @Override public void onEnabled(Context c) {
        renderAll(c);
        WeatherRefreshJobService.start(c);
    }
    @Override public void onReceive(Context c,Intent i) {
        super.onReceive(c,i);
        if (ACTION_REFRESH.equals(i.getAction())) {
            WeatherRefreshJobService.start(c);
            renderAll(c);
        }
    }

    private static String gregorian() {
        try {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy",AR));
        } catch(Exception ignored) { return "التاريخ الميلادي غير متاح"; }
    }
    private static String hijri(Context c) {
        PrayerEngine.Snapshot prayer=PrayerEngine.snapshot(c);
        if (prayer!=null && prayer.hijri()!=null && !prayer.hijri().isEmpty()) return prayer.hijri();
        try {
            HijrahDate h=HijrahDate.from(LocalDate.now());
            return h.format(DateTimeFormatter.ofPattern("d MMMM yyyy",AR))+" هـ";
        } catch(Exception ignored) { return "الهجري غير متاح"; }
    }

    public static void renderAll(Context c) {
        AppWidgetManager manager=AppWidgetManager.getInstance(c);
        int[] ids=manager.getAppWidgetIds(new ComponentName(c,ClockWeatherWidgetProvider.class));
        if (ids==null || ids.length==0) return;
        WeatherStore.State state=WeatherStore.get(c);
        long now=System.currentTimeMillis();
        for (int id : ids) {
            RemoteViews rv=new RemoteViews(c.getPackageName(),R.layout.clock_weather_widget);
            rv.setTextViewText(R.id.cw_city,PrayerStore.city(c));
            rv.setTextViewText(R.id.cw_gregorian,gregorian());
            rv.setTextViewText(R.id.cw_hijri,hijri(c));
            if (state==null) {
                rv.setTextViewText(R.id.cw_temperature,"--°");
                rv.setTextViewText(R.id.cw_weather_icon,"☼");
                rv.setTextViewText(R.id.cw_condition,"لا توجد بيانات");
                rv.setTextViewText(R.id.cw_status,
                    PrayerStore.hasLocation(c) ? "اتصل بالإنترنت للتحديث" : "فعّل الموقع من التطبيق");
            } else {
                rv.setTextViewText(R.id.cw_weather_icon,
                    WeatherCodes.glyph(state.weatherCode(),state.day()));
                rv.setTextViewText(R.id.cw_temperature,
                    Math.round(state.temperature())+"°");
                rv.setTextViewText(R.id.cw_condition,WeatherCodes.label(state.weatherCode()));
                long age=Math.max(0L,now-state.fetchedAt());
                rv.setTextViewText(R.id.cw_status,
                    age<95*60_000L ? "طقس حسب الموقع" : "بيانات محفوظة · اضغط للتحديث");
            }
            Intent launch=new Intent(c,MainActivity.class);
            PendingIntent open=PendingIntent.getActivity(c,41883,launch,
                    PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.cw_root,open);
            Intent req=new Intent(c,ClockWeatherWidgetProvider.class).setAction(ACTION_REFRESH);
            PendingIntent refresh=PendingIntent.getBroadcast(c,41884,req,
                    PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.cw_refresh,refresh);
            manager.updateAppWidget(id,rv);
        }
    }
}
''')

manifest=root/"app/src/main/AndroidManifest.xml"
xml=manifest.read_text(encoding="utf-8")
anchor='    </application>'
assert xml.count(anchor)==1
xml=xml.replace(anchor,'''        <receiver android:name=".ClockWeatherWidgetProvider"
            android:exported="true" android:label="ساعة وطقس قاسيون · طولي">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE"/>
                <action android:name="com.sarf.qasioun.prayer.WEATHER_REFRESH"/>
            </intent-filter>
            <meta-data android:name="android.appwidget.provider"
                android:resource="@xml/clock_weather_widget"/>
        </receiver>
        <service android:name=".WeatherRefreshJobService"
            android:permission="android.permission.BIND_JOB_SERVICE"
            android:exported="true"/>
'''+anchor)
manifest.write_text(xml,encoding="utf-8")

prayer_provider=java/"WidgetProvider.java"
src=prayer_provider.read_text(encoding="utf-8")
anchor='        scheduleTimelineChange(context, manager, snapshot);'
assert src.count(anchor)==1
prayer_provider.write_text(src.replace(anchor,anchor+'\n        ClockWeatherWidgetProvider.renderAll(context);'),encoding="utf-8")

refresh=java/"RefreshJobService.java"
src=refresh.read_text(encoding="utf-8")
anchor='        AlarmReceiver.scheduleAll(c);'
assert src.count(anchor)==1
refresh.write_text(src.replace(anchor,anchor+'\n        WeatherRefreshJobService.start(c);'),encoding="utf-8")

build=root/"app/build.gradle"
src=build.read_text(encoding="utf-8")
assert src.count("versionCode 7")==1
assert src.count("versionName '0.7.0-next-event-beta'")==1
src=src.replace("versionCode 7","versionCode 8")
src=src.replace("versionName '0.7.0-next-event-beta'","versionName '0.8.0-portrait-weather-beta'")
build.write_text(src,encoding="utf-8")

for xmlfile in (manifest,res/"layout/clock_weather_widget.xml",res/"xml/clock_weather_widget.xml",
                res/"drawable/clock_weather_background.xml"):
    ET.parse(xmlfile)
assert "TextClock" in (res/"layout/clock_weather_widget.xml").read_text()
assert "targetCellHeight=\"3\"" in (res/"xml/clock_weather_widget.xml").read_text()
print("PASS: v0.8 clock weather widget, prayer integration, XML, app version 8")
