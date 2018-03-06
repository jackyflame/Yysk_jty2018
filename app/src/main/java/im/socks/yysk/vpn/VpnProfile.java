package im.socks.yysk.vpn;


import java.util.List;

public class VpnProfile {
    public int id = 0; // 配置项id

    public String authscheme = "rc4-md5"; // 加密算法
    /**
     * ss-server的ip
     */
    public String host = ""; // 服务器地址
    /**
     * ss-server的port
     */
    public int port = 10400; // 连接端口

    public String name = "";//"新加坡优化线路[0.3金币\\/G]"; // 名称

    public String password = ""; // 密码

    //public double price = 0; // 价格

    public String ssrObfs = "plain"; // 混淆插件

    public String ssrProtocol = "origin"; // 协议插件

    public String ssrObfsParam = "";

    public String ssrProtocolParam = "";


    //public String user = ""; // 用户名

    //public String type = "local"; // 本地或者服务器上下载

    /**
     * vpn的file descriptor
     */
    //public int fd = -1; // fd

    public String applicationDir = "";

    /**
     * socks5的端口
     */
    public int localPort = 1080;
    /**
     * true表示开启tun2socks5支持udp，自动支持dns upd查询，使用系统的dns+VpsService.Builder中配置的dns server，
     * 因为会使用系统的，可能存在dns污染
     * <p>
     * false表示强制转换dns的upd查询为pdnsd，必须开启pdnsd，总是使用pdnsd配置的上级dns，因为上级dns为海外的，所以，
     * 不会被污染
     * 这个选项在启动tun2socks使用
     * <p>
     * 如果设置为true，表示使用系统的dns设置，系统的dns server第一个基本都是为本地路由的，第二个可能为外部，
     * 所以，当使用第二个dns server的时候，就会通过代理，所以ss-server也需要支持udp
     */
    public boolean udpdns = false;


    public boolean ipv6 = false;

    //public String individual = "";

    //public int userOrder = 0;

    public String dns = "8.8.8.8:53";

    //public AssetManager am = null;

    public String route = "all";

    //public String china_dns ="114.114.114.114:53,223.5.5.5:53";

    /**
     * true表示国内的直接连接，不走代理，false表示全部都走代理
     */
    public boolean bypass_china = false;
    /**
     * 获得自定义的访问过滤列表
     */
    public List<String> acl = null;
}
