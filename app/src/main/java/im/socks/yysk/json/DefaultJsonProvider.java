package im.socks.yysk.json;


import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.List;
import java.util.Map;

import im.socks.yysk.util.Json;
import im.socks.yysk.util.Json.IJsonable;


public class DefaultJsonProvider implements Json.IJsonProvider {
    public Object read(Reader reader, Json.Supplier<List> listFactory, Json.Supplier<Map> mapFactory) throws Exception {
        return new JsonParser(reader, listFactory, mapFactory).parse();
    }

    public void write(Object obj, Writer writer) throws IOException {
        JsonWriter jsonWriter = new JsonWriter(writer);
        write(obj, jsonWriter);

    }

    private static void write(Object obj, JsonWriter writer) throws IOException {
        if (obj == null) {
            writer.writeLiteral("null");
        } else if (obj instanceof String) {
            writer.writeString((String) obj);
        } else if (obj instanceof Boolean) {
            if (Boolean.TRUE.equals(obj)) {
                writer.writeLiteral("true");
            } else {
                writer.writeLiteral("false");
            }
        } else if (obj instanceof Number) {
            // int,long,float,double
            if (obj instanceof Float) {
                Float f = (Float) obj;
                //jdk6
                if (/*Float.isFinite(f)*/ !f.isInfinite() && !f.isNaN()) {
                    writer.writeNumber(obj.toString());
                } else {
                    // NaN or Infinite
                    writer.writeLiteral("null");
                }

            } else if (obj instanceof Double) {
                Double f = (Double) obj;
                if (/*Double.isFinite(f)*/!f.isInfinite() && !f.isNaN()) {
                    writer.writeNumber(obj.toString());
                } else {
                    // NaN or Infinite
                    writer.writeLiteral("null");
                }
            } else {
                writer.writeNumber(obj.toString());
            }

        } else if (obj instanceof Date) {
            writer.writeNumber(Long.toString(((Date) obj).getTime()));
        } else if (obj instanceof Map) {
            Map<?, ?> map = (Map) obj;
            writer.writeObjectOpen();
            boolean first = true;
            for (Map.Entry entry : map.entrySet()) {
                if (!first) {
                    writer.writeObjectSeparator();
                }
                writer.writeMemberName((String) entry.getKey());
                writer.writeMemberSeparator();
                write(entry.getValue(), writer);

                first = false;
            }

            writer.writeObjectClose();
        } else if (obj instanceof IJsonable) {
            write(((IJsonable) obj).toJson(), writer);
        } else if (obj instanceof Iterable) {
            writer.writeArrayOpen();
            boolean first = true;
            for (Object value : ((Iterable) obj)) {
                if (!first) {
                    writer.writeArraySeparator();
                }
                write(value, writer);
                first = false;
            }
            writer.writeArrayClose();
        } else if (obj.getClass().isArray()) {
            writer.writeArrayOpen();
            boolean first = true;
            for (int i = 0, len = Array.getLength(obj); i < len; i++) {
                if (!first) {
                    writer.writeArraySeparator();
                }
                write(Array.get(obj, i), writer);
                first = false;
            }
            writer.writeArrayClose();
        } else {
            writer.writeString(obj.toString());
        }

    }

}
