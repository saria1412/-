package com.sarf.engineering.themeswitch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class ScheduleReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        if(context==null||intent==null)return;
        String action=intent.getAction();
        if(action==null)return;
        if(ThemeScheduler.DAY_ALARM.equals(action)){
            ThemeScheduler.onAlarm(context,ThemeScheduler.DAY);
        }else if(ThemeScheduler.NIGHT_ALARM.equals(action)){
            ThemeScheduler.onAlarm(context,ThemeScheduler.NIGHT);
        }else if(Intent.ACTION_BOOT_COMPLETED.equals(action)
                ||Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
                ||Intent.ACTION_TIME_CHANGED.equals(action)
                ||Intent.ACTION_TIMEZONE_CHANGED.equals(action)){
            ThemeScheduler.schedule(context);
            ThemeScheduler.ensureMode(context);
        }else if(Intent.ACTION_USER_PRESENT.equals(action)){
            // Never attempt GUI automation while the phone is locked.
            if(ThemeScheduler.enabled(context)){
                ThemeScheduler.ensureMode(context);
                ThemeAccessibilityService svc=ThemeAccessibilityService.current();
                if(svc!=null)svc.startPendingIfPossible();
            }
        }
    }
}
