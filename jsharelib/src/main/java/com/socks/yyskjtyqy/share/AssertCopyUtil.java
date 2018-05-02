package com.socks.yyskjtyqy.share;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssertCopyUtil {

    public static File copyResurces(Context context, String src){
        return copyResurces(context,src,src);
    }

    public static File copyResurces(Context context, String src, String dest){
        File filesDir = null;
        int flag = 0;
        try {
            if(flag == 0) {//copy to sdcard
                filesDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/yysk/" + dest);
                File parentDir = filesDir.getParentFile();
                if(!parentDir.exists()){
                    parentDir.mkdirs();
                }
            }else{//copy to data
                filesDir = new File(context.getFilesDir(), dest);
            }
            if(!filesDir.exists()) {
                filesDir.createNewFile();
                InputStream open = context.getAssets().open(src);
                FileOutputStream fileOutputStream = new FileOutputStream(filesDir);
                byte[] buffer = new byte[4 * 1024];
                int len = 0;
                while ((len = open.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                }
                open.close();
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filesDir;
    }

    public static String getAbsoluteFilePath(String name){
        return Environment.getExternalStorageDirectory().getAbsoluteFile() + "/yysk/" + name;
    }
}
