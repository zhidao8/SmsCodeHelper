package chenmc.sms.data.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
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

    fun findOne(id: Int): SmsCodeRegex? {

        return dbHelper.readableDatabase.transaction {
            val selection = "$ID = ?"
            val selectionArgs = arrayOf("$id")
            query(TABLE, null, selection, selectionArgs, null, null, null)
                .use { cursor ->
                    if (cursor.moveToFirst()) {
                        SmsCodeRegex(
                            cursor.getInt(ID),
                            cursor.getString(SMS),
                            cursor.getString(CODE),
                            cursor.getString(REGEX)
                        )
                    } else null
                }
        }
    }

    fun findOneByRegex(regex: String): SmsCodeRegex? {

        return dbHelper.readableDatabase.transaction {
            val selection = "$REGEX = ?"
            val selectionArgs = arrayOf(regex)
            query(TABLE, null, selection, selectionArgs, null, null, null)
                .use { cursor ->
                    if (cursor.moveToFirst()) {
                        SmsCodeRegex(
                            cursor.getInt(ID),
                            cursor.getString(SMS),
                            cursor.getString(CODE),
                            cursor.getString(REGEX)
                        )
                    } else null
                }
        }
    }

    /**
     * 返回每一条插入到数据库中的记录的 rowId
     */
    fun insert(vararg args: SmsCodeRegex): LongArray {
        val result: MutableList<Long> = ArrayList(args.size)

        dbHelper.writableDatabase.transaction {
            args.mapTo(result) {
                val regex = it.regex

                if (regex != null && findOneByRegex(regex) == null) {
                    val cv = ContentValues(3)
                    cv.put(SMS, it.sms)
                    cv.put(REGEX, it.regex)
                    cv.put(CODE, it.verificationCode)

                    insert(TABLE, null, cv)
                } else -1
            }
        }

        cacheRef = null
        return result.toLongArray()
    }

    /**
     * 返回更新的记录的数量
     */
    fun update(vararg args: SmsCodeRegex): Int {
        var result = 0

        dbHelper.writableDatabase.transaction {
            for (it in args) {
                val regex = it.regex

                if (regex != null && findOneByRegex(regex) == null) {
                    val cv = ContentValues(3)
                    cv.put(SMS, it.sms)
                    cv.put(REGEX, it.regex)
                    cv.put(CODE, it.verificationCode)

                    result += update(TABLE, cv, "$ID = ?", arrayOf("${it.id}"))
                }
            }
        }

        cacheRef = null
        return result
    }

    /**
     * 返回删除的记录的数量
     */
    fun delete(vararg args: SmsCodeRegex): Int {
        var result = 0

        dbHelper.writableDatabase.transaction {
            for (it in args) {
                result += delete(TABLE, "$ID = ?", arrayOf("${it.id}"))
            }
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