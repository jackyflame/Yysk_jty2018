package im.socks.yysk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

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
    private TextView txv_invite_code;
    private TextView txv_invite_count;

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
        txv_invite_code = findViewById(R.id.txv_invite_code);
        txv_invite_count = findViewById(R.id.txv_invite_count);
        refreshUserInfo();
        findViewById(R.id.btn_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.txv_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        txv_invite_code.setText(inviteCode);
    }

}
