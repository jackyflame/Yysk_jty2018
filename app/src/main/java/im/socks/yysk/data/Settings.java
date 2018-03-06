package im.socks.yysk.data;

import java.io.IOException;

import im.socks.yysk.App;
import im.socks.yysk.MyLog;
import im.socks.yysk.Yysk;
import im.socks.yysk.util.IOUtil;
import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/31.
 */

public class Settings {

    private XBean data;
    private App app;

    public Settings(App app) {
        this.app = app;
        this.data = this.load();
    }

    /**
     * 获得设置的数据，返回该对象的目的是为了简化代码，而不需要在settings对象定义太多的get方法
     *
     * @return 获得设置的数据，对返回的对象只应该执行只读的操作
     */
    public XBean getData() {
        return data;
    }

    /**
     * 获得一个新的data对象，直接从文件中加载，所以总是最新的，给vpn process使用
     *
     * @return
     */
    public XBean newData() {
        return load();
    }

    /**
     * 设置数据，如：set("a","a-value","b","b-value")
     *
     * @param args
     */
    public void set(Object... args) {
        for (int i = 0; i < args.length; ) {
            String key = (String) args[i++];
            Object value = args[i++];
            data.put(key, value);
        }
        save(data);

        for (int i = 0; i < args.length; ) {
            String key = (String) args[i++];
            Object value = args[i++];
            app.getEventBus().emit(Yysk.EVENT_SETTINGS, new XBean("name", key, "value", value), false);
        }
    }

    private XBean load() {
        XBean data = IOUtil.load(app.getDataFile("settings.json"), XBean.class);
        return data != null ? data : new XBean();
    }

    private void save(XBean data) {
        String text = Json.stringify(data);
        try {
            IOUtil.save(text, app.getDataFile("settings.json"));
        } catch (IOException e) {
            MyLog.e(e);
        }
    }

}
