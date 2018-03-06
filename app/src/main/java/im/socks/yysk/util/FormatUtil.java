package im.socks.yysk.util;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by cole on 2017/11/1.
 */

public class FormatUtil {

    public static String formatDate(Context context, long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(date);
    }

    /**
     * 格式化时间长度，如：10日20小时5分5秒
     *
     * @param context
     * @param ms      单位为毫秒
     * @return
     */
    public static String formatDuration(Context context, long ms) {
        //xx日xx小时xx分xx秒
        StringBuilder buf = new StringBuilder();
        long[] units = new long[]{24 * 60 * 60 * 1000L, 60 * 60 * 1000L, 60 * 1000L, 1000L};
        String[] labels = new String[]{"天", "小时", "分", "秒"};
        for (int i = 0; i < units.length; i++) {
            long unit = units[i];
            String label = labels[i];
            long j = ms / unit;
            if (j > 0) {
                buf.append(j).append(label);
                ms = ms % unit;
            }
        }

        return buf.toString();
    }

    /**
     * 格式化大小，如：1GB，200MB等
     *
     * @param context
     * @param value   单位为字节
     * @return
     */
    public static String formatSize(Context context, long value) {
        //StringBuilder buf = new StringBuilder();
        long[] units = new long[]{1024 * 1024 * 1024 * 1024L, 1024 * 1024 * 1024L, 1024 * 1024L, 1024L, 1};
        String[] labels = new String[]{"TB", "GB", "MB", "KB", "Byte"};
        for (int i = 0; i < units.length; i++) {
            double unit = units[i];
            String label = labels[i];
            if (value >= unit) {
                double j = value / unit;
                DecimalFormat df = new DecimalFormat("#.##");
                return df.format(j) + " " + label;
            }
        }

        return value + " " + labels[labels.length - 1];


    }

    public static String formatRate(Context context, long value) {
        return formatSize(context, value) + "/秒";
    }
}
