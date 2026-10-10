package com.sarf.qasioun.wallpaper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * Safe animated satin fold highlights clipped to precisely segmented Syrian-flag contours.
 * Unlike the old bitmap mesh, NO original pixels move beyond the outline.
 * This prevents split cloth, sky holes, and doubled/missing flag sections.
 */
final class FlagOverlay {
    private static final float[] DAY = {
        74,119, 68,291, 86,300, 110,307, 133,321, 178,335,
        198,353, 211,360, 256,370, 279,387, 295,393, 308,394,
        316,399, 315,337, 318,331, 319,305, 314,296, 314,284,
        307,273, 309,258, 275,240, 264,230, 189,199, 170,183
    };
    private static final float[] NIGHT = {
        56,71, 50,264, 93,280, 116,295, 178,318, 190,329,
        216,341, 260,351, 275,365, 290,373, 321,377, 321,308,
        324,302, 324,275, 318,265, 319,251, 311,239, 313,224,
        281,211, 260,194, 201,169, 177,154, 156,135
    };
    private static final long LOOP_MILLIS = 8000L;
    private final Path silhouette = new Path();
    private final RectF region = new RectF();
    private final float period;
    private final Paint wide=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    private final Paint fine=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DITHER_FLAG);
    private final Shader largeFold;
    private final Shader smallFold;
    private final Matrix shaderMatrix = new Matrix();

    FlagOverlay(Bitmap image,boolean night) {
        if(image==null || image.getWidth()<800 || image.getHeight()<1600)
            throw new IllegalArgumentException("Expected R13 or R16 portrait art.");
        final float sx=image.getWidth()/540f;
        final float sy=image.getHeight()/1080f;
        float[] verts=night?NIGHT:DAY;
        for(int index=0;index<verts.length;index+=2) {
            float x=verts[index]*sx;
            float y=verts[index+1]*sy;
            if(index==0) silhouette.moveTo(x,y);
            else silhouette.lineTo(x,y);
        }
        silhouette.close();
        silhouette.computeBounds(region,true);
        period=(region.width() * .48f);
        // Translucent, repeating highlights and soft folds. Both ends are zero-alpha,
        // so the 8-second loop does not jump.
        largeFold=new LinearGradient(region.left,region.top,
            region.left+period,region.top+region.height()*.11f,
            new int[]{0x00000000,0x13FFF7E4,0x00000000,0x18000000,0x00000000},
            new float[]{0f,.17f,.46f,.76f,1f},Shader.TileMode.REPEAT);
        smallFold=new LinearGradient(region.left,region.top,
            region.left+period*.55f,region.top-region.height()*.08f,
            new int[]{0x00000000,0x12FFFAEE,0x00000000,0x10000000,0x00000000},
            new float[]{0f,.22f,.5f,.81f,1f},Shader.TileMode.REPEAT);
        wide.setShader(largeFold);
        fine.setShader(smallFold);
    }

    static float loopPhase(long uptimeMillis) {
        long phase=uptimeMillis%LOOP_MILLIS;
        return phase<0?0f:phase/(float)LOOP_MILLIS;
    }
    void draw(Canvas canvas,long uptimeMs) {
        final float phase=loopPhase(uptimeMs);
        canvas.save();
        canvas.clipPath(silhouette);
        shaderMatrix.reset();
        shaderMatrix.setTranslate(-period*phase,0f);
        largeFold.setLocalMatrix(shaderMatrix);
        canvas.drawRect(region,wide);
        shaderMatrix.reset();
        shaderMatrix.setTranslate(period*.55f*2f*phase,0f);
        smallFold.setLocalMatrix(shaderMatrix);
        canvas.drawRect(region,fine);
        canvas.restore();
    }
    void release() { /* No copy of any wallpaper bitmap or additional memory. */ }
}
