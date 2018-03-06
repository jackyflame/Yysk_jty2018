package im.socks.yysk.vpn;

/**
 * Created by cole on 2017/10/16.
 */

public interface ITunnelListener {
    /**
     * 获得统计信息
     * @param rxRate
     * @param txRate
     * @param rxTotal
     * @param txTotal
     */
    void onTrafficUpdate(long rxRate, long txRate, long rxTotal, long txTotal);

    /**
     * 获得当前的状态
     * @param status
     */
    void onStatusChanged(int status);
}
