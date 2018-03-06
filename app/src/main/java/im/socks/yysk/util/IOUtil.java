package im.socks.yysk.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import im.socks.yysk.MyLog;

/**
 * Created by cole on 2017/10/15.
 */

public class IOUtil {

    public static byte[] readData(InputStream input) throws IOException {
        try {
            byte[] buf = new byte[8 * 1024];
            byte[] data = new byte[0];
            int len = 0;
            while ((len = input.read(buf)) != -1) {
                int oldLength = data.length;
                data = Arrays.copyOf(data, data.length + len);
                System.arraycopy(buf, 0, data, oldLength, len);
            }
            return data;
        } finally {
            closeQuietly(input);
        }

    }

    public static String readText(InputStream input, String charset) throws IOException {
        return new String(readData(input), charset);
    }

    public static String readText(Reader reader) throws IOException {
        try {
            char[] buf = new char[8 * 1024];
            char[] data = new char[0];
            int len = 0;
            while ((len = reader.read(buf)) != -1) {
                int oldLength = data.length;
                data = Arrays.copyOf(data, data.length + len);
                System.arraycopy(buf, 0, data, oldLength, len);
            }
            return new String(data);
        } finally {
            closeQuietly(reader);
        }

    }

    /**
     * 保存文本到文件
     *
     * @param text
     * @param path
     * @throws IOException
     */
    public static void save(String text, String path) throws IOException {
        save(text, new FileOutputStream(path));

    }

    public static void save(String text, OutputStream out) throws IOException {
        try {
            out.write(text.getBytes("utf-8"));
        } finally {
            closeQuietly(out);
        }
    }

    public static void save(String text, File file) throws IOException {
        save(text, new FileOutputStream(file));
    }

    public static <T> T load(File file, Class<T> type) {
        XBean data = new XBean();
        if (file.isFile()) {
            try (FileInputStream input = new FileInputStream(file)) {
                String text = IOUtil.readText(input, "utf-8");
                MyLog.d("file=%s,data=%s", file.getName(), text);
                return Json.parse(text, type);
            } catch (IOException e) {
                MyLog.e(e);
            }
        } else {

        }

        return null;

    }

    public static void copy(InputStream input, OutputStream out) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        int byteCount = 0;
        try {
            while ((byteCount = input.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                out.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
        } finally {
            closeQuietly(input, out);
        }

    }

    public static void closeQuietly(AutoCloseable... objs) {
        for (AutoCloseable obj : objs) {
            try {
                if (obj != null) {
                    obj.close();
                }

            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

    //

    /**
     * 获得输入流
     *
     * @param input InputStream，File，Path，String（路径），byte[]
     */
    public static InputStream getInputStream(Object input) throws IOException {
        if (input instanceof InputStream) {
            return (InputStream) input;
        } else if (input instanceof String) {
            // URL url = new URL((String) input);
            // return url.openStream();
            //return Files.newInputStream(Paths.get((String) input));
            return new FileInputStream((String) input);

        } else if (input instanceof URL) {
            return ((URL) input).openStream();
        } else if (input instanceof File) {
            return new FileInputStream((File) input);
        } else if (input instanceof byte[]) {
            return new ByteArrayInputStream((byte[]) input);
        } else {
            throw parameterException(input);
        }

    }

    /**
     * 获得一个输出流对象
     *
     * @param output OutputStream，File，URL，Path，String（路径）
     * @throws IOException
     */
    public static OutputStream getOutputStream(Object output) throws IOException {
        if (output instanceof OutputStream) {
            return (OutputStream) output;
        } else if (output instanceof File) {
            return new FileOutputStream((File) output);
        } else if (output instanceof URL) {
            URLConnection connection = ((URL) output).openConnection();
            connection.setDoOutput(true);
            return connection.getOutputStream();
        } else if (output instanceof String) {
            return new FileOutputStream((String) output);
        } else {
            throw parameterException(output);
        }
    }

    public static Reader getReader(Object input, String charset) throws IOException {
        if (input instanceof Reader) {
            return (Reader) input;
        }
        return new InputStreamReader(getInputStream(input), charset);
    }

    /**
     * 获得一个Writer
     *
     * @param output  路径（String，Path，URL，File），OutputStream，Writer（忽略charset参数）
     * @param charset output为Writer忽略这个值，其它的必须设置
     * @throws IOException
     */
    public static Writer getWriter(Object output, String charset) throws IOException {
        if (output instanceof Writer) {
            return ((Writer) output);
        }
        return new OutputStreamWriter(getOutputStream(output), charset);
    }

    private static IllegalArgumentException parameterException(Object obj) {
        if (obj == null) {
            return new IllegalArgumentException("参数不能够为null");
        } else {
            return new IllegalArgumentException("不支持的对象类型：" + obj.getClass().getName());
        }
    }
}
