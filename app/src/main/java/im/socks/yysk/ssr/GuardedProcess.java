package im.socks.yysk.ssr;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import im.socks.yysk.MyLog;

public class GuardedProcess {
    private boolean isDebug = false;
    private List<String> cmd;
    private Process process;
    private Thread worker;

    private Object lock = new Object();
    private boolean stopped=false;


    public GuardedProcess(List<String> cmd) {
        this.cmd = cmd;
    }

    public void start() {
        if (worker != null) {
            throw new RuntimeException("已经调用过该方法了");
        }


        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock){
                    if(stopped){
                        return;
                    }
                    //
                    MyLog.d("start cmd=%s", cmd);
                    ProcessBuilder processBuilder = new ProcessBuilder(cmd);
                    try {
                        process = processBuilder.redirectErrorStream(true).start();
                    } catch (IOException e) {
                        MyLog.e(e);
                    }
                }

                Process p = process;
                if(p!=null){
                    try {
                        if (isDebug) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            String s = "";
                            while ((s = br.readLine()) != null) {
                                MyLog.d("cmd=%s,output=%s", cmd, s);
                            }
                        } else {
                            p.waitFor();
                        }
                    } catch (Exception e) {
                        MyLog.e(e, "cmd=%s", cmd);
                    }
                }

            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    public void destroy() {
        synchronized (lock){
            stopped=true;
            if(process!=null){
                process.destroy();
            }
            process=null;
        }

        if (worker != null) {
            try {
                //worker.interrupt();
                worker.join();
            } catch (InterruptedException e) {
                MyLog.e(e);
            }
        }

        MyLog.d("exit cmd=%s", cmd);


    }
}
