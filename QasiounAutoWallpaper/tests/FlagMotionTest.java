import com.sarf.qasioun.wallpaper.FlagMotion;

public final class FlagMotionTest {
    static void check(boolean ok,String label) {
        if (!ok)throw new AssertionError(label);
    }
    public static void main(String[] args) {
        for(int i=0;i<240;i++){
            double time=i/30.0;
            check(Math.abs(FlagMotion.shiftX(0,0.35f,time))<0.0001, "Pinned pole X");
            check(Math.abs(FlagMotion.shiftY(0,0.35f,time))<0.0001, "Pinned pole Y");
        }
        boolean visiblyMoves=false;
        float maxX=0f,maxY=0f;
        for(int i=0;i<240;i++) {
            double t=i/30.0;
            float x=FlagMotion.shiftX(1f,0.5f,t);
            float y=FlagMotion.shiftY(1f,0.5f,t);
            maxX=Math.max(maxX,Math.abs(x));
            maxY=Math.max(maxY,Math.abs(y));
            if(i>0 && Math.abs(y-FlagMotion.shiftY(1f,0.5f,t-1./30))>0.04)
                visiblyMoves=true;
            check(Math.abs(x)<5.2f,"Motion amplitude X safe");
            check(Math.abs(y)<21f,"Motion amplitude Y safe");
        }
        check(visiblyMoves,"Visible frame-by-frame movement");
        check(maxY>8.0f,"Realistic waving free edge");
        // Integer temporal harmonics guarantee a truly loopable 8s motion.
        for(float u:new float[]{0f,.25f,.5f,1f}) {
            for(float v:new float[]{0f,.5f,1f}) {
                check(Math.abs(FlagMotion.shiftX(u,v,0)-FlagMotion.shiftX(u,v,8.0))<0.001,"Seamless X");
                check(Math.abs(FlagMotion.shiftY(u,v,0)-FlagMotion.shiftY(u,v,8.0))<0.001,"Seamless Y");
                check(Math.abs(FlagMotion.shiftY(u,v,0)-FlagMotion.shiftY(u,v,7.999))<0.1,"C1 loop neighborhood");
            }
        }
        check(maxX>1.0f,"Horizontal flag deformation");
        System.out.println("PASS: pinned edges, 8-second loop, 30fps continuity, safe amplitude.");
    }
}