package im.socks.yysk;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.FragmentManager;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.NotificationCompat;

import java.util.List;

/**
 * Created by cole on 2017/10/11.
 */

public class TestVpn {
    public static void test1(Activity context) {
        //1. 检查该app是否有权力创建vpnserivce
        Intent intent = VpnService.prepare(context);
        if (intent != null) {
            //表示还没有授权，需要启动一个系统activity，获得用户授权
            context.startActivityForResult(intent, 1);
        } else {
            //已经授权，可以启动VpnService
            Intent service = null;//new Intent();
            context.startService(service);
            //停止服务
            //context.stopService(service);

        }


        //其它进程通过调用这个方法，获得VpnService的IBinder对象
        Intent service = new Intent(VpnService.SERVICE_INTERFACE);
        // => vpnService.onBind(intent)
        //获得通知
        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        context.bindService(service, conn, 0);
    }


    //对于同一个service，全局只需要一个connection即可，当接收到onBindingDied，可以context.unbindService
    //然后从新context.bindService
    //一个connection可以bind多个service，然后通过ComponentName区分service
    //当然，也可以每个service使用一个独立的connection，简化实现
    public static class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //绑定成功
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //绑定断开，如：service所在的进程被杀死，但是connection还是有效的，当service所在的
            //进程重新创建后，会自动连接，再次发出onServiceConnected
        }

        @Override
        public void onBindingDied(ComponentName name) {
            //表示绑定已经死掉了，需要重新bindService
            //主要是service所在的app升级了，考虑到升级可能导致接口不兼容，所以，需要重新执行bindService

        }
    }

    public static class MyVpnBinder implements IInterface {
        @Override
        public IBinder asBinder() {
            return null;
        }
    }

    //简单的实现可以直接子类Binder
    //但是一般都是通过aidl工具生成一个
    public static class MyVpnBinder2 extends Binder {

    }

    public static class MyVpnService extends VpnService {

        private IBinder binder = new MyVpnBinder2();

        @Override
        public IBinder onBind(Intent intent) {
            //其它进程通过其获得一个接口对象
            //return super.onBind(intent);

            return binder;
        }

        @Override
        public void onRevoke() {
            //表示VpnService权限被收回
            super.onRevoke();
        }

        @Override
        public boolean onUnbind(Intent intent) {
            //如果返回true，表示再次绑定的时候，调用onRebind
            //默认为false，表示不要调用onRebind，而是onBind
            return super.onUnbind(intent);
        }

        @Override
        public void onRebind(Intent intent) {
            super.onRebind(intent);
        }
        //标准的service的接口，都是在主线程调用

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            //新系统使用的接口，默认的实现为调用老系统的接口
            //在这里通过intent获得参数，建立tunnel和通过VpnService.Builder创建vpn interface(获得返回的FD)
            //因为涉及网络操作，需要使用独立的线程执行，因为该方法是在主线程
            //因为这个方法可能调用多次，所以需要适当处理
            VpnService.Builder builder = new VpnService.Builder();
            ParcelFileDescriptor fd = builder.establish();
            if (fd == null) {
                //表示app没有prepare（也就是没有获得权限）或者被revoked了
                //可以考虑重新prepare
            }
            //如果不需要了，必须关闭
            //fd.close();

            //为了让进程尽可能的长期存在，可以设置为前端service，也就是显示通知或者一个控制UI
            Context context = null;
            NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
            Notification n = nb.build();
            startForeground(111, n);


            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onStart(Intent intent, int startId) {
            //老系统使用的接口
            super.onStart(intent, startId);
        }

        @Override
        public void onDestroy() {
            //service被销毁
            super.onDestroy();
        }

        @Override
        public void onCreate() {
            super.onCreate();

        }

        private void testStop() {
            super.stopSelf();//停止服务
            int startId = 0;//停止指定的请求，也就是必须先触发了onStartCommand
            super.stopSelf(startId);

            super.stopSelf();//=> super.stopSelf(-1)，-1表示最后一个startId

            //停止指定的请求，等同于stopSelf(startId)，返回true表示startId为最后收到的一个startId
            //当前的service会被停止

            //如果onStartCommand的实现为启动一个独立的线程去执行intent（command）
            //如：onStartCommand(a1),onStartCommand(a2),onStartCommand(a3)
            //假设a3为最后一个命令，当调用 stopSelfResult(a3)，就会停止service，不考虑a1,a2是否停止
            //所以，如果业务逻辑需要a1，a2先停止，就必须先调用stopSelfResult(a1),stopSelfResult(a2)
            //在多数情况下，一般不需要启动多个线程去执行，只需要一个，所以，只需要保留最后面的一个startId即可
            //这样，当服务完成后，可以手动context.stopService()，或者自动stopSelf(startId)
            //或者调用简便的方法: stopSelf()

            boolean stoppe = super.stopSelfResult(startId);


            Messenger messenger = new Messenger(new Handler() {
            });
            messenger.getBinder();


        }


    }

    // your package name is the same with your main process name
    private boolean isMainProcess(Context context) {
        return context.getPackageName().equals(getProcessName(context));
    }

    // you can use this method to get current process name, you will get
// name like "com.package.name"(main process name) or "com.package.name:remote"
    private String getProcessName(Context context) {
        int mypid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == mypid) {
                return info.processName;
            }
        }
        // may never return null
        return null;
    }


    private static void f11(Context context) {
        context.getApplicationContext();
    }

    public static class A1 {
        private A1(Context context) {

        }
    }

    private static A1 a1 = null;

    public static A1 getA1(Context context) {
        if (a1 == null) {
            a1 = new A1(context.getApplicationContext());
        }
        return a1;
    }

    private static void f44() {
        VpnService s;
        //s.protect()
        VpnService.Builder builder = null;
        builder.setSession("");
        builder.addRoute("0.0.0.0", 0);
        builder.addDnsServer("");//添加dns，该dns会自动通过vpn，不需要再使用addRoute添加规则
        //如果设置了，点击系统的vpn通知的时候，会有一个配置按钮，点击该按钮就会使用配置的intent启动activity
        //builder.setConfigureIntent();
        ParcelFileDescriptor conn = builder.establish();
        conn.getFd();

        FragmentManager kk = null;
        kk.putFragment(null, "", null);
        kk.beginTransaction().remove(null);
        kk.beginTransaction().replace(0, null, "");
        kk.beginTransaction().addToBackStack("");


    }
}
