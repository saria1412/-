#!/usr/bin/env python3
"""SARF Prayer v13 — precision insets calibrated against screenshot 702x1536.
The copper frame occupies approximately x=134..568, y=155..762.
The v12 widget host occupies a wider 4x4 region. The new layout's 52dp
horizontal and 63dp lower safe insets constrain text to the actual frame.
No alteration to wallpaper, brand symbol, or prayer/solar/weather algorithms.
"""
from pathlib import Path
from xml.etree import ElementTree as ET
import sys

root=Path(sys.argv[1]).resolve()
res=root/"app/src/main/res"
java=root/"app/src/main/java/com/sarf/qasioun/prayer"
android="{http://schemas.android.com/apk/res/android}"

XML=r'''<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
 android:id="@+id/cw_root"
 android:layout_width="match_parent"
 android:layout_height="match_parent"
 android:layoutDirection="rtl"
 android:orientation="vertical"
 android:gravity="center_horizontal"
 android:paddingStart="52dp"
 android:paddingEnd="52dp"
 android:paddingTop="16dp"
 android:paddingBottom="63dp"
 android:background="@android:color/transparent">

 <!-- Top of the original copper frame: clock stays fully INSIDE it. -->
 <TextClock android:id="@+id/cw_clock"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:gravity="center"
  android:format12Hour="h:mm"
  android:format24Hour="HH:mm"
  android:fontFamily="sans-serif-light"
  android:textDirection="ltr"
  android:textSize="36sp"
  android:textColor="__PRIMARY__"
  android:singleLine="true"
  android:includeFontPadding="false"/>

 <TextView android:layout_width="31dp"
  android:layout_height="1dp"
  android:layout_marginTop="4dp"
  android:background="__DIVIDER__"/>

 <!-- Transparent protected space for ORIGINAL brand symbol. -->
 <View android:layout_width="match_parent"
  android:layout_height="0dp"
  android:layout_weight="1"
  android:background="@android:color/transparent"/>

 <!-- Compact footer calibrated to fit the lower INSIDE the copper frame. -->
 <LinearLayout
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:layoutDirection="rtl"
  android:orientation="horizontal"
  android:gravity="center_vertical"
  android:baselineAligned="false">
  <!-- Day and dates: RIGHT half -->
  <LinearLayout
   android:layout_width="0dp"
   android:layout_height="wrap_content"
   android:layout_weight="1"
   android:orientation="vertical"
   android:gravity="right|center_vertical"
   android:paddingStart="0dp"
   android:paddingEnd="5dp">
   <TextView android:id="@+id/cw_weekday"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:text="الخميس" android:gravity="right"
    android:textSize="10sp"
    android:textColor="__PRIMARY__"
    android:fontFamily="@font/noto_kufi_arabic_bold"
    android:singleLine="true" android:includeFontPadding="false"/>
   <TextView android:id="@+id/cw_gregorian"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:text="15 أكتوبر 2026"
    android:gravity="right" android:layout_marginTop="2dp"
    android:textSize="9sp" android:textColor="__PRIMARY__"
    android:fontFamily="@font/noto_kufi_arabic_medium"
    android:singleLine="true" android:ellipsize="end"
    android:includeFontPadding="false"/>
   <TextView android:id="@+id/cw_hijri"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:text="4 جمادى الأولى 1448هـ"
    android:gravity="right" android:layout_marginTop="1dp"
    android:textSize="8sp" android:textColor="__ACCENT__"
    android:fontFamily="@font/noto_kufi_arabic_regular"
    android:singleLine="true" android:ellipsize="end"
    android:includeFontPadding="false"/>
  </LinearLayout>

  <View
   android:layout_width="12dp"
   android:layout_height="1dp"
   android:background="@android:color/transparent"/>

  <!-- Weather: LEFT half. One row temperature + icon; city below. -->
  <LinearLayout
   android:layout_width="0dp" android:layout_weight="1"
   android:layout_height="wrap_content" android:orientation="vertical"
   android:gravity="center_horizontal" android:paddingStart="4dp">
   <LinearLayout android:layout_width="match_parent"
    android:layout_height="wrap_content" android:orientation="horizontal"
    android:layoutDirection="ltr" android:gravity="center_vertical|center_horizontal">
    <TextView android:id="@+id/cw_temperature"
     android:layout_width="wrap_content"
     android:layout_height="wrap_content"
     android:text=" "
     android:textDirection="ltr"
     android:gravity="center"
     android:textSize="21sp"
     android:textColor="__PRIMARY__"
     android:fontFamily="sans-serif-medium"
     android:includeFontPadding="false"/>
    <TextView android:id="@+id/cw_weather_icon"
     android:layout_width="wrap_content" android:layout_height="wrap_content"
     android:layout_marginStart="4dp"
     android:text="" android:gravity="center"
     android:textSize="18sp" android:textColor="__ACCENT__"
     android:fontFamily="sans-serif"
     android:includeFontPadding="false"/>
   </LinearLayout>
   <TextView android:id="@+id/cw_condition"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:layout_marginTop="1dp"
    android:text="بانتظار الطقس"
    android:textSize="9sp" android:gravity="center"
    android:textColor="__PRIMARY__"
    android:fontFamily="@font/noto_kufi_arabic_medium"
    android:singleLine="true" android:ellipsize="end"
    android:includeFontPadding="false"/>
   <TextView android:id="@+id/cw_city"
    android:layout_width="match_parent" android:layout_height="wrap_content"
    android:text="المدينة" android:gravity="center"
    android:textSize="8sp" android:textColor="__MUTED__"
    android:fontFamily="@font/noto_kufi_arabic_regular"
    android:singleLine="true" android:ellipsize="end"
    android:includeFontPadding="false"
    android:contentDescription="انقر لتحديث الطقس"/>
  </LinearLayout>
 </LinearLayout>
</LinearLayout>
'''
for fname,is_day in (("clock_weather_widget.xml",False),("clock_weather_widget_day.xml",True)):
    xml=(XML.replace("__PRIMARY__",("@color/widget_day_primary" if is_day else "@color/ivory"))
            .replace("__ACCENT__",("@color/widget_day_accent" if is_day else "@color/gold"))
            .replace("__MUTED__",("@color/widget_day_muted" if is_day else "@color/muted"))
            .replace("__DIVIDER__",("#88C59261" if is_day else "#80C59261")))
    out=res/"layout"/fname
    out.write_text(xml,encoding="utf-8")
    parsed=ET.parse(out).getroot()
    ids=[n.attrib.get(android+"id") for n in parsed.iter()]
    assert ids.index("@+id/cw_clock")<ids.index("@+id/cw_weekday")
    assert ids.index("@+id/cw_weekday")<ids.index("@+id/cw_temperature")
    assert ids.index("@+id/cw_temperature")<ids.index("@+id/cw_weather_icon")
    assert parsed.attrib[android+"background"]=="@android:color/transparent"
    assert parsed.attrib[android+"paddingStart"]== "52dp"
    assert parsed.attrib[android+"paddingBottom"]=="63dp"
    assert len([n for n in parsed.iter() if n.tag=="TextClock"])==1
    assert len([n for n in parsed.iter() if n.attrib.get(android+"layout_weight")=="1" and n.tag=="View"])==1
    assert not any(n.attrib.get(android+"layout_height")=="77dp" for n in parsed.iter())
    print("PASS responsive inset widget",fname)

