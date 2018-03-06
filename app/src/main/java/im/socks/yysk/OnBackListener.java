package im.socks.yysk;

/**
 * Created by cole on 2017/11/2.
 */

public interface OnBackListener {
    /**
     * @return 返回true表示已经处理，不要执行默认的操作
     */
    boolean onBack();
}
