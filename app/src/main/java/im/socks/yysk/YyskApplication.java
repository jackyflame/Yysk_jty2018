package im.socks.yysk;

import android.app.Application;

import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.PlatformConfig;


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
        initJShare();
    }

    public static YyskApplication getInstatnce(){
        return instance;
    }

    private void initJShare(){
        JShareInterface.setDebugMode(true);
        PlatformConfig platformConfig = new PlatformConfig()
                .setWechat("wxc40e16f3ba6ebabc", "dcad950cd0633a27e353477c4ec12e7a")
                .setQQ("1106011004", "YIbPvONmBQBZUGaN");
        JShareInterface.init(this,platformConfig);
    }
}
