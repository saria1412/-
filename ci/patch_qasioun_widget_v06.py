#!/usr/bin/env python3
"""Apply the reviewed V0.6 sunrise widget delta to the verified V0.5 tree."""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]).resolve()
layouts = root / "app/src/main/res/layout"
java = root / "app/src/main/java/com/sarf/qasioun/prayer/WidgetProvider.java"

def replace_once(content, old, new, label):
    matches = content.count(old)
    if matches != 1:
        raise RuntimeError(f"{label}: expected one match, got {matches}")
    return content.replace(old, new, 1)

sunrise_compact = '''
    <TextView
        android:id="@+id/widget_sunrise"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="1dp"
        android:layout_marginBottom="1dp"
        android:gravity="end"
        android:text="☀ الشروق --:--"
        android:textSize="9sp"
        android:textColor="@color/gold_soft"
        android:fontFamily="@font/noto_kufi_arabic_medium"
        android:singleLine="true"
        android:ellipsize="end"
        android:includeFontPadding="false" />
'''
sunrise_expanded = '''
    <TextView
        android:id="@+id/widget_sunrise"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="2dp"
        android:layout_marginBottom="2dp"
        android:gravity="end"
        android:text="☀ الشروق --:--"
        android:textSize="10sp"
        android:textColor="@color/gold_soft"
        android:fontFamily="@font/noto_kufi_arabic_medium"
        android:singleLine="true"
        android:ellipsize="end"
        android:includeFontPadding="false" />
'''

compact_path = layouts / "prayer_widget_compact.xml"
compact = compact_path.read_text(encoding="utf-8")
compact_marker = '''    </LinearLayout>

    <LinearLayout android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:baselineAligned="false"
        android:layout_marginTop="1dp">'''
compact = replace_once(compact, compact_marker,
    "    </LinearLayout>\n" + sunrise_compact + "\n" +
    '''    <LinearLayout android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:baselineAligned="false"
        android:layout_marginTop="1dp">''', "compact sunrise insertion")
compact = replace_once(compact, 'android:paddingTop="5dp"', 'android:paddingTop="3dp"', "compact padding top")
compact = replace_once(compact, 'android:paddingBottom="5dp"', 'android:paddingBottom="3dp"', "compact padding bottom")
compact = replace_once(compact, 'android:layout_width="25dp" android:layout_height="25dp"',
                      'android:layout_width="19dp" android:layout_height="19dp"', "compact refresh height")
compact = replace_once(compact, 'android:textSize="20sp" android:textColor="@color/gold"',
                      'android:textSize="17sp" android:textColor="@color/gold"', "compact refresh font")
compact = replace_once(compact, 'android:text="--:--" android:textSize="20sp"',
                      'android:text="--:--" android:textSize="18sp"', "compact timer font")
compact_path.write_text(compact, encoding="utf-8")

expanded_path = layouts / "prayer_widget.xml"
expanded = expanded_path.read_text(encoding="utf-8")
expanded_marker = '''    </LinearLayout>
    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:layout_marginTop="4dp" android:layout_marginBottom="5dp"'''
expanded = replace_once(expanded, expanded_marker,
    "    </LinearLayout>\n" + sunrise_expanded +
    '''    <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
        android:layout_marginTop="2dp" android:layout_marginBottom="4dp"''', "expanded sunrise insertion")
expanded = replace_once(expanded, 'android:paddingTop="8dp" android:paddingBottom="8dp"',
                        'android:paddingTop="6dp" android:paddingBottom="6dp"', "expanded padding")
expanded_path.write_text(expanded, encoding="utf-8")

provider = java.read_text(encoding="utf-8")
provider = replace_once(provider,
    'rv.setTextViewText(R.id.widget_hijri, snapshot.hijri());',
    'rv.setTextViewText(R.id.widget_hijri, snapshot.hijri());\n'
    '                rv.setTextViewText(R.id.widget_sunrise,\n'
    '                        "☀ الشروق " + PrayerEngine.format(snapshot.sunrise(), snapshot.tz()));',
    "live sunrise binding")
provider = replace_once(provider,
    'rv.setTextViewText(R.id.widget_hijri, "تقويم أم القرى");',
    'rv.setTextViewText(R.id.widget_hijri, "تقويم أم القرى");\n'
    '                rv.setTextViewText(R.id.widget_sunrise, "☀ الشروق --:--");',
    "offline sunrise placeholder")
java.write_text(provider, encoding="utf-8")

