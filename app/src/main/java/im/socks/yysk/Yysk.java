package im.socks.yysk;

/**
 * Created by cole on 2017/10/13.
 */

public class Yysk {
    /**
     * 监听全部的事件
     */
    public static final String EVENT_ALL = "*";
    public static final String EVENT_PROXY_CHANGED = "proxy_changed";
    public static final String EVENT_PROXY_CHANGED_SERVER = "proxy_changed_server";
    public static final String EVENT_LOGIN = "login";
    public static final String EVENT_LOGOUT = "logout";
    public static final String EVENT_SETTINGS = "settings";
    public static final String EVENT_VPNCHECK = "vpn_check";

    public static final String EVENT_CUSTOM_PROXY_ADD="custom_proxy_add";
    public static final String EVENT_CUSTOM_PROXY_REMOVE="custom_proxy_remove";
    public static final String EVENT_CUSTOM_PROXY_UPDATE="custom_proxy_update";

    public static final String EVENT_DZ_PROXY_ADD="dz_proxy_add";
    public static final String EVENT_DZ_PROXY_REMOVE="dz_proxy_remove";
    public static final String EVENT_DZ_PROXY_UPDATE="dz_proxy_update";

    /**
     * 充值成功
     */
    public static final String EVENT_PAY_SUCCESS="pay_success";
    /**
     * 充值失败
     */
    public static final String EVENT_PAY_FAIL="pay_fail";


    public final static int STATUS_INIT = 0; //刚创建，没有执行任何操作
    public final static int STATUS_CONNECTING = 1; // 连接中
    public final static int STATUS_CONNECTED = 2; // 已连接
    public final static int STATUS_STOPPING = 3; // 停止中
    public final static int STATUS_STOPPED = 4; //停止


    public static final AppDZ app = new AppDZ();

}
