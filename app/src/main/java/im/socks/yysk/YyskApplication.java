package im.socks.yysk;

import android.app.Application;

import com.socks.yyskjtyqy.share.AssertCopyUtil;

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
                .setWechat("wxdc9b5aba869d61d5", "f23fd623263459b3670b560826c92a28")
                .setQQ("1106872392", "G4gpB4RGu5WDkubq");
        JShareInterface.init(this,platformConfig);
        //复制ICON图标用于分享
        AssertCopyUtil.copyResurces(this,"ic_launcher.png");
    }
}
