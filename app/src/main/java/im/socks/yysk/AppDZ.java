package im.socks.yysk;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.vector.update_app.HttpManager;
import com.vector.update_app.UpdateAppBean;
import com.vector.update_app.UpdateAppManager;
import com.vector.update_app.UpdateCallback;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;
import java.util.Map;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.api.YyskDZApi;
import im.socks.yysk.data.DZProxyManager;
import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Android Studio.
 * ProjectName: Yysk
 * Author: haozi
 * Date: 2018/1/2
 * Time: 16:27
 */

public class AppDZ extends App {

    protected YyskDZApi apiDZ;

    protected DZProxyManager dzProxyManager;

    @Override
    public void init(final Application application) {
        super.init(application);
        dzProxyManager = new DZProxyManager(this);
        if (isMainProcess) {
            MyLog.d("AppDZ.init on MainProcess");
            apiDZ = new YyskDZApi(this);
        } else {
            MyLog.d("AppDZ.init on %s",getProcessName(context));
            //vpn process or jpush process
        }
    }

    public YyskDZApi getApi() {
        checkMainProcess();
        return apiDZ;
    }

    public DZProxyManager getDzProxyManager() {
        return dzProxyManager;
    }

    public void checkUpdateDZ(final Activity activity, final boolean isAutoCheck){

        HttpManager httpManager = new HttpManager() {
            @Override
            public void asyncGet(@NonNull String url, @NonNull Map<String, String> params, @NonNull final Callback cb) {
                apiDZ.getAppVersionDZ(params.get("version"), new YyskApi.ICallback<XBean>() {
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
            public void asyncPost(@NonNull String url, @NonNull Map<String, String> params, @NonNull Callback callBack) {}

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

}
