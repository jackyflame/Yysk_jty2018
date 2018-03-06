package im.socks.yysk.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Looper;

import java.util.List;

import im.socks.yysk.util.XBean;

/**
 * Created by cole on 2017/10/24.
 */

public class UserDao {
    private SQLiteOpenHelper helper = null;

    public UserDao(SQLiteOpenHelper helper) {
        this.helper = helper;
    }

    public List<XBean> getUsers() {
        checkThread();
        //使用完毕后就可以关闭了，数据库操作应该都仅仅在主线程执行
        try (SQLiteDatabase db = helper.getReadableDatabase();) {
            String sql = "select * from users";
            try (Cursor cursor = db.rawQuery(sql, null);) {
                List<XBean> users = Dao.fetchAll(cursor);
            }
        } finally {

        }
        return null;

    }

    private void checkThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("必须在主线程调用");
        }
    }
}
