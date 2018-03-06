package im.socks.yysk.util;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.socks.yysk.json.DefaultJsonProvider;


public class Json {

    private static Supplier<List> LIST_FACTORY = new Supplier<List>() {
        @Override
        public List get() {
            return new ArrayList();
        }
    };
    private static Supplier<Map> MAP_FACTORY = new Supplier<Map>() {
        @Override
        public Map get() {
            return new XBean();
        }
    };
    private static IJsonProvider provider = new DefaultJsonProvider();

    /**
     * 如果对默认json不满意，可以设置一个替代
     *
     * @param provider
     */
    public static void setJsonProvider(IJsonProvider provider) {
        Json.provider = provider;
    }

    public static String stringify(Object obj) {
        StringWriter writer = null;
        try {
            writer = new StringWriter();
            provider.write(obj, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("", e);
        } finally {
            IOUtil.closeQuietly(writer);
        }
    }

    /**
     * 使用ArrayList和XBean对象
     *
     * @param s
     * @return 返回{} => Map,[] => List,"" => String, true,false,123,1.2 => String
     * 等类型
     */
    public static Object parse(String s) {
        return parse(s, LIST_FACTORY, MAP_FACTORY);
    }

    /**
     * 使用json解码
     *
     * @param <T>  对象的类型
     * @param s    需要解码的字符串
     * @param type 解码后的对象的类型
     * @return 如果解码后的类型满足type的要求，返回解码后的对象，如果不满足，返回null
     */
    public static <T> T parse(String s, Class<T> type) {
        Object obj = parse(s, LIST_FACTORY, MAP_FACTORY);
        return convertObject(obj, type);
    }

    /**
     * 仅仅支持："null","" => null<br>
     * {} => Map(mapFactory指定）<br>
     * [] => List(listFactory指定)<br>
     * "abc",true,1,2.0 => 返回字符串
     *
     * @param s
     * @param listFactory
     * @param mapFactory
     * @return 如果为"null"，返回null，如果是"{}"返回Map（mapFactory决定），如果是
     * "[]"，返回List（listFactory）决定，其他的返回字符串 @throws
     */
    public static Object parse(String s, Supplier<List> listFactory, Supplier<Map> mapFactory) {
        StringReader reader = null;
        try {
            reader = new StringReader(s);
            return provider.read(reader, listFactory, mapFactory);
        } catch (Exception e) {

            return null;
        } finally {
            IOUtil.closeQuietly(reader);
        }
    }

    public static <T> T read(Object input, String charset, Class<T> type) throws IOException {
        Object obj = read(input, charset, LIST_FACTORY, MAP_FACTORY);
        return convertObject(obj, type);
    }

    public static Object read(Object input, String charset, Supplier<List> listFactory, Supplier<Map> mapFactory)
            throws IOException {
        Reader reader = null;
        try {
            reader = IOUtil.getReader(input, charset);
            return provider.read(reader, listFactory, mapFactory);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw ((IOException) e);
            } else {
                return null;
            }
        } finally {
            IOUtil.closeQuietly(reader);
        }
    }

    private static <T> T convertObject(Object obj, Class<T> type) {
        if (obj == null) {
            return null;
        }
        if (type == null) {
            return (T) obj;
        }
        if (IJsonable.class.isAssignableFrom(type)) {
            XBean bean = X.to(obj, XBean.class, null);
            if (bean != null) {
                IJsonable json;
                try {
                    json = (IJsonable) type.newInstance();
                    if (json.fill(bean)) {
                        return (T) json;
                    }
                    return null;
                } catch (InstantiationException e) {
                    throw new RuntimeException("", e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("不能够访问类", e);
                }

            } else {
                return null;
            }
        } else {
            return X.to(obj, type);

        }
    }

    /**
     * 输出json
     *
     * @param obj     需要输出的json对象
     * @param out     可以是OutputStream，Writer，File,Path,URL，String（文件路径），会关闭流对象
     * @param charset 表示使用的编码，如果是Writer，忽略，默认为utf-8
     * @return
     * @throws IOException
     */
    public static void write(Object obj, Object out, String charset) throws IOException {
        if (charset == null || charset.length() == 0) {
            charset = "utf-8";
        }
        Writer writer = null;
        try {
            writer = IOUtil.getWriter(out, charset);
            provider.write(obj, writer);
        } finally {
            IOUtil.closeQuietly(writer);
        }
    }

    /**
     * 表示一个类支持返回json的解码和编码
     *
     * @author cole
     */
    public static interface IJsonable<T> {
        /**
         * 返回一个支持json的对象，如：Map，List，或者primitive类型等
         *
         * @return
         */
        T toJson();

        /**
         * 在解码的时候，使用XBean对象填充
         *
         * @param bean
         * @return 返回false表示填充失败，可能是数据不正确
         */
        boolean fill(XBean bean);
    }

    public interface IJsonProvider {
        // String stringify(Object obj);
        // Object parse(String s,Supplier<List> listFactory,Supplier<Map>
        // mapFactory)throws Exception;
        Object read(Reader reader, Supplier<List> listFactory, Supplier<Map> mapFactory) throws Exception;

        void write(Object obj, Writer writer) throws IOException;
    }

    public interface Supplier<T> {
        T get();
    }

}
