package im.socks.yysk;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.vector.update_app.HttpManager;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.UpdateCallback;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;
import java.util.List;
import java.util.Map;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.CustomProxyManager;
import im.socks.yysk.data.SessionManager;
import im.socks.yysk.data.Settings;
import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;
import im.socks.yysk.vpn.YyskVpn;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by cole on 2017/10/27.
 */

public class App {

    public static final boolean DEBUG=true;

    protected YyskApi api;

    protected Context context;
    protected boolean isMainProcess = false;
    protected EventBus eventBus;
    protected SessionManager sessionManager;
    protected Settings settings;
    protected YyskVpn vpn;
    protected CustomProxyManager customProxyManager;

    public App() {

    }

    protected void checkMainProcess() {
        if (!isMainProcess) {
            throw new IllegalStateException("只能够在主进程调用该方法");
        }
    }

    public void init(final Application application) {
        context = application.getApplicationContext();
        isMainProcess = isMainProcess(context);

        //vpn,main都需要
        eventBus = new EventBus();
        settings = new Settings(this);
        customProxyManager = new CustomProxyManager(this);
        sessionManager = new SessionManager(this);


        vpn = new YyskVpn(this);

        initJPush();

        if (isMainProcess) {
            MyLog.d("App.init on MainProcess");
            api = new YyskApi(this);

        } else {
            MyLog.d("App.init on %s",getProcessName(context));

            //vpn process or jpush process

        }
    }

    public Context getContext() {
        return context;
    }

    public boolean isMainProcess() {
        return isMainProcess;
    }

    public YyskApi getApi() {
        checkMainProcess();
        return api;
    }

    public CustomProxyManager getCustomProxyManager(){
        return customProxyManager;
    }

    /**
     * 获得存储app数据的目录，为了避免冲突，会使用一个子目录
     *
     * @return
     */
    public File getDataDir() {
        return context.getDir("yysk", Context.MODE_PRIVATE);
    }

    /**
     * 获得文件
     *
     * @param name
     * @return
     */
    public File getDataFile(String name) {
        File file = new File(getDataDir(), name);
        if (!file.isFile()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    public YyskVpn getVpn() {
        return vpn;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public Settings getSettings() {
        return settings;
    }

    public void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    protected void initJPush(){
//        //jpush ，会启动一个service，在独立的进程
//        JPushInterface.setDebugMode(DEBUG);
//
//        //下面这2个方法仅仅是保存builder的toString()，而不是保存builder对象，所以
//        //没有什么用处
//        //JPushInterface.setDefaultPushNotificationBuilder(builder);
//        //JPushInterface.setPushNotificationBuilder(1,builder);
//
//        JPushInterface.init(context);

    }

    public void checkUpdate(final Activity activity,final boolean isAutoCheck){

        HttpManager httpManager = new HttpManager() {
            @Override
            public void asyncGet(@NonNull String url, @NonNull Map<String, String> params, @NonNull final Callback cb) {
                api.getAppVersion(params.get("version"), new YyskApi.ICallback<XBean>() {
                    @Override
                    public void onResult(XBean result) {

                        if(result!=null){
                            //
                            cb.onResponse(Json.stringify(result));
                        }else{
                            cb.onError("网络错误");
                        }
                    }
                });
            }

            @Override
            public void asyncPost(@NonNull String url, @NonNull Map<String, String> params, @NonNull Callback callBack) {

            }

            @Override
            public void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull final FileCallback callback) {
                //直接使用下载了，不通过vpn service，因为太大了
                OkHttpUtils.get()
                        .url(url)
                        .build()
                        .execute(new FileCallBack(path, fileName) {
                            @Override
                            public void inProgress(float progress, long total, int id) {
                                callback.onProgress(progress, total);
                            }

                            @Override
                            public void onError(Call call, Response response, Exception e, int id) {
                                callback.onError(validateError(e, response));
                            }

                            @Override
                            public void onResponse(File response, int id) {
                                callback.onResponse(response);

                            }

                            @Override
                            public void onBefore(Request request, int id) {
                                super.onBefore(request, id);
                                callback.onBefore();
                            }
                        });

            }
        };

        UpdateCallback cb = new UpdateCallback(){
            @Override
            protected UpdateAppBean parseJson(String s) {
                XBean bean = Json.parse(s,XBean.class);
                UpdateAppBean appBean = new UpdateAppBean();
                //for test
                if(false){
                    appBean.setUpdate("Yes");
                    appBean.setApkFileUrl("http://192.168.31.10:8123/app-debug.apk");
                    appBean.setNewVersion("1.9");
                    return appBean;
                }

                if(bean.isNull("downurl")){
                    //appBean.setUpdate("No");
                    return appBean;
                }
                //
                appBean.setOnlyWifi(true);
                appBean.setApkFileUrl(bean.getString("downurl"));
                appBean.setNewVersion(bean.getString("versionid"));
                appBean.setNewMd5(bean.getString("md5"));
                appBean.setTargetSize(bean.getString("size"));
                appBean.setUpdateLog(bean.getString("log"));
                appBean.setUpdate("Yes");
                return appBean;
            }

            @Override
            protected void noNewApp() {
                //super.noNewApp();
                if(!isAutoCheck){
                    Toast.makeText(activity,"已经是最新版本",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            protected void hasNewApp(UpdateAppBean updateApp, UpdateAppManager updateAppManager) {
                super.hasNewApp(updateApp, updateAppManager);
            }
        };

        new UpdateAppManager
                .Builder()
                //当前Activity
                .setActivity(activity)
                //更新地址，不能够为空，否则抛异常，所以这里随便写了几个字符
                .setUpdateUrl("abc")
                .setPost(false)
                //实现httpManager接口的对象
                .setHttpManager(httpManager)
                .build()

                .checkNewApp(cb);
    }

    protected static boolean isMainProcess(Context context) {
        //主进程默认的名字为包名，其它进程的为packageName:xxxx
        return context.getPackageName().equals(getProcessName(context));
    }

    protected static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid) {
                return info.processName;
            }
        }
        // may never return null
        return null;
    }
}
