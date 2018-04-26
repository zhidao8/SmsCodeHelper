package chenmc.sms.data.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import android.content.Context

/**
 * @author Carter
 * Created on 2018-02-12
 */
class AppDatabaseWrapper(context: Context) {
    
    private val migration1to2 = object: Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `sms_code_regex` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sms` TEXT, `code` TEXT, `regex` TEXT)")
            database.execSQL("INSERT INTO sms_code_regex(`sms`, `code`, `regex`) SELECT `sms`, `code`, `rule` FROM SmsMatchRules")
        }
    }
    
    val database: AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java,
            AppDatabaseContract.DATABASE)
        .addMigrations(migration1to2)
        .allowMainThreadQueries()
        .build()
}