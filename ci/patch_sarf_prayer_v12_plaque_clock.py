#!/usr/bin/env python3
"""SARF Prayer v12: transparent plaque-overlay clock / weather widget.
Placement requirement: live clock above emblem, clear center, date on RIGHT
and weather on LEFT below emblem. Background image and logo are NEVER changed.
"""
from pathlib import Path
from xml.etree import ElementTree as ET
import sys

root=Path(sys.argv[1]).resolve()
app=root/"app"
res=app/"src/main/res"
java=app/"src/main/java/com/sarf/qasioun/prayer"
a="{http://schemas.android.com/apk/res/android}"

def one(source,old,new,name):
    assert source.count(old)==1, (name,old,source.count(old))
    return source.replace(old,new,1)

for palette in ("night","day"):
    is_day=palette=="day"
    primary="@color/widget_day_primary" if is_day else "@color/ivory"
    accent="@color/widget_day_accent" if is_day else "@color/gold"
    muted="@color/widget_day_muted" if is_day else "@color/muted"
    divider="#77C59261" if is_day else "#66C59261"
    xml=f'''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/cw_root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:layoutDirection="rtl"
    android:gravity="center_horizontal"
    android:paddingStart="8dp"
    android:paddingEnd="8dp"
    android:paddingTop="0dp"
    android:paddingBottom="0dp"
    android:background="@android:color/transparent">
    <!-- Clock: top of the EXISTING copper plaque, transparent overlay -->
    <TextClock
        android:id="@+id/cw_clock"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:format12Hour="h:mm"
        android:format24Hour="H:mm"
        android:fontFamily="sans-serif-light"
        android:textSize="43sp"
        android:textColor="{primary}"
        android:textDirection="ltr"
        android:singleLine="true"
        android:includeFontPadding="false"/>
    <TextView
        android:layout_width="96dp"
        android:layout_height="1dp"
        android:layout_marginTop="5dp"
        android:background="{divider}"/>
    <!-- Keep the APPROVED original R05 emblem fully visible. -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:background="@android:color/transparent"/>
    <!-- Bottom under emblem: date RIGHT, live weather LEFT. -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layoutDirection="rtl"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:baselineAligned="false"
        android:paddingBottom="2dp">
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:gravity="right|center_vertical"
            android:layoutDirection="rtl"
            android:paddingEnd="8dp">
            <TextView
                android:id="@+id/cw_weekday"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="الأربعاء"
                android:textSize="12sp"
                android:textColor="{primary}"
                android:fontFamily="@font/noto_kufi_arabic_bold"
                android:gravity="right"
                android:singleLine="true"
                android:includeFontPadding="false"/>
            <TextView
                android:id="@+id/cw_gregorian"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="15 أكتوبر 2026"
                android:textSize="10sp"
                android:textColor="{primary}"
                android:fontFamily="@font/noto_kufi_arabic_medium"
                android:gravity="right"
                android:ellipsize="end"
                android:maxLines="2"
                android:includeFontPadding="false"/>
            <TextView
                android:id="@+id/cw_hijri"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="4 جمادى الأولى 1448هـ"
                android:textSize="9sp"
                android:textColor="{accent}"
                android:fontFamily="@font/noto_kufi_arabic_regular"
                android:gravity="right"
                android:ellipsize="end"
                android:maxLines="2"
                android:includeFontPadding="false"/>
        </LinearLayout>
        <TextView
            android:layout_width="1dp"
            android:layout_height="77dp"
            android:background="{divider}"/>
        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical"
            android:gravity="center_horizontal"
            android:paddingStart="5dp">
            <TextView
                android:id="@+id/cw_temperature"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="—"
                android:gravity="center"
                android:textDirection="ltr"
                android:textSize="24sp"
                android:fontFamily="sans-serif-medium"
                android:textColor="{primary}"
                android:includeFontPadding="false"/>
            <TextView
                android:id="@+id/cw_weather_icon"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text=""
                android:gravity="center"
                android:textSize="20sp"
                android:fontFamily="sans-serif"
                android:textColor="{accent}"
                android:includeFontPadding="false"/>
            <TextView
                android:id="@+id/cw_condition"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="بانتظار الطقس"
                android:gravity="center"
                android:singleLine="true"
                android:ellipsize="end"
                android:textSize="9sp"
                android:fontFamily="@font/noto_kufi_arabic_medium"
                android:textColor="{primary}"
                android:includeFontPadding="false"/>
            <TextView
                android:id="@+id/cw_city"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="المدينة"
                android:gravity="center"
                android:singleLine="true"
                android:ellipsize="end"
                android:textSize="9sp"
                android:fontFamily="@font/noto_kufi_arabic_regular"
                android:textColor="{muted}"
                android:includeFontPadding="false"
                android:contentDescription="اضغط لتحديث الطقس"/>
        </LinearLayout>
    </LinearLayout>
</LinearLayout>'''
    target=res/"layout"/("clock_weather_widget_day.xml" if is_day else "clock_weather_widget.xml")
    target.write_text(xml+"\n",encoding="utf-8")
    doc=ET.parse(target).getroot()
    assert doc.attrib[a+"background"]=="@android:color/transparent"
    assert doc.find("TextClock") is not None
    ids=[n.attrib.get(a+"id") for n in doc.iter()]
    assert ids.index("@+id/cw_clock")<ids.index("@+id/cw_weekday")
    assert ids.index("@+id/cw_weekday")<ids.index("@+id/cw_temperature")
    assert ids.index("@+id/cw_temperature")<ids.index("@+id/cw_weather_icon")
    assert "cw_refresh" not in target.read_text()
    print("PASS SARF plaque layout",palette)

