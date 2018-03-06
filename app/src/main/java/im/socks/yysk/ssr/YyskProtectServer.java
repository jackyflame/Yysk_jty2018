package im.socks.yysk.ssr;

import android.net.LocalSocket;
import android.net.VpnService;

import com.github.shadowsocks.System;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import im.socks.yysk.MyLog;
import im.socks.yysk.vpn.YyskVpn;

/**
 * 监听 {app_data_dir}/protect_path，接收fd，然后让其不通过vpn，而是直接连接
 */
public class YyskProtectServer extends BaseSocketServer {

    private VpnService vpnService;


    public YyskProtectServer(String socketPath, VpnService vpnService) {
        super(socketPath);
        this.vpnService = vpnService;

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
                    OutputStream output = socket.getOutputStream();) {

                if (!isRunning.get()) {
                    //stop command or other
                    input.read();
                    //有些client发送fd后就关闭了socket，不关心返回
                    try {
                        output.write(0);
                    } catch (IOException e) {
                        //ignore
                    }

                } else {
                    input.read();
                    //因为前面读取数据了，调用下面的方法，就会解析，如果为发送file-descriptor
                    //的消息，就可以获得了
                    FileDescriptor[] fds = socket.getAncillaryFileDescriptors();
                    if (fds != null && fds.length > 0) {
                        int fd = YyskVpn.getFd(fds[0]);
                        MyLog.d("protect fd=%s", fd);
                        boolean ret = false;
                        if (fd != -1) {
                            //必须在prepare和授权后才有效，否则总是返回false
                            ret = vpnService.protect(fd);

                            // Trick to close file decriptor
                            //执行protect后，本地的file-descriptor已经不需要了,可以释放了
                            //对应的文件（or socket）并不会关闭，因为其它进程还在打开
                            //因为android并没有提供方法，所以只能够通过jni来执行了
                            System.jniclose(fd);
                        }

                        //有些client发送收就关闭了，不需要返回，这了就忽略异常
                        try {
                            if (ret) {
                                output.write(0);
                            } else {
                                output.write(1);
                            }
                        } catch (IOException e) {
                            //ignore
                        }

                    }
                }


            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }
}
