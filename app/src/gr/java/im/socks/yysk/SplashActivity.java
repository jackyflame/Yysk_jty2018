package im.socks.yysk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.api.YyskDZApi;
import im.socks.yysk.data.User;
import im.socks.yysk.util.Constants;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.StringUtils;
import im.socks.yysk.util.XBean;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/20
 * Time: 20:37
 */

public class SplashActivity extends AppCompatActivity implements View.OnClickListener{

    private Runnable runnable;
    private int SPLASH_DURATION = 2500;
    private Handler mHandler = new Handler();
    private final AppDZ app = Yysk.app;

    private static SplashActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findViewById(R.id.btn_regist).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
        instance = this;
        autoLogin();
    }

    private void postRunnable(final long delay) {
        runnable = new Runnable() {
            @Override
            public void run() {
                enterNextActivity();
            }
        };
        mHandler.postDelayed(runnable, delay);
    }

    private void removeRunnable() {
        if (runnable != null && mHandler != null) mHandler.removeCallbacks(runnable);
    }

    private void enterNextActivity() {

    }

    private void autoLogin(){
        if(app.getSessionManager() == null || app.getSessionManager().getSession() == null){
            return;
        }
        User user = app.getSessionManager().getSession().user;
        if(user == null || StringUtils.isEmpty(user.mobile_number) || StringUtils.isEmpty(user.password)){
            return;
        }
        final String phoneNumber = user.mobile_number;
        final String password = user.password;

        //发起登录请求
        YyskDZApi api = app.getApi();
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("正在登录...");
        dialog.show();

        api.loginDZ(phoneNumber, password, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                MyLog.d("login=%s",result);
                dialog.dismiss();
                String errorSuffix = "请检查你的本地网络是否通畅，或是登录服务器故障需要恢复后重新尝试登录";
                if(NetUtil.checkAndHandleRsp(result,SplashActivity.this,"自动登录失败",errorSuffix,null)){
                    //缓存登录信息
                    XBean loginRst = NetUtil.getRspData(result);
                    //保存登录状态信息
                    saveLoginRst(loginRst,phoneNumber,password);
                    //跳转到主页面
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    finish();
                }
            }
        });
    }

    private void saveLoginRst(XBean loginRst,String phoneNum,String psw){
        //判断是否登录成功
        if(loginRst == null || loginRst.isEquals("retcode", "succ")){
            return;
        }
        //保存登录状态信息
        app.getSessionManager().onLogin(loginRst,phoneNum,psw);
    }

    @Override
    protected void onDestroy() {
        removeRunnable();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_regist:
                //startActivity(new Intent(this,IntruduceActivity.class));
                Intent intent = new Intent(this,MainActivity.class);
                intent.putExtra(Constants.EXTRA_JUMP,Constants.EXTRA_JUMP_REGIST);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_login:
                intent = new Intent(this,MainActivity.class);
                intent.putExtra(Constants.EXTRA_JUMP,Constants.EXTRA_JUMP_LOGIN);
                startActivity(intent);
                finish();
                break;
        }
    }

    public static void closeSplash(){
        if(instance != null){
            instance.finish();
            instance = null;
        }
    }
}
