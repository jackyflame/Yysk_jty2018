package im.socks.yysk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MainFragmentDZ mainFragment;


    private FragmentStack fragmentStack = null;


    private final App app = Yysk.app;

    private boolean isCheckUpdate=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        fragmentStack = new FragmentStack(this, getSupportFragmentManager());

        mainFragment = MainFragmentDZ.newInstance();

        fragmentStack.show(mainFragment, "main", false);


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
            super.onBackPressed();
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