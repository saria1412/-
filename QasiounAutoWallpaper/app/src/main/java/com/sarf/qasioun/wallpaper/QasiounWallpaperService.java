package com.sarf.qasioun.wallpaper;

import android.content.res.Configuration;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Listens to the system UI_MODE_NIGHT setting. HONOR already determines sunset /
 * sunrise based on its own "Dark from sunset to sunrise" schedule.
 * Does not use location, internet, root, or privileged theme-manager APIs.
 */
public final class QasiounWallpaperService extends WallpaperService {
    private final Set<ModeEngine> engines=new HashSet<>();
    @Override public Engine onCreateEngine(){
        ModeEngine engine=new ModeEngine();
        engines.add(engine);
        return engine;
    }
    @Override public void onConfigurationChanged(Configuration updated){
        super.onConfigurationChanged(updated);
        for(ModeEngine engine:new HashSet<>(engines))engine.refreshNow();
    }
    private final class ModeEngine extends Engine {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final Paint imagePaint=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
        private boolean visible=false,dead=false;
        private int displayedMode=-1;
        private String bitmapPath="";
        private long modifiedTime=0;
        private Bitmap bitmap;
        private final Runnable tick=new Runnable(){
            @Override public void run(){checkAndDraw(false);}
        };
        @Override public void onVisibilityChanged(boolean nowVisible){
            visible=nowVisible;
            handler.removeCallbacks(tick);
            if(nowVisible)checkAndDraw(true);
        }
        @Override public void onSurfaceCreated(SurfaceHolder holder){
            super.onSurfaceCreated(holder);
            checkAndDraw(true);
        }
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height){
            super.onSurfaceChanged(holder,format,width,height);
            checkAndDraw(true);
        }
        @Override public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            checkAndDraw(true);
        }
        void refreshNow(){
            checkAndDraw(true);
        }
        private boolean isNight(){
            Configuration cfg=getResources().getConfiguration();
            return (cfg.uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
        }
        private Bitmap getImage(File f){
            if(!f.isFile())return null;
            String path=f.getAbsolutePath();
            long changed=f.lastModified();
            if(bitmap!=null&&!bitmap.isRecycled()&&path.equals(bitmapPath)&&changed==modifiedTime)return bitmap;
            if(bitmap!=null&&!bitmap.isRecycled())bitmap.recycle();
            bitmap=null;
            bitmapPath=path;
            modifiedTime=changed;
            BitmapFactory.Options opts=new BitmapFactory.Options();
            opts.inPreferredConfig=Bitmap.Config.RGB_565;
            opts.inSampleSize=1;
            bitmap=BitmapFactory.decodeFile(path,opts);
            return bitmap;
        }
        private void checkAndDraw(boolean force){
            handler.removeCallbacks(tick);
            if(dead||!visible||!getSurfaceHolder().getSurface().isValid())return;
            boolean night=isNight();
            int mode=night?1:0;
            File asset=ThemeStorage.image(QasiounWallpaperService.this,night);
            long modification=asset.lastModified();
            if(force||mode!=displayedMode||modification!=modifiedTime){
                drawFrame(asset);
                displayedMode=mode;
            }
            if(!dead&&visible)handler.postDelayed(tick,30_000L);
        }
        private void drawFrame(File f){
            Bitmap img=getImage(f);
            Canvas canvas=null;
            try {
                canvas=getSurfaceHolder().lockCanvas();
                if(canvas==null)return;
                canvas.drawColor(Color.rgb(7,46,38));
                if(img!=null&&img.getWidth()>0&&img.getHeight()>0){
                    int width=canvas.getWidth(),height=canvas.getHeight();
                    float sx=img.getWidth(),sy=img.getHeight();
                    float scale=Math.max(width/sx,height/sy);
                    float renderW=sx*scale,renderH=sy*scale;
                    float x=(width-renderW)/2f,y=(height-renderH)/2f;
                    canvas.drawBitmap(img,null,new RectF(x,y,x+renderW,y+renderH),imagePaint);
                }
            }catch(Exception ignored){}
            finally {
                if(canvas!=null)try{getSurfaceHolder().unlockCanvasAndPost(canvas);}catch(Exception ignored){}
            }
        }
        @Override public void onDestroy(){
            dead=true;
            visible=false;
            handler.removeCallbacks(tick);
            if(bitmap!=null&&!bitmap.isRecycled())bitmap.recycle();
            bitmap=null;
            engines.remove(this);
            super.onDestroy();
        }
    }
}
