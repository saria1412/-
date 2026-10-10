#!/usr/bin/env python3
"""v0.10: clock/weather order (temperature -> weather symbol -> condition), no footer."""
from pathlib import Path
from xml.etree import ElementTree as ET
import re
import sys

root=Path(sys.argv[1]).resolve()
layout_dir=root/"app/src/main/res/layout"
java_dir=root/"app/src/main/java/com/sarf/qasioun/prayer"
android="{http://schemas.android.com/apk/res/android}"

names=("cw_weather_icon","cw_temperature","cw_condition","cw_status")
block_regex=re.compile(r'    <TextView android:id="@\+id/(cw_weather_icon|cw_temperature|cw_condition|cw_status)"[\s\S]*?/>')

for fname in ("clock_weather_widget.xml","clock_weather_widget_day.xml"):
    path=layout_dir/fname
    source=path.read_text(encoding="utf-8")
    blocks=list(block_regex.finditer(source))
    if tuple(m.group(1) for m in blocks)!=names:
        raise AssertionError(f"{fname}: weather block order changed: {[m.group(1) for m in blocks]}")
    if any(source[a.end():b.start()].strip() for a,b in zip(blocks,blocks[1:])):
        raise AssertionError(f"{fname}: unexpected content between weather elements")
    found={m.group(1):m.group(0) for m in blocks}
    temperature=found["cw_temperature"]
    temperature=temperature.replace('android:layout_width="match_parent"',
                                    'android:layout_width="match_parent"\n'
                                    '        android:layout_marginTop="3dp"',1)
    temperature=temperature.replace('android:textSize="31sp"','android:textSize="33sp"')
    symbol=found["cw_weather_icon"]
    symbol=symbol.replace('android:layout_width="match_parent"',
                          'android:layout_width="match_parent"\n'
                          '        android:layout_marginTop="1dp"',1)
    symbol=symbol.replace('android:textSize="33sp"','android:textSize="29sp"')
    condition=found["cw_condition"]
    condition=condition.replace('android:layout_width="match_parent"',
                                'android:layout_width="match_parent"\n'
                                '        android:layout_marginTop="2dp"',1)
    updated=source[:blocks[0].start()]+"\n".join((temperature,symbol,condition))+source[blocks[-1].end():]
    path.write_text(updated,encoding="utf-8")
    root_element=ET.parse(path).getroot()
    ids=[e.attrib.get(android+"id") for e in root_element.iter()]
    selected=[v for v in ids if v in ('@+id/cw_temperature','@+id/cw_weather_icon',
                                        '@+id/cw_condition','@+id/cw_status')]
    if selected!=['@+id/cw_temperature','@+id/cw_weather_icon','@+id/cw_condition']:
        raise AssertionError(f"{fname}: incorrect new layout: {selected}")
    if root_element.attrib.get(android+"background")!='@android:color/transparent':
        raise AssertionError(f"{fname}: widget transparency lost")
    print("PASS",fname,": temperature -> weather symbol -> condition; no footer; transparent")

provider=java_dir/"ClockWeatherWidgetProvider.java"
source=provider.read_text(encoding="utf-8")
old_missing='''                rv.setTextViewText(R.id.cw_status,
                    PrayerStore.hasLocation(c) ? "اتصل بالإنترنت للتحديث" : "فعّل الموقع من التطبيق");
'''
old_cached='''                long age=Math.max(0L,now-state.fetchedAt());
                rv.setTextViewText(R.id.cw_status,
                    age<95*60_000L ? "طقس حسب الموقع" : "بيانات محفوظة · اضغط للتحديث");
'''
if source.count(old_missing)!=1 or source.count(old_cached)!=1:
    raise AssertionError("status control structure does not match v09")
source=source.replace(old_missing,"").replace(old_cached,"")
source=source.replace("        long now=System.currentTimeMillis();\n","")
if 'cw_status' in source or 'طقس حسب الموقع' in source:
    raise AssertionError("legacy weather footer was not completely removed")
for key in ("R.id.cw_temperature","R.id.cw_weather_icon","R.id.cw_condition",
            "PrayerVisualTheme.isDay","WeatherStore.get(c)"):
    if key not in source: raise AssertionError("lost weather update: "+key)
provider.write_text(source,encoding="utf-8")
print("PASS: weather updates remain live and widget footer is removed")

gradle=root/"app/build.gradle"
g=gradle.read_text(encoding="utf-8")
if g.count("versionCode 9")!=1 or g.count("versionName '0.9.0-solar-day-night-beta'")!=1:
    raise AssertionError("expected v0.9 source")
g=g.replace("versionCode 9","versionCode 10")
g=g.replace("versionName '0.9.0-solar-day-night-beta'",
            "versionName '0.10.0-weather-layout-beta'")
gradle.write_text(g,encoding="utf-8")
print("PASS: v0.10 weather-order beta build")
