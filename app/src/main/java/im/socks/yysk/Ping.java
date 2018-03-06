package im.socks.yysk;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by cole on 2017/12/9.
 */

public class Ping {
    private Handler handler;
    private IPingListener listener;
    private int count = 3;
    /**
     * 超时，单位为秒
     */
    private int timeout = 30;
    private boolean isClosed = false;
    private List<PingWorker> workers;

    public Ping() {
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (listener == null) {
                    return;
                }
                if (msg.what == 1) {
                    Object[] args = (Object[]) msg.obj;
                    listener.onTime((String) args[0], (String) args[1]);
                }
            }
        };
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void ping(List<String> hosts, IPingListener listener) {
        this.listener = listener;
        workers = new ArrayList<>();
        for (int i = 0; i < hosts.size(); i++) {
            PingWorker worker = new PingWorker(hosts.get(i));
            worker.setDaemon(true);
            worker.start();
            workers.add(worker);
        }
    }

    /**
     * 停止ping，必须在主线程调用
     */
    public void close() {
        listener = null;
        isClosed = true;
        if (workers != null) {
            for (PingWorker worker : workers) {
                worker.close();
            }
        }

    }

    public interface IPingListener {
        /**
         *
         * 获得主机ping的时间
         * @param host
         * @param time 如：32.33，单位为毫秒
         */
        void onTime(String host, String time);
        //void onEnd(String host);
    }


    private class PingWorker extends Thread {
        private String host;
        private Process process;

        public PingWorker(String host) {
            this.host = host;
        }

        @Override
        public void run() {
            //为了避免host为恶意的情况，使用字符串
            //"/system/bin/ping -c " + count + " -W " + timeout + " \"" + host+"\"";
            String command = null;//
            if(count>0){
                command = "/system/bin/ping -c " + count + " -W " + timeout + " \"" + host+"\"";
            }else{
                command = "/system/bin/ping -W " + timeout + " \"" + host+"\"";
            }
            try {
                process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    //64 bytes from 14.215.177.39: icmp_seq=0 ttl=54 time=25.042 ms
                    String time = parseTime(line);
                    MyLog.d("ping=%s,line=%s,time=%s", command, line, time);
                    if (time != null && time.length() > 0) {
                        handler.sendMessage(handler.obtainMessage(1, new Object[]{host, time}));
                    }

                    //如果已经关闭了，就退出
                    if (isClosed) {
                        break;
                    }
                }

            } catch (IOException e) {
                MyLog.e(e);
            } finally {
                if (process != null) {
                    process.destroy();
                }

            }
        }

        public void close() {
            if (process != null) {
                process.destroy();
            }
        }

        private String parseTime(String s) {
            //64 bytes from 14.215.177.39: icmp_seq=0 ttl=54 time=25.042 ms
            int i = s.indexOf("time=");
            if (i <= 0) {
                return null;
            }
            i += 5;
            int j = s.indexOf("ms", i);
            if (j <= 0) {
                return null;
            }
            return s.substring(i, j);
        }
    }
}
