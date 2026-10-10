package com.sarf.qasioun.wallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** One-time HNT image setup; from then on HONOR schedules Light / Dark itself. */
public final class MainActivity extends Activity {
    private static final int PICK_DAY=41, PICK_NIGHT=42;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private LinearLayout body;
    private TextView state;
    private Button activate;

    private GradientDrawable bg(int color, int borderColor) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(18));
        d.setStroke(dp(1),borderColor);
        return d;
    }
    private int dp(int val){
        return (int)(val*getResources().getDisplayMetrics().density+0.5f);
    }
    private TextView label(String s,int sp,int color,boolean strong) {
        TextView v=new TextView(this);
        v.setText(s);
        v.setTextColor(color);
        v.setTextSize(sp);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setIncludeFontPadding(false);
        if(strong)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return v;
    }
    private void addLabel(String text,int fontSize,int color,boolean strong,int top){
        TextView v=label(text,fontSize,color,strong);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.topMargin=dp(top);
        body.addView(v,p);
    }
    private void addButton(String text,Runnable action,boolean primary) {
        Button b=new Button(this);
        b.setAllCaps(false);
        b.setText(text);
        b.setTextSize(15f);
        b.setTextColor(primary?0xFF0B332B:0xFFF4ECDC);
        b.setBackground(bg(primary?0xFFE6C98E:0xFF124A3B,0xFFBDA877));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));
        p.topMargin=dp(10);
        body.addView(b,p);
        b.setOnClickListener(v->action.run());
        if(primary)activate=b;
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(0xFF092F28);
        getWindow().setNavigationBarColor(0xFF092F28);
        getWindow().getDecorView().setSystemUiVisibility(0);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFF082F28);
        body=new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(20),dp(28),dp(20),dp(32));
        body.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(body,new ScrollView.LayoutParams(-1,-2));
        addLabel("سورية | قاسيون",27,0xFFE6C98E,true,3);
        addLabel("خلفية الصباح والمساء",21,Color.WHITE,true,8);
        addLabel("يعتمد التبديل على إعداد HONOR: الوضع الداكن من الغروب إلى الشروق. لا نحتاج إلى إذن الموقع أو الإنترنت.",14,0xFFCBD9D2,false,15);
        state=label("",15,0xFFF1E4B9,true);
        LinearLayout.LayoutParams statusParams=new LinearLayout.LayoutParams(-1,-2);
        statusParams.topMargin=dp(18);
        statusParams.bottomMargin=dp(9);
        state.setPadding(dp(14),dp(18),dp(14),dp(18));
        state.setBackground(bg(0xFF10473B,0xFF68806E));
        body.addView(state,statusParams);
        addButton("١. اختيار ملف HNT النهاري",()->select(PICK_DAY),false);
        addButton("٢. اختيار ملف HNT الليلي",()->select(PICK_NIGHT),false);
        addButton("٣. تفعيل الخلفية الحية التلقائية",this::activateWallpaper,true);
        addLabel("لتجربة التبديل فورًا: افتح إعدادات العرض والسطوع، ثم بدّل Light وDark. ستتغير الخلفية مباشرة عند العودة إلى الشاشة الرئيسية.",13,0xFFCDDAD3,false,23);
        addLabel("الأيقونات وألوان النظام الخاصة بـ HNT لا تتبدل تلقائيًا بهذه الطريقة. طبّق حزمة الأيقونات التي تفضلها يدويًا؛ هذا التطبيق يتحكم بالخلفية الحية فقط.",13,0xFFE6C98E,false,16);
        setContentView(scroll);
        refreshUi();
    }
    private void refreshUi(){
        int n=getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK;
        String mode = n==Configuration.UI_MODE_NIGHT_YES?"المساء (Dark)":n==Configuration.UI_MODE_NIGHT_NO?"الصباح (Light)":"غير محدد";
        String day=ThemeStorage.image(this,false).isFile()?"جاهزة":"غير محددة";
        String night=ThemeStorage.image(this,true).isFile()?"جاهزة":"غير محددة";
        if(state!=null)state.setText("وضع HONOR حاليًا: "+mode+"\nخلفية النهار: "+day+"\nخلفية الليل: "+night);
        if(activate!=null)activate.setEnabled(ThemeStorage.ready(this));
    }
    private void select(int request){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/zip","application/octet-stream","application/x-hnt","*/*"});
        startActivityForResult(i,request);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null||data.getData()==null)return;
        if(request!=PICK_DAY && request!=PICK_NIGHT)return;
        final boolean night=request==PICK_NIGHT;
        state.setText("جارٍ قراءة ملف HNT...");
        io.execute(()->{
            String resultMessage;
            try {
                int length=ThemeStorage.importHnt(this,data.getData(),night);
                resultMessage=(night?"الخلفية الليلية":"الخلفية النهارية")+" جاهزة ("+(length/1024)+" كيلوبايت)";
            }catch(Exception ex){
                resultMessage="تعذر استيراد الخلفية: "+ex.getMessage();
            }
            final String outcome=resultMessage;
            runOnUiThread(()->{
                refreshUi();
                android.widget.Toast.makeText(this,outcome,android.widget.Toast.LENGTH_LONG).show();
            });
        });
    }
    private void activateWallpaper(){
        if(!ThemeStorage.ready(this)){
            android.widget.Toast.makeText(this,"اختر ملفَي HNT أولًا",android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        Intent wallpaper=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        wallpaper.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
              new ComponentName(this,QasiounWallpaperService.class));
        try {
            startActivity(wallpaper);
        }catch(Exception issue){
            startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));
        }
    }
    @Override public void onConfigurationChanged(Configuration configuration){
        super.onConfigurationChanged(configuration);
        refreshUi();
    }
    @Override protected void onResume(){
        super.onResume();
        refreshUi();
    }
    @Override protected void onDestroy(){
        io.shutdown();
        super.onDestroy();
    }
}
