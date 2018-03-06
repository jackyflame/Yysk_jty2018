package com.github.shadowsocks;


public class System {
    static {
        java.lang.System.loadLibrary("system");
    }

    public static native int exec(String cmd);
    //public static native String getABI();

    /**
     * 发送fd到protect socket，发送后就关闭socket，不会等待返回
     *
     * @param fd   需要发送到fd
     * @param path socket的path
     * @return 返回-1表示发送失败，0表示成功
     */
    public static native int sendfd(int fd, String path);

    /**
     * 关闭fd
     *
     * @param fd
     */
    public static native void jniclose(int fd);
}
