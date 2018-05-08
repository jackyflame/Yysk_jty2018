package im.socks.yysk.api;


import android.os.Build;

import java.util.List;

import im.socks.yysk.App;
import im.socks.yysk.AppDZ;
import im.socks.yysk.util.StringUtils;
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
        String model= android.os.Build.MODEL;
        if(StringUtils.isEmpty(model)){
            model = "android device";
        }
        loginParams.put("terminal_model",model);
        String brand = Build.BRAND;
        loginParams.put("terminal_number",brand);
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

    /** 获取动态加速节点
     * */
    public void getProxyInfo(String lineId, ICallback<XBean> cb) {
        String model= android.os.Build.MODEL;
        if(StringUtils.isEmpty(model)){
            model = "android device";
        }
        invoke("10058", "20058", new XBean("line_id", lineId,"terminal_model",model), cb);
    }

    /** 关闭动态加速节点
     * */
    public void sendProxyClose(String lineId, ICallback<XBean> cb) {
        String model= android.os.Build.MODEL;
        if(StringUtils.isEmpty(model)){
            model = "android device";
        }
        invoke("10059", "20059", new XBean("line_id", lineId,"terminal_model",model), cb);
    }

    /**
     * 获取资费套餐列表
     * */
    public void getOrderList(ICallback<XBean> cb){
        invoke("10045", "20045", new XBean(), cb);
    }

    /**
     * 获取用户信息
     * */
    public void getUserInfo(ICallback<XBean> cb){
        invoke("10024", "20024", new XBean(), cb);
    }

    /**
     * 获取购买记录
     * */
    public void getBuyRecord(ICallback<XBean> cb){
        invoke("10047", "20047", new XBean(), cb);
    }

    /**
     * 设备查询
     * */
    public void getDeviceList(ICallback<XBean> cb){
        invoke("10048", "20048", new XBean(), cb);
    }

    /**
     * 常见问题列表
     * */
    public void getQuestionList(ICallback<XBean> cb){
        invoke("10050", "20050", new XBean(), cb);
    }

    /**
     * 用户消息列表
     * */
    public void getMsgList(ICallback<XBean> cb){
        invoke("10051", "20051", new XBean(), cb);
    }

    /**
     * 用户消息详情
     * */
    public void getMsgDetail(long messageid,ICallback<XBean> cb){
        invoke("10052", "20052", new XBean("messageid",messageid), cb);
    }

    /**
     * 用户访问规则列表
     * */
    public void getRuleList(ICallback<XBean> cb){
        invoke("10053", "20053", new XBean(), cb);
    }

    /**
     * 用户提交工单(意见反馈)
     * */
    public void sendFeedBack(String content,ICallback<XBean> cb){
        invoke("10055", "20055", new XBean("content",content), cb);
    }

    /**
     * 用户工单列表
     * */
    public void getFeedbackList(ICallback<XBean> cb){
        invoke("10054", "20054", new XBean(), cb);
    }

    /**
     * 用户工单详情
     * */
    public void getFeedbackDetail(long work_order_id,ICallback<XBean> cb){
        invoke("10056", "20056", new XBean("work_order_id",work_order_id), cb);
    }

    /**
     * 用户工单提交回复
     * */
    public void getFeedbackReply(long work_order_id,String content,ICallback<XBean> cb){
        invoke("10057", "20057", new XBean("work_order_id",work_order_id,"content",content), cb);
    }
}
