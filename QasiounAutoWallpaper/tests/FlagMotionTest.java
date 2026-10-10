import com.sarf.qasioun.wallpaper.FlagMotion;
public class FlagMotionTest {
    static void require(boolean ok,String text){if(!ok)throw new AssertionError(text);}
    public static void main(String[] args){
        require(Math.abs(FlagMotion.shiftX(0.0f,0.2f,1.0))<0.001,"flag remains anchored X");
        require(Math.abs(FlagMotion.shiftY(0.0f,0.2f,1.0))<0.001,"flag remains anchored Y");
        float dayA=FlagMotion.shiftY(1.0f,0.5f,0.0);
        float dayB=FlagMotion.shiftY(1.0f,0.5f,1.15);
        require(Math.abs(dayA-dayB)>0.75,"free-edge cloth animation is non-static");
        require(Math.abs(FlagMotion.shiftY(1.0f,0.5f,5.0)-dayA)<0.001,"wave loops");
        require(Math.abs(FlagMotion.shiftY(0.2f,0.5f,1.0)) <
                Math.abs(FlagMotion.shiftY(1f,0.5f,1.0))+0.01,"more motion near free edge");
        System.out.println("PASS: 5 real fabric motion/anchor/loop tests");
    }
}
