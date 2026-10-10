package com.sarf.qasioun.wallpaper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Automatic sunrise/sunset live wallpaper using actual MP4 video from R16 HNT.
 * Preferred: hardware decoded 30fps, seamless flag video. Fallback: fully intact
 * photo with gentle clipped fold highlights, never warped polygons or sky holes.
 * HONOR native lockscreen may ignore an external live wallpaper.
 */
public final class QasiounWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine() {return new VideoEngine();}

    private final class VideoEngine extends Engine {
        private static final long SOLAR_POLL_MS=12_000L;
        private static final long FALLBACK_FRAME_MS=33L;
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final Paint bitmapPaint=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG);
        private final Runnable tick=()->refresh(false);
        private boolean visible,dead,night,initialized,prepared;
        private MediaPlayer player;
        private String activeVideoPath="";
        private long activeVideoModified;
        private String lastVideoFailure="";
        private Bitmap fallbackPhoto;
        private FlagOverlay foldOverlay;
        private String photoPath="";
        private long photoModified;

        @Override public void onVisibilityChanged(boolean isVisible) {
            visible=isVisible;
            handler.removeCallbacks(tick);
            if(isVisible)refresh(true);
            else pauseVideo();
        }
        @Override public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            if(visible)refresh(true);
        }
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int w,int h) {
            super.onSurfaceChanged(holder,format,w,h);
            if(player!=null && holder.getSurface()!=null && holder.getSurface().isValid()) {
                try{player.setSurface(holder.getSurface());}catch(RuntimeException ignored){}
            }
            if(visible)refresh(true);
        }
        @Override public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            if(visible)refresh(true);
        }
        private boolean isNight() {
            if(!GeoPreferences.has(QasiounWallpaperService.this))return false;
            ZonedDateTime now=ZonedDateTime.now(ZoneId.systemDefault());
            return SolarClock.isNight(now,
                GeoPreferences.latitude(QasiounWallpaperService.this),
                GeoPreferences.longitude(QasiounWallpaperService.this));
        }
        private void pauseVideo() {
            if(player!=null&&prepared) {
                try{if(player.isPlaying())player.pause();}catch(RuntimeException ignored){}
            }
        }
        private void releaseVideo() {
            prepared=false;
            if(player!=null) {
                try{player.setOnPreparedListener(null);player.setOnErrorListener(null);player.stop();}
                catch(RuntimeException ignored){}
                try{player.release();}catch(RuntimeException ignored){}
                player=null;
            }
            activeVideoPath="";
            activeVideoModified=0L;
        }
        private void playVideo(File video) {
            if(dead || !visible || !video.isFile())return;
            String path=video.getAbsolutePath();
            if(path.equals(lastVideoFailure))return;
            if(player!=null && activeVideoPath.equals(path) &&
                video.lastModified()==activeVideoModified) {
                if(prepared) {
                    try{if(!player.isPlaying())player.start();}catch(RuntimeException ignored){}
                }
                return;
            }
            releaseVideo();
            activeVideoPath=path;
            activeVideoModified=video.lastModified();
            try {
                Surface surface=getSurfaceHolder().getSurface();
                if(surface==null||!surface.isValid())return;
                MediaPlayer p=new MediaPlayer();
                player=p;
                p.setDataSource(path);
                p.setSurface(surface);
                p.setLooping(true);
                p.setVolume(0f,0f);
                p.setOnPreparedListener(mp->{
                    if(dead||player!=mp)return;
                    prepared=true;
                    if(visible) {
                        try {
                            mp.setVideoScalingMode(
                                MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                            mp.start();
                        } catch(RuntimeException error){videoFailed(path);}
                    }
                });
                p.setOnErrorListener((mp,what,extra)->{
                    if(player==mp)videoFailed(path);
                    return true;
                });
                p.prepareAsync();
            }catch(Exception failed) {videoFailed(path);}
        }
        private void videoFailed(String path) {
            lastVideoFailure=path;
            releaseVideo();
            if(visible)drawFallback(SystemClock.uptimeMillis());
        }
        private void releasePhoto() {
            if(foldOverlay!=null){foldOverlay.release();foldOverlay=null;}
            if(fallbackPhoto!=null&&!fallbackPhoto.isRecycled())fallbackPhoto.recycle();
            fallbackPhoto=null;
        }
        private Bitmap loadPhoto(File image) {
            if(!image.isFile())return null;
            String path=image.getAbsolutePath();
            if(fallbackPhoto!=null&&!fallbackPhoto.isRecycled()&&
                photoPath.equals(path)&&photoModified==image.lastModified())
                return fallbackPhoto;
            releasePhoto();
            photoPath=path;
            photoModified=image.lastModified();
            BitmapFactory.Options opt=new BitmapFactory.Options();
            opt.inPreferredConfig=Bitmap.Config.RGB_565;
            opt.inDither=true;
            fallbackPhoto=BitmapFactory.decodeFile(path,opt);
            if(fallbackPhoto!=null&&fallbackPhoto.getHeight()>1.65*fallbackPhoto.getWidth()) {
                try{foldOverlay=new FlagOverlay(fallbackPhoto,night);}
                catch(RuntimeException invalid){foldOverlay=null;}
            }
            return fallbackPhoto;
        }
        private void drawFallback(long uptimeMillis) {
            Bitmap src=loadPhoto(ThemeStorage.image(QasiounWallpaperService.this,night));
            Canvas canvas=null;
            try {
                canvas=getSurfaceHolder().lockCanvas();
                if(canvas==null)return;
                canvas.drawColor(Color.rgb(7,44,37));
                if(src==null||src.isRecycled())return;
                float w=canvas.getWidth(),h=canvas.getHeight();
                float sx=src.getWidth(),sy=src.getHeight();
                float scale=Math.max(w/sx,h/sy);
                canvas.save();
                canvas.translate((w-sx*scale)/2f,(h-sy*scale)/2f);
                canvas.scale(scale,scale);
                canvas.drawBitmap(src,0,0,bitmapPaint);
                if(foldOverlay!=null)foldOverlay.draw(canvas,uptimeMillis);
                canvas.restore();
            }catch(RuntimeException ignored){}
            finally {
                if(canvas!=null)
                    try{getSurfaceHolder().unlockCanvasAndPost(canvas);}
                    catch(RuntimeException ignored){}
            }
        }
        private void refresh(boolean force) {
            handler.removeCallbacks(tick);
            if(dead||!visible)return;
            boolean phase=night;
            try{phase=isNight();}catch(RuntimeException ignored){}
            boolean changed=!initialized||phase!=night;
            night=phase;
            initialized=true;
            File video=ThemeStorage.video(QasiounWallpaperService.this,night);
            boolean useVideo=video.isFile() && video.length()>80_000;
            if(changed) {
                lastVideoFailure="";
                releaseVideo();
                releasePhoto();
            }
            if(useVideo&&!video.getAbsolutePath().equals(lastVideoFailure)) {
                if(changed||force||player==null || video.lastModified()!=activeVideoModified) {
                    // Shows a still until MediaPlayer's first decoded frame is ready.
                    drawFallback(SystemClock.uptimeMillis());
                    playVideo(video);
                } else if(prepared&&player!=null) {
                    try{if(!player.isPlaying())player.start();}catch(RuntimeException ignored){}
                }
            } else {
                if(player!=null)releaseVideo();
                drawFallback(SystemClock.uptimeMillis());
            }
            if(!dead&&visible)handler.postDelayed(tick,
                player!=null?SOLAR_POLL_MS:FALLBACK_FRAME_MS);
        }
        @Override public void onDestroy() {
            dead=true;
            visible=false;
            handler.removeCallbacks(tick);
            releaseVideo();
            releasePhoto();
            super.onDestroy();
        }
    }
}
