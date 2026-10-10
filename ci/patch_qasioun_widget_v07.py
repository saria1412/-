#!/usr/bin/env python3
"""V07: show sunrise only when it is the next chronological event, never as an adhan."""
import re
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(sys.argv[1]).resolve()
layouts = root / "app/src/main/res/layout"
main = root / "app/src/main/java/com/sarf/qasioun/prayer"
a = '{http://schemas.android.com/apk/res/android}'

def exact(src, old, new, label):
    if src.count(old) != 1:
        raise AssertionError(f"{label}: expected one match, got {src.count(old)}")
    return src.replace(old, new, 1)

for xmlname in ("prayer_widget_compact.xml", "prayer_widget.xml"):
    p = layouts / xmlname
    data = p.read_text(encoding="utf-8")
    # Remove the permanent sunrise strip added by v0.6; the expanded timetable retains sunrise.
    data, n = re.subn(
        r'\n[ \t]*<TextView\b(?=[^>]*\bandroid:id="@\+id/widget_sunrise")[^>]*/>[ \t]*\n',
        '\n', data, count=2)
    if n != 1:
        raise AssertionError(f"{xmlname}: removable sunrise row count={n}")
    # Give the remaining label an ID so it changes from "الأذان" to "الشروق" dynamically.
    choices = ['android:text="متبقي على الأذان"', 'android:text="المتبقي للأذان"']
    located = [(x,data.find(x)) for x in choices if data.find(x)>=0]
    if len(located)!=1:
        raise AssertionError(f"{xmlname}: timer label not unique")
    _, pos = located[0]
    start = data.rfind("<TextView", 0, pos)
    if start<0 or '<' in data[start+9:pos]:
        raise AssertionError(f"{xmlname}: unsafe TextView insertion")
    data = data[:start]+data[start:].replace('<TextView','<TextView android:id="@+id/widget_countdown_label"',1)
    data = data.replace(choices[0], 'android:text="المتبقي للأذان"')
    p.write_text(data, encoding="utf-8")
    tree=ET.parse(p)
    ids=[n.attrib.get(a+'id') for n in tree.iter()]
    assert '@+id/widget_sunrise' not in ids
    assert ids.count('@+id/widget_countdown_label')==1
    assert '@+id/widget_next' in ids
    print("PASS layout", xmlname)

math_file = main / "PrayerMath.java"
text=math_file.read_text(encoding="utf-8")
marker = '    public static long remainingMillis(long nowMs, long nextMs)'
new_method = '''    /**
     * True only after the last prayer and before today's sunrise, when sunrise
     * occurs sooner than the next prayer. Sunrise is a calendar event, not adhan.
     */
    public static boolean shouldShowSunrise(long nowMs, long sunriseMs, long nextPrayerMs) {
        return sunriseMs > nowMs && (nextPrayerMs <= 0L || sunriseMs < nextPrayerMs);
    }

'''
if text.count(marker)!=1: raise AssertionError("PrayerMath anchor")
text=text.replace(marker,new_method+marker,1)
math_file.write_text(text, encoding="utf-8")

widget_file=main/"WidgetProvider.java"
text=widget_file.read_text(encoding="utf-8")
text=exact(text,'import android.app.PendingIntent;', 'import android.app.AlarmManager;\nimport android.app.PendingIntent;', 'alarm manager import')
text=exact(text,'import android.os.SystemClock;', 'import android.os.SystemClock;\nimport android.os.Build;', 'SDK import')
text=exact(text,
    '    public static final String ACTION_REFRESH = "com.sarf.qasioun.prayer.REFRESH";',
    '    public static final String ACTION_REFRESH = "com.sarf.qasioun.prayer.REFRESH";\n'
    '    public static final String ACTION_TRANSITION = "com.sarf.qasioun.prayer.WIDGET_TRANSITION";',
    'transition action')
text=exact(text,
    '''            renderAll(context);
        }
    }
    public static void renderAll(Context context) {''',
    '''            renderAll(context);
        } else if (ACTION_TRANSITION.equals(intent.getAction())) {
            // Update the event title after Fajr / sunrise / each prayer without playing audio.
            renderAll(context);
        }
    }
    public static void renderAll(Context context) {''',
    'transition broadcast receiver')
text=exact(text,
    '''        updateGroup(context, manager, snapshot, WidgetCompactProvider.class, R.layout.prayer_widget_compact, false);
    }
    private static void updateGroup''',
    '''        updateGroup(context, manager, snapshot, WidgetCompactProvider.class, R.layout.prayer_widget_compact, false);
        scheduleTimelineChange(context, manager, snapshot);
    }

    private static PrayerEngine.Event displayedEvent(PrayerEngine.Snapshot snapshot, long nowMs) {
        if (snapshot == null) return null;
        PrayerEngine.Event prayer = snapshot.next();
        PrayerEngine.Event sunrise = snapshot.sunrise();
        if (sunrise != null && PrayerMath.shouldShowSunrise(nowMs, sunrise.epochMs(),
                prayer == null ? 0L : prayer.epochMs())) return sunrise;
        return prayer;
    }

    private static void scheduleTimelineChange(Context context, AppWidgetManager manager,
                                                 PrayerEngine.Snapshot snapshot) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent intent = new Intent(context, WidgetProvider.class).setAction(ACTION_TRANSITION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 37107, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
        int compactCount = manager.getAppWidgetIds(new ComponentName(context, WidgetCompactProvider.class)).length;
        int expandedCount = manager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class)).length;
        if (compactCount + expandedCount == 0) return;
        PrayerEngine.Event displayed = displayedEvent(snapshot, System.currentTimeMillis());
        if (displayed == null) return;
        long when = Math.max(System.currentTimeMillis()+1500L, displayed.epochMs()+1500L);
        try {
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms())
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
            else
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
        } catch (SecurityException denied) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
        }
    }

    private static void updateGroup''',
    'schedule event transitions')
