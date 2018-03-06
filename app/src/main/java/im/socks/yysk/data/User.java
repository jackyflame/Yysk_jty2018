package im.socks.yysk.data;

import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/27.
 */

public class User implements Json.IJsonable<XBean> {
    public String id;
    public String phoneNumber;
    public String psw;

    public String terminalNum;//最大终端限制数
    public String bindedTerminalNum;//已绑定终端数
    public String entername;//企业名

    public User() {
    }

    public boolean isGuest2() {
        //return id==null||id.isEmpty();
        return phoneNumber == null || phoneNumber.isEmpty();
    }


    @Override
    public XBean toJson() {
        return new XBean("id", id, "phone_number", phoneNumber,"psw", psw,"terminal_num",terminalNum,
                "binded_terminal_num",bindedTerminalNum,"entername",entername);
    }

    @Override
    public boolean fill(XBean bean) {
        id = bean.getString("id", null);
        phoneNumber = bean.getString("phone_number", null);
        psw = bean.getString("psw", null);
        terminalNum = bean.getString("terminal_num", null);
        bindedTerminalNum = bean.getString("binded_terminal_num", null);
        entername = bean.getString("entername", null);
        return true;
    }
}
