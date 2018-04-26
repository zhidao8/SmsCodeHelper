package chenmc.sms.data.storage

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * @author Carter
 * Created on 2018-02-12
 */
@Database(entities = [SmsCodeRegex::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsCodeRegexDao(): SmsCodeRegexDao
}