package im.socks.yysk.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

public class NetUtil {

    public static boolean checkAndHandleRsp(XRspBean result, Context context, String errorPrefix,ProgressDialog dialog){
        if(dialog != null){
            dialog.dismiss();
        }
        if (result != null) {
            if (result.isRspSuccess()) {
               return true;
            } else {
                //错误提示
                if(!StringUtils.isEmpty(errorPrefix)){
                    String msg = (errorPrefix + ":" + result.getRspError());
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            //错误
            if(!StringUtils.isEmpty(errorPrefix)) {
                String msg = (errorPrefix + ",请检查网络后再次尝试");
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

}
