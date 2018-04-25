package im.socks.yysk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
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

public class SystemMsgDetailActivity extends AppCompatActivity {

    private PageBar title_bar;
    private long messageid;
    private final AppDZ app = Yysk.app;

    private TextView txv_title;
    private TextView txv_content;
    private TextView txv_time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_msg_detail);
        title_bar = findViewById(R.id.title_bar);
        txv_title = findViewById(R.id.txv_title);
        txv_content = findViewById(R.id.txv_content);
        txv_time = findViewById(R.id.txv_time);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        messageid = getIntent().getLongExtra("messageid", -1);
        initData();
    }

    private void initData() {
        if(messageid <= 0){
            Toast.makeText(this, "获取信息id失败", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        app.getApi().getMsgDetail(messageid,new YyskApi.ICallback<XBean>() {
            @Override
            public void onResult(XBean result) {
                if(NetUtil.checkAndHandleRsp(result, SystemMsgDetailActivity.this,"获取信息详情失败",null)){
                    //缓存登录信息
                    XBean msgData = NetUtil.getRspData(result);
                    if(msgData != null){
                        txv_title.setText(msgData.getString("title", "消息"));
                        txv_content.setText(msgData.getString("content", ""));
                        String time = msgData.getString("created", "");
                        if(time != null){
                            time = time.replace("T", " ");
                        }
                        txv_time.setText(time);
                    }
                }
            }
        });
    }
}
