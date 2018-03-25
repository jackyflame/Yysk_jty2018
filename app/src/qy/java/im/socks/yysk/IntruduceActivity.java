package im.socks.yysk;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import im.socks.yysk.util.Constants;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/26
 * Time: 0:33
 */

public class IntruduceActivity extends AppCompatActivity {

    private PageBar title_bar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intruduce);
        title_bar = findViewById(R.id.title_bar);
        title_bar.setBackListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.btn_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntruduceActivity.this,MainActivity.class);
                intent.putExtra(Constants.EXTRA_JUMP,Constants.EXTRA_JUMP_REGIST);
                startActivity(intent);
            }
        });
    }

}
