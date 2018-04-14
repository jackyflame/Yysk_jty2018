package im.socks.yyskjtyqy.wxapi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelpay.PayResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

public class WXPayEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    public static final int RECHARGE_SUCCESS = 0x12;
    public static final int RECHARGE_FAIL = 0x14;

    public static final int HANDLER_GOVIP = 1000;
    public static final int HANDLER_CLOSE = 4000;

    public static final String JUMP_CLOSE = "JUMP_CLOSE";
    public static final String JUMP_BUYPACKGE = "JUMP_BUYPACKGE";
    public static final String JUMP_BUYMSC = "JUMP_BUYMSC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onReq(BaseReq baseReq) {}

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            //baseResp.errCode == 0 ? RECHARGE_SUCCESS : RECHARGE_FAIL
            //保存成功,跳转到购买套餐页面
            if(baseResp.errCode == 0){
                //记录支付成功，设置日志
                String log = "WeiXin Pay onResp success ";
                //针对不通结果进行处理
                if(baseResp instanceof PayResp){
                    String extData = ((PayResp)baseResp).extData;
                    log = log + " with extData:"+extData;
                }
                Log.e("WXPayEntryActivity",log);
            }else{
                String errorinfo = "WeiXin Pay onResp error-code:" + baseResp.errCode;
                Log.e("WXPayEntryActivity",errorinfo);
            }
        }
    }
}
