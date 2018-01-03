package chenmc.sms.utils.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 明明 on 2017/6/30.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    
    public static final String DATABASE_NAME = "MainDatabase.db";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "create table " + CodeMatchRuleTable.TABLE_NAME + "(" +
                CodeMatchRuleTable.Columns._ID + " integer primary key autoincrement," +
                CodeMatchRuleTable.Columns.SMS + "," +
                CodeMatchRuleTable.Columns.CODE + "," +
                CodeMatchRuleTable.Columns.RULE +
                ")"
        );
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
}