gradle_path = root / "app/build.gradle"
gradle = gradle_path.read_text(encoding="utf-8")
gradle = replace_once(gradle, "versionCode 5", "versionCode 6", "version code")
gradle = replace_once(gradle, "versionName '0.5.0-widget-layout-beta'",
                      "versionName '0.6.0-sunrise-beta'", "version name")
gradle_path.write_text(gradle, encoding="utf-8")

android = '{http://schemas.android.com/apk/res/android}'
for fname in ('prayer_widget_compact.xml', 'prayer_widget.xml'):
    view = ET.parse(layouts / fname)
    id_counts = [node.attrib.get(android + 'id')
                 for node in view.iter()].count('@+id/widget_sunrise')
    assert id_counts == 1, (fname, id_counts)
assert provider.count('R.id.widget_sunrise') == 2
assert 'snapshot.sunrise()' in provider
assert 'format(snapshot.sunrise(), snapshot.tz())' in provider
assert 'if (expanded)' in provider
assert 'R.layout.prayer_widget_compact, false' in provider
print('PASS: v0.6 both widget layouts, live Umm Al-Qura sunrise, fallback, compact height')

# Supply accurate v0.6 launcher previews; never present sample timings as live data.
from PIL import Image, ImageDraw, ImageFont
fonts = root / "app/src/main/res/font"
FONT_NORMAL = fonts / "noto_kufi_arabic_regular.ttf"
FONT_MEDIUM = fonts / "noto_kufi_arabic_medium.ttf"
FONT_BOLD = fonts / "noto_kufi_arabic_bold.ttf"
WHITE, GOLD, MUTED = "#F5F1E4", "#E6C98E", "#D6DBD1"

def f(path, size):
    return ImageFont.truetype(str(path), size)

def draw_text(draw, xy, text, font, color, anchor="ra", rtl=True):
    args = dict(font=font, fill=color, anchor=anchor)
    if rtl:
        args["direction"] = "rtl"
    draw.text(xy, text, **args)

def render_preview(w, h, expanded_widget, outfile):
    im = Image.new("RGB", (w, h), "#132F2A")
    draw = ImageDraw.Draw(im)
    draw.rounded_rectangle((5, 5, w - 5, h - 5), radius=58,
                           fill="#104139", outline="#B9A675", width=4)
    draw_text(draw, (w-35, 46), "جدة", f(FONT_BOLD, 35), GOLD)
    draw_text(draw, (70, 48), "28 ربيع الثاني 1448 هـ", f(FONT_NORMAL, 25), MUTED,
              anchor="la")
    draw.line((35, 74, w-35, 74), fill="#607867", width=2)
    draw_text(draw, (w-36, 118), "☀ الشروق 06:17 ص",
              f(FONT_MEDIUM, 29), GOLD)
    if expanded_widget:
        draw.rounded_rectangle((32, 139, w-32, 262), radius=28,
                               fill="#155044", outline="#867B56", width=2)
        draw_text(draw, (w-55, 200), "الظهر القادم", f(FONT_BOLD, 36), WHITE)
        draw_text(draw, (w-55, 241), "12:10 م", f(FONT_NORMAL, 26), MUTED)
        draw_text(draw, (65, 183), "متبقي على الأذان", f(FONT_NORMAL, 21), MUTED,
                  anchor="la")
        draw.text((65, 203), "6:27:48",
                  font=f("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 45),
                  fill=GOLD)
        names = ["الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"]
        times = ["05:01", "06:17", "12:10", "03:33", "06:04", "07:34"]
        for i,(name,time) in enumerate(zip(names,times)):
            row, col = divmod(i,3)
            x = w - 160 - col*280
            y = 300 + row*79
            draw_text(draw, (x, y), name, f(FONT_MEDIUM, 24),
                      GOLD if name == "الشروق" else WHITE)
            draw.text((x-48, y+13), time,
                      font=f("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 24),
                      fill=GOLD if name == "الشروق" else MUTED, anchor="mm")
    else:
        draw_text(draw, (w-36, 190), "الظهر القادم", f(FONT_BOLD, 36), WHITE)
        draw_text(draw, (w-36, 230), "12:10 م", f(FONT_NORMAL, 25), MUTED)
        draw_text(draw, (55, 173), "متبقي على الأذان", f(FONT_NORMAL, 20), MUTED,
                  anchor="la")
        draw.text((55, 191), "6:27:48",
                  font=f("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 40),
                  fill=GOLD)
    im.save(outfile, optimize=True)

preview_dir = root / "app/src/main/res/drawable-nodpi"
render_preview(900, 256, False, preview_dir / "widget_preview_compact.png")
render_preview(900, 475, True, preview_dir / "widget_preview_expanded.png")
print('PASS: regenerated distinct v0.6 preview images')
