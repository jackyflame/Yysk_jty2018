package im.socks.yysk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import im.socks.yysk.api.YyskApi;
import im.socks.yysk.util.XBean;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("im.socks.yysk", appContext.getPackageName());

        final YyskApi api = new YyskApi(Yysk.app);
        Thread.sleep(5000);
        Log.e("Yysk","wake up");
        api.getProxyList("18583752672", new YyskApi.ICallback<List<XBean>>() {
            @Override
            public void onResult(List<XBean> result) {
                Log.e("Yysk","servers="+result);
                api.destroy();
            }
        });

        Thread.sleep(5000);


    }
}
