package im.socks.yysk.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import im.socks.yysk.App;
import im.socks.yysk.MyLog;
import im.socks.yysk.Yysk;
import im.socks.yysk.util.IOUtil;
import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/11/22.
 */

public class CustomProxyManager {
    //private List<XBean> items = null;
    private App app;
    public CustomProxyManager(App app){
        this.app = app;
        //items = load();
    }


    /**
     *
     * @param proxy
     * @return
     */
    public void save(XBean proxy){
        boolean isUpdated=false;
        List<XBean> items = load();
        String id = proxy.getString(Proxy.BEANNAME_ID);
        for(int i=0;i<items.size();i++){
            XBean item = items.get(i);
            if(item.isEquals(Proxy.BEANNAME_ID,id)){
                items.set(i,proxy);
                save(items);
                isUpdated=true;
                break;
            }
        }
        //如果不是更新，就添加到后面
        if(isUpdated){
            app.getEventBus().emit(Yysk.EVENT_CUSTOM_PROXY_UPDATE,proxy,false);
        }else{
            items.add(proxy);
            save(items);
            app.getEventBus().emit(Yysk.EVENT_CUSTOM_PROXY_ADD,proxy,false);
        }

        Proxy oldProxy = app.getSessionManager().getProxy();
        if(oldProxy!=null&& oldProxy.isCustom&&proxy.isEquals(Proxy.BEANNAME_ID,oldProxy.id)){
            //如果代理修改了
            Proxy newProxy = app.getCustomProxyManager().newProxy(proxy);
            app.getSessionManager().setProxy(null,newProxy,true);
        }
    }

    /**
     *
     * @param proxy
     * @return
     */
    public void remove(XBean proxy){
        List<XBean> items = load();
        String id = proxy.getString(Proxy.BEANNAME_ID);
        for(int i=0;i<items.size();i++){
            XBean item = items.get(i);
            if(item.isEquals(Proxy.BEANNAME_ID,id)){
                items.remove(i);
                save(items);
                break;
            }
        }
        //即使当前的items中没有，也发出事件，表示删除了
        app.getEventBus().emit(Yysk.EVENT_CUSTOM_PROXY_REMOVE,proxy,false);

        Proxy oldProxy = app.getSessionManager().getProxy();
        if(oldProxy!=null&& oldProxy.isCustom&&proxy.isEquals(Proxy.BEANNAME_ID,oldProxy.id)){
            //如果删除，当前的必须停止
            app.getSessionManager().setProxy(null,null,true);
        }

    }

    public List<XBean> load(){
        XBean data = IOUtil.load(app.getDataFile("custom_proxies.json"),XBean.class);
        List<XBean> items = null;
        if(data!=null){
            items = data.getList("items",XBean.class);
        }
        if(items==null){
            items = new ArrayList<>();
        }
        return items;
    }
    public XBean getProxy(String id){
        if(id==null||id.isEmpty()){
            return null;
        }
        List<XBean> items = load();
        for(XBean item : items){
            if(item.isEquals(Proxy.BEANNAME_ID,id)){
                return item;
            }
        }
        return null;
    }
    public Proxy newProxy(XBean proxy){
        Proxy newProxy = new Proxy();
        newProxy.id = proxy.getString(Proxy.BEANNAME_ID);
        newProxy.name = buildName(proxy);
        newProxy.isCustom=true;
        //newProxy.data=null;//就不需要存储了
        return newProxy;
    }
    public String buildName(XBean proxy){
        String name = proxy.getString(Proxy.BEANNAME_NAME,null);
        String host = proxy.getString("host","");
        String port = proxy.getString("port","");
        if(name!=null){
            return name;
        }else{
            return "("+host+":"+port+")";
        }
    }
    private void save(List<XBean> items){
        try {
            IOUtil.save(Json.stringify(new XBean("items",items)),app.getDataFile("custom_proxies.json"));
        } catch (IOException e) {
            MyLog.e(e);
        }
    }
}
