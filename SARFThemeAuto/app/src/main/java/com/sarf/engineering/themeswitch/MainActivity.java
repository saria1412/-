package com.sarf.engineering.themeswitch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import java.util.Locale;

/** User-authorized, narrowly scoped scheduling UI. No private theme APIs. */
public final class MainActivity extends Activity {
    private static final int CHARCOAL=Color.rgb(25,30,37);
    private static final int PANEL=Color.rgb(17,23,32);
    private static final int STEEL=Color.rgb(100,117,135);
    private static final int COPPER=Color.rgb(197,146,97);
    private static final int STONE=Color.rgb(243,242,239);
    private static final int MUTED=Color.rgb(184,192,200);
    private EditText nameDay,nameNight;
    private TextView status,captionTimes,captionAccess;
    private Switch autoSwitch;
    private Button clockDay,clockNight;
    private LinearLayout screen;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable refreshTick=new Runnable(){
        @Override public void run(){refresh();handler.postDelayed(this,3000L);}
    };
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(CHARCOAL);
        getWindow().setNavigationBarColor(CHARCOAL);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        ThemeScheduler.schedule(this);
        build();
        if(getIntent()!=null && getIntent().hasExtra("mode")){
            String requested=getIntent().getStringExtra("mode");
            if(ThemeScheduler.DAY.equals(requested)||ThemeScheduler.NIGHT.equals(requested)){
                handler.postDelayed(()->resumePending(),800);
            }
        }
    }
    @Override public void onResume(){
        super.onResume();
        handler.removeCallbacks(refreshTick);
        handler.post(refreshTick);
    }
    @Override public void onPause(){
        handler.removeCallbacks(refreshTick);
        super.onPause();
    }
    private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private GradientDrawable rounded(int fill,int stroke,int radius){
        GradientDrawable d=new GradientDrawable();
        d.setColor(fill);d.setCornerRadius(dp(radius));
        if(stroke!=Color.TRANSPARENT)d.setStroke(dp(1),stroke);
        return d;
    }
    private TextView text(String value,int sp,int color,boolean bold){
        TextView v=new TextView(this);
        v.setText(value);v.setTextColor(color);v.setTextSize(sp);
        v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        v.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
        if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        v.setIncludeFontPadding(false);
        v.setLineSpacing(dp(2),1.12f);
        return v;
    }
    private LinearLayout col(){
        LinearLayout l=new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return l;
    }
    private LinearLayout row(){
        LinearLayout l=new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return l;
    }
    private LinearLayout card(String heading){
        LinearLayout c=col();
        c.setPadding(dp(18),dp(18),dp(18),dp(19));
        c.setBackground(rounded(PANEL,0xff354856,20));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(15);
        screen.addView(c,lp);
        TextView h=text(heading,16,STONE,true);
        LinearLayout.LayoutParams hl=new LinearLayout.LayoutParams(-1,-2);
        hl.bottomMargin=dp(12);c.addView(h,hl);
        return c;
    }
    private Button button(String title,boolean primary){
        Button b=new Button(this);
        b.setText(title);b.setAllCaps(false);b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setTextColor(primary?CHARCOAL:STONE);
        b.setBackgroundTintList(null);
        b.setBackground(rounded(primary?COPPER:0xff354856,Color.TRANSPARENT,13));
        b.setMinimumHeight(dp(45));
        b.setMinHeight(dp(45));
        return b;
    }
    private void addButton(LinearLayout parent,Button button,int marginTop){
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));
        lp.topMargin=dp(marginTop);parent.addView(button,lp);
    }
    private TextView note(String s){
        return text(s,12,MUTED,false);
    }
    private EditText field(String name){
        EditText e=new EditText(this);
        e.setSingleLine(true);
        e.setText(name);
        e.setTextColor(STONE);
        e.setHintTextColor(MUTED);
        e.setTextSize(13);
        e.setSelectAllOnFocus(true);
        e.setTextDirection(View.TEXT_DIRECTION_LTR);
        e.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        e.setBackground(rounded(CHARCOAL,0xff354856,10));
        e.setPadding(dp(12),0,dp(12),0);
        return e;
    }
    private void divider(LinearLayout c){
        View v=new View(this);
        v.setBackgroundColor(0xff354856);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(1));
        lp.topMargin=dp(13);lp.bottomMargin=dp(13);c.addView(v,lp);
    }
    private void build(){
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(CHARCOAL);
        screen=col();
        screen.setPadding(dp(19),dp(25),dp(19),dp(35));
        scroll.addView(screen);
        setContentView(scroll);

        TextView eyebrow=text("SARF  /  ENGINEERING",11,COPPER,true);
        eyebrow.setTextDirection(View.TEXT_DIRECTION_LTR);
        eyebrow.setGravity(Gravity.LEFT);
        screen.addView(eyebrow);
        TextView headline=text("التبديل التلقائي للثيم",25,STONE,true);
        LinearLayout.LayoutParams headLp=new LinearLayout.LayoutParams(-1,-2);
        headLp.topMargin=dp(12);
        screen.addView(headline,headLp);
        TextView subtitle=note("تطبيق ثيمي SARF النهاري والليلي كاملين في تطبيق السمات — وليس مجرد تفعيل الوضع الداكن.");
        LinearLayout.LayoutParams subtitleLp=new LinearLayout.LayoutParams(-1,-2);
        subtitleLp.topMargin=dp(8);
        screen.addView(subtitle,subtitleLp);

        LinearLayout c=card("01 / الجدول اليومي");
        autoSwitch=new Switch(this);
        autoSwitch.setText("تفعيل التبديل حسب الجدول");
        autoSwitch.setTextSize(14);autoSwitch.setTextColor(STONE);
        autoSwitch.setThumbTintList(ColorStateList.valueOf(COPPER));
        autoSwitch.setChecked(ThemeScheduler.enabled(this));
        autoSwitch.setOnCheckedChangeListener((v,checked)->{
            if(checked && !ThemeScheduler.enabled(this)){
                autoSwitch.setOnCheckedChangeListener(null);
                autoSwitch.setChecked(false);
                autoSwitch.setOnCheckedChangeListener((a,b)->toggleAuto(b));
                toggleAuto(true);
            }else if(!checked)ThemeScheduler.setEnabled(this,false);
        });
        c.addView(autoSwitch);

        captionTimes=note(""); 
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);
        cp.topMargin=dp(9);c.addView(captionTimes,cp);

        LinearLayout line=row();
        clockDay=button("النهاري",false);
        clockNight=button("الليلي",false);
        LinearLayout.LayoutParams pw=new LinearLayout.LayoutParams(0,dp(47),1);
        pw.leftMargin=dp(5);
        line.addView(clockDay,pw);
        LinearLayout.LayoutParams pn=new LinearLayout.LayoutParams(0,dp(47),1);
        pn.rightMargin=dp(5);
        line.addView(clockNight,pn);
        LinearLayout.LayoutParams lineLp=new LinearLayout.LayoutParams(-1,-2);
        lineLp.topMargin=dp(10);
        c.addView(line,lineLp);
        clockDay.setOnClickListener(v->chooseTime(ThemeScheduler.DAY));
        clockNight.setOnClickListener(v->chooseTime(ThemeScheduler.NIGHT));
        TextView modeNote=note("الوضع الافتراضي: 06:00 صباحًا و18:00 مساءً. التوقيت قابل للتعديل. عند قفل الشاشة ينتظر التطبيق فتحها.");
        LinearLayout.LayoutParams modLp=new LinearLayout.LayoutParams(-1,-2);
        modLp.topMargin=dp(10);c.addView(modeNote,modLp);

        LinearLayout themes=card("02 / ثيمات HONOR المحفوظة");
        themes.addView(note("الصباح — الاسم المعروض داخل «السمات ← تنزيلاتي»"));
        nameDay=field(ThemeScheduler.themeName(this,ThemeScheduler.DAY));
        LinearLayout.LayoutParams f1=new LinearLayout.LayoutParams(-1,dp(50));
        f1.topMargin=dp(7);themes.addView(nameDay,f1);
        TextView nl=note("المساء — الاسم المعروض داخل «السمات ← تنزيلاتي»");
        LinearLayout.LayoutParams nlLp=new LinearLayout.LayoutParams(-1,-2);
        nlLp.topMargin=dp(12);themes.addView(nl,nlLp);
        nameNight=field(ThemeScheduler.themeName(this,ThemeScheduler.NIGHT));
        LinearLayout.LayoutParams f2=new LinearLayout.LayoutParams(-1,dp(50));
        f2.topMargin=dp(7);themes.addView(nameNight,f2);

        Button save=button("حفظ أسماء الثيمين",false);
        addButton(themes,save,12);
        save.setOnClickListener(v->saveNames());
        divider(themes);
        Button day=button("تطبيق النهاري الآن",true);
        Button night=button("تطبيق الليلي الآن",false);
        addButton(themes,day,0);addButton(themes,night,8);
        day.setOnClickListener(v->apply(ThemeScheduler.DAY));
        night.setOnClickListener(v->apply(ThemeScheduler.NIGHT));

        LinearLayout a=card("03 / إذن التشغيل الآمن");
        captionAccess=note("");
        a.addView(captionAccess);
        Button accessibility=button("فتح إعدادات إمكانية الوصول",false);
        addButton(a,accessibility,10);
        accessibility.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("ما الذي يسمح به هذا الإذن؟")
            .setMessage("يمكن لخدمة SARF قراءة عناوين وأزرار تطبيق سمات HONOR فقط أثناء تبديل الثيمين. لا تجمع كلمات المرور أو محتويات التطبيقات الأخرى، ولا تتصل بالإنترنت. ستحتاج إلى تفعيل الخدمة بنفسك من إعدادات النظام. يمكنك إيقافها في أي وقت.")
            .setNegativeButton("إلغاء",null)
            .setPositiveButton("فتح الإعدادات",(d,which)->{
                try{startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));}
                catch(Exception x){Toast.makeText(this,"تعذر فتح إعدادات إمكانية الوصول",Toast.LENGTH_LONG).show();}
            }).show());
        Button openTheme=button("فتح تطبيق السمات يدويًا",false);
        addButton(a,openTheme,8);
        openTheme.setOnClickListener(v->launchThemeApp());

        LinearLayout stat=card("04 / حالة التطبيق");
        status=note(ThemeScheduler.lastStatus(this));
        stat.addView(status);
        divider(stat);
        stat.addView(note("هام: أذونات Android 16 قد تمنع فتح تطبيق «السمات» من الخلفية. عندئذ يظهر إشعار لاستكمال التبديل بعد فتح الهاتف. التطبيق لا يحتاج روت، ولا يتجاوز حماية القفل."));
        Button continueBtn=button("متابعة تبديل معلّق",false);
        addButton(stat,continueBtn,12);
        continueBtn.setOnClickListener(v->resumePending());
        refresh();
    }
    private void toggleAuto(boolean checked){
        if(!checked){
            ThemeScheduler.setEnabled(this,false);
            autoSwitch.setChecked(false);
            refresh();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("تفويض التبديل التلقائي")
            .setMessage("سيحاول التطبيق في الوقت الذي حددته فتح تطبيق سمات HONOR والضغط على ثيم SARF الموافق ثم «تطبيق». يحتاج إذن إمكانية الوصول؛ إذا منعه النظام من الفتح في الخلفية سيُظهر إشعارًا بدل تجاوز قيود Android. لا يتم تغيير أي ثيم آخر.")
            .setNegativeButton("إلغاء",(d,w)->autoSwitch.setChecked(false))
            .setPositiveButton("أوافق وأفعّل",(d,w)->{
                ThemeScheduler.setEnabled(this,true);
                autoSwitch.setOnCheckedChangeListener(null);
                autoSwitch.setChecked(true);
                autoSwitch.setOnCheckedChangeListener((v,val)->{
                    if(val!=ThemeScheduler.enabled(this))toggleAuto(val);
                });
                ThemeScheduler.notificationPermissions(this);
                refresh();
            }).show();
    }
    private void chooseTime(String mode){
        int time=ThemeScheduler.minute(this,mode);
        new TimePickerDialog(this,(picker,h,m)->{
            int newMinute=h*60+m;
            int other=ThemeScheduler.minute(this,ThemeScheduler.DAY.equals(mode)?ThemeScheduler.NIGHT:ThemeScheduler.DAY);
            if(newMinute==other){
                Toast.makeText(this,"يجب اختلاف التوقيتين",Toast.LENGTH_LONG).show();
                return;
            }
            ThemeScheduler.setMinute(this,mode,newMinute);
            refresh();
        },time/60,time%60,true).show();
    }
    private boolean saveNames(){
        String a=nameDay.getText().toString().trim();
        String b=nameNight.getText().toString().trim();
        if(a.isEmpty()||b.isEmpty()||a.equalsIgnoreCase(b)){
            Toast.makeText(this,"أدخل اسمين مختلفين للثيمين",Toast.LENGTH_LONG).show();
            return false;
        }
        ThemeScheduler.setThemeNames(this,a,b);
        refresh();
        Toast.makeText(this,"حُفظت أسماء الثيمين",Toast.LENGTH_SHORT).show();
        return true;
    }
    private void apply(String mode){
        if(!saveNames())return;
        ThemeScheduler.request(this,mode,true);
        if(ThemeAccessibilityService.current()==null){
            new AlertDialog.Builder(this)
                .setTitle("الخدمة غير مفعلة")
                .setMessage("لم يتم تفعيل خدمة إمكانية الوصول بعد. افتح تطبيق السمات واختر الثيم يدويًا، أو فعّل خدمة SARF من الإعدادات لتجربة الأتمتة.")
                .setPositiveButton("فتح السمات",(d,x)->launchThemeApp())
                .setNegativeButton("إغلاق",null).show();
        }
        refresh();
    }
    private void resumePending(){
        ThemeAccessibilityService service=ThemeAccessibilityService.current();
        if(service==null){
            ThemeScheduler.status(this,"خدمة إمكانية الوصول غير مفعلة");
            refresh();return;
        }
        String pending=ThemeScheduler.pending(this);
        if(pending.isEmpty() && ThemeScheduler.enabled(this)) {
            ThemeScheduler.ensureMode(this);
            pending=ThemeScheduler.pending(this);
        }
        if(pending.isEmpty()){
            Toast.makeText(this,"لا يوجد تبديل معلّق",Toast.LENGTH_SHORT).show();
        }else{
            service.startPendingIfPossible();
        }
        refresh();
    }
    private void launchThemeApp(){
        try{
            Intent i=getPackageManager().getLaunchIntentForPackage("com.hihonor.android.thememanager");
            if(i==null)i=getPackageManager().getLaunchIntentForPackage("com.huawei.android.thememanager");
            if(i==null)throw new ActivityNotFoundException("Themes not visible");
            startActivity(i);
        }catch(Exception ex){
            Toast.makeText(this,"افتح تطبيق «السمات» من الشاشة الرئيسية",Toast.LENGTH_LONG).show();
        }
    }
    private void refresh(){
        if(clockDay==null || clockNight==null)return;
        clockDay.setText("النهاري  "+ThemeScheduler.clock(ThemeScheduler.minute(this,ThemeScheduler.DAY)));
        clockNight.setText("الليلي  "+ThemeScheduler.clock(ThemeScheduler.minute(this,ThemeScheduler.NIGHT)));
        String current=ThemeScheduler.modeNow(this).equals(ThemeScheduler.DAY)?"النهاري":"الليلي";
        captionTimes.setText("الوضع المستهدف حاليًا: "+current);
        captionAccess.setText(ThemeAccessibilityService.current()!=null?
                "حالة الخدمة: مفعّلة وجاهزة.":"حالة الخدمة: غير مفعّلة. يلزم تفعيلها مرة واحدة.");
        status.setText(ThemeScheduler.lastStatus(this));
    }
}
