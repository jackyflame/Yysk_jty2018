package im.socks.yysk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ViewUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.XBean;

import static com.zhy.http.okhttp.utils.Utils.getContext;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/25
 * Time: 23:19
 */

public class FeedbackActivity extends AppCompatActivity {

    private PageBar title_bar;
    private EditText txv_content;
    private final AppDZ app = Yysk.app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        title_bar = findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txv_content = findViewById(R.id.txv_content);
        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txv_content.getText() != null && txv_content.getText().length() > 0){
                    app.getApi().sendFeedBack(txv_content.getText().toString(), new YyskApi.ICallback<XBean>() {
                        @Override
                        public void onResult(XBean result) {
                            if(NetUtil.checkAndHandleRsp(result,FeedbackActivity.this,"提交失败",null)){
                                showError("意见提交成功");
                                finish();
                            }
                        }
                    });
                }else{
                    showError("请输入意见内容");
                }
            }
        });
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
