package im.socks.yysk.util;

import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.socks.yysk.YyskApplication;

/**
 * Created by Android Studio.
 * ProjectName: Yysk
 * Author: Haozi
 * Date: 2018/1/2
 * Time: 22:03
 */

public class StringUtils {

    public static boolean isInteger(String str){
        if(TextUtils.isEmpty(str)){
            return false;
        }
        return TextUtils.isDigitsOnly(str);
    }

    /**
     * 判断字符串是否为正浮点型（Float or Double）数据
     * x.xx或+x.xx都算作正浮点数
     * 0.0不算做正浮点数
     * @param str
     * @return true=>是;false=>不是
     */
    public static boolean strIsFloat(String str) {
        if (TextUtils.isEmpty(str)){
            return false;
        }
        if(isInteger(str)){
            return true;
        }
        Pattern p = Pattern.compile("//d+(//.//d+)?");
        Matcher m = p.matcher(str);
        if (m.matches()){
            return true;
        }
        return false;
    }

    public static String getNowTimeStr(){
        long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(time));
    }

    public static String getTimeStr(long time){
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(time));
    }

    public static String getTextViewStr(TextView textView){
        if(textView == null){
            return "";
        }
        return textView.getText() != null ? textView.getText().toString() : "";
    }

    public static boolean isEqualWithoutEmpty(String str1,String str2) {
        if(isEmpty(str1) || isEmpty(str2)){
            return false;
        }
        if(str1.equals(str2)){
            return true;
        }
        return false;
    }

    public static boolean isEmpty(String phoneNumber) {
        if(phoneNumber == null || phoneNumber.isEmpty()){
            return true;
        }
        return false;
    }

    public static boolean checkEmpty(String phoneNumber,String tips) {
        if(phoneNumber == null || phoneNumber.isEmpty()){
            if(!isEmpty(tips)){
                showToast(tips);
            }
            return true;
        }
        return false;
    }

    public static void showToast(String msg) {
        //或者显示对话框
        Toast.makeText(YyskApplication.getInstatnce(), msg, Toast.LENGTH_LONG).show();
    }
}
