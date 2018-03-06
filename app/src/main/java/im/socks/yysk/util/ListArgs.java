package im.socks.yysk.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListArgs {
    /**
     * 支持使用list，[]作为多个参数，如：
     * <p>
     * <pre>
     * wrap(1) => [1]
     * wrap(1,2,3)=>[1,2,3]
     * wrap(list) => list
     *
     * Object a=[1,2,3]
     * wrap(a) => [1,2,3]
     *
     * 如果元素的类型是数组，必须这么传递
     * list.add(array1)
     * wrap(list)
     *
     * 也就是只要元素的类型不要求是List和[]，都可以使用ListArgs，避免定义2个方法，如：f1(Object ...args), f1(List list)
     * </pre>
     *
     * @param objs
     * @return
     */
    public static <T> List<T> wrap(Object... objs) {
        // wrap() => [] => empty list
        if (objs == null || objs.length == 0) {
            return Collections.emptyList();
        } else if (objs.length == 1) {
            Object obj = objs[0];
            if (obj == null) {
                // Object obj=null;wrap(obj); => [null] => empty list
                return Collections.emptyList();
            } else if (obj instanceof List) {
                // wrap(list); => [list] => list
                // 如果需要安全的，可以wrap
                return Collections.unmodifiableList((List<T>) obj);

            } else if (obj instanceof Collection<?>) {
                return (List<T>) Arrays.asList(((Collection) obj).toArray());
            } else if (obj.getClass().isArray()) {
                // Object obj=[1,2,3]
                // wrap(obj) => [[1,2,3]] => [1,2,3]
                List<T> list = new ArrayList<>();
                for (int i = 0; i < Array.getLength(obj); i++) {
                    list.add((T) Array.get(obj, i));
                }
                return list;
            } else {
                // wrap(1); => [1] => [1]
                return Arrays.asList((T) obj);
            }
        } else {
            // wrap(1,2,3) => [1,2,3]
            // wrap([1,2,3])=>[1,2,3]
            return (List<T>) Arrays.asList(objs);
        }
    }

}
