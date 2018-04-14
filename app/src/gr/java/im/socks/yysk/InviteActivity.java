package im.socks.yysk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.User;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.XBean;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/25
 * Time: 23:19
 */

public class InviteActivity extends AppCompatActivity {

    private PageBar title_bar;
    private final AppDZ app = Yysk.app;
    private String inviteCode = "暂无";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        title_bar = findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        refreshUserInfo();
        findViewById(R.id.btn_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建对话框构建器
                AlertDialog.Builder builder = new AlertDialog.Builder(InviteActivity.this);
                builder.setTitle("邀请")
                        .setMessage("您的邀请码是："+inviteCode)
                        .setNegativeButton("确定",null)
                        .create().show();
            }
        });
    }

    private void refreshUserInfo(){
        if(app.getSessionManager().isUserInfoNeedUdate()){
            app.getApi().getUserInfo(new YyskApi.ICallback<XBean>() {
                @Override
                public void onResult(XBean result) {
                    if(NetUtil.checkAndHandleRsp(result,InviteActivity.this,"获取个人信息失败",null)){
                        XBean userInfo = NetUtil.getRspData(result);
                        app.getSessionManager().onUserInfoUpdate(userInfo);
                        inviteCode = userInfo.getString("invite_code");
                    }
                }
            });
        }else{
            inviteCode = app.getSessionManager().getSession().user.invite_code;
        }
    }

}
