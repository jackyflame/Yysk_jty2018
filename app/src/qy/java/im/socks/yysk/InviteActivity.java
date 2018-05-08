package im.socks.yysk;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.socks.yyskjtyqy.share.AssertCopyUtil;

import java.util.HashMap;

import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.PlatActionListener;
import cn.jiguang.share.android.api.Platform;
import cn.jiguang.share.android.api.ShareParams;
import cn.jiguang.share.qqmodel.QQ;
import cn.jiguang.share.wechat.Wechat;
import cn.jiguang.share.wechat.WechatMoments;
import im.socks.yysk.api.YyskApi;
import im.socks.yysk.data.User;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.XBean;
import im.socks.yysk.vpn.VpnConfig;

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

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String toastMsg = (String) msg.obj;
            Toast.makeText(InviteActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
        }
    };

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
                showSharePop();
            }
        });
        findViewById(R.id.txv_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InviteActivity.this,InviteDetailActivity.class));
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

    private void showError(String msg) {
        if(handler != null) {
            Message message = handler.obtainMessage();
            message.obj = msg;
            handler.sendMessage(message);
        }
    }

    private void showSharePop(){
        SharePopWindow takePhotoPopWin = new SharePopWindow(this, inviteCode, new SharePopWindow.ClickListener() {
            @Override
            public void wechatClick() {
                postShareReq(Wechat.Name);
            }
            @Override
            public void friendCycleClick() {
                postShareReq(WechatMoments.Name);
            }
            @Override
            public void qqClick() {
                postShareReq(QQ.Name);
            }
        });
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.7f;
        getWindow().setAttributes(params);
        takePhotoPopWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });
        takePhotoPopWin.showAtLocation(findViewById(R.id.lin_root), Gravity.BOTTOM,0,0);
    }

    private void postShareReq(String name){
        //创建分享参数
        ShareParams shareParams = new ShareParams();
        //设置分享的数据类型
        shareParams.setShareType(Platform.SHARE_WEBPAGE);
        //分享URL
        shareParams.setUrl(VpnConfig.API_SHARE_URL+inviteCode);
        shareParams.setImageUrl(VpnConfig.API_SHAREIMAGE_URL);
        //标题内容
        shareParams.setTitle("邀请有奖");
        shareParams.setText("易加速邀请您使用，邀请码："+inviteCode);
        JShareInterface.share(name, shareParams, new PlatActionListener() {
            @Override
            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                showError("分享成功");
            }
            @Override
            public void onError(Platform platform, int i, int i1, Throwable throwable) {
                showError("分享失败："+throwable.getMessage());
            }
            @Override
            public void onCancel(Platform platform, int i) {
                showError("用户取消了分享");
            }
        });
    }
}
