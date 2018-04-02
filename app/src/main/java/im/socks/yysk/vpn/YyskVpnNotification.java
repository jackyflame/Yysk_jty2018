package im.socks.yysk.vpn;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.support.v4.app.NotificationCompat;

import im.socks.yysk.MainActivity;
import im.socks.yysk.R;

/**
 * Created by cole on 2017/10/17.
 */

public class YyskVpnNotification {
    private static final int ID = 1;
    private VpnService service;
    private Context context;
    private VpnProfile profile;

    public YyskVpnNotification(VpnProfile profile, VpnService service) {
        this.profile = profile;
        this.service = service;
        this.context = service.getApplicationContext();
    }

    public void show() {
        service.startForeground(ID, build());
    }

    public void cancel() {
        service.stopForeground(true);
        //NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //manager.cancel(ID);
    }

    private Notification build() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);//?
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        //channelId=""
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, null);
        builder.setWhen(System.currentTimeMillis());
        //这3个是必须的，否则显示的是系统默认的通知的内容
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText("已经建立VPN连接:" + profile.name);
        //
        builder.setContentIntent(contentIntent);


        if (false) {
            Intent vpnIntent = new Intent(context, YyskVpnService.class);
            intent.setAction(YyskVpnService.STOP_ACTION);
            PendingIntent vpnPendingIntent = PendingIntent.getService(context, 0, vpnIntent, 0);

            //如果设置了，替代setContextText("")
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText("mmmmmmmmmmmm"));
            builder.addAction(R.drawable.cricle, "连接/断开", vpnPendingIntent);

        }

        //android5，api>=21，锁屏的时候，显示通知的全部在屏幕
        //当然，如果锁屏了，就不需要频繁的更新通知，如：速率等
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        return builder.build();

    }
}
