// IYyskService.aidl
package im.socks.yysk.vpn;

import im.socks.yysk.vpn.IYyskServiceListener;

// Declare any non-default types here with import statements

interface IYyskService {

  // 注册
   oneway void addListener(IYyskServiceListener listener);

  // 反注册
   oneway void removeListener(IYyskServiceListener listener);

   int getStatus();

   long getStartTime();


   //执行http get，如果是在同一个进程，因为执行了网络操作，不能够在主线程中执行
   //如果在不同的进程，因为提供服务的在线程池中执行，所以，远程可以在主进程或者其他
   String doGet(String url);

  // 启动
   //oneway void start(int profileId);

  // 停止
   //oneway void stop();

  // 启动流量监视
   //oneway void startTrafficMonitor();

  // 停止流量监视
  // oneway void stopTrafficMonitor();
}
