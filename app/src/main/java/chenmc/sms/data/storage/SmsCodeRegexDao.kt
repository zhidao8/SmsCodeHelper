package chenmc.sms.data.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import chenmc.sms.data.storage.AppDBContract.SmsCodeRegex.CODE
import chenmc.sms.data.storage.AppDBContract.SmsCodeRegex.ID
import chenmc.sms.data.storage.AppDBContract.SmsCodeRegex.REGEX
import chenmc.sms.data.storage.AppDBContract.SmsCodeRegex.SMS
import chenmc.sms.data.storage.AppDBContract.SmsCodeRegex.TABLE
import java.lang.ref.WeakReference

/**
 * @author Carter
 * Created on 2018-07-17
 */
class SmsCodeRegexDao private constructor(context: Context) {

    private val dbHelper: SQLiteOpenHelper = AppDatabase(context)

    fun selectAll(): MutableList<SmsCodeRegex> {
        cacheRef?.get()?.run { return this }

        val result: MutableList<SmsCodeRegex>

        val db = dbHelper.readableDatabase
        db.beginTransaction()
        try {
            val cursor = db.query(TABLE, null, null, null, null, null, null)
            result = ArrayList(cursor.count)
            cursor.use {
                while (cursor.moveToNext()) {
                    result.add(
                        SmsCodeRegex(
                            cursor.getInt(ID),
                            cursor.getString(SMS),
                            cursor.getString(CODE),
                            cursor.getString(REGEX)
                        )
                    )
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }

        cacheRef = WeakReference(result)
        return result
    }

    /**
     * 返回每一条插入到数据库中的记录的 rowId
     */
    fun insert(vararg smsCodeRegexes: SmsCodeRegex): LongArray {
        val result: MutableList<Long> = ArrayList(smsCodeRegexes.size)

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (it in smsCodeRegexes) {
                val cv = ContentValues(3)
                cv.put(SMS, it.sms)
                cv.put(REGEX, it.regex)
                cv.put(CODE, it.verificationCode)

                val rowId = db.insert(TABLE, null, cv)
                result.add(rowId)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }

        cacheRef = null
        return result.toLongArray()
    }

    /**
     * 返回更新的记录的数量
     */
    fun update(vararg smsCodeRegexes: SmsCodeRegex): Int {
        var result = 0

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (it in smsCodeRegexes) {
                val cv = ContentValues(3)
                cv.put(SMS, it.sms)
                cv.put(REGEX, it.regex)
                cv.put(CODE, it.verificationCode)

                result += db.update(TABLE, cv, "$ID = ?", arrayOf("${it.id}"))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }

        cacheRef = null
        return result
    }

    /**
     * 返回删除的记录的数量
     */
    fun delete(vararg smsCodeRegexes: SmsCodeRegex): Int {
        var result = 0

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (it in smsCodeRegexes) {
                result += db.delete(TABLE, "$ID = ?", arrayOf("${it.id}"))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }

        cacheRef = null
        return result
    }

    private fun Cursor.getInt(columnName: String) =
        this.getInt(this.getColumnIndex(columnName))

    private fun Cursor.getString(columnName: String) =
        this.getString(this.getColumnIndex(columnName))

    companion object {
        fun getInstance(context: Context): SmsCodeRegexDao =
            instanceRef?.get() ?: SmsCodeRegexDao(context).apply {
                instanceRef = WeakReference(this)
            }

        @JvmStatic
        private var instanceRef: WeakReference<SmsCodeRegexDao>? = null
        @JvmStatic
        private var cacheRef: WeakReference<MutableList<SmsCodeRegex>>? = null
    }
}