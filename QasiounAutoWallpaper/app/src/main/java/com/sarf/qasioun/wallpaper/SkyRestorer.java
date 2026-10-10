package com.sarf.qasioun.wallpaper;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Restores the sky behind the original photo's static flag once after loading.
 * The flag is extracted separately by FlagOverlay and is drawn over this background.
 */
final class SkyRestorer {
    private SkyRestorer() {}
    private static final int[][] DAY = {
        {143,218},{183,263},{254,304},{335,363},{428,408},{519,461},
        {615,508},{636,550},{644,635},{642,766},{619,787},{559,756},
        {495,729},{427,698},{345,679},{281,650},{210,617},{150,572}
    };
    private static final int[][] NIGHT = {
        {107,144},{159,188},{234,247},{334,312},{448,371},{546,443},
        {638,516},{658,545},{658,653},{658,730},{640,759},
        {559,709},{467,686},{357,634},{261,591},{199,566},{106,533}
    };
    private static int lerpColor(int left,int right,float t) {
        t=Math.max(0f,Math.min(1f,t));
        int r=(int)(Color.red(left)*(1f-t)+Color.red(right)*t);
        int g=(int)(Color.green(left)*(1f-t)+Color.green(right)*t);
        int b=(int)(Color.blue(left)*(1f-t)+Color.blue(right)*t);
        return Color.rgb(r,g,b);
    }
    static Bitmap erase(Bitmap original,boolean night) {
        final int w=original.getWidth(),h=original.getHeight();
        if(w<800||h<1600)throw new IllegalArgumentException("Expected portrait artwork");
        int[] buffer=new int[w*h];
        original.getPixels(buffer,0,w,0,0,w,h);
        int[] result=buffer.clone();
        int[][] poly=night?NIGHT:DAY;
        float xscale=w/1080.0f,yscale=h/2160.0f;
        float[] crosses=new float[poly.length];
        int minY=h,maxY=0;
        for(int[] vertex:poly){
            int y=(int)(vertex[1]*yscale);
            minY=Math.min(minY,y);maxY=Math.max(maxY,y);
        }
        final int pad=Math.max(12,Math.round(19*xscale));
        for(int y=Math.max(0,minY);y<=Math.min(h-1,maxY);y++) {
            float scan=(y+0.5f)/yscale;
            int count=0;
            for(int i=0;i<poly.length;i++){
                int[] a=poly[i],b=poly[(i+1)%poly.length];
                if((a[1]<=scan&&scan<b[1])||(b[1]<=scan&&scan<a[1])) {
                    float t=(scan-a[1])/(b[1]-a[1]);
                    crosses[count++]=(a[0]+t*(b[0]-a[0]))*xscale;
                }
            }
            if(count<2)continue;
            float left=w,right=0;
            for(int i=0;i<count;i++){
                left=Math.min(left,crosses[i]);right=Math.max(right,crosses[i]);
            }
            int x0=Math.max(0,(int)left-pad),x1=Math.min(w-1,(int)right+pad);
            int leftSample=Math.max(0,(int)left-pad*2-7);
            int rightSample=Math.min(w-1,(int)right+pad*2+7);
            int cLeft=buffer[y*w+leftSample],cRight=buffer[y*w+rightSample];
            for(int x=x0;x<=x1;x++){
                float t=(x-x0)/(float)Math.max(1,x1-x0);
                result[y*w+x]=lerpColor(cLeft,cRight,t);
            }
        }
        Bitmap clean=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        clean.setPixels(result,0,w,0,0,w,h);
        return clean;
    }
}