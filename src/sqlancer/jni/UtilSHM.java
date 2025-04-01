package sqlancer.jni;

public class UtilSHM {
    static {
        System.loadLibrary("utilshm"); // loads libutilshm.so on Linux
    }

    public static native void initBarrier(int threshold);
    public static native void updateBarrier(int threshold);
    public static native void initLoc(int pid_1, int loc_1, int target_counter_1, int pid_2, int loc_2, int target_counter_2);
}