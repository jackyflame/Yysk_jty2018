package im.socks.yysk.util;

public class XRspBean extends XBean{

    public boolean isRspSuccess(){
        return isEquals("status_code", 0);
    }

    public XBean getRspData(){
        return getXBean("data");
    }

    public String getRspError(){
        return getString("error");
    }

}
