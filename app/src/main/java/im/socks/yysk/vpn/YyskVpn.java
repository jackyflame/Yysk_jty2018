package im.socks.yysk.vpn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.VpnService;

import com.github.shadowsocks.System;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import im.socks.yysk.App;
import im.socks.yysk.MyLog;
import im.socks.yysk.data.Proxy;
import im.socks.yysk.data.Session;
import im.socks.yysk.util.IOUtil;
import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;

import static android.app.Activity.RESULT_OK;

/**
 * Created by cole on 2017/10/16.
 */

public class YyskVpn {
    private static Method getFileDescriptor$ = null;
    private static Method getInt$ = null;
    private static Method setInt$ = null;

    static {
        try {
            getFileDescriptor$ = Socket.class.getDeclaredMethod("getFileDescriptor$");
            getFileDescriptor$.setAccessible(true);
            getInt$ = FileDescriptor.class.getDeclaredMethod("getInt$");
            getInt$.setAccessible(true);
            setInt$ = FileDescriptor.class.getDeclaredMethod("setInt$", int.class);
        } catch (NoSuchMethodException e) {
            MyLog.e(e);
        }
    }


    /**
     * 请求获得vpn的授权
     */
    public final static int REQUEST_VPN = 1;

    private final Context context;

    private final App app;

    public YyskVpn(App app) {
        this.app = app;
        this.context = app.getContext();
        //在这里监听事件?
    }

    /**
     * 在activity中调用，所在的activity需要处理onActivityResult
     *
     * @param activity
     */
    public void start(Activity activity) {
        Intent intent = VpnService.prepare(activity);
        if (intent != null) {
            activity.startActivityForResult(intent, REQUEST_VPN);
        } else {
            start();
        }
    }

    /**
     * 如果是在Activity中调用，建议先检查VpnService.prepare(context)
     */
    public void start() {
        Intent intent = new Intent(context, YyskVpnService.class);
        intent.setAction(YyskVpnService.START_ACTION);
        context.startService(intent);

    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VPN) {
            if (resultCode == RESULT_OK) {
                start();
            } else {
                //取消
            }
            return true;
        } else {
            return false;
        }
    }


    public void stop() {
        Intent intent = new Intent(context, YyskVpnService.class);
        //如果还有Context.BIND_AUTO_CREATE的connection存在，不会destroy service
        //context.stopService(intent);
        //使用下面的方式
        intent.setAction(YyskVpnService.STOP_ACTION);
        context.startService(intent);
    }

    //如果是在Activity中调用，建议先检查VpnService.prepare(context)
    public void reload() {
        Intent intent = new Intent(context, YyskVpnService.class);
        intent.setAction(YyskVpnService.RELOAD_ACTION);
        context.startService(intent);
    }

    public void bind(ServiceConnection conn) {
        Intent intent = new Intent(context, YyskVpnService.class);
        intent.setAction(YyskVpnService.SERVICE_ACTION);
        if (context.bindService(intent, conn, Context.BIND_AUTO_CREATE)) {
            //do nothing
        } else {
            context.unbindService(conn);
        }

    }

    public void unbind(ServiceConnection conn) {
        if (conn != null) {
            context.unbindService(conn);
        }
    }

    //================

    /**
     * 保护socket不通过vpn
     *
     * @param socket     需要保护的socket
     * @param socketPath 监听的路径，如：{app_data_dir}/protect_path
     */
    public static void protect(Socket socket, String socketPath) {
        int i = getFd(socket);
        if (i != -1) {
            System.sendfd(i, socketPath);
            //nativeProtect(i,socketPath);
        }
    }

    private static void nativeProtect(int fd, String socketPath) {
        MyLog.d("native protect fd=%s", fd);
        try (LocalSocket localSocket = new LocalSocket()) {
            FileDescriptor f = new FileDescriptor();
            setInt$.invoke(f, fd);
            localSocket.connect(new LocalSocketAddress(socketPath, LocalSocketAddress.Namespace.FILESYSTEM));
            localSocket.setFileDescriptorsForSend(new FileDescriptor[]{f});
            localSocket.getOutputStream().write(1);
            localSocket.close();
        } catch (Exception e) {
            MyLog.e(e);
        }
    }


    /**
     * @param fd
     * @return
     */
    public static int getFd(FileDescriptor fd) {
        try {
            return (Integer) getInt$.invoke(fd);
        } catch (IllegalAccessException | InvocationTargetException e) {
            MyLog.e(e);
        }
        return -1;
    }

    public static int getFd(Socket socket) {
        try {
            FileDescriptor fd = (FileDescriptor) getFileDescriptor$.invoke(socket);
            return getFd(fd);
        } catch (IllegalAccessException | InvocationTargetException e) {
            MyLog.e(e);
        }
        return -1;

    }


    public VpnProfile getProfile() {
        Proxy proxy = app.getSessionManager().newProxy();
        if (proxy == null||proxy.data==null) {
            return null;
        }

        VpnProfile profile = new VpnProfile();
        profile.applicationDir = app.getContext().getApplicationInfo().dataDir;

        XBean data = proxy.data;
        if(proxy.isCustom){
            profile.id=1;
            profile.name = app.getCustomProxyManager().buildName(data);
            profile.host = data.getString("host");
            profile.port = data.getInteger("port",10400);
            profile.password=data.getString("password");
            profile.authscheme=data.getString("method");
            profile.ssrObfs = data.getString("obfs","");
            profile.ssrObfsParam = data.getString("obfs_param","");
            profile.ssrProtocol = data.getString("protocol","");
            profile.ssrProtocolParam = data.getString("protocol_param","");
            MyLog.d("proxy=%s",data);
            MyLog.d("port=%s",profile.port);
        }else{
            //authscheme=rc4-md5, password=P0Edpy, ssrProtocol=origin, port=28041, price=0, ssrObfs=plain, host=45.32.75.241, name=美国优化线路2[0.25金币/G], user=18011353062
            profile.id = 1;
            profile.name = data.getString("name");
            profile.host = data.getString("host");
            profile.port = data.getInteger("port",10400);
            profile.password = data.getString("password");
            profile.authscheme = data.getString("authscheme");
            profile.ssrProtocol = data.getString("ssrProtocol");
            profile.ssrObfs = data.getString("ssrObfs");
            //profile.ssrObfsParam="";
        }

        //每次都是使用一个最新的
        XBean cfg = app.getSettings().newData();
        String aclStr = cfg.getString("acl", null);
        MyLog.d("--------------》》》》》》 aclStr=%s",aclStr);
        List<String> acl = parseAcl(aclStr);
        MyLog.d("--------------》》》》》》 acl-size=%s",acl != null ? String.valueOf(acl.size()) : "empty");
        profile.acl = acl;
        profile.bypass_china = cfg.getBoolean("bypass_china", VpnConfig.BYPASS_CHINA_DEFAULT);

        return profile;
    }

    private List<String> parseAcl(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        List<String> rules = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(text));) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    //ignore comment
                } else {
                    rules.add(line);
                }
            }
        } catch (IOException e) {
            MyLog.e(e);
        }
        return rules;
    }

    public void setLastAction(String action) {
        String text = Json.stringify(new XBean("last_action", action));
        try {
            IOUtil.save(text, app.getDataFile("vpn.json"));
        } catch (IOException e) {
            MyLog.e(e);
        }
    }

    /**
     * 获得最后一次执行的action，start or stop
     *
     * @return
     */
    public String getLastAction() {
        XBean data = IOUtil.load(app.getDataFile("vpn.json"), XBean.class);
        if (data != null) {
            return data.getString("last_action", null);
        } else {
            return null;
        }
    }

}
