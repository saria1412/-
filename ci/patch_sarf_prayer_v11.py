#!/usr/bin/env python3
"""Branding-only transformation of the verified Mawaqit v0.10 Android project.
Runs AFTER all v0.10 source patches; retains prayer calculations, Adhan,
Qibla, weather, widget scheduling and day/night behavior.
"""
from pathlib import Path
from xml.etree import ElementTree as ET
import re
import sys
from PIL import Image, ImageDraw

root=Path(sys.argv[1]).resolve()
app=root/"app"
res=app/"src/main/res"
java=app/"src/main/java/com/sarf/qasioun/prayer"
assert (java/"ClockWeatherWidgetProvider.java").exists(), "v10 clock-weather source is absent"
assert (res/"layout/clock_weather_widget_day.xml").exists(), "daytime widget absent"

gradle=app/"build.gradle"
s=gradle.read_text()
for old,new in (
    ("applicationId 'com.sarf.qasioun.prayer'","applicationId 'com.sarf.engineering.prayer'"),
    ("versionCode 10","versionCode 11"),
    ("versionName '0.10.0-weather-layout-beta'","versionName '1.1.0-sarf-brand-beta'"),
):
    assert old in s,("Gradle input changed",old)
    s=s.replace(old,new)
gradle.write_text(s)

manifest=app/"src/main/AndroidManifest.xml"
s=manifest.read_text()
s=s.replace('android:label="مواقيت الصلاة"','android:label="مواقيت الصلاة | SARF"')
s=s.replace('android:label="مواقيت الصلاة · موسع"','android:label="SARF · مواقيت الصلاة الموسعة"')
s=s.replace('android:label="مواقيت الصلاة · مختصر"','android:label="SARF · مواقيت الصلاة المختصرة"')
s=s.replace('android:label="الساعة والطقس"','android:label="SARF · الساعة والطقس"')
manifest.write_text(s)

colors=res/"values/colors.xml"
s=colors.read_text()
palette={
    "damascus_green":"#191E25",
    "green2":"#111720",
    "green_deep":"#0E1217",
    "gold":"#C59261",
    "gold_soft":"#DAB594",
    "ivory":"#F3F2EF",
    "muted":"#B8C0C8",
    "syrian_red":"#647587",
    "widget_day_primary":"#191E25",
    "widget_day_muted":"#516273",
    "widget_day_accent":"#6F482B",
    "widget_day_cream":"#FCFBF9"
}
for name,hx in palette.items():
    rx=rf'(<color name="{re.escape(name)}">)#[0-9A-Fa-f]{{6}}(</color>)'
    s,num=re.subn(rx,rf'\g<1>{hx}\g<2>',s)
    assert num==1, f"Missing palette {name}: {num}"
colors.write_text(s)

# Source-proven icon: new mosque geometry only; NOT a substitute for the
# approved SARF logo, which is deliberately not redrawn here.
side=432
pic=Image.new("RGB",(side,side),(25,30,37))
d=ImageDraw.Draw(pic)
d.rounded_rectangle((13,13,418,418),radius=83,outline=(197,146,97),width=8)
d.rounded_rectangle((28,28,404,404),radius=73,fill=(17,23,32))
# architectural line rhythm / steel accent
for yy in (304,314,324):
    d.line((58,yy,373,yy),fill=(100,117,135),width=2)
# Base and mosque colonnade: bronze/copper
d.rounded_rectangle((72,270,358,309),radius=9,fill=(197,146,97))
d.rectangle((83,249,346,275),fill=(243,242,239))
for xx in (94,147,200,253,306):
    d.arc((xx,246,xx+36,286),180,360,fill=(25,30,37),width=9)
    d.rectangle((xx+4,268,xx+32,286),fill=(25,30,37))
# central dome and drum
d.rectangle((178,183,252,245),fill=(243,242,239))
d.pieslice((155,138,275,239),180,360,fill=(197,146,97))
d.line((215,128,215,145),fill=(243,242,239),width=6)
d.ellipse((209,121,221,133),fill=(243,242,239))
# twin slender minarets
for x in (104,301):
    d.rectangle((x,134,x+26,250),fill=(243,242,239))
    d.rectangle((x-5,161,x+31,171),fill=(197,146,97))
    d.polygon([(x-3,134),(x+13,108),(x+29,134)],fill=(197,146,97))
    d.line((x+13,97,x+13,109),fill=(243,242,239),width=4)
