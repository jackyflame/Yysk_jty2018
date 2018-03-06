package im.socks.yysk.data;

import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/27.
 */

public class Proxy implements Json.IJsonable<XBean> {
    public String id;
    public String name;

    /**true表示为自己添加的，false表示系统提供的*/
    public boolean isCustom=false;

    /**
     * 如果不是自定义的，表示为那个用户的代理
     */
    //public String phoneNumber;
    
    /**
     * 获得原始的
     */
    public XBean data;

    public Proxy() {

    }


    @Override
    public XBean toJson() {
        return new XBean("name", name, "data", data,"id",id,"is_custom",isCustom);
    }

    @Override
    public boolean fill(XBean bean) {
        this.id = bean.getString("id");
        this.name = bean.getString("name");
        this.data = bean.getXBean("data");
        this.isCustom = bean.getBoolean("is_custom",false);
        //this.phoneNumber = bean.getString("phone_number",null);
        return true;
    }
}
