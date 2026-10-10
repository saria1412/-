package com.sarf.qasioun.wallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Sun-based automatic day/night wallpaper; independent from Android dark mode.
 * Recomputes position and current time every 30 seconds while visible,
 * including at sunrise/sunset boundary and when the screen wakes.
 */
public final class QasiounWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine(){return new SunEngine();}
    private final class SunEngine extends Engine {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final Paint brush=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
        private boolean visible, destroyed;
        private boolean wasNight;
        private boolean firstDraw=true;
        private Bitmap bitmap;
        private String cachedPath="";
        private long cachedModified;
        private final Runnable tick=new Runnable(){
            @Override public void run(){refreshFrame(false);}
        };
        @Override public void onVisibilityChanged(boolean value){
            visible=value;
            handler.removeCallbacks(tick);
            if(value)refreshFrame(true);
        }
        @Override public void onSurfaceCreated(SurfaceHolder holder){
            super.onSurfaceCreated(holder);
            if(visible)refreshFrame(true);
        }
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height){
            super.onSurfaceChanged(holder,format,width,height);
            if(visible)refreshFrame(true);
        }
        @Override public void onSurfaceRedrawNeeded(SurfaceHolder holder){
            if(visible)refreshFrame(true);
        }
        private Bitmap decode(File file){
            if(!file.isFile())return null;
            String path=file.getAbsolutePath();
            long modified=file.lastModified();
            if(bitmap!=null&&!bitmap.isRecycled()&&cachedPath.equals(path)&&
                cachedModified==modified)return bitmap;
            if(bitmap!=null&&!bitmap.isRecycled())bitmap.recycle();
            bitmap=null;
            cachedPath=path;
            cachedModified=modified;
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inPreferredConfig=Bitmap.Config.RGB_565;
            options.inDither=true;
            bitmap=BitmapFactory.decodeFile(path,options);
            return bitmap;
        }
        private void draw(File file){
            Bitmap source=decode(file);
            Canvas canvas=null;
            try{
                canvas=getSurfaceHolder().lockCanvas();
                if(canvas==null)return;
                canvas.drawColor(Color.rgb(7,39,31));
                if(source!=null&&!source.isRecycled()&&source.getWidth()>0&&source.getHeight()>0){
                    int w=canvas.getWidth(), h=canvas.getHeight();
                    float sx=source.getWidth(),sy=source.getHeight();
                    float scale=Math.max(w/sx,h/sy);
                    float pw=sx*scale,ph=sy*scale;
                    float left=(w-pw)/2.0f,top=(h-ph)/2.0f;
                    canvas.drawBitmap(source,null,new RectF(left,top,left+pw,top+ph),brush);
                }
            }catch(RuntimeException ignored) {
                // Surfaces can be destroyed while the device is sleeping.
            }finally{
                if(canvas!=null)
                    try{getSurfaceHolder().unlockCanvasAndPost(canvas);}catch(Exception ignored){}
            }
        }
        private void refreshFrame(boolean force){
            handler.removeCallbacks(tick);
            if(!visible||destroyed)return;
            boolean night=false;
            try{
                if(GeoPreferences.has(QasiounWallpaperService.this)){
                    ZonedDateTime now=ZonedDateTime.now(ZoneId.systemDefault());
                    night=SolarClock.isNight(now,GeoPreferences.latitude(QasiounWallpaperService.this),
                        GeoPreferences.longitude(QasiounWallpaperService.this));
                }
            }catch(Exception ignored){}
            File target=ThemeStorage.image(QasiounWallpaperService.this,night);
            if(force||firstDraw||wasNight!=night||cachedModified!=target.lastModified()||
               !target.getAbsolutePath().equals(cachedPath)){
                draw(target);
                firstDraw=false;
                wasNight=night;
            }
            if(visible&&!destroyed)handler.postDelayed(tick,30_000L);
        }
        @Override public void onDestroy(){
            destroyed=true;
            visible=false;
            handler.removeCallbacks(tick);
            if(bitmap!=null&&!bitmap.isRecycled())bitmap.recycle();
            bitmap=null;
            super.onDestroy();
        }
    }
}
