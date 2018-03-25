package im.socks.yysk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import im.socks.yysk.util.Constants;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findViewById(R.id.btn_regist).setOnClickListener(this);
        findViewById(R.id.btn_login).setOnClickListener(this);
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

    @Override
    protected void onDestroy() {
        removeRunnable();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_regist:
                startActivity(new Intent(this,IntruduceActivity.class));
                break;
            case R.id.btn_login:
                Intent intent = new Intent(this,MainActivity.class);
                intent.putExtra(Constants.EXTRA_JUMP,Constants.EXTRA_JUMP_LOGIN);
                startActivity(intent);
                break;
        }
    }
}
