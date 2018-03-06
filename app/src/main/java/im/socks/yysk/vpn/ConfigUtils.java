package im.socks.yysk.vpn;

/**
 * Created by Administrator on 2017/9/21 0021.
 */

public class ConfigUtils {
    public String EscapedJson(String OriginString) {
        return OriginString.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
    }

    public static String SHADOWSOCKS = "{\"server\": \"%s\", \"server_port\": %d, \"local_port\": %d, \"password\": \"%s\", \"method\":\"%s\", \"timeout\": %d, \"protocol\": \"%s\", \"obfs\": \"%s\", \"obfs_param\": \"%s\", \"protocol_param\": \"%s\"}";
    public static String REMOTE_SERVER =
            "|server { | label = 'remote-servers'; | ip = %s; | port = %d; | timeout = 3; | query_method = udp_only; | %s | policy = included; | reject = %s; | reject_policy = fail; | reject_recursively = on; |}";
    public static String PDNSD_DIRECT =
            " |global { | perm_cache = 2048; | %s | cache_dir = '%s'; | server_ip = %s; | server_port = %d; | query_method = udp_only; | min_ttl = 15m; | max_ttl = 1w; | timeout = 10; | daemon = off; | par_queries = 4; |} | |%s | |server { | label = 'local-server'; | ip = 127.0.0.1; | query_method = tcp_only; | port = %d; | reject = %s; | reject_policy = negate; | reject_recursively = on; |} | |rr { | name=localhost; | reverse=on; | a=127.0.0.1; | owner=localhost; | soa=localhost,root.localhost,42,86400,900,86400,86400; |}";//.stripMargin
    public static String PDNSD_LOCAL =
            " |global { | perm_cache = 2048; | %s | cache_dir = '%s'; | server_ip = %s; | server_port = %d; | query_method = tcp_only; | min_ttl = 15m; | max_ttl = 1w; | timeout = 10; | daemon = off; |} | |server { | label = 'local'; | ip = 127.0.0.1; | port = %d; | reject = %s; | reject_policy = negate; | reject_recursively = on; |} | |rr { | name=localhost; | reverse=on; | a=127.0.0.1; | owner=localhost; | soa=localhost,root.localhost,42,86400,900,86400,86400; |}";//.stripMargin
}
