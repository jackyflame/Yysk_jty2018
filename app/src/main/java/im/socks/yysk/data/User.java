package im.socks.yysk.data;

import java.util.List;

import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/27.
 */

public class User implements Json.IJsonable<XBean> {
    public int id;
    public int user_type;//用户类型，1：YY员工；2：JYT员工；3：个人用户；4：企业用户；5：企业子用户
    public String username;
    public String mobile_number;
    public String password;
    public String email;
    public String corporate_name;//企业名
    public String department;//部门
    public String created;//创建时间
    public String last_login_time;//最后登录时间
    public String invite_code;//邀请码
    public String ss_pass;//加密密码
    public String token;//token

    public String expiring_time;//过期时间
    public int rest_days;//到期天数
    public XBean tariff_package;//购买包
    public List<XBean> promotions;//活动
    public int terminal_no;
    public boolean is_active;//是否激活

    public User() {
    }

    public boolean isGuest2() {
        //return id==null||id.isEmpty();
        return mobile_number == null || mobile_number.isEmpty();
    }


    @Override
    public XBean toJson() {
        return new XBean("id", id, "user_type", user_type,  "username", username, "mobile_number", mobile_number,
                "password", password, "email",email, "corporate_name",corporate_name,"department",department,"created",created,
                "last_login_time",last_login_time,"invite_code",invite_code,"ss_pass",ss_pass,"token",token,
                "expiring_time",expiring_time,"rest_days",rest_days,"tariff_package",tariff_package,
                "terminal_no",terminal_no,"is_active",is_active,"promotions",promotions
        );
    }

    @Override
    public boolean fill(XBean bean) {
        id = bean.getInteger("id", -1);
        user_type = bean.getInteger("user_type", -1);
        username = bean.getString("username", null);
        mobile_number = bean.getString("mobile_number", null);
        password = bean.getString("password", null);
        email = bean.getString("email", null);
        corporate_name = bean.getString("corporate_name", null);
        department = bean.getString("department", null);
        created = bean.getString("created", null);
        last_login_time = bean.getString("last_login_time", null);
        invite_code = bean.getString("invite_code", null);
        ss_pass = bean.getString("ss_pass", null);
        token = bean.getString("token", null);
        expiring_time = bean.getString("expiring_time", null);
        rest_days = bean.getInteger("rest_days", 0);
        tariff_package = bean.getXBean("tariff_package");
        terminal_no = bean.getInteger("terminal_no", -1);
        is_active = bean.getBoolean("is_active", false);
        promotions = bean.getList("promotions", XBean.class);
        return true;
    }
}
