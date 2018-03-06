package im.socks.yysk.ssr;

import android.net.LocalSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import im.socks.yysk.MyLog;
import im.socks.yysk.vpn.ITunnelListener;

/**
 * 监听ss-local发送的流量通知
 */
public class YyskTrafficMonitor extends BaseSocketServer {

    private long lastTime = 0;
    private long lastTx = 0;
    private long lastRx = 0;
    private long rxRate = 0;
    private long txRate = 0;

    private ITunnelListener listener;

    public YyskTrafficMonitor(String socketPath, ITunnelListener listener) {
        super(socketPath);
        this.listener = listener;

    }


    private synchronized void update(long tx, long rx) {
        long now = System.currentTimeMillis();
        if (lastTime == 0) {
            lastTime = now;
        }

        if (tx > lastTx && now > lastTime) {
            txRate = Math.round((tx - lastTx) * 1.0 / (now - lastTime) * 1000);
        }
        if (rx > lastRx && now > lastTime) {
            rxRate = Math.round((rx - lastRx) * 1.0 / (now - lastTime) * 1000);
        }

        lastRx = rx;
        lastTx = tx;
        lastTime = now;

        if (listener != null) {
            listener.onTrafficUpdate(rxRate, txRate, rx, tx);
        }


    }

    @Override
    protected Runnable createWorker(LocalSocket socket) {
        return new Worker(socket);
    }

    private class Worker implements Runnable {
        private LocalSocket socket;

        public Worker(LocalSocket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    LocalSocket socket = this.socket;
                    InputStream input = socket.getInputStream();
                    OutputStream output = socket.getOutputStream();
            ) {
                if (!isRunning.get()) {
                    //表示已经停止，获得的是stop command或者其它，全部忽略
                    input.read();
                    output.write(0);
                } else {
                    byte[] buffer = new byte[16];
                    if (input.read(buffer) == 16) {
                        ByteBuffer buf = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
                        long tx = buf.getLong();
                        long rx = buf.getLong();
                        //发出通知
                        update(tx, rx);
                    } else {
                        //ignore
                    }

                    //必须返回一个值，否则发送的socket还在等待
                    output.write(0);
                }

            } catch (Exception e) {
                MyLog.e(e);
            } finally {

            }
        }
    }


}
