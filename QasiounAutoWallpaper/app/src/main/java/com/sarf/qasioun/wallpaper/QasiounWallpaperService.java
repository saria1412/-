package com.sarf.qasioun.wallpaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.service.wallpaper.WallpaperService;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * V1.4 uses complete, pre-rendered R16 H.264 lock movies (no jagged
 * alpha polygons, no double silhouettes, no reconstructed sky artifacts).
 *
 * Sunrise/sunset switching is based on GPS coordinates, not system Dark mode.
 * Videos are hardware-decoded only while the live wallpaper is visible.
 */
public final class QasiounWallpaperService extends WallpaperService {
    @Override public Engine onCreateEngine() { return new VideoEngine(); }

    private final class VideoEngine extends Engine {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final Runnable tick=new Runnable() {
            @Override public void run() { checkPhase(); }
        };
        private static final long CHECK_MS=15_000L;
        private boolean visible;
        private boolean destroyed;
        private boolean surfaceReady;
        private boolean prepared;
        private MediaPlayer player;
        private String playingPath="";
        private long playingModified=0;

        @Override public void onVisibilityChanged(boolean state) {
            visible=state;
            handler.removeCallbacks(tick);
            if(state)checkPhase();
            else pausePlayback();
        }
        @Override public void onSurfaceCreated(SurfaceHolder h) {
            super.onSurfaceCreated(h);
            surfaceReady=true;
            checkPhase();
        }
        @Override public void onSurfaceChanged(SurfaceHolder h,int format,int width,int height) {
            super.onSurfaceChanged(h,format,width,height);
            surfaceReady=true;
            // Rebinding a new Surface is safe after a rotation or launcher rebuild.
            if(player!=null)try{player.setSurface(h.getSurface());}catch(RuntimeException ignored){}
            checkPhase();
        }
        @Override public void onSurfaceDestroyed(SurfaceHolder h) {
            surfaceReady=false;
            releasePlayer();
            super.onSurfaceDestroyed(h);
        }
        private boolean night() {
            if(!GeoPreferences.has(QasiounWallpaperService.this))return false;
            ZonedDateTime now=ZonedDateTime.now(ZoneId.systemDefault());
            return SolarClock.isNight(now,
                GeoPreferences.latitude(QasiounWallpaperService.this),
                GeoPreferences.longitude(QasiounWallpaperService.this));
        }
        private void pausePlayback() {
            if(player!=null && prepared) {
                try{if(player.isPlaying())player.pause();}catch(RuntimeException ignored){}
            }
        }
        private void releasePlayer() {
            MediaPlayer old=player;
            player=null;
            prepared=false;
            playingPath="";
            playingModified=0;
            if(old!=null) {
                try{old.setOnPreparedListener(null);old.setOnErrorListener(null);
                    old.stop();}catch(Exception ignored){}
                try{old.release();}catch(Exception ignored){}
            }
        }
        private void safeBackground() {
            Canvas canvas=null;
            try{
                canvas=getSurfaceHolder().lockCanvas();
                if(canvas!=null)canvas.drawColor(Color.rgb(5,42,34));
            }catch(RuntimeException ignored){}
            finally{
                if(canvas!=null)try{getSurfaceHolder().unlockCanvasAndPost(canvas);}catch(RuntimeException ignored){}
            }
        }
        private void playVideo(File target) {
            if(!target.isFile()||target.length()<150_000L) {
                releasePlayer();
                safeBackground();
                return;
            }
            releasePlayer();
            playingPath=target.getAbsolutePath();
            playingModified=target.lastModified();
            safeBackground();
            try{
                MediaPlayer next=new MediaPlayer();
                player=next;
                prepared=false;
                next.setDataSource(playingPath);
                Surface surface=getSurfaceHolder().getSurface();
                if(surface==null||!surface.isValid()) {
                    releasePlayer();
                    return;
                }
                next.setSurface(surface);
                next.setVolume(0f,0f);
                next.setLooping(true);
                next.setOnPreparedListener(mp->{
                    if(mp!=player||destroyed)return;
                    prepared=true;
                    try{
                        mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                        if(visible&&surfaceReady)mp.start();
                    }catch(RuntimeException ignored){}
                });
                next.setOnErrorListener((mp,what,extra)->{
                    handler.post(()->{
                        if(player==mp){releasePlayer();safeBackground();}
                    });
                    return true;
                });
                next.prepareAsync();
            }catch(Exception unavailable){
                releasePlayer();
                safeBackground();
            }
        }
        private void checkPhase() {
            handler.removeCallbacks(tick);
            if(destroyed||!visible||!surfaceReady)return;
            boolean isNight=false;
            try{isNight=night();}catch(RuntimeException ignored){}
            File target=ThemeStorage.video(QasiounWallpaperService.this,isNight);
            if(!target.getAbsolutePath().equals(playingPath)
               ||target.lastModified()!=playingModified||player==null){
                playVideo(target);
            } else if(prepared) {
                try{if(!player.isPlaying())player.start();}catch(RuntimeException ignored){}
            }
            if(!destroyed&&visible)handler.postDelayed(tick,CHECK_MS);
        }
        @Override public void onDestroy() {
            destroyed=true;
            visible=false;
            handler.removeCallbacks(tick);
            releasePlayer();
            super.onDestroy();
        }
    }
}
