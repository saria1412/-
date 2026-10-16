package com.sarf.engineering.themeswitch;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Deterministic two-time local theme schedule. No hidden settings writes or
 * silent system theme APIs are attempted; the accessibility service clicks
 * inside the HONOR Themes application only with explicit user opt-in.
 */
public final class ThemeScheduler {
    private static final String TAG = "SARFThemeAuto";
    private static final String PREF = "sarf_auto_theme_v1";
    public static final String DAY = "DAY";
    public static final String NIGHT = "NIGHT";
    public static final String DAY_ALARM = "com.sarf.engineering.themeswitch.DAY_ALARM";
    public static final String NIGHT_ALARM = "com.sarf.engineering.themeswitch.NIGHT_ALARM";
    private static final String CHANNEL = "sarf_theme_request";
    private ThemeScheduler() {}

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }
    public static boolean enabled(Context c) {
        return prefs(c).getBoolean("enabled",false);
    }
    public static void setEnabled(Context c, boolean value) {
        prefs(c).edit().putBoolean("enabled",value).apply();
        schedule(c);
        status(c, value ? "الجدول التلقائي مفعل — يتطلب خدمة الوصول" : "تم إيقاف الجدول التلقائي");
        if (value) ensureMode(c);
    }
    public static String themeName(Context c, String mode) {
        return prefs(c).getString(DAY.equals(mode) ? "dayName":"nightName",
                DAY.equals(mode) ? "SARF Material Flow Day R09" : "SARF Material Flow Night R09");
    }
    public static void setThemeNames(Context c, String day, String night) {
        prefs(c).edit().putString("dayName",day.trim())
                .putString("nightName",night.trim()).apply();
        status(c,"حُفظ اسما الثيمين");
    }
    public static int minute(Context c, String mode) {
        return prefs(c).getInt(DAY.equals(mode) ? "dayMinute":"nightMinute",
                DAY.equals(mode) ? 360:1080);
    }
    public static void setMinute(Context c,String mode,int value) {
        if(value<0 || value>=1440) throw new IllegalArgumentException("Invalid clock minute");
        prefs(c).edit().putInt(DAY.equals(mode) ? "dayMinute":"nightMinute",value).apply();
        schedule(c);
        status(c, "حُفظ موعد " + (DAY.equals(mode) ? "الثيم النهاري":"الثيم الليلي"));
    }
    public static String clock(int minute) {
        return String.format(Locale.US,"%02d:%02d",minute/60,minute%60);
    }
    public static String modeNow(Context c) {
        Calendar cal=Calendar.getInstance();
        int now=cal.get(Calendar.HOUR_OF_DAY)*60+cal.get(Calendar.MINUTE);
        int start=minute(c,DAY), end=minute(c,NIGHT);
        int span=(end-start+1440)%1440;
        int since=(now-start+1440)%1440;
        return span>0 && since<span ? DAY:NIGHT;
    }
    static String todayKey() {
        return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());
    }
    public static String lastStatus(Context c) {
        return prefs(c).getString("status","لم تُنفّذ أي محاولة بعد");
    }
    public static void status(Context c,String message) {
        String t=new SimpleDateFormat("HH:mm",Locale.US).format(new Date());
        prefs(c).edit().putString("status",t+" — "+message).apply();
        Log.i(TAG,message);
    }
    public static String pending(Context c) {
        String value=prefs(c).getString("pending","");
        long age=System.currentTimeMillis()-prefs(c).getLong("pendingAt",0L);
        if(age<0 || age>30*60_000L) return "";
        return DAY.equals(value)||NIGHT.equals(value)?value:"";
    }
    public static void clearPending(Context c) {
        prefs(c).edit().remove("pending").remove("pendingAt").apply();
    }
    public static boolean isUnlocked(Context c) {
        KeyguardManager km=(KeyguardManager)c.getSystemService(Context.KEYGUARD_SERVICE);
        PowerManager pm=(PowerManager)c.getSystemService(Context.POWER_SERVICE);
        return (km==null || !km.isKeyguardLocked()) && pm!=null && pm.isInteractive();
    }
    public static void request(Context c,String mode,boolean forced) {
        if (!forced && !enabled(c)) return;
        if (!DAY.equals(mode) && !NIGHT.equals(mode)) return;
        String target=themeName(c,mode);
        if (target.isEmpty()) {
            status(c,"اسم الثيم غير محدد"); return;
        }
        long now=System.currentTimeMillis();
        if (!forced && mode.equals(prefs(c).getString("lastTapMode",""))
                && todayKey().equals(prefs(c).getString("lastTapDay",""))) return;
        if (!forced && mode.equals(prefs(c).getString("lastRequestedMode",""))
                && now-prefs(c).getLong("lastRequestedAt",0L)<10*60_000L) return;
        prefs(c).edit().putString("pending",mode)
                .putLong("pendingAt",now)
                .putString("lastRequestedMode",mode)
                .putLong("lastRequestedAt",now).apply();
        status(c,"مطلوب تطبيق "+target);
        ThemeAccessibilityService svc=ThemeAccessibilityService.current();
        if (svc!=null && isUnlocked(c)) {
            svc.startPendingIfPossible();
        } else {
            if (svc==null) status(c,"فعّل خدمة الوصول لإكمال التبديل");
            else status(c,"سيُطلب تطبيق الثيم بعد فتح قفل الشاشة");
        }
        if (forced || svc==null || !isUnlocked(c)) notifyContinue(c,mode);
    }
    public static void ensureMode(Context c) {
        if (!enabled(c)) return;
        request(c,modeNow(c),false);
    }
    public static void markClicked(Context c,String mode) {
        prefs(c).edit().putString("lastTapMode",mode)
                .putString("lastTapDay",todayKey())
                .putLong("lastTapAt",System.currentTimeMillis()).apply();
        clearPending(c);
        status(c,"نُقر زر تطبيق ثيم "+themeName(c,mode)+"؛ التأكيد النهائي يحتاج معاينة الهاتف");
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm!=null)nm.cancel(2317);
    }
    public static void markFailed(Context c,String why) {
        clearPending(c);
        status(c,why);
    }
    private static long futureMillis(int minuteOfDay) {
        Calendar c=Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY,minuteOfDay/60);
        c.set(Calendar.MINUTE,minuteOfDay%60);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        if(c.getTimeInMillis()<=System.currentTimeMillis()+5000L)c.add(Calendar.DAY_OF_YEAR,1);
        return c.getTimeInMillis();
    }
    private static PendingIntent alarmIntent(Context c,String mode) {
        Intent i=new Intent(c,ScheduleReceiver.class);
        i.setAction(DAY.equals(mode) ? DAY_ALARM:NIGHT_ALARM);
        return PendingIntent.getBroadcast(c,DAY.equals(mode)?201:202,i,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static void schedule(Context c) {
        AlarmManager alarm=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(alarm==null)return;
        for(String mode:new String[]{DAY,NIGHT}) {
            PendingIntent pending=alarmIntent(c,mode);
            alarm.cancel(pending);
            if(!enabled(c))continue;
            try {
                // Inexact alarms need no special exact-alarm permission.
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        futureMillis(minute(c,mode)),pending);
            } catch(SecurityException ex) {
                status(c,"لم يتمكن Android من جدولة التوقيت: "+ex.getClass().getSimpleName());
            }
        }
    }
    public static void onAlarm(Context c,String mode) {
        if(!enabled(c)) return;
        schedule(c);
        if(mode.equals(modeNow(c)))request(c,mode,false);
    }
    public static void notificationPermissions(Activity a) {
        if(Build.VERSION.SDK_INT>=33 && a.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                !=PackageManager.PERMISSION_GRANTED) {
            a.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},511);
        }
    }
    public static void notifyContinue(Context c,String mode) {
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm==null)return;
        if(Build.VERSION.SDK_INT>=33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                !=PackageManager.PERMISSION_GRANTED)return;
        nm.createNotificationChannel(new NotificationChannel(CHANNEL,"SARF — تبديل الثيم",NotificationManager.IMPORTANCE_DEFAULT));
        Intent i=new Intent(c,MainActivity.class).setAction("com.sarf.engineering.themeswitch.CONTINUE");
        i.putExtra("mode",mode);
        PendingIntent pi=PendingIntent.getActivity(c,301,i,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification n=new Notification.Builder(c,CHANNEL)
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setContentTitle("SARF — موعد الثيم "+(DAY.equals(mode)?"النهاري":"الليلي"))
                .setContentText("افتح الهاتف لاستكمال تطبيق الثيم")
                .setAutoCancel(true).setContentIntent(pi).build();
        nm.notify(2317,n);
    }
}
