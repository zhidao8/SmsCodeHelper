package chenmc.sms.transaction.handler;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import chenmc.sms.code.helper.R;

/**
 * 验证码和取件码短信清理
 * Created by 明明 on 2017/7/20.
 */

public class CodeSmsClearHandler {
    private static final String TAG = CodeSmsClearHandler.class.getName();
    
    /**
     * 获取系统中所有的验证码和取件码短信
     * @param context 上下文
     * @return 包含所有验证码和取件码短信的线性表
     */
    public static List<SmsHandler> getCodeSmsFromDatabase(Context context) {
        ArrayList<SmsHandler> list = new ArrayList<>();
        
        ContentResolver cr = context.getContentResolver();
        // 获取接收到的短信（type = 1）
        String where = "type = 1";
        Cursor cur = cr.query(Uri.parse("content://sms/"),
            new String[]{"_id", "body"}, where, null, "date desc");
        if (cur == null) return list;
    
        try {
            if (cur.moveToFirst()) {
                do {
                    SmsHandler smsHandler = new SmsHandler(
                        context,
                        cur.getString(cur.getColumnIndex("body")),
                        cur.getInt(cur.getColumnIndex("_id"))
                    );
                    if (smsHandler.analyse()) {
                        list.add(smsHandler);
                    }

                } while (cur.moveToNext());
            }
        } catch (SQLiteCantOpenDatabaseException ex) {
            Log.e(TAG, "getCodeSmsFromDatabase: ", ex);
            Toast.makeText(context, R.string.error_occurred_while_read_sms, Toast.LENGTH_SHORT).show();
        }
        cur.close();
        
        return list;
    }
    
    /**
     * 删除系统中的验证码和取件码短信
     * @param context 上下文
     * @param deleteList 将要删除的包含验证码和取件码短信的线性表
     * @return 删除成功的 List<SmsHandler>
     */
    public static List<SmsHandler> deleteCodeSmsFromDatabase(Context context, List<SmsHandler> deleteList) {
        List<SmsHandler> deleteSuccessList = new ArrayList<>(deleteList.size());
        
        ContentResolver contentResolver = context.getContentResolver();

        for (SmsHandler smsHandler : deleteList) {
            int count = contentResolver.delete(Uri.parse("content://sms/"), "_id = ?",
                new String[]{String.valueOf(smsHandler.getDatabaseId())});

            if (count > 0) {
                deleteSuccessList.add(smsHandler);
            }
        }
        
        return deleteSuccessList;
    }
}
