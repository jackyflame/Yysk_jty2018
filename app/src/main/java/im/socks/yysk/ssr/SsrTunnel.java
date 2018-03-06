package im.socks.yysk.ssr;

import android.content.Context;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import com.github.shadowsocks.System;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import im.socks.yysk.MyLog;
import im.socks.yysk.Yysk;
import im.socks.yysk.data.IPValidator;
import im.socks.yysk.util.IOUtil;
import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;
import im.socks.yysk.vpn.ITunnel;
import im.socks.yysk.vpn.ITunnelListener;
import im.socks.yysk.vpn.VpnProfile;


public class SsrTunnel implements ITunnel {
    private static final int VPN_MTU = 1500;
    private static final String PRIVATE_VLAN = "26.26.26.%s";
    private static final String PRIVATE_VLAN6 = "fdfe:dcba:9876::%s";

    /**
     * true表示已经复制过了，false表示没有，应用启动只需要复制一次，如果应用升级了，会重启应用，自动复制一次。
     * 当然，也可以做得更合理一下，检查md5是否一致，如果一致的就不需要复制了。
     */
    private static boolean isAssetUpdated = false;

    private IProcess mTun2socksProcess = null;
    private IProcess mSstunnelProcess = null;
    private IProcess mSslocalProcess = null;
    private IProcess mPdnsdProcess = null;

    private YyskProtectServer protectServer;
    private YyskTrafficMonitor trafficMonitor;

    private ParcelFileDescriptor vpnConn;

    private int status = Yysk.STATUS_CONNECTING;
    private ITunnelListener listener;

    private VpnProfile profile;
    private VpnService vpnService;
    private VpnService.Builder builder;


    public Context context = null;

    //private App app;

    private long startTime;

