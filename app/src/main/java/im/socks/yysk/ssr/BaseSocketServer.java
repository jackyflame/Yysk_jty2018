package im.socks.yysk.ssr;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import im.socks.yysk.MyLog;

/**
 * Created by cole on 2017/10/18.
 */

public abstract class BaseSocketServer {

    private LocalServerSocket serverSocket = null;
    protected AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread thread;

    private String socketPath;
    private LocalSocketAddress socketAddress;
    private LocalSocket fdSocket;


    public BaseSocketServer(String socketPath) {
        this.socketPath = socketPath;
        this.socketAddress = new LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM);
    }

    public boolean start() {
        try {
            new File(socketPath).delete();
            //必须使用成员变量，否则使用局部对象，LocalSocket释放后，会引起LocakSocketImpl释放，会关闭fd
            //这样导致LocalServerSocket.accept()的时候，抛出IOException("Bad File Descriptor")
            fdSocket = new LocalSocket();
            fdSocket.bind(socketAddress);//创建fd
            serverSocket = new LocalServerSocket(fdSocket.getFileDescriptor());
        } catch (IOException e) {
            MyLog.e(e);
            return false;
        }

        isRunning.set(true);
        thread = new Thread() {
            @Override
            public void run() {
                loop();
            }
        };
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    public void stop() {
        isRunning.set(false);

        if (serverSocket != null) {
            sendStopCommand();
            try {
                serverSocket.close();
            } catch (IOException e) {
                MyLog.e(e);
            }
        }
        if (thread != null) {
            //如果正在accept，需要等待很久
            try {
                thread.join();
            } catch (InterruptedException e) {
                MyLog.e(e);
            }
        }

        MyLog.d("BaseSocketServer stop");
    }

    private void sendStopCommand() {

        try (LocalSocket localSocket = new LocalSocket();) {
            localSocket.connect(socketAddress);
            //随便写几个
            localSocket.getOutputStream().write(1);
            localSocket.getOutputStream().flush();
            localSocket.getInputStream().read();
        } catch (IOException e) {
            MyLog.e(e);
        }

    }

    private void loop() {
        MyLog.d("BaseSocketServer enter loop");
        ExecutorService pool = Executors.newFixedThreadPool(4);
        while (isRunning.get() && fdSocket.getFileDescriptor() != null) {
            try {
                LocalSocket socket = serverSocket.accept();
                pool.execute(createWorker(socket));
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        pool.shutdown();


        MyLog.d("BaseSocketServer exit loop");


    }

    protected abstract Runnable createWorker(LocalSocket socket);
}