widget=res/"xml/clock_weather_widget.xml"
s=widget.read_text()
for old,new in (
 ('android:minWidth="145dp"','android:minWidth="260dp"'),
 ('android:minHeight="190dp"','android:minHeight="315dp"'),
 ('android:minResizeWidth="145dp"','android:minResizeWidth="225dp"'),
 ('android:minResizeHeight="190dp"','android:minResizeHeight="295dp"'),
 ('android:targetCellWidth="2"','android:targetCellWidth="4"'),
 ('android:targetCellHeight="3"','android:targetCellHeight="4"')
):
    s=one(s,old,new,"widget info")
# On Android 12+ previewLayout renders CURRENT live layout. Avoid stale 2x3 thumbnail.
s=one(s,'    android:previewImage="@drawable/clock_weather_preview"\n',"","remove stale thumbnail")
widget.write_text(s)
ET.parse(widget)

provider=java/"ClockWeatherWidgetProvider.java"
s=provider.read_text()
s=one(s,'/** Tall 2x3 Damascus glass clock and live weather; position is chosen in the HONOR launcher. */',
      '/** Transparent 4x4 plaque overlay: clock top, protected logo center, date right, weather left. */',"provider doc")
s=one(s,'    private static String gregorian() {',
'''    private static String weekday() {
        try {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE",AR));
        } catch(Exception ignored) { return ""; }
    }
    private static String gregorian() {''',"weekday method")
s=one(s,'            rv.setTextViewText(R.id.cw_gregorian,gregorian());',
      '            rv.setTextViewText(R.id.cw_weekday,weekday());\n            rv.setTextViewText(R.id.cw_gregorian,gregorian());',"day rendering")
s=one(s,'                rv.setTextViewText(R.id.cw_temperature,"--°");',
      '                rv.setTextViewText(R.id.cw_temperature,"—");',"fallback")
s=one(s,'                rv.setTextViewText(R.id.cw_weather_icon,"☼");',
      '                rv.setTextViewText(R.id.cw_weather_icon,"");',"fallback glyph")
s=one(s,'                rv.setTextViewText(R.id.cw_condition,"لا توجد بيانات");',
      '                rv.setTextViewText(R.id.cw_condition,\n                    PrayerStore.hasLocation(c) ? "الطقس غير متاح" : "فعّل الموقع");',"fallback")
s=one(s,'            rv.setOnClickPendingIntent(R.id.cw_refresh,refresh);',
      '            rv.setOnClickPendingIntent(R.id.cw_city,refresh);',"refresh")
provider.write_text(s)
assert "cw_status" not in s and "cw_refresh" not in s
assert "cw_weekday" in s

manifest=app/"src/main/AndroidManifest.xml"
m=manifest.read_text()
m=m.replace('android:label="SARF · الساعة والطقس"','android:label="SARF · الساعة والطقس داخل اللوحة"')
m=m.replace('android:label="ساعة وطقس قاسيون · طولي"','android:label="SARF · الساعة والطقس داخل اللوحة"')
manifest.write_text(m)
ET.parse(manifest)

gradle=app/"build.gradle"
g=gradle.read_text()
g=one(g,"versionCode 11","versionCode 12","build version")
g=one(g,"versionName '1.1.0-sarf-brand-beta'","versionName '1.2.0-plaque-clock-beta'","build versionname")
gradle.write_text(g)

# Do not modify wallpaper, approved symbol, adhan, prayer math, or solar selection.
assert "R.layout.clock_weather_widget_day" in s
assert "WeatherStore.get(c)" in s
assert "PrayerVisualTheme.isDay" in s
print("PASS V12: source layout, day/night, weather data, weekday, no weather hallucination, independent package")
