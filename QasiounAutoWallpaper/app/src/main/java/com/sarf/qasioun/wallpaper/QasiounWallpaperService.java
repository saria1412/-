package com.sarf.qasioun.wallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Draws a real moving flag from the two lock portraits inside the user's R13 HNTs.
 * Sunrise/sunset is calculated from the GPS coordinates, NEVER device Light/Dark.
 * Rendering pauses when Android hides this wallpaper to conserve battery.
 */
public final class QasiounWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine(){return new AnimatedEngine();}

    private final class AnimatedEngine extends Engine {
        private static final long FRAME_INTERVAL_MS=90L; // ~11fps, only while visible
        private static final long SOLAR_CHECK_MS=18_000L;
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final Paint basePaint=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
        private final Runnable frame=new Runnable(){
            @Override public void run(){frame(false);}
        };
        private boolean visible, destroyed;
        private boolean night;
        private boolean first=true;
        private long nextSolarCheck=0;
        private Bitmap backdrop;
        private FlagOverlay flag;
        private String loadedPath="";
        private long modified=0;
        private long previousFrame=0;

        @Override public void onVisibilityChanged(boolean state){
            visible=state;
            handler.removeCallbacks(frame);
            if(state)frame(true);
        }
        @Override public void onSurfaceCreated(SurfaceHolder holder){
            super.onSurfaceCreated(holder);
            if(visible)frame(true);
        }
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height){
            super.onSurfaceChanged(holder,format,width,height);
            if(visible)frame(true);
        }
        @Override public void onSurfaceRedrawNeeded(SurfaceHolder holder){
            if(visible)frame(true);
        }
        private void releaseImages(){
            if(flag!=null){flag.release();flag=null;}
            if(backdrop!=null&&!backdrop.isRecycled())backdrop.recycle();
            backdrop=null;
        }
        private Bitmap load(File file,boolean isNight){
            if(!file.isFile())return null;
            if(backdrop!=null&&!backdrop.isRecycled() &&
                file.getAbsolutePath().equals(loadedPath) &&
                file.lastModified()==modified)return backdrop;
            releaseImages();
            loadedPath=file.getAbsolutePath();
            modified=file.lastModified();
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inPreferredConfig=Bitmap.Config.RGB_565;
            options.inDither=true;
            backdrop=BitmapFactory.decodeFile(file.getAbsolutePath(),options);
            if(backdrop==null)return null;
            if(backdrop.getHeight()>1.65*backdrop.getWidth()) {
                try {flag=new FlagOverlay(backdrop,isNight);}
                catch(RuntimeException ignored){flag=null;}
            }
            return backdrop;
        }
        private boolean calculateNight(){
            if(!GeoPreferences.has(QasiounWallpaperService.this))return false;
            ZonedDateTime now=ZonedDateTime.now(ZoneId.systemDefault());
            return SolarClock.isNight(now,
                GeoPreferences.latitude(QasiounWallpaperService.this),
                GeoPreferences.longitude(QasiounWallpaperService.this));
        }
        private void render(Bitmap img,long uptime){
            Canvas canvas=null;
            try {
                canvas=getSurfaceHolder().lockCanvas();
                if(canvas==null)return;
                canvas.drawColor(Color.rgb(7,39,31));
                if(img==null || img.isRecycled())return;
                float w=canvas.getWidth(),h=canvas.getHeight();
                float imgW=img.getWidth(),imgH=img.getHeight();
                float scale=Math.max(w/imgW,h/imgH);
                canvas.save();
                canvas.translate((w-imgW*scale)/2f,(h-imgH*scale)/2f);
                canvas.scale(scale,scale);
                canvas.drawBitmap(img,0f,0f,basePaint);
                if(flag!=null)flag.draw(canvas,uptime);
                canvas.restore();
            }catch(RuntimeException ignored){
                // Surface can disappear while the screen sleeps or the launcher changes.
            }finally{
                if(canvas!=null){
                    try{getSurfaceHolder().unlockCanvasAndPost(canvas);}
                    catch(RuntimeException ignored){}
                }
            }
        }
        private void frame(boolean force){
            handler.removeCallbacks(frame);
            if(destroyed||!visible)return;
            long now=SystemClock.uptimeMillis();
            if(force||first||now>=nextSolarCheck){
                try {
                    night=calculateNight();
                }catch(RuntimeException ignored){
                    // Keep the last known phase if timezone service briefly fails.
                }
                nextSolarCheck=now+SOLAR_CHECK_MS;
            }
            File image=ThemeStorage.image(QasiounWallpaperService.this,night);
            Bitmap b=load(image,night);
            render(b,now);
            first=false;
            previousFrame=now;
            if(!destroyed&&visible)handler.postDelayed(frame,FRAME_INTERVAL_MS);
        }
        @Override public void onDestroy(){
            destroyed=true;
            visible=false;
            handler.removeCallbacks(frame);
            releaseImages();
            super.onDestroy();
        }
    }
}
