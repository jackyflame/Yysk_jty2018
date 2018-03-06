package im.socks.yysk.api;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.IBinder;
import android.os.RemoteException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import im.socks.yysk.App;
import im.socks.yysk.MyLog;
import im.socks.yysk.vpn.IYyskService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by cole on 2017/10/21.
 * <p>
 * 目的是支持http请求不通过vpn，如果通过vpn，直接使用OkHttpClient即可
 */

public class Http {

    private AtomicReference<IYyskService> service = new AtomicReference<>();
    private ServiceConnection serviceConnection = null;

    /**
     * 如果一时不能够获得vpn service，使用这个来直接请求，通过vpn
     */
    private OkHttpClient fallbackHttpClient = null;

    private App app;

    /**
     * @param app
     */
    public Http(App app) {
        this.app = app;
        bindService();
        fallbackHttpClient = createFallbackHttpClient();
    }

    public void destroy() {
        unbindService();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroy();
    }

    private void bindService() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                service.set(IYyskService.Stub.asInterface(binder));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                service.set(null);
            }

            @Override
            public void onBindingDied(ComponentName name) {
                service.set(null);
            }
        };

        app.getVpn().bind(serviceConnection);
    }

    private void unbindService() {
        if (serviceConnection != null) {
            app.getVpn().unbind(serviceConnection);
            serviceConnection = null;
        }
    }

    public String doGet(String url) {
        return doGetByRpc(url);
    }

    //在vpn进程执行http操作，这样也可以protect socket
    private String doGetByRpc(String url) {
        //先保存一个，因为可以在多个线程执行
        IYyskService service = this.service.get();
        if (service != null) {
            try {
                return service.doGet(url);
            } catch (RemoteException e) {
                MyLog.e(e);
            }
        }

        if (fallbackHttpClient != null) {
            //如果service不能够获得，就使用默认的尝试，也就是不绕过vpn
            return doGet(fallbackHttpClient, url);
        } else {
            return null;
        }

    }

    public static String doGet(OkHttpClient httpClient, String url) {
        Request request = new Request.Builder().get().url(url).build();
        try (Response response = httpClient.newCall(request).execute();) {
            String s = response.body().string();
            int code = response.code();
            MyLog.d("doGet=%s,%s", code, s);
            if (code == 200) {
                return s;
            } else if (code == 500) {
                //后台服务器错误了
            } else {
                //
            }
        } catch (IOException e) {
            MyLog.e(e, "doGet=%s", url);
        }
        return null;

    }


    private static OkHttpClient createFallbackHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };


        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            MyLog.e(e);
        }


        if (sslContext != null) {
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        }

        return builder.build();
    }

    public static OkHttpClient createHttpClient(final VpnService vpnService) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        SocketFactory socketFactory = new MySocketFactory(SocketFactory.getDefault()) {
            @Override
            public Socket protectSocket(Socket socket) {
                //socket必须bound，也就是获得fd后protect才有效，否则不起作用
                vpnService.protect(socket);
                return socket;
            }
        };
        builder.socketFactory(socketFactory);

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };


        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            MyLog.e(e);
        }


        if (sslContext != null) {
            SSLSocketFactory sslSocketFactory = new MySSLSocketFactory(sslContext.getSocketFactory()) {
                @Override
                public Socket protectSocket(Socket socket) {
                    vpnService.protect(socket);
                    return socket;
                }
            };
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        }

        return builder.build();
    }


    private static abstract class MySocketFactory extends SocketFactory {
        private SocketFactory impl;

        public MySocketFactory(SocketFactory impl) {
            this.impl = impl;
        }

        @Override
        public Socket createSocket() throws IOException {
            return protect(impl.createSocket());
        }

        //============== 如果调用的是下面的创建方法，返回的socket已经连接了，protect已经无效，幸运的是OkHttpClient不使用下面的方法
        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return protect(impl.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            return protect(impl.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return protect(impl.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return protect(impl.createSocket(address, port, localAddress, localPort));
        }

        private Socket protect(Socket socket) {
            if (socket.isConnected()) {
                //已经连接，protect已经不起作用了
            } else if (!socket.isBound()) {
                //or YyskVpn.getFd(socket)==-1;
                try {
                    socket.bind(new InetSocketAddress(0));
                } catch (IOException e) {
                    MyLog.e(e);
                }
            } else {
                //已经bind
            }

            return protectSocket(socket);
        }

        protected abstract Socket protectSocket(Socket socket);
    }

    private static abstract class MySSLSocketFactory extends SSLSocketFactory {
        private SSLSocketFactory impl;

        public MySSLSocketFactory(SSLSocketFactory impl) {
            this.impl = impl;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return impl.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return impl.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket() throws IOException {
            return protect(impl.createSocket());
        }

        //============== 如果调用的是下面的创建方法，返回的socket已经连接了，protect已经无效，幸运的是OkHttpClient不使用下面的方法
        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return protect(impl.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
            return protect(impl.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return protect(impl.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return protect(impl.createSocket(address, port, localAddress, localPort));
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return protect(impl.createSocket(s, host, port, autoClose));
        }

        private Socket protect(Socket socket) {
            if (socket.isConnected()) {
                //已经连接，protect已经不起作用了
            } else if (!socket.isBound()) {
                //or YyskVpn.getFd(socket)==-1;
                try {
                    socket.bind(new InetSocketAddress(0));
                } catch (IOException e) {
                    MyLog.e(e);
                }
            } else {
                //已经bind
            }

            return protectSocket(socket);
        }

        protected abstract Socket protectSocket(Socket socket);
    }


}
