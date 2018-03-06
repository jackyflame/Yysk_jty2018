package im.socks.yysk.ssr;

import java.io.IOException;
import java.util.List;

import im.socks.yysk.MyLog;

/**
 * Created by cole on 2017/11/15.
 */

public class ProcessImpl implements  IProcess{

    private List<String> cmd;
    private Process process;

    public ProcessImpl(List<String> cmd) {
        this.cmd = cmd;
    }

    @Override
    public void start() {
        if(process!=null){
            return;
        }
        MyLog.d("start cmd=%s", cmd);
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        try {
            //启动很快，不会阻塞main线程
            process = processBuilder.redirectErrorStream(true).start();
        } catch (IOException e) {
            MyLog.e(e);
        }
    }
    @Override
    public void stop() {
        if(process!=null){
            process.destroy();
        }
        MyLog.d("exit cmd=%s", cmd);
    }
}