text=exact(text,
    '''                rv.setTextViewText(R.id.widget_hijri, snapshot.hijri());
                rv.setTextViewText(R.id.widget_sunrise,
                        "☀ الشروق " + PrayerEngine.format(snapshot.sunrise(), snapshot.tz()));
                if (snapshot.next() != null) {
                    rv.setTextViewText(R.id.widget_next, expanded ? "أذان " + snapshot.next().name() : snapshot.next().name() + " القادم");
                    rv.setTextViewText(R.id.widget_next_time, PrayerEngine.format(snapshot.next(), snapshot.tz()));
                    long remaining = PrayerMath.remainingMillis(System.currentTimeMillis(), snapshot.next().epochMs());''',
    '''                rv.setTextViewText(R.id.widget_hijri, snapshot.hijri());
                long now = System.currentTimeMillis();
                PrayerEngine.Event shown = displayedEvent(snapshot, now);
                if (shown != null) {
                    rv.setTextViewText(R.id.widget_next, shown.name());
                    rv.setTextViewText(R.id.widget_next_time, PrayerEngine.format(shown, snapshot.tz()));
                    rv.setTextViewText(R.id.widget_countdown_label,
                            shown.index() == -1 ? "المتبقي للشروق" : "المتبقي للأذان");
                    long remaining = PrayerMath.remainingMillis(now, shown.epochMs());''',
    'live event switch')
text=exact(text,
    '''                    rv.setTextViewText(R.id.widget_next, "جاري الانتقال إلى يوم جديد");
                    rv.setTextViewText(R.id.widget_next_time, "سيتم التحديث قريبًا");''',
    '''                    rv.setTextViewText(R.id.widget_next, "جاري الانتقال إلى يوم جديد");
                    rv.setTextViewText(R.id.widget_next_time, "سيتم التحديث قريبًا");
                    rv.setTextViewText(R.id.widget_countdown_label, "المتبقي للأذان");''',
    'calendar edge fallback')
text=exact(text,
    '''                rv.setTextViewText(R.id.widget_hijri, "تقويم أم القرى");
                rv.setTextViewText(R.id.widget_sunrise, "☀ الشروق --:--");
                rv.setTextViewText(R.id.widget_next, "في انتظار المواقيت");''',
    '''                rv.setTextViewText(R.id.widget_hijri, "تقويم أم القرى");
                rv.setTextViewText(R.id.widget_countdown_label, "المتبقي للأذان");
                rv.setTextViewText(R.id.widget_next, "في انتظار المواقيت");''',
    'no-data sunrise removal')
widget_file.write_text(text,encoding="utf-8")

gradle_file = root / "app/build.gradle"
data=gradle_file.read_text(encoding="utf-8")
data=exact(data, 'versionCode 6', 'versionCode 7', 'version 7')
data=exact(data, "versionName '0.6.0-sunrise-beta'", "versionName '0.7.0-next-event-beta'", 'version label')
gradle_file.write_text(data,encoding="utf-8")

test_java=root/"tests/PrayerMathTest.java"
data=test_java.read_text(encoding="utf-8")
needle='        long dst = PrayerMath.epochMillis('
tests='''        // Timeline: 05:01 Fajr, 06:17 Sunrise, 12:10 Dhuhr.
        long fajr = PrayerMath.epochMillis(d,"05:01","Asia/Riyadh");
        long sunrise = PrayerMath.epochMillis(d,"06:17","Asia/Riyadh");
        long dhuhr = PrayerMath.epochMillis(d,"12:10","Asia/Riyadh");
        ok(!PrayerMath.shouldShowSunrise(fajr-60_000L,sunrise,fajr),"Before fajr next is Fajr");
        ok(PrayerMath.shouldShowSunrise(fajr,sunrise,dhuhr),"After Fajr next is sunrise");
        ok(PrayerMath.shouldShowSunrise(sunrise-1000L,sunrise,dhuhr),"Sunrise countdown until event");
        ok(!PrayerMath.shouldShowSunrise(sunrise,sunrise,dhuhr),"At sunrise next is Dhuhr");
        ok(!PrayerMath.shouldShowSunrise(sunrise+1000L,sunrise,dhuhr),"After sunrise next is Dhuhr");
        ok(!PrayerMath.shouldShowSunrise(dhuhr+1000L,sunrise,fajr+24L*3600*1000),"Late night sunrise never persistent");
        ok(PrayerMath.shouldShowSunrise(fajr+1000L,sunrise,0L),"Missing prayer does not hide sunrise");
'''
data=exact(data,needle,tests+needle,"test insertion")
test_java.write_text(data,encoding="utf-8")

# Verify no sunrise audio was introduced. All adhan scheduling remains the 5 prayer keys.
alarms=(main/"AlarmReceiver.java").read_text(encoding="utf-8")
assert 'for (PrayerEngine.Event e : sn.upcoming())' in alarms
assert 'ACTION_TRANSITION' not in alarms
assert "shown.index() == -1" in text
assert "setExactAndAllowWhileIdle" in text
assert 'android:id="@+id/widget_sunrise"' not in (layouts/"prayer_widget_compact.xml").read_text()
print("PASS v07: sunrise only as next event, no adhan at sunrise, Android alarm transition, concise titles")
