package com.sarf.qasioun.wallpaper;

/** Physically motivated, gently traveling cloth waves: a pinned pole and three seamless harmonics. */
public final class FlagMotion {
    private FlagMotion() {}
    public static final double LOOP_SECONDS=8.0;

    private static double pin(double u) {
        return Math.pow(Math.max(0.0,Math.min(1.0,u)),1.70);
    }
    private static double phase(double seconds) {
        return 2.0*Math.PI*((seconds%LOOP_SECONDS)/LOOP_SECONDS);
    }
    public static float shiftX(float u,float v,double seconds) {
        double t=phase(seconds),a=pin(u);
        double wave1=t-6.2*u+0.45*v;
        double wave2=2.0*t-10.6*u-0.48*v;
        return (float)(a*(3.2*Math.sin(wave1)+1.75*Math.sin(wave2)));
    }
    public static float shiftY(float u,float v,double seconds) {
        double t=phase(seconds),a=pin(u);
        double wave1=t-6.2*u+0.45*v;
        double wave2=2.0*t-10.6*u-0.48*v;
        double wave3=3.0*t-15.9*u+0.67*v;
        return (float)(a*(14.3*Math.sin(wave1+0.2)+4.6*Math.sin(wave2-0.25)+1.25*Math.sin(wave3)));
    }
}