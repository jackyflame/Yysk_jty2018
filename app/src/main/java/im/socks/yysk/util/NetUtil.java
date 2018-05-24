package im.socks.yysk.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class NetUtil {

    public static boolean checkAndHandleRsp(XBean result, Context context, String errorPrefix,ProgressDialog dialog){
        return checkAndHandleRsp(result,context, errorPrefix,null,dialog,false);
    }

    public static boolean checkAndHandleRsp(XBean result, Context context, String errorPrefix, String errorSuffix,ProgressDialog dialog){
        return checkAndHandleRsp(result,context, errorPrefix,errorSuffix,dialog,false);
    }

    public static boolean checkAndHandleRspWithData(XBean result, Context context, String errorPrefix,ProgressDialog dialog){
        return checkAndHandleRspWithData(result,context,errorPrefix,null,dialog);
    }

    public static boolean checkAndHandleRspWithData(XBean result, Context context, String errorPrefix, String errorSuffix,ProgressDialog dialog){
        return checkAndHandleRsp(result,context, errorPrefix,errorSuffix,dialog,true);
    }

    public static boolean checkAndHandleRsp(XBean result, Context context, String errorPrefix, String errorSuffix,ProgressDialog dialog,boolean isCheckData){
        if(dialog != null){
            dialog.dismiss();
        }
        if (result != null) {
            boolean isSuccess = isRspSuccess(result);
            if(isCheckData){
                isSuccess = isSuccess && !result.isEmpty("data");
            }
            if (isSuccess) {
               return true;
            } else {
                //错误提示
                if(!StringUtils.isEmpty(errorPrefix)){
                    String rspError = getRspError(result);
                    String msg = errorPrefix;
                    if(rspError != null && !rspError.isEmpty()){
                        msg = msg + ":" + rspError;
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            //错误
            if(!StringUtils.isEmpty(errorPrefix)) {
                String msg = errorPrefix;
                if(StringUtils.isEmpty(errorSuffix)){
                    msg = msg + ",请检查网络后再次尝试";
                }else{
                    msg = msg + "," +errorSuffix;
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    public static boolean isRspSuccess(XBean result){
        return result.isEquals("status_code", 0);
    }

    public static XBean getRspData(XBean result){
        try{
            return result.getXBean("data");
        }catch (Exception e){
            Log.e("NetUtil","getRspData error:"+e.getMessage());
            return null;
        }
    }

    public static List<XBean> getRspDataList(XBean result){
        try{
            return result.getList("data");
        }catch (Exception e){
            Log.e("NetUtil","getRspDataList error:" +e.getMessage());
            return null;
        }
    }

    public static String getRspError(XBean result){
        return result.getString("error");
    }

}