    public SsrTunnel(VpnProfile profile, VpnService vpnService, VpnService.Builder builder, ITunnelListener listener) {
        //this.app = app;
        this.profile = profile;
        this.vpnService = vpnService;
        this.builder = builder;
        this.listener = listener;
        this.status = Yysk.STATUS_INIT;
        this.context = vpnService.getApplicationContext();

    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public synchronized boolean start() {
        //启动，可以在独立的线程执行，而不一定需要在main thread
        if (status != Yysk.STATUS_INIT) {
            return false;
        }


        setStatus(Yysk.STATUS_CONNECTING);
        boolean ok = doStart();
        if (ok) {
            startTime = java.lang.System.currentTimeMillis();
            setStatus(Yysk.STATUS_CONNECTED);
        } else {
            stop();
        }
        return ok;
    }

    private boolean doStart() {
        if (!setupVpn(profile, builder)) {
            return false;
        }

        updateAsset(profile, context);

        //1.启动protect server，接收fd，然后保护其不通过vpn
        protectServer = new YyskProtectServer(profile.applicationDir + "/protect_path", vpnService);
        if (!protectServer.start()) {
            MyLog.e("protect server启动失败");
            return false;
        }

        //2.启动stat server，获得ss-local的tx,rx统计通知
        trafficMonitor = new YyskTrafficMonitor(profile.applicationDir + "/stat_path", listener);
        if (!trafficMonitor.start()) {
            MyLog.e("traffic monitor启动失败");
            return false;
        }


        //启动ss-local，如果upddns=true，会同时支持tcp+udp
        //如果udpdns=false，不需要支持udp，因为tun2socks也没有开启
        if (!startSsrDaemon(profile)) {
            return false;
        }
        //如果upddns=false，表示强制使用pdnsd
        if (!profile.udpdns) {
            if (!startDnsTunnel(profile)) {
                return false;
            }
            if (!startDnsDaemon(profile)) {
                return false;
            }

        }

        //3.启动tun2sock，把vpn interface的流量定向到ss-local, dns udp到pdns（如果pf.udpdns=false)
        if (!startTun2Socks(profile)) {
            return false;
        }
        return true;
    }


    public synchronized void stop() {
        if (status == Yysk.STATUS_STOPPED || status == Yysk.STATUS_STOPPING) {
            return;
        }
        setStatus(Yysk.STATUS_STOPPING);
        try {
            doStop();
        } finally {
            setStatus(Yysk.STATUS_STOPPED);
        }

    }

    private synchronized void doStop() {
        if (vpnConn != null) {
            IOUtil.closeQuietly(vpnConn);
            vpnConn = null;
        }
        if (mTun2socksProcess != null) {
            mTun2socksProcess.stop();
            mTun2socksProcess = null;
        }
        if (mSslocalProcess != null) {
            mSslocalProcess.stop();
            mSslocalProcess = null;
        }

        if (mPdnsdProcess != null) {
            mPdnsdProcess.stop();
            mPdnsdProcess = null;
        }
        if (mSstunnelProcess != null) {
            mSstunnelProcess.stop();
            mSstunnelProcess = null;
        }

        //关闭这2个非常耗时间
        if (protectServer != null) {
            protectServer.stop();
            protectServer = null;
        }
        if (trafficMonitor != null) {
            trafficMonitor.stop();
            trafficMonitor = null;
        }
    }

    private void setStatus(int status) {
        if (this.status != status) {
            this.status = status;
            if (listener != null) {
                listener.onStatusChanged(status);
            }
        }

    }

    public synchronized int getStatus() {
        return status;
    }


    private boolean setupVpn(VpnProfile pf, VpnService.Builder builder) {
        // 构建vpn
        builder.setSession(pf.name)
                .setMtu(VPN_MTU)
                .addAddress(String.format(Locale.ENGLISH, PRIVATE_VLAN, "1"), 24)
                .addDnsServer(pf.dns.split(":")[0]);
        if (pf.ipv6) {
            builder.addAddress(String.format(Locale.ENGLISH, PRIVATE_VLAN6, "1"), 120);
        }

        //true表示绕过哪些app，也就不允许指定的app通过
        //false表示仅仅允许指定的app

        //builder.addAllowedApplication("");
        //builder.addDisallowedApplication("");

        if (pf.route == "all") {

            builder.addRoute("0.0.0.0", 0);//全部都通过
            if (pf.ipv6) {
                builder.addRoute("::", 0);//
            }

        } else {
            //可以限制仅仅哪些
            builder.addRoute("8.8.0.0", 16);//这个是为了支持google dns，如：8.8.8.8
        }

        //builder.addRoute("8.8.0.0", 16);//这个是为了支持google dns，如：8.8.8.8


        vpnConn = builder.establish();
        if (vpnConn != null) {
            return true;
        } else {
            //如果没有prepare，会返回null
            //但是有些手机，即使已经prepare了，也会返回null
            //因为android系统存在的bug,当卸载app再安装后，vpn权限处理不当
            //要么重启手机
            //要么使用另外一个app，建立新的vpn连接，这样当前的app的vpn权限就会被回收
             //然后当前app再次申请vpn权限，获得新的权限后，就可以建立vpn连接了
            MyLog.e("VpnBuilder.establish=null");
            return false;
        }
    }

    private boolean startTun2Socks(VpnProfile pf) {
        List<String> cmd = new ArrayList<String>();
        cmd.add(pf.applicationDir + "/tun2socks");
        cmd.add("--netif-ipaddr");
        cmd.add(String.format(Locale.ENGLISH, PRIVATE_VLAN, "2"));
        cmd.add("--netif-netmask");
        cmd.add("255.255.255.0");
        cmd.add("--socks-server-addr");
        cmd.add("127.0.0.1:" + pf.localPort);
        cmd.add("--tunfd");
        cmd.add(vpnConn.getFd() + "");
        cmd.add("--tunmtu");
        cmd.add(VPN_MTU + "");
        cmd.add("--sock-path");//监听获得vpn的fd
        cmd.add(pf.applicationDir + "/sock_path");
        cmd.add("--loglevel");
        cmd.add("3");
        if (pf.ipv6) {
            cmd.add("--netif-ip6addr");
            cmd.add(String.format(Locale.ENGLISH, PRIVATE_VLAN6, "2"));
        }
        if (pf.udpdns) {
            //同时需要ss-local开启udp
            cmd.add("--enable-udprelay");
        } else {
            //表示所有的dns udp查询都强制使用指定的dns server，也就是稍后启动的pdnsd
            cmd.add("--dnsgw");
            cmd.add(String.format(Locale.ENGLISH, "%s:%d", String.format(Locale.ENGLISH, PRIVATE_VLAN, "1"), (pf.localPort + 53)));
        }
        mTun2socksProcess = new ProcessImpl(cmd);
        mTun2socksProcess.start();
        return sendFd(vpnConn.getFd(), pf.applicationDir + "/sock_path");
    }

    /**
     * 开启ss-local，udp_only，现在没有用到
     *
     * @param pf
     * @return
     */
    private boolean startSsrUdpDaemon(VpnProfile pf) {
        XBean cfg = new XBean();
        try {
            cfg.put("server", pf.host);
            cfg.put("server_port", pf.port);
            cfg.put("local_port", pf.localPort);
            cfg.put("password", pf.password);
            cfg.put("method", pf.authscheme);
            cfg.put("timeout", 600);
            putIfNotEmpty(cfg,"protocol",pf.ssrProtocol);
            //这个参数比价特别，即使为空字符串也不可以，有些自定义的服务器连接不上，特别是自定义的
            putIfNotEmpty(cfg,"protocol_param",pf.ssrProtocolParam);
            putIfNotEmpty(cfg,"obfs",pf.ssrObfs);
            putIfNotEmpty(cfg,"obfs_param",pf.ssrObfsParam);


            // 写入配置文件
            String confPath = pf.applicationDir + "/ss-local-udp-vpn.conf";
            IOUtil.save(Json.stringify(cfg), confPath);

            String aclPath = createAclFile(profile, "ss-local-udp-acl.conf");


            // 构建命令行
            List<String> cmd = new ArrayList<String>();
            cmd.add(pf.applicationDir + "/ss-local");
            cmd.add("-V");//表示为VPN
            cmd.add("-U");//udp_only
            cmd.add("-b");
            cmd.add("127.0.0.1");
            cmd.add("-t");
            cmd.add("600");

            //前缀，也就是{app_data_dir} => {app_data_dir}/protect_path
            cmd.add("-P");
            cmd.add(pf.applicationDir);
            cmd.add("-c");
            cmd.add(confPath);
            cmd.add("--acl");
            cmd.add(aclPath);
            mSslocalProcess = new ProcessImpl(cmd);
            mSslocalProcess.start();
            return true;
        } catch (Exception e) {
            MyLog.e(e);
            return false;
        }
    }

    private boolean startSsrDaemon(VpnProfile pf) {
        XBean cfg = new XBean();
        try {
            cfg.put("server", pf.host);
            cfg.put("server_port", pf.port);
            cfg.put("local_port", pf.localPort);
            cfg.put("password", pf.password);
            cfg.put("method", pf.authscheme);
            cfg.put("timeout", 600);

            putIfNotEmpty(cfg,"protocol",pf.ssrProtocol);
            //这个参数比价特别，即使为空字符串也不可以，有些自定义的服务器连接不上，特别是自定义的
            putIfNotEmpty(cfg,"protocol_param",pf.ssrProtocolParam);
            putIfNotEmpty(cfg,"obfs",pf.ssrObfs);
            putIfNotEmpty(cfg,"obfs_param",pf.ssrObfsParam);


            // 写入配置文件
            String confPath = pf.applicationDir + "/ss-local-vpn.conf";
            IOUtil.save(Json.stringify(cfg), confPath);

            //acl
            String aclPath = createAclFile(profile, "ss-local-acl.conf");

            // 构建命令行
            List<String> cmd = new ArrayList<String>();
            cmd.add(pf.applicationDir + "/ss-local");
            cmd.add("-V");//表示为VPN
            cmd.add("-b");
            cmd.add("127.0.0.1");
            cmd.add("-t");
            cmd.add("600");
            if (pf.udpdns) {
                cmd.add("-u");//tcp_udp
            }
            //前缀，也就是{app_data_dir} => {app_data_dir}/protect_path
            cmd.add("-P");
            cmd.add(pf.applicationDir);
            cmd.add("-c");
            cmd.add(confPath);

            //需要添加一个acl
            cmd.add("--acl");
            cmd.add(aclPath);
            mSslocalProcess = new ProcessImpl(cmd);
            mSslocalProcess.start();
            return true;
        } catch (Exception e) {
            MyLog.e(e);
            return false;
        }

    }

    private String createAclFile(VpnProfile pf, String name) throws IOException {
        String aclPath = profile.applicationDir + "/" + name;
        //现在仅仅排除局域网，将来可以更加复杂，排除国内的ip
        StringBuilder buf = new StringBuilder();


        if (pf.bypass_china) {
            //表示国内的直接连接，默认就是直接连接，不走代理(bypass)
            //buf.append("[bypass_all]\n\n");
            //buf.append("[proxy_list]\n");
            //添加代理规则，如：(^|\.)facebook.com
            //buf.append("(^|\\.)facebook\\.com").append("\n");

            //直接从模版添加
            String text = IOUtil.readText(context.getAssets().open("default.acl"), "utf-8");
            buf.append(text);
            List<String> aclList = pf.acl;
            if (aclList != null && aclList.size() > 0) {
                buf.append("\n");
                for (String rule : aclList) {
                    if (rule.indexOf('/') != -1) {
                        //192.168.0.0/22
                    } else if (IPValidator.getInstance().isValid(rule)) {
                        // 192.168.0.1
                    } else {
                        //a.com => (^|\.)a\.com$
                        //rule = Pattern.quote(rule); //如果使用这个\Qxxx\E，shadowsocks支持吗？
                        rule = rule.replace(".", "\\.");
                        rule = "(^|\\.)" + rule + "$";
                    }
                    buf.append(rule).append("\n");
                }
            }

        } else {
            buf.append("[proxy_all]\n\n");
            buf.append("[bypass_list]\n");
            buf.append("127.0.0.1\n");
            buf.append("::1\n");//ip6
            buf.append("10.0.0.0/8\n");
            buf.append("172.16.0.0/12\n");
            buf.append("192.168.0.0/16\n");
            buf.append("fc00::/7\n");
        }

        IOUtil.save(buf.toString(), aclPath);

        return aclPath;
    }

    private static void putIfNotEmpty(XBean cfg,String name,String value){
        if(value!=null&&value.length()>0){
            cfg.put(name,value);
        }else{
            //
        }
    }
    private boolean startDnsTunnel(VpnProfile pf) {
        XBean cfg = new XBean();
        try {
            cfg.put("server", pf.host);
            cfg.put("server_port", pf.port);
            cfg.put("local_port", pf.localPort + 63);
            cfg.put("password", pf.password);
            cfg.put("method", pf.authscheme);
            cfg.put("timeout", 10);
            putIfNotEmpty(cfg,"protocol",pf.ssrProtocol);
            //这个参数比较特别，即使为空字符串也不可以，有些自定义的服务器连接不上，特别是自定义的
            putIfNotEmpty(cfg,"protocol_param",pf.ssrProtocolParam);
            putIfNotEmpty(cfg,"obfs",pf.ssrObfs);
            putIfNotEmpty(cfg,"obfs_param",pf.ssrObfsParam);



            // 写入配置文件
            String confPath = pf.applicationDir + "/ss-tunnel-vpn.conf";
            IOUtil.save(Json.stringify(cfg), confPath);

            // 构建命令行
            List<String> cmd = new ArrayList<String>();
            cmd.add(pf.applicationDir + "/ss-tunnel");
            cmd.add("-V");//vpn model
            cmd.add("-t");
            cmd.add("10");
            cmd.add("-b");
            cmd.add("127.0.0.1");
            cmd.add("-L");
            cmd.add(pf.dns);
            cmd.add("-P");
            cmd.add(pf.applicationDir);
            cmd.add("-c");
            cmd.add(confPath);
            mSstunnelProcess = new ProcessImpl(cmd);
            mSstunnelProcess.start();
            return true;
        } catch (Exception e) {
            MyLog.e(e);
            return false;
        }

    }

    private boolean startDnsDaemon(VpnProfile pf) {
        String reject = "";
        if (pf.route.equals("all")) {
            reject += "global {\n";
            reject += " perm_cache = 2048;\n";
            reject += " cache_dir = \"" + pf.applicationDir + "\";\n";
            reject += " server_ip = 0.0.0.0;\n";
            reject += " server_port = " + (pf.localPort + 53) + ";\n";
            reject += " query_method = tcp_only;\n";
            reject += " run_ipv4 = on;\n";
            reject += " min_ttl = 15m;\n";
            reject += " max_ttl = 1w;\n";
            reject += " timeout = 10;\n";
            reject += " daemon = off;\n";
            reject += "}\n\n";
            reject += "server {\n";
            reject += " label = \"local\";\n";
            reject += " ip = 127.0.0.1;\n";
            reject += " port = " + (pf.localPort + 63) + ";\n";
            reject += pf.ipv6 ? " \n" : " reject = ::/0;\n";
            reject += " reject_policy = negate;\n";
            reject += " reject_recursively = on;\n";
            reject += " timeout = 5;\n";
            reject += "}\n\n";
            reject += "rr {\n";
            reject += " name=localhost;\n";
            reject += " reverse=on;\n";
            reject += " a=127.0.0.1;\n";
            reject += " owner=localhost;\n";
            reject += " soa=localhost,root.localhost,42,86400,900,86400,86400;\n";
            reject += "}\n";
        } else {
            //一部分直接使用国内dns，一部分使用国外dns（通过ss-tunnel）

        }
        String confPath = pf.applicationDir + "/pdnsd-vpn.conf";
        try {
            IOUtil.save(reject, confPath);
            List<String> cmd = new ArrayList<String>();
            cmd.add(pf.applicationDir + "/pdnsd");
            cmd.add("-c");
            cmd.add(confPath);
            mPdnsdProcess = new ProcessImpl(cmd);
            mPdnsdProcess.start();
            return true;
        } catch (Exception e) {
            MyLog.e(e);
        }
        return false;
    }

    private static void updateAsset(VpnProfile pf, Context context) {
        if (isAssetUpdated) {
            return;
        }
        // 遍历cpu类型拷贝执行文件
        String[] cpuInfo = null;
        if (Build.VERSION.SDK_INT >= 21) {
            cpuInfo = Build.SUPPORTED_ABIS;
            MyLog.d("abis=%s", Arrays.asList(cpuInfo));
        } else {
            //支持api<21
            MyLog.d("abis=%s,%s", Build.CPU_ABI, Build.CPU_ABI2);
            cpuInfo = new String[]{Build.CPU_ABI};
        }
        int nIndex = cpuInfo[0].toLowerCase().indexOf("x64");
        int nIndex2 = cpuInfo[0].toLowerCase().indexOf("x86");
        if (nIndex >= 0 || nIndex2 >= 0) {
            copyAssetFile2Data("x86", pf, context);
        } else {
            //支持armeabi，armeabi-v7a，armeabi-v8a
            copyAssetFile2Data("armeabi-v7a", pf, context);
        }
        isAssetUpdated = true;
    }

    private static void copyAssetFile2Data(String strDir, VpnProfile pf, Context context) {
        try {
            // 将strDir文件拷贝至pf.applicationdir
            for (String file : context.getAssets().list(strDir)) {
                String inputName = strDir + File.separator + file;
                String outputName = pf.applicationDir + File.separator + file;

                MyLog.d("copy %s to %s", inputName, outputName);

                InputStream input = context.getAssets().open(inputName);
                OutputStream out = new FileOutputStream(outputName);
                IOUtil.copy(input, out);

                String cmd = "chmod 777 " + outputName;
                Process p = Runtime.getRuntime().exec(cmd);
                int status = p.waitFor();
                if (status == 0) {
                    MyLog.d("exec cmd=%s 成功", cmd);
                } else {
                    MyLog.d("exec cmd=%s 失败", cmd);
                }
            }
        } catch (IOException | InterruptedException e) {
            MyLog.e(e);
        }
    }

    /**
     * 把获得的vpn的file descriptor发送给tun2socks，tun2socks就可以开始代理vpn流量了
     */
    private static boolean sendFd(int fd, String socketPath) {
        if (fd != -1) {
            int tries = 1;
            while (tries < 5) {
                //需要等待tun2socks启动成功
                try {
                    Thread.sleep(1000 * tries);
                } catch (Exception e) {
                }
                if (System.sendfd(fd, socketPath) != -1) {
                    MyLog.i("发送fd到tun2socks成功");
                    return true;
                }
                MyLog.e("第[%s]次发送fd到tun2socks失败", tries);
                tries += 1;
            }
        }

        return false;
    }
}
