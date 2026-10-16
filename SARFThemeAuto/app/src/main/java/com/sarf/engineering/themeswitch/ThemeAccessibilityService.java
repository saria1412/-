package com.sarf.engineering.themeswitch;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayDeque;
import java.util.Locale;

/**
 * Opt-in, fixed-script accessibility automation for HONOR's own Themes app.
 * It NEVER inspects other applications, records fields, uses coordinates,
 * writes private system settings, or bypasses the lock screen.
 * UI labels may change with an HONOR Themes update; failure is reported.
 */
public final class ThemeAccessibilityService extends AccessibilityService {
    private static final String TAG="SARFThemesUI";
    private static final String HONOR="com.hihonor.android.thememanager";
    private static final String HUAWEI="com.huawei.android.thememanager";
    private static volatile ThemeAccessibilityService instance;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private boolean active=false;
    private int stage=0;
    private int taps=0;
    private long startMs=0L, lastTapMs=0L, appliedButtonAt=0L;
    private String targetMode="", targetName="";
    private boolean opening=false;

    public static ThemeAccessibilityService current(){ return instance; }

    @Override public void onServiceConnected(){
        super.onServiceConnected();
        instance=this;
        ThemeScheduler.status(this,"خدمة أتمتة السمات جاهزة (بموافقتك)");
        if(!ThemeScheduler.pending(this).isEmpty() && ThemeScheduler.isUnlocked(this)){
            handler.postDelayed(this::startPendingIfPossible,400);
        }
    }
    @Override public void onInterrupt(){
        Log.w(TAG,"Accessibility interrupted");
    }
    @Override public void onDestroy(){
        if(instance==this)instance=null;
        active=false;
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    private boolean isThemePackage(CharSequence name) {
        return name!=null && (HONOR.contentEquals(name)||HUAWEI.contentEquals(name));
    }
    public void startPendingIfPossible(){
        if(!ThemeScheduler.isUnlocked(this))return;
        String next=ThemeScheduler.pending(this);
        if(next.isEmpty())return;
        if(active && next.equals(targetMode))return;
        targetMode=next;
        targetName=ThemeScheduler.themeName(this,next);
        if(targetName.isEmpty())return;
        stage=0;
        taps=0;
        lastTapMs=0;
        appliedButtonAt=0;
        startMs=System.currentTimeMillis();
        active=true;
        if(opening)return;
        opening=true;
        boolean ok=openThemeManager();
        opening=false;
        if(!ok){
            active=false;
            ThemeScheduler.status(this,"يمنع MagicOS فتح السمات تلقائيًا؛ استخدم زر متابعة في الإشعار");
            ThemeScheduler.notifyContinue(this,next);
        } else {
            ThemeScheduler.status(this,"فتح تطبيق السمات لاختيار "+targetName);
            handler.postDelayed(this::processWindow,750);
            handler.postDelayed(this::timeoutCheck,26_000L);
        }
    }
    private boolean openThemeManager(){
        try {
            // Screenshot identifier observed on user's MagicOS 10 build.
            Intent direct=new Intent(Intent.ACTION_MAIN);
            direct.setClassName(HONOR,HONOR+".NewLocalThemeActivity");
            direct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(direct);
            return true;
        }catch(Exception e){ Log.d(TAG,"Local Themes page unavailable: "+e.getClass().getSimpleName()); }
        try{
            PackageManager pm=getPackageManager();
            Intent launcher=pm.getLaunchIntentForPackage(HONOR);
            if(launcher==null)launcher=pm.getLaunchIntentForPackage(HUAWEI);
            if(launcher==null)return false;
            launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(launcher);
            return true;
        }catch(Exception e){
            Log.w(TAG,"HONOR theme UI unavailable: "+e.getClass().getSimpleName());
            return false;
        }
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(event==null || !isThemePackage(event.getPackageName()))return;
        if(!active){
            String requested=ThemeScheduler.pending(this);
            if(requested.isEmpty())return;
            targetMode=requested;
            targetName=ThemeScheduler.themeName(this,requested);
            active=true; stage=0;taps=0;startMs=System.currentTimeMillis();
        }
        if(System.currentTimeMillis()-lastTapMs<500)return;
        handler.postDelayed(this::processWindow,160);
    }
    private void timeoutCheck() {
        if(!active)return;
        long elapsed=System.currentTimeMillis()-startMs;
        if(elapsed>=26_000L){
            String mode=targetMode;
            active=false;
            if(stage>=3){
                ThemeScheduler.markClicked(this,mode);
            }else{
                ThemeScheduler.markFailed(this,"تعذّر إكمال خطوات الثيم (تغيّرت واجهة تطبيق HONOR أو أذونات البدء)");
                ThemeScheduler.notifyContinue(this,mode);
            }
        }
    }
    private static String normalize(String text){
        return text.replace('\u00a0',' ').replaceAll("\\s+"," ").trim().toLowerCase(Locale.ROOT);
    }
    private static boolean sameOrContains(String label,String expected){
        if(label==null||expected==null)return false;
        String a=normalize(label), b=normalize(expected);
        return a.equals(b) || (b.length()>=12 && a.contains(b));
    }
    private static String title(AccessibilityNodeInfo n) {
        if(n==null)return "";
        CharSequence t=n.getText();
        if(t!=null && t.length()>0)return t.toString();
        CharSequence d=n.getContentDescription();
        return d==null?"":d.toString();
    }
    private static AccessibilityNodeInfo findNode(AccessibilityNodeInfo root,String...labels){
        if(root==null)return null;
        ArrayDeque<AccessibilityNodeInfo> queue=new ArrayDeque<>();
        queue.add(root);
        int visited=0;
        while(!queue.isEmpty() && visited++<300){
            AccessibilityNodeInfo n=queue.remove();
            String label=title(n);
            for(String value:labels) if(sameOrContains(label,value))return n;
            for(int i=0;i<n.getChildCount();i++){
                AccessibilityNodeInfo ch=n.getChild(i);
                if(ch!=null)queue.add(ch);
            }
        }
        return null;
    }
    private boolean clickText(AccessibilityNodeInfo root,String...labels){
        AccessibilityNodeInfo n=findNode(root,labels);
        if(n==null)return false;
        AccessibilityNodeInfo clickable=n;
        for(int i=0;i<5 && clickable!=null;i++){
            if(clickable.isClickable() && clickable.isEnabled()){
                boolean clicked=clickable.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                if(clicked){
                    taps++;
                    lastTapMs=System.currentTimeMillis();
                    return true;
                }
            }
            clickable=clickable.getParent();
        }
        return false;
    }
    private boolean seen(AccessibilityNodeInfo root,String...labels){
        return findNode(root,labels)!=null;
    }
    private void processWindow(){
        if(!active)return;
        if(!ThemeScheduler.isUnlocked(this))return;
        if(System.currentTimeMillis()-startMs>26_000L || taps>14){
            timeoutCheck(); return;
        }
        if(System.currentTimeMillis()-lastTapMs<480)return;
        AccessibilityNodeInfo root=getRootInActiveWindow();
        if(root==null||!isThemePackage(root.getPackageName()))return;

        // Phase 0: Navigate downloaded/local themes.
        if(stage==0){
            if(clickText(root,targetName)){
                stage=1;
                handler.postDelayed(this::processWindow,780);
                return;
            }
            if(clickText(root,"Downloads","My downloads","My Downloads","التنزيلات","تنزيلاتي","تحميلاتي")){
                handler.postDelayed(this::processWindow,780);return;
            }
            if(clickText(root,"Me","Mine","أنا","حسابي","My")){
                handler.postDelayed(this::processWindow,780);return;
            }
            // Click Themes tab only when Downloads is actually displayed.
            if(seen(root,"Downloads","تنزيلات") &&
                    clickText(root,"Themes","الثيمات","السمات")){
                handler.postDelayed(this::processWindow,780);return;
            }
            return;
        }
        // Phase 1: Opened the exact locally-installed SARF theme.
        if(stage==1) {
            if(clickText(root,"APPLY","Apply","تطبيق","تطبيق السمة","تطبيق الثيم")){
                stage=2;appliedButtonAt=System.currentTimeMillis();
                ThemeScheduler.status(this,"تم الضغط على «تطبيق» في ثيم "+targetName);
                handler.postDelayed(this::processWindow,1000);
            }return;
        }
        // Phase 2: HONOR might ask whether to discard the prior customizations.
        if(stage==2) {
            if(clickText(root,"DISCARD","Discard","استبدال","تجاهل","تأكيد","Confirm","OK","موافق","تطبيق الكل")){
                stage=3;
                ThemeScheduler.status(this,"تمت متابعة التأكيد في تطبيق HONOR");
                handler.postDelayed(this::finishClicked,1600);
                return;
            }
            if(seen(root,"Applied","تم التطبيق","تم بنجاح")){
                stage=3;
                handler.postDelayed(this::finishClicked,700);
                return;
            }
            if(System.currentTimeMillis()-appliedButtonAt>2200L){
                // No confirmation appeared: the Apply click was sent. Do not
                // claim the system theme has been independently verified.
                stage=3;
                finishClicked();
            }
        }
    }
    private void finishClicked(){
        if(!active || stage<3)return;
        String completedMode=targetMode;
        active=false;
        ThemeScheduler.markClicked(this,completedMode);
    }
}
