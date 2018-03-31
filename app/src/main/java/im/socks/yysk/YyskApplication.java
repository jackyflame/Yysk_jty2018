package im.socks.yysk;

import android.app.Application;


/**
 * Created by cole on 2017/10/23.
 */

public class YyskApplication extends Application {

    private static YyskApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        Yysk.app.init(this);
        instance = this;
    }

    public static YyskApplication getInstatnce(){
        return instance;
    }
}
