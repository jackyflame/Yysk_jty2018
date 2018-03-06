package im.socks.yysk.data;

import android.app.Activity;

import java.io.IOException;

import im.socks.yysk.App;
import im.socks.yysk.MyLog;
import im.socks.yysk.Yysk;
import im.socks.yysk.util.IOUtil;
import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/27.
 */

public class SessionManager {
    private Session session = null;
    private Proxy proxy;
    private final App app;

    public SessionManager(App app) {
        this.app = app;
        session = this.loadSession();
        proxy = this.loadProxy();
    }

    /**
     * 获得当前的session
     *
     * @return
     */
    public Session getSession() {
        return session;
    }

    /**
     * 获得当前使用的proxy
     * @return
     */
    public Proxy getProxy(){
        return proxy;
    }

    /**
     * 新创建一个独立的session对象，给vpn process使用，目的是获得最新的数据
     *
     * @return
     */
    public Session newSession() {
        return loadSession();
    }

    /**
     * 如果代理不存在，返回null
     * @return
     */
    public Proxy newProxy(){
        //Session session = loadSession();
        Proxy proxy = loadProxy();
        return proxy;
        //总是返回代理，即使用户没有登录，雨燕梭客也返回代理列表，只是不是user为deviceId
        /*
        if(proxy!=null){
            if(proxy.isCustom){
                return proxy;
            }else if(proxy.phoneNumber!=null && proxy.phoneNumber.equals(session.user.phoneNumber)){
                return proxy;
            }else{
                return null;
            }
        }else{
            return null;
        }*/

    }

    /**
     * @param id
     * @param phoneNumber
     */
    public void onLogin(String id, String phoneNumber) {
        onLogin(id,phoneNumber,null,null,null,null);
    }

    public void onLogin(String id, String phoneNumber,String terminalNum,String bindedTerminalNum,String entername,String psw) {

        session = new Session();
        session.user.phoneNumber = phoneNumber;
        session.user.id = id;
        if(terminalNum != null){
            session.user.terminalNum = terminalNum;
        }
        if(bindedTerminalNum != null){
            session.user.bindedTerminalNum = bindedTerminalNum;
        }
        if(entername != null){
            session.user.entername = entername;
        }
        if(psw != null){
            session.user.psw = psw;
        }
        //session.user.password = password;//
        //session.setToken("");//正常登录后应该获得一个token的
        session.setLogin(true);

        saveSession(session);

        app.getEventBus().emit(Yysk.EVENT_LOGIN, session, false);

        //如果原来存在代理，且代理是用户的，就需要停止
        if(proxy!=null &&!proxy.isCustom){
            setProxy(null,null,true);
        }
    }

    public void onLogout() {
        session = new Session();
        saveSession(session);
        app.getEventBus().emit(Yysk.EVENT_LOGOUT, session, false);

        if (proxy!=null&&!proxy.isCustom) {
           setProxy(null,null,true);
        }

    }

    public void onVpnVerCheck(int vpnVersion,int companyId){
        if(session == null){
            return;
        }
        session.vpnVersion = vpnVersion;
        session.companyid = companyId;
        session.vpnUpdateTime = System.currentTimeMillis();

        saveSession(session);

        app.getEventBus().emit(Yysk.EVENT_VPNCHECK, session, false);
    }

    private void onProxyChanged(Activity activity, Proxy proxy, boolean isReload, boolean isAutoOpen) {
        if (!isReload) {
            if (activity == null) {
                throw new IllegalArgumentException("如果不是reload，必须提供activity参数");
            }
        }
        app.getEventBus().emit(Yysk.EVENT_PROXY_CHANGED, proxy, false);
        if (proxy == null) {
            app.getVpn().stop();
        } else if (isReload) {
            app.getVpn().reload();
        } else if(isAutoOpen == true){
            app.getVpn().start(activity);
        }
    }

    /**
     * @param activity 这个activity需要重写onActivityResult且调用YyskVpn.onActivityResult()，处理授权结果
     * @param proxy
     * @param isReload true表示reload vpn，也就是如果当前为停止的，还是停止，当前为开始，就使用新的代理，false表示如果proxy不为null，就开启vpn
     */
    public void setProxy(Activity activity, Proxy proxy, boolean isReload) {
        setProxy(activity,proxy,isReload,true);
    }

    /**
     * @param activity 这个activity需要重写onActivityResult且调用YyskVpn.onActivityResult()，处理授权结果
     * @param proxy
     * @param isReload true表示reload vpn，也就是如果当前为停止的，还是停止，当前为开始，就使用新的代理，false表示如果proxy不为null，就开启vpn
     */
    public void setProxy(Activity activity, Proxy proxy, boolean isReload, boolean isAutoOpen) {
        this.proxy = proxy;
        saveProxy(proxy);
        onProxyChanged(activity, proxy, isReload, isAutoOpen);
    }

    private Session loadSession() {
        Session session = new Session();
        XBean data = IOUtil.load(app.getDataFile("session.json"), XBean.class);
        if (data != null) {
            session.fill(data);
        }
        return session;

    }

    private void saveSession(Session session) {
        String text = Json.stringify(session.toJson());
        try {
            IOUtil.save(text, app.getDataFile("session.json"));
        } catch (IOException e) {
            MyLog.e(e);
        }
    }

    private Proxy loadProxy() {

        XBean data = IOUtil.load(app.getDataFile("proxy.json"), XBean.class);
        if(data==null){
            return null;
        }

        Proxy proxy = new Proxy();
        proxy.fill(data);

        if(proxy.isCustom){
            //获得最新的自定义
            //如果已经不存在了，就返回null，出现这种情况主要是用户手动删除了custom_proxies.json文件
            XBean d = app.getCustomProxyManager().getProxy(proxy.id);
            if(d==null){
                return null;
            }
            proxy.data = d;
        }
        return proxy;

    }

    private void saveProxy(Proxy proxy) {
        String text = proxy !=null ? Json.stringify(proxy.toJson()):"";
        try {
            IOUtil.save(text, app.getDataFile("proxy.json"));
        } catch (IOException e) {
            MyLog.e(e);
        }
    }

}
