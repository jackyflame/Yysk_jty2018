package im.socks.yysk.vpn;

public interface ITunnel {
    /**
     * 启动
     * @return true表示启动成功
     */
    boolean start();

    /**
     * 停止
     */
    void stop();

    /**
     * 获得状态
     * @return
     */
    int getStatus();

    /**
     * 获得开始时间
     * @return
     */
    long getStartTime();

    //ParcelFileDescriptor getFileDescriptor();


}
