package im.socks.yysk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.NetUtil;
import im.socks.yysk.util.XBean;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/25
 * Time: 23:19
 */

public class FeedbackActivity extends AppCompatActivity {

    private PageBar title_bar;
    private EditText edt_title;
    private EditText edt_content;
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
        edt_title = findViewById(R.id.edt_title);
        edt_content = findViewById(R.id.edt_content);
        findViewById(R.id.submitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void uploadMsg(){
        String title = edt_title.getText() != null ? edt_title.getText().toString():"";
        if(title.isEmpty()){
            showError("请输入标题");
            return;
        }
        String content = edt_content.getText() != null ? edt_content.getText().toString():"";
        if(content.isEmpty()){
            showError("请输入问题内容");
            return;
        }
        app.getApi().sendFeedBack(edt_content.getText().toString(), new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(NetUtil.checkAndHandleRsp(result,FeedbackActivity.this,"提交失败",null)){
                    showError("问题提交成功");
                    finish();
                }
            }
        });
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
