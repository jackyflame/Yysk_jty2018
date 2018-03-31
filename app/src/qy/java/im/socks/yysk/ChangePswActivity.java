package im.socks.yysk;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.StringUtils;
import im.socks.yysk.util.XBean;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/25
 * Time: 23:19
 */

public class ChangePswActivity extends AppCompatActivity {

    private PageBar title_bar;
    private EditText txv_psw_orgin;
    private EditText txv_psw_new;
    private EditText txv_psw_repeat;

    private final AppDZ app = Yysk.app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_psw);
        title_bar = findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txv_psw_orgin = findViewById(R.id.txv_psw_orgin);
        txv_psw_new = findViewById(R.id.txv_psw_new);
        txv_psw_repeat = findViewById(R.id.txv_psw_repeat);
        findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePsw();
            }
        });
    }

    private void changePsw(){
        String pswOrgin = StringUtils.getTextViewStr(txv_psw_orgin);
        final String pswNew = StringUtils.getTextViewStr(txv_psw_new);
        String pswRepeat = StringUtils.getTextViewStr(txv_psw_repeat);
        if(StringUtils.checkEmpty(pswOrgin,"请输入初始密码")){
            return;
        }
        if(StringUtils.checkEmpty(pswNew,"请输入新密码")){
            return;
        }
        if(StringUtils.checkEmpty(pswRepeat,"请再次输入新密码")){
            return;
        }
        if(StringUtils.isEqualWithoutEmpty(pswNew,pswRepeat) == false){
            StringUtils.showToast("确认密码与新密码不匹配，请重新输入");
            return;
        }
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("正在修改密码...");
        dialog.show();
        app.getApi().changePassword(pswOrgin, pswNew, new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                dialog.dismiss();
                if(NetUtil.checkAndHandleRsp(result, ChangePswActivity.this,"修改密码失败",null)){
                    app.sessionManager.getSession().user.password = pswNew;
                    app.sessionManager.updateSession();
                    finish();
                }
            }
        });
    }
}