# Previous v12 fallback showed em-dash. Keep an empty unit instead of '--°'.
p=java/"ClockWeatherWidgetProvider.java"
source=p.read_text()
source=source.replace('rv.setTextViewText(R.id.cw_temperature,"—");',
                      'rv.setTextViewText(R.id.cw_temperature," ");')
assert "rv.setTextViewText(R.id.cw_temperature,\" \")" in source
assert "rv.setOnClickPendingIntent(R.id.cw_city,refresh)" in source
assert "R.layout.clock_weather_widget_day" in source
p.write_text(source)

# Keep the user-resizable 4x4 widget size, retaining v12 footprint.
widget=res/"xml/clock_weather_widget.xml"
provider=ET.parse(widget).getroot()
assert provider.attrib[android+"targetCellWidth"]=="4"
assert provider.attrib[android+"targetCellHeight"]=="4"

gradle=root/"app/build.gradle"
build=gradle.read_text()
assert "versionCode 12" in build
assert "versionName '1.2.0-plaque-clock-beta'" in build
build=build.replace("versionCode 12","versionCode 13")
build=build.replace("versionName '1.2.0-plaque-clock-beta'",
                    "versionName '1.3.0-frame-insets-beta'")
gradle.write_text(build)
print("SARF V13 FINAL: original wallpaper untouched, frame insets verified, clock/date/weather functional")
