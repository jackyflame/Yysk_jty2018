package im.socks.yysk.api;


import java.util.List;

import im.socks.yysk.App;
import im.socks.yysk.AppDZ;
import im.socks.yysk.util.XBean;
import im.socks.yysk.util.XRspBean;

/**
 * Created by Android Studio.
 * ProjectName: Yysk
 * Author: haozi
 * Date: 2018/1/2
 * Time: 16:29
 */
public class YyskDZApi extends YyskApi {

    protected AppDZ app;

    public YyskDZApi(AppDZ app) {
        super(app);
    }

    @Override
    protected void initApi(App app) {
        super.initApi(app);
        if(app instanceof AppDZ){
            this.app = (AppDZ) app;
        }
    }

    /*--------------------------------------------------------------------------------------------*/
    public void loginDZ(String strPhoneNum, String strPassword, final ICallback<XBean> cb) {
        //如果仅仅执行登录，感觉没有做任何事情，什么都不返回，token也没有
        final XBean loginParams = new XBean("mobile_number", strPhoneNum, "password", strPassword);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                //先登录，不登录也不影响，实际上应该返回用户的基本信息，如：user_id+token(控制api的访问)
                XBean result = invoke("10020", "20020", loginParams);
                if (cb != null) {
                    final XBean result2 = result;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            cb.onResult(result2);
                        }
                    });
                }
            }
        });
    }

    public void bindDevice(String account, final ICallback<XBean> cb) {
        //如果仅仅执行登录，感觉没有做任何事情，什么都不返回，token也没有
        final XBean params = new XBean("account", account);
        //先登录，不登录也不影响，实际上应该返回用户的基本信息，如：user_id+token(控制api的访问)
        invoke("10041", "20041", params, cb);
    }

    public void getAppVersionDZ(String version,ICallback<XBean> cb){
        invoke("10043","20043",new XBean("os","android","versionid",version),cb);
    }

    public void checkVpnUpdateVerson(String account,String psw,ICallback<XBean> cb) {
        invoke("10039","20039",new XBean("account",account,"password",psw),cb);
    }

    public void checkVpnEndTime(String account,ICallback<XBean> cb) {
        invoke("10044","20044",new XBean("account",account),cb);
    }

    /** 获取企业用户专用节点
    * strPhoneNum: 登录手机号
    * 返回值: 成功返回[{"authscheme":"","host":"","name":"","password":"","port":"","price":,"ssrObfs":"n","ssrProtocol":"","user":""}],失败返回null
    * */
    public void getDZProxyList(String strPhoneNum, ICallback<XBean> cb) {
        invoke("10014", "20014", new XBean("mobile_number", strPhoneNum), cb);
    }

    /**
     * 获取资费套餐列表
     * */
    public void getOrderList(ICallback<List<XBean>> cb){
        invoke("10045", "20045", new XBean(), cb);
    }
}
