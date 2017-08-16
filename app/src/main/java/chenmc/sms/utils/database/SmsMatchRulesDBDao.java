package chenmc.sms.utils.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import chenmc.sms.data.SmsMatchRuleBean;

/**
 * Created by 明明 on 2017/6/30.
 */

public class SmsMatchRulesDBDao {
    
    private final DatabaseHelper mDatabaseHelper;
    
    public SmsMatchRulesDBDao(Context context) {
        mDatabaseHelper = new DatabaseHelper(context);
    }
    
    public void close() {
        mDatabaseHelper.close();
    }
    
    public void insert(SmsMatchRuleBean... beans) {
        String sql = "insert into " + CodeMatchRuleTable.TABLE_NAME + "(" +
            CodeMatchRuleTable.Columns.SMS + "," +
            CodeMatchRuleTable.Columns.CODE + "," +
            CodeMatchRuleTable.Columns.RULE + ") " +
            " values (?, ?, ?)";
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        for (SmsMatchRuleBean bean : beans) {
            db.execSQL(sql,
                new Object[]{
                    bean.getSms(),
                    bean.getVerificationCode(),
                    bean.getRegExp()
                }
            );
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
    
    public void update(SmsMatchRuleBean... beans) {
        String sql = "update " + CodeMatchRuleTable.TABLE_NAME + " set " +
            CodeMatchRuleTable.Columns.SMS + " = ?," +
            CodeMatchRuleTable.Columns.CODE + " = ?," +
            CodeMatchRuleTable.Columns.RULE + " = ? " +
            " where " +
            CodeMatchRuleTable.Columns._ID + " = ?";
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        for (SmsMatchRuleBean bean : beans) {
            db.execSQL(sql,
                new Object[]{
                    bean.getSms(),
                    bean.getVerificationCode(),
                    bean.getRegExp(),
                    bean.getId()
                }
            );
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
    
    public void delete(SmsMatchRuleBean... beans) {
        String sql = "delete from " + CodeMatchRuleTable.TABLE_NAME +
            " where " +
            CodeMatchRuleTable.Columns._ID + " = ?";
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.beginTransaction();
        for (SmsMatchRuleBean bean : beans) {
            db.execSQL(sql,
                new Object[]{
                    bean.getId()
                }
            );
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }
    
    public List<SmsMatchRuleBean> selectAll() {
        String sql = "select * from " + CodeMatchRuleTable.TABLE_NAME;
        ArrayList<SmsMatchRuleBean> list = new ArrayList<>();
        Cursor cursor = mDatabaseHelper.getReadableDatabase().rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            do {
                SmsMatchRuleBean bean = new SmsMatchRuleBean();
                bean.setId(cursor.getInt(cursor.getColumnIndex(CodeMatchRuleTable.Columns._ID)));
                bean.setSms(cursor.getString(cursor.getColumnIndex(CodeMatchRuleTable.Columns.SMS)));
                bean.setVerificationCode(cursor.getString(cursor.getColumnIndex(CodeMatchRuleTable.Columns.CODE)));
                bean.setRegExp(cursor.getString(cursor.getColumnIndex(CodeMatchRuleTable.Columns.RULE)));
                list.add(bean);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}
