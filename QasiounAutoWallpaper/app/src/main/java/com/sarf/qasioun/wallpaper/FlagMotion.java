package com.sarf.qasioun.wallpaper;

/** Pure math for a pinned flag waving at the free edge; deterministic and unit-testable. */
public final class FlagMotion {
    private FlagMotion() {}
    static final double LOOP_SECONDS=5.0;
    public static float shiftX(float u, float v, double seconds) {
        float anchor=(float)Math.pow(Math.max(0.0,Math.min(1.0,u)),1.45);
        double phase=2.0*Math.PI*(seconds%LOOP_SECONDS)/LOOP_SECONDS;
        return (float)(anchor*5.5*Math.sin(phase-7.0*u+0.5*v));
    }
    public static float shiftY(float u, float v, double seconds) {
        float anchor=(float)Math.pow(Math.max(0.0,Math.min(1.0,u)),1.45);
        double phase=2.0*Math.PI*(seconds%LOOP_SECONDS)/LOOP_SECONDS;
        return (float)(anchor*(10.0*Math.sin(phase-9.0*u+0.8*v)
                +5.0*Math.cos(phase*1.2-4.5*u)));
    }
}
