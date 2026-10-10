package com.sarf.qasioun.wallpaper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

/**
 * Real frame-by-frame flag animation using an alpha-cutout and Android bitmap mesh.
 * The pole and scenery stay still, while the flag cloth moves gently.
 * Mask coordinates are calibrated for R13 lock-portrait art (1080 x 2160).
 */
final class FlagOverlay {
    private static final int COLS=14;
    private static final int ROWS=12;
    private static final int[][] DAY={
        {143,218},{183,263},{254,304},{335,363},{428,408},{519,461},
        {615,508},{636,550},{644,635},{642,766},{619,787},{559,756},
        {495,729},{427,698},{345,679},{281,650},{210,617},{150,572}
    };
    private static final int[][] NIGHT={
        {107,144},{159,188},{234,247},{334,312},{448,371},{546,443},
        {638,516},{658,545},{658,653},{658,730},{640,759},
        {559,709},{467,686},{357,634},{261,591},{199,566},{106,533}
    };
    private final Bitmap flag;
    private final int left,top,cols,rows;
    private final float[] vertices;
    private final Paint paint=new Paint(Paint.FILTER_BITMAP_FLAG|Paint.DITHER_FLAG|Paint.ANTI_ALIAS_FLAG);

    FlagOverlay(Bitmap backdrop, boolean night) {
        if(backdrop==null||backdrop.getWidth()<800||backdrop.getHeight()<1600)
            throw new IllegalArgumentException("flag base requires portrait lock art");
        int[][] poly=night?NIGHT:DAY;
        float sx=backdrop.getWidth()/1080.0f;
        float sy=backdrop.getHeight()/2160.0f;
        int l=Integer.MAX_VALUE,t=Integer.MAX_VALUE,r=0,b=0;
        for(int[] v:poly){
            int x=Math.round(v[0]*sx),y=Math.round(v[1]*sy);
            l=Math.min(l,x); t=Math.min(t,y); r=Math.max(r,x); b=Math.max(b,y);
        }
        left=Math.max(0,l-6);top=Math.max(0,t-6);
        int right=Math.min(backdrop.getWidth(),r+10);
        int bottom=Math.min(backdrop.getHeight(),b+10);
        int width=right-left,height=bottom-top;
        flag=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas cut=new Canvas(flag);
        Path p=new Path();
        for(int i=0;i<poly.length;i++){
            float x=poly[i][0]*sx-left,y=poly[i][1]*sy-top;
            if(i==0)p.moveTo(x,y);else p.lineTo(x,y);
        }
        p.close();
        Paint white=new Paint(Paint.ANTI_ALIAS_FLAG);
        white.setColor(0xFFFFFFFF);
        cut.drawPath(p,white);
        Paint transfer=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        transfer.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        cut.drawBitmap(backdrop, -left, -top, transfer);
        transfer.setXfermode(null);
        cols=COLS;rows=ROWS;
        vertices=new float[(cols+1)*(rows+1)*2];
    }
    void draw(Canvas canvas, long uptimeMs){
        if(flag.isRecycled())return;
        double seconds=uptimeMs/1000.0;
        int i=0;
        for(int row=0;row<=rows;row++){
            float v=row/(float)rows;
            for(int column=0;column<=cols;column++){
                float u=column/(float)cols;
                float x=left+u*flag.getWidth()+FlagMotion.shiftX(u,v,seconds);
                float y=top+v*flag.getHeight()+FlagMotion.shiftY(u,v,seconds);
                vertices[i++]=x;
                vertices[i++]=y;
            }
        }
        canvas.drawBitmapMesh(flag,cols,rows,vertices,0,null,0,paint);
    }
    void release(){
        if(!flag.isRecycled())flag.recycle();
    }
}
