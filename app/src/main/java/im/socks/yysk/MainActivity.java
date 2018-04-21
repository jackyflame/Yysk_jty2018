package im.socks.yysk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import im.socks.yysk.util.Constants;

public class MainActivity extends AppCompatActivity {

    private MainFragment mainFragment;
    private FragmentStack fragmentStack = null;
    private final App app = Yysk.app;
    private boolean isCheckUpdate=true;
    private Handler mHandler;

    private static final int HANDLER_GOTO_LOGIN = 100002;
    private static final int HANDLER_GOTO_REGIST = 100003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        fragmentStack = new FragmentStack(this, getSupportFragmentManager());

        mainFragment = MainFragment.newInstance();

        fragmentStack.show(mainFragment, "main", false);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case HANDLER_GOTO_LOGIN:
                        getFragmentStack().show(LoginFragment.newInstance(null), "login", false);
                        break;
                    case HANDLER_GOTO_REGIST:
                        getFragmentStack().show(RegisterFragment.newInstance(), "regist", false);
                        break;
                }
            }
        };

        if(getIntent().hasExtra(Constants.EXTRA_JUMP)){
            String action = getIntent().getStringExtra(Constants.EXTRA_JUMP);
            if(Constants.EXTRA_JUMP_LOGIN.equals(action)){
                mHandler.sendEmptyMessageDelayed(HANDLER_GOTO_LOGIN,500);
            }else if(Constants.EXTRA_JUMP_REGIST.equals(action)){
                mHandler.sendEmptyMessageDelayed(HANDLER_GOTO_REGIST,500);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //每次启动仅仅检查一次
        if(isCheckUpdate){
            app.checkUpdate(this,true);
            isCheckUpdate=false;
        }
    }



    @Override
    public void onBackPressed() {
        if (!fragmentStack.back()) {
            //提示是否保存修改
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("是否退出APP?");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    MainActivity.super.onBackPressed();
                }
            });
            builder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!app.getVpn().onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public FragmentStack getFragmentStack() {
        return fragmentStack;
    }
}