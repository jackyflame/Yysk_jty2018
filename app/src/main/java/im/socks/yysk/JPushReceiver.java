package im.socks.yysk;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by cole on 2017/11/20.
 */

public class JPushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //参考：https://docs.jiguang.cn/jpush/client/Android/android_api/
        String action = intent.getAction();
//        if(JPushInterface.ACTION_MESSAGE_RECEIVED.equals(action)){
//            //自定义消息
//        }else if(JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(action)){
//            //通知
//            showNotification(context,intent);
//        }else{
//
//        }
    }

    private void showNotification(Context context,Intent intent){
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //manager.cancel(ID);

//        Bundle bundle = intent.getExtras();
//        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
//        String content = bundle.getString(JPushInterface.EXTRA_ALERT);
//        //自定义的key:value
//        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
//        if(extras!=null){
//            XBean bean = Json.parse(extras,XBean.class);
//        }
//
//        int notificationId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
//        if(content==null||content.isEmpty()){
//            manager.cancel(notificationId);
//        }else{
//            Notification n = build(context,title,content);
//            manager.notify(notificationId,n);
//        }
    }

    private Notification build(Context context,String title,String content) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);//?
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, null);
        builder.setWhen(System.currentTimeMillis());
        //这3个是必须的，否则显示的是系统默认的通知的内容
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(title);
        builder.setContentText(content);
        //
        builder.setContentIntent(contentIntent);


        builder.setAutoCancel(true);//click dismiss

        //android5，api>=21，锁屏的时候，显示通知的全部在屏幕
        //当然，如果锁屏了，就不需要频繁的更新通知，如：速率等
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        return builder.build();

    }
}
