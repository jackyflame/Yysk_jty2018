package im.socks.yysk.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import im.socks.yysk.util.Json;
import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/24.
 */

public class Dao {
    private Dao() {

    }

    public static List<XBean> fetchAll(Cursor cursor) {
        List<XBean> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            items.add(fetchOne(cursor));
        }
        return items;
    }

    public static XBean fetchOne(Cursor cursor) {
        XBean item = new XBean();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
            int type = cursor.getType(i);
            Object value = null;
            if (type == Cursor.FIELD_TYPE_NULL) {
                value = null;
            } else if (type == Cursor.FIELD_TYPE_INTEGER) {
                value = cursor.getLong(i);//总是转换为long
            } else if (type == Cursor.FIELD_TYPE_FLOAT) {
                value = cursor.getDouble(i);
            } else if (type == Cursor.FIELD_TYPE_STRING) {
                value = cursor.getString(i);
            } else if (type == Cursor.FIELD_TYPE_BLOB) {
                value = cursor.getBlob(i);
            } else {
                //不支持的类型？
            }
            item.put(cursor.getColumnName(i), value);
        }
        return item;
    }

    public static ContentValues toContentValues(Map map) {
        ContentValues values = new ContentValues();
        for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                values.putNull(key);
            } else if (value instanceof Integer) {
                values.put(key, (Integer) value);
            } else if (value instanceof Long) {
                values.put(key, (Long) value);
            } else if (value instanceof Float) {
                values.put(key, (Float) value);
            } else if (value instanceof Double) {
                values.put(key, (Double) value);
            } else if (value instanceof Boolean) {
                values.put(key, (Boolean) value);
            } else if (value instanceof String) {
                values.put(key, (String) value);
            } else if (value instanceof Date) {
                //存储时间??
                values.put(key, formatDate((Date) value));
            } else {
                //json object??
                values.put(key, Json.stringify(value));
            }
        }
        return values;
    }

    public static String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return format.format(date);
    }

}
