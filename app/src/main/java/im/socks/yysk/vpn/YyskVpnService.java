package im.socks.yysk.vpn;

import android.app.Service;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import im.socks.yysk.App;
import im.socks.yysk.MyLog;
import im.socks.yysk.Yysk;
import im.socks.yysk.api.Http;
import im.socks.yysk.ssr.SsrTunnel;
import okhttp3.OkHttpClient;

/**
 * 建立vpn连接，有3种启动的形式：
 * <pre>
 *      1. 通过context.startService() => onStartCommand() => 可以获得intent => action
 *      2. 通过context.bindService() =>  onBind() => 可以获得intent => action
 *      3. 自动恢复，onStartCommand() => action为null，所以，根据上一次的状态来决定是vpn是on或者off
 *  </pre>
 */
public class YyskVpnService extends VpnService {
    public final static String SERVICE_ACTION = "im.socks.SERVICE";
    public final static String START_ACTION = "im.socks.START";
    public final static String STOP_ACTION = "im.socks.STOP";
    public final static String RELOAD_ACTION = "im.socks.RELOAD";

    private ITunnel tunnel = null;
    private Object lock = new Object();
    private YyskVpnNotification notification = null;
    //private Handler handler  = null;

    private ITunnelListener tunnelListener = new ITunnelListener() {
        private Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void onTrafficUpdate(final long rxRate, final long txRate, final long rxTotal, final long txTotal) {
            execute(new Runnable() {
                @Override
                public void run() {
                    service.fireTrafficUpdate(rxRate, txRate, rxTotal, txTotal);
                }
            });
        }

        @Override
        public void onStatusChanged(final int status) {
            execute(new Runnable() {
                @Override
                public void run() {
                    service.fireStatusChanged(status);
                }
            });
        }

        private void execute(Runnable code) {
            if (handler.getLooper() == Looper.myLooper()) {
                code.run();
            } else {
                handler.post(code);
            }
        }
    };


    // 回调对象列表

    private YyskServiceImpl service = new YyskServiceImpl();

    private OkHttpClient httpClient;
    private final App app = Yysk.app;

    @Override
    public void onCreate() {
        httpClient = Http.createHttpClient(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVpn(false);
    }


    // 服务绑定
    @Override
    public IBinder onBind(Intent intent) {
        MyLog.d("YyskVpnService.bind=%s", intent.getAction());
        if (SERVICE_ACTION.equals(intent.getAction())) {
            //可以根据last_action判断是否需要startVpn
            return service;
        } else {
            return super.onBind(intent);
        }
    }


    @Override
    public void onRevoke() {
        //取消vpn授权后，调用
        //不一定在主线程调用，默认实现为stopSelf(),然后在主线程onDestroy=>stopVpn()
        //super.onRevoke();
        stopVpn(true);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        //可能多次调用startService()
        String action = null;
        if (intent != null) {
            action = intent.getAction();
        } else {
            //表示自动重启
            action = app.getVpn().getLastAction();
        }

        MyLog.i("YyskVpnService.onStartCommand=%s", action);
        if (START_ACTION.equals(action)) {
            if (startVpn()) {
                //进程被杀死后，可以再次启动
                return Service.START_STICKY;
            } else {
                return Service.START_NOT_STICKY;
            }
        } else if (STOP_ACTION.equals(action)) {
            stopVpn(true);
            return Service.START_NOT_STICKY;
        } else if (RELOAD_ACTION.equals(action)) {
            if (reloadVpn()) {
                //进程被杀死后，可以再次启动
                return Service.START_STICKY;
            } else {
                return Service.START_NOT_STICKY;
            }
        } else {
            //自动重启
            return Service.START_NOT_STICKY;
            //可以直接关闭吗？或者需要使用其它线程
            //不停止service也可以，可以让服务继续存在，等待下一次的startService
            //stopSelf();

        }

    }

    private boolean startVpn() {
        VpnProfile profile = app.getVpn().getProfile();
        MyLog.d("startVpn,profile=%s", (profile != null ? profile.name : null));
        if (profile == null || VpnService.prepare(this) != null) {
            //MyLog.d("VpnService.prepare=%s",VpnService.prepare(this));
            synchronized (lock) {
                stopVpn(false);
            }
            return false;
        } else {
            synchronized (lock) {
                stopVpn(false);
                tunnel = new SsrTunnel(profile, this, new Builder(), tunnelListener);
                notification = new YyskVpnNotification(profile, this);

                app.getVpn().setLastAction(START_ACTION);
            }
            Thread thread = new Thread() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (tunnel != null && tunnel.start()) {
                            if (notification != null) {
                                //启动成功后，可以显示一个通知，更新状态
                                notification.show();
                            }
                        }
                    }
                }
            };
            thread.start();
            return true;
        }


    }

    private void stopVpn(boolean saveAction) {
        synchronized (lock) {
            if (tunnel != null) {
                tunnel.stop();
                tunnel = null;

            }
            if (notification != null) {
                notification.cancel();
                notification = null;
            }

            if (saveAction) {
                app.getVpn().setLastAction(STOP_ACTION);
            }
        }

    }

    private boolean reloadVpn() {
        synchronized (lock) {
            if (tunnel != null) {
                return startVpn();
            } else {
                stopVpn(false);
                return false;
            }
        }
    }


    private class YyskServiceImpl extends IYyskService.Stub {

        private RemoteCallbackList<IYyskServiceListener> listeners = new RemoteCallbackList<>();

        @Override
        public void addListener(IYyskServiceListener listener) {
            listeners.register(listener);
        }

        @Override
        public void removeListener(IYyskServiceListener listener) {
            listeners.unregister(listener);
        }

        @Override
        public int getStatus() throws RemoteException {
            synchronized (lock) {
                return tunnel != null ? tunnel.getStatus() : Yysk.STATUS_STOPPED;
            }
        }

        @Override
        public long getStartTime() throws RemoteException {
            synchronized (lock) {
                return tunnel != null ? tunnel.getStartTime() : 0;
            }
        }

        @Override
        public String doGet(String url) throws RemoteException {
            return Http.doGet(httpClient, url);
        }

        //同一时间只能够发出一个事件，要么使用同步的方式，要么使用在固定的线程执行，如：handler
        //这里没有使用同步，所以必须使用handler调用
        public void fireStatusChanged(int status) {
            int nCount = listeners.beginBroadcast();
            for (int n = 0; n < nCount; ++n) {
                try {

                    listeners.getBroadcastItem(n).onStatusChanged(status);
                } catch (RemoteException e) {
                    MyLog.e(e);
                }
            }
            listeners.finishBroadcast();
        }

        public void fireTrafficUpdate(long rxRate, long txRate, long rxTotal, long txTotal) {
            int nCount = listeners.beginBroadcast();
            for (int n = 0; n < nCount; ++n) {
                try {
                    listeners.getBroadcastItem(n).onTrafficUpdate(rxRate, txRate, rxTotal, txTotal);
                } catch (RemoteException e) {
                    MyLog.e(e);
                }
            }
            listeners.finishBroadcast();
        }
    }
}
