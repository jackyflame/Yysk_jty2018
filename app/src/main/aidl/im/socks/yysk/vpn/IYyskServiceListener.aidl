package im.socks.yysk.vpn;

// Declare any non-default types here with import statements

interface IYyskServiceListener {
    // 状态变更
     oneway void onStatusChanged(int status);


    // 流量统计变更
     oneway void onTrafficUpdate(long rxRate, long txRate, long rxTotal, long txTotal);
}
