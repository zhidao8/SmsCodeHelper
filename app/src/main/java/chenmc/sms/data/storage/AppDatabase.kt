package chenmc.sms.data.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * @author Carter
 * Created on 2018-07-17
 */
class AppDatabase(context: Context)
    : SQLiteOpenHelper(context, AppDBContract.DATABASE, null, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        db ?: return
        db.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db ?: return
        when (newVersion) {
            2 -> upgrade1to2(db)
        }
    }

    private fun upgrade1to2(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE)
        db.execSQL("INSERT INTO sms_code_regex(`sms`, `code`, `regex`) SELECT `sms`, `code`, `rule` FROM SmsMatchRules")
    }

    private companion object {
        private const val SQL_CREATE_TABLE: String = "CREATE TABLE IF NOT EXISTS `sms_code_regex` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sms` TEXT, `code` TEXT, `regex` TEXT)"
    }
}