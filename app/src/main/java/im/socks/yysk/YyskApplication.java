package im.socks.yysk;

import android.app.Application;


/**
 * Created by cole on 2017/10/23.
 */

public class YyskApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Yysk.app.init(this);
    }


}
