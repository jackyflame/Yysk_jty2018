package im.socks.yysk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Android Studio.
 * ProjectName: Yysk_jty2018
 * Author: Haozi
 * Date: 2018/3/25
 * Time: 23:19
 */

public class ChangePswActivity extends AppCompatActivity {

    private PageBar title_bar;

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
    }

}
