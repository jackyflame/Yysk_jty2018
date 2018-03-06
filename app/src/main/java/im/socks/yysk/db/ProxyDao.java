package im.socks.yysk.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/24.
 */

public class ProxyDao {
    private SQLiteOpenHelper helper;

    public ProxyDao(SQLiteOpenHelper helper) {
        this.helper = helper;
    }

    public XBean getProxyById(String id) {
        return null;
    }

    public void add(XBean proxy) {
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("", proxy.getDouble(""));

            db.insert("t_proxy", null, new ContentValues());
        }
    }

    public void remove(String id) {
        try (SQLiteDatabase db = helper.getWritableDatabase()) {
            db.delete("t_proxy", "id=?", new String[]{id});
        }
    }
}
