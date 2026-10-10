package com.sarf.qasioun.wallpaper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Explicit SAF folder/file selection and user-approved GPS coordinates.
 * A live wallpaper needs only the cached images and solar coordinates.
 */
public final class MainActivity extends Activity {
    private static final int REQUEST_TREE=501;
    private static final int REQUEST_DAY=502;
    private static final int REQUEST_NIGHT=503;
    private static final int REQUEST_LOCATION=504;
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private LinearLayout panel;
    private TextView status;
    private TextView gpsStatus;
    private Button apply;
    private final int gold=0xFFE9CC91, pale=0xFFF4F0E5, subdued=0xFFCED8D0;
    private volatile boolean fileBusy;

    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    private GradientDrawable rounded(int color) {
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(15));
        g.setStroke(dp(1),0xFFB4A06C);
        return g;
    }
    private TextView text(String value,int sp,int color,boolean bold) {
        TextView v=new TextView(this);
        v.setText(value);
        v.setTextColor(color);
        v.setTextSize(sp);
        v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        v.setIncludeFontPadding(false);
        if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return v;
    }
    private void addTitle(String value,int font,int color,int top) {
        TextView v=text(value,font,color,true);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(top);
        panel.addView(v,lp);
    }
    private void addNote(String value,int top) {
        TextView v=text(value,13,subdued,false);
        v.setLineSpacing(dp(4),1.0f);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(top);
        panel.addView(v,lp);
    }
    private Button addButton(String label,boolean primary,Runnable click) {
        Button b=new Button(this);
        b.setAllCaps(false);
        b.setText(label);
        b.setTextSize(14.5f);
        b.setTextColor(primary?0xFF123B30:pale);
        b.setBackground(rounded(primary?gold:0xFF1D4B3E));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(49));
        lp.topMargin=dp(10);
        panel.addView(b,lp);
        b.setOnClickListener(v->click.run());
        return b;
    }
    private TextView addStatus() {
        TextView v=text("",13,gold,false);
        v.setPadding(dp(12),dp(12),dp(12),dp(12));
        v.setBackground(rounded(0xFF124337));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
        lp.topMargin=dp(10);
        panel.addView(v,lp);
        return v;
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(0xFF072C24);
        getWindow().setNavigationBarColor(0xFF072C24);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFF0B332B);
        panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        panel.setPadding(dp(19),dp(25),dp(19),dp(30));
        scroll.addView(panel);
        addTitle("قاسيون | الصباح والمساء",24,gold,5);
        addNote("تُشغّل نسخة R16 فيديو العلم بسلاسة عند الشروق والغروب حسب موقع GPS. لا يعتمد التبديل على Light/Dark.",9);
        addTitle("١. صور الثيم من مجلد HONOR",17,pale,22);
        status=addStatus();
        addButton("اختر المجلد Honor / Themes",true,this::chooseFolder);
        addButton("إعادة استيراد R16 نهارًا وليلًا (بالفيديو)",false,this::rescanFolder);
        addButton("اختيار النهاري HNT منفردًا",false,()->chooseFile(false));
        addButton("اختيار الليلي HNT منفردًا",false,()->chooseFile(true));
        addButton("عرض صورة النهار قبل الحركة",false,()->preview(false));
        addButton("عرض صورة الليل قبل الحركة",false,()->preview(true));
        addTitle("٢. الشروق والغروب حسب الموقع",17,pale,22);
        gpsStatus=addStatus();
        addButton("تحديد الموقع الحالي GPS",true,this::requestLocation);
        addButton("استخدام جدة مؤقتًا (يدوي)",false,()->{
            GeoPreferences.save(this,21.5433,39.1728,"جدة - اختيار يدوي",-1);
            refreshState();
        });
        addTitle("٣. تفعيل الخلفية التلقائية",17,pale,22);
        apply=addButton("تفعيل الخلفية المتحركة للعلم",true,this::activateWallpaper);
        addNote("للحركة الطبيعية دون تقطيع: اختر ملفَي R16 الموجودين في Honor/Themes أو حددهما منفردين. يتحقق التطبيق من فيديو القفل داخل كل HNT.",13);
        addNote("تنبيه: لا يستطيع هذا التطبيق تغيير أيقونات HNT تلقائيًا. لاستخدام الأيقونات العاجية نهارًا، اختر أيقونات ثيم R13 النهاري من تطبيق HONOR Themes يدويًا، دون تغيير الخلفية الحية.",13);
        addNote("لتغيير مكان ساعة القفل: افتح تخصيص شاشة القفل في HONOR، واضغط مطولًا على الساعة واسحبها أسفل العلم، إذا كان نمط القفل يدعم ذلك.",13);
        setContentView(scroll);
        refreshState();
    }
    private void refreshState() {
        if(status!=null) {
            status.setText("النهار: "+ThemeStorage.selected(this,false)+"\n"+
                 (ThemeStorage.image(this,false).isFile()?
                        (ThemeStorage.hasMotion(this,false)?"✓ فيديو النهار جاهز":"صورة نهارية فقط؛ اختر R16 للحركة الطبيعية"):
                        "يجب استيراد ملف HNT")+"\n"+
                 "الليل: "+ThemeStorage.selected(this,true)+"\n"+
                 (ThemeStorage.image(this,true).isFile()?
                        (ThemeStorage.hasMotion(this,true)?"✓ فيديو الليل جاهز":"صورة ليلية فقط؛ اختر R16 للحركة الطبيعية"):
                        "يجب استيراد ملف HNT"));
        }
        if(gpsStatus!=null) {
            if(!GeoPreferences.has(this)) {
                gpsStatus.setText("الموقع غير محدد. فعّل GPS، أو اختر جدة مؤقتًا.");
            } else {
                try {
                    ZoneId zone=ZoneId.systemDefault();
                    ZonedDateTime now=ZonedDateTime.now(zone);
                    SolarClock.Events times=SolarClock.forDate(now.toLocalDate(),
                       GeoPreferences.latitude(this),GeoPreferences.longitude(this),zone);
                    DateTimeFormatter fmt=DateTimeFormatter.ofPattern("HH:mm",Locale.US);
                    String phase=SolarClock.isNight(now,GeoPreferences.latitude(this),
                         GeoPreferences.longitude(this))?"المساء":"النهار";
                    String accuracy=GeoPreferences.accuracy(this)>0 ?
                            " · دقة نحو "+Math.round(GeoPreferences.accuracy(this))+" متر" : "";
                    gpsStatus.setText("الموقع: "+GeoPreferences.source(this)+accuracy+
                        "\nالشروق اليوم: "+times.sunrise.format(fmt)+
                        "  |  الغروب اليوم: "+times.sunset.format(fmt)+
                        "\nالوضع المتوقع الآن: "+phase+" (حسب "+zone.getId()+")");
                } catch(Exception e) {
                    gpsStatus.setText("الموقع محفوظ لكن تعذر حساب الشروق والغروب: "+e.getMessage());
                }
            }
        }
        if(apply!=null)apply.setEnabled(ThemeStorage.ready(this)&&GeoPreferences.has(this)&&!fileBusy);
    }
    private void show(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }
    private void chooseFolder() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|
          Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try{startActivityForResult(i,REQUEST_TREE);}
        catch(Exception e){show("مدير الملفات لا يدعم اختيار المجلد؛ اختر ملفات HNT منفردة");}
    }
    private void rescanFolder() {
        Uri saved=ThemeStorage.folder(this);
        if(saved==null){show("اختر المجلد Honor/Themes أولًا");return;}
        scanInBackground(saved);
    }
    private void chooseFile(boolean night) {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|
           Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i,night?REQUEST_NIGHT:REQUEST_DAY);
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try {
            getContentResolver().takePersistableUriPermission(
                uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }catch(SecurityException ignored){}
        if(request==REQUEST_TREE) {
            scanInBackground(uri);
        }else if(request==REQUEST_DAY||request==REQUEST_NIGHT) {
            boolean night=request==REQUEST_NIGHT;
            importFileInBackground(uri,night);
        }
    }
    private void scanInBackground(Uri tree) {
        if(fileBusy){show("انتظر انتهاء القراءة");return;}
        fileBusy=true;
        status.setText("جارٍ فحص ملفي HNT داخل المجلد…");
        refreshButtons();
        executor.execute(()->{
            try{
                String result=ThemeStorage.importFolder(this,tree);
                runOnUiThread(()->{
                    fileBusy=false;refreshState();show(result);
                });
            }catch(Exception error){
                runOnUiThread(()->{
                    fileBusy=false;refreshState();
                    show("تعذر قراءة المجلد: "+error.getMessage());
                    status.setText(status.getText()+"\nخطأ: "+error.getMessage());
                });
            }
        });
    }
    private void importFileInBackground(Uri uri,boolean night) {
        if(fileBusy)return;
        fileBusy=true;
        status.setText("جارٍ قراءة الخلفية من HNT…");
        refreshButtons();
        executor.execute(()->{
            try {
                String filename="ملف HNT "+(night?"الليلي":"النهاري");
                ThemeStorage.importHnt(this,uri,night,filename);
                runOnUiThread(()->{
                    fileBusy=false;refreshState();show("تم استيراد "+filename);
                });
            }catch(Exception error){
                runOnUiThread(()->{
                    fileBusy=false;refreshState();show("تعذر الاستيراد: "+error.getMessage());
                });
            }
        });
    }
    private void refreshButtons(){
        if(apply!=null)apply.setEnabled(false);
    }
    private void preview(boolean night) {
        java.io.File file=ThemeStorage.image(this,night);
        if(!file.isFile()){show("الملف غير مستورد بعد");return;}
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inSampleSize=2;
        Bitmap b=BitmapFactory.decodeFile(file.getAbsolutePath(),options);
        if(b==null){show("تعذر معاينة الصورة");return;}
        ImageView view=new ImageView(this);
        view.setImageBitmap(b);
        view.setAdjustViewBounds(true);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        new AlertDialog.Builder(this).setTitle(night?"خلفية الليل":"خلفية النهار")
            .setView(view).setPositiveButton("حسنًا",(dialog,ignored)->{
                view.setImageDrawable(null);
                b.recycle();
            }).show();
    }
    private void requestLocation() {
        if(Build.VERSION.SDK_INT>=23 &&
           checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
           checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOCATION);
            return;
        }
        queryLocation();
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){
        super.onRequestPermissionsResult(request,permissions,results);
        if(request==REQUEST_LOCATION) {
            boolean allowed=false;
            for(int x:results)if(x==PackageManager.PERMISSION_GRANTED)allowed=true;
            if(allowed)queryLocation();
            else show("لم يتم منح إذن الموقع. اختر جدة مؤقتًا، أو امنح الإذن من إعدادات التطبيق.");
        }
    }
    private void queryLocation(){
        LocationManager manager=(LocationManager)getSystemService(LOCATION_SERVICE);
        if(manager==null){show("خدمة الموقع غير متاحة");return;}
        gpsStatus.setText("جارٍ تحديد الموقع الحالي…");
        boolean requested=false;
        for(String provider:new String[]{LocationManager.NETWORK_PROVIDER,LocationManager.GPS_PROVIDER}){
            if(!manager.isProviderEnabled(provider))continue;
            requested=true;
            try{
                if(Build.VERSION.SDK_INT>=30) {
                    manager.getCurrentLocation(provider,null,getMainExecutor(),this::receivedLocation);
                } else {
                    manager.requestSingleUpdate(provider,new LocationListener(){
                        @Override public void onLocationChanged(Location location){
                            receivedLocation(location);
                        }
                    },Looper.getMainLooper());
                }
            }catch(SecurityException denied) {
                show("يرجى منح إذن موقع الجهاز");
            }catch(Exception ignored){}
        }
        if(!requested) {
            gpsStatus.setText("GPS مغلق. فعّل الموقع من إعدادات الهاتف ثم أعد المحاولة.");
        }
    }
    private void receivedLocation(Location location) {
        if(location==null)return;
        if(location.getLatitude()< -90||location.getLatitude()>90||
           location.getLongitude()< -180||location.getLongitude()>180)return;
        float last=GeoPreferences.accuracy(this);
        long updated=GeoPreferences.updated(this);
        // Within a location-request burst, keep the more accurate fix.
        if(GeoPreferences.has(this)&&
            System.currentTimeMillis()-updated<30_000L&&last>0 &&
            location.hasAccuracy()&&location.getAccuracy()>last*1.8f)return;
        GeoPreferences.save(this,location.getLatitude(),location.getLongitude(),
            "GPS ("+location.getProvider()+")",location.hasAccuracy()?location.getAccuracy():-1);
        refreshState();
        show("تم تحديث الشروق والغروب حسب الموقع");
    }
    private void activateWallpaper() {
        if(!ThemeStorage.ready(this)||!GeoPreferences.has(this)){
            show("اختر الثيمين والموقع أولًا");return;
        }
        Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            new ComponentName(this,QasiounWallpaperService.class));
        try{startActivity(i);}
        catch(Exception error){
            try{startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER));}
            catch(Exception ignored){show("افتح تطبيق الخلفيات في HONOR واختر قاسيون | شروق وغروب");}
        }
    }
    @Override protected void onResume(){super.onResume();refreshState();}
    @Override protected void onDestroy(){executor.shutdown();super.onDestroy();}
}