# precise base copper hairline
d.line((78,338,353,338),fill=(197,146,97),width=4)
icon=res/"mipmap-xxxhdpi/ic_launcher.webp"
pic.save(icon,format="WEBP",quality=95,method=6)

# Preserve the existing automatic solar switch. Update hardcoded V09 colors.
hexmap={
    "0xFF916324":"0xFF6F482B",
    "0xFF0A352D":"0xFF191E25",
    "0xFF48645A":"0xFF516273",
    "0xFFF6F0E4":"0xFFF3F2EF",
    "0xFFF9F4EA":"0xFFFCFBF9",
    "0xFFEADDC8":"0xFFDEE4E9",
    "0xFF08352C":"0xFF191E25",
    "0xFF11483C":"0xFF354856",
    "0xFFE6C98E":"0xFFC59261",
    "0xFFB8D0C4":"0xFFB8C0C8"
}
for name in ("MainActivity.java","QiblaActivity.java"):
    p=java/name
    s=p.read_text()
    assert "applyVisualPalette" in s if name=="MainActivity.java" else "applyCompassPalette" in s
    for old,new in hexmap.items():s=s.replace(old,new)
    s=s.replace("Color.rgb(7, 40, 33)","Color.rgb(25, 30, 37)")
    s=s.replace("Color.rgb(13, 67, 56)","Color.rgb(53, 72, 86)")
    s=s.replace("Color.argb(85, 212, 183, 117)","Color.argb(85, 197, 146, 97)")
    s=s.replace("Color.argb(130,212,183,117)","Color.argb(130,197,146,97)")
    s=s.replace("Color.argb(35, 10, 53, 45)","Color.argb(35, 25, 30, 37)")
    if name=="MainActivity.java":
        s=s.replace('text("★ ★ ★", 10, getColor(R.color.syrian_red), true)',
                    'text("SARF", 10, gold, true)')
        s=s.replace("رمز الجامع الأموي بدمشق","أيقونة مسجد بطابع SARF")
    p.write_text(s)

# Transparent clock/forecast stays transparent; only typography and accent adapt.
for name in ("clock_weather_widget.xml","clock_weather_widget_day.xml",
             "prayer_widget.xml","prayer_widget_day.xml",
             "prayer_widget_compact.xml","prayer_widget_compact_day.xml"):
    p=res/"layout"/name
    s=p.read_text()
    s=s.replace("#55E6C98E","#55C59261").replace("#703C594D","#70647587")
    s=s.replace("#66C8AF83","#66C59261").replace("#35E6C98E","#35C59261")
    p.write_text(s)

drawables={
  "widget_background.xml":("""<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#D91A1E25"/><corners android:radius="22dp"/>
    <stroke android:width="1dp" android:color="#80647587"/>
</shape>"""),
  "widget_background_day.xml":("""<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#D9F3F2EF"/><corners android:radius="22dp"/>
    <stroke android:width="1dp" android:color="#A9C59261"/>
</shape>"""),
  "widget_next_panel.xml":("""<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#55354856"/><corners android:radius="12dp"/>
    <stroke android:width="0.5dp" android:color="#80647587"/>
</shape>"""),
  "widget_next_panel_day.xml":("""<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#B3DEE4E9"/><corners android:radius="12dp"/>
    <stroke android:width="0.5dp" android:color="#80C59261"/>
</shape>""")
}
for name,body in drawables.items():
    (res/"drawable"/name).write_text('<?xml version="1.0" encoding="utf-8"?>\n'+body+'\n')

for p in list(res.rglob("*.xml"))+[manifest]:
    ET.parse(p)
assert "com.sarf.engineering.prayer" in gradle.read_text()
assert 'android:background="@android:color/transparent"' in (res/"layout/clock_weather_widget.xml").read_text()
assert 'android:background="@android:color/transparent"' in (res/"layout/clock_weather_widget_day.xml").read_text()
assert "R.layout.clock_weather_widget_day" in (java/"ClockWeatherWidgetProvider.java").read_text()
assert "WeatherStore.get(c)" in (java/"ClockWeatherWidgetProvider.java").read_text()
assert "PrayerVisualTheme" in (java/"WidgetProvider.java").read_text()
print("SARF V11 BRAND PATCH PASS: independent appId; day/night; icon; clock+weather; prayer widgets")
