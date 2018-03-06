package im.socks.yysk;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by cole on 2017/11/10.
 */

public class MyLog {
    private static final String TAG = "Yysk";
    private static boolean isLog = true;

    private static void print(String tag, int level, Throwable e, String format, Object... args) {
        //如果不需要输出日志，就全部不显示
        if (!isLog) {
            return;
        }
        String msg = args != null && args.length > 0 ? String.format(format, args) : format;

        //android log的实现如下
        //如果异常有cause，将不会打印，说法是为了减少日志，因为cause可能已经打印了
        //如果是UnknownHostException也不会打印
        //现在为了方便测试，会打印出来
        if (/*e instanceof UnknownHostException || e.getCause() == null*/e != null) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(bos, "utf-8"));) {
                e.printStackTrace(writer);
                writer.close();
                msg += "\n" + bos.toString("utf-8");
                e = null;
            } catch (IOException e2) {
                //ignore
            }
        }

        if (e != null) {

            if (level == Log.VERBOSE) {
                Log.v(tag, msg, e);
            } else if (level == Log.DEBUG) {
                Log.d(tag, msg, e);
            } else if (level == Log.INFO) {
                Log.i(tag, msg, e);
            } else if (level == Log.WARN) {
                Log.w(tag, msg, e);
            } else if (level == Log.ERROR) {
                Log.e(tag, msg, e);

            } else if (level == Log.ASSERT) {
                //Log.(tag,msg,e);
            }


        } else {
            if (level == Log.VERBOSE) {
                Log.v(tag, msg);
            } else if (level == Log.DEBUG) {
                Log.d(tag, msg);
            } else if (level == Log.INFO) {
                Log.i(tag, msg);
            } else if (level == Log.WARN) {
                Log.w(tag, msg);
            } else if (level == Log.ERROR) {
                Log.e(tag, msg);
            } else if (level == Log.ASSERT) {
                //Log.e(tag,msg);
            }
        }
    }

    public static void d(String format, Object... args) {
        print(TAG, Log.DEBUG, null, format, args);
    }

    public static void d(Throwable e, String format, Object... args) {
        print(TAG, Log.DEBUG, e, format, args);
    }

    public static void i(String format, Object... args) {
        print(TAG, Log.INFO, null, format, args);
    }


    public static void i(Throwable e, String format, Object... args) {
        print(TAG, Log.INFO, e, format, args);
    }

    public static void e(String format, Object... args) {
        print(TAG, Log.ERROR, null, format, args);
    }

    public static void e(Throwable e, String format, Object... args) {
        print(TAG, Log.ERROR, e, format, args);
    }

    public static void e(Throwable e) {
        print(TAG, Log.ERROR, e, "");
    }


}
