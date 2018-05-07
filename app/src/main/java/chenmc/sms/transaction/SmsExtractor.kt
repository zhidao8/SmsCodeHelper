package chenmc.sms.transaction

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.telephony.SmsMessage

/**
 * @author Carter
 * Created on 2018-02-06
 */
object SmsExtractor {
    /**
     * 从接收短信的 Receiver 的 Intent 中获取对象
     *
     * @param intent Receiver 接收的 Intent
     * @return [String] 对象
     */
    @JvmStatic
    fun extractFromIntent(intent: Intent): String {
        
        val sb = StringBuilder()
        
        val bundle = intent.extras ?: return ""
        
        val pdus = bundle.get("pdus") as Array<*>? ?: return ""
        
        val messages = arrayOfNulls<SmsMessage>(pdus.size)
        for (i in pdus.indices) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray, intent.getStringExtra("format"))
            } else {
                @Suppress("DEPRECATION")
                messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
            }
        }
        for (message in messages) {
            sb.append(message?.displayMessageBody ?: "")
        }
        return sb.toString()
    }
    
    /**
     * 从系统短信数据库中获取对象
     *
     * @param context 上下文
     * @return [DatabaseSms] 对象
     */
    @JvmStatic
    fun extractFromDatabase(context: Context): DatabaseSms {
        var databaseSms = DatabaseSms("")
        
        val cr = context.contentResolver
        // 获取接收到的短信（type = 1），并且只获取 5 秒以内的消息
        val where = "type = 1 and date > " + (System.currentTimeMillis() - 5000)
        val cursor: Cursor? = cr.query(Uri.parse("content://sms/"),
                arrayOf("_id", "body"), where, null, "date desc")
        
        if (cursor?.moveToFirst() == true) {
            databaseSms = DatabaseSms(
                    cursor.getString(cursor.getColumnIndex("body")),
                    cursor.getInt(cursor.getColumnIndex("_id"))
            )
        }
        cursor?.close()

        return databaseSms
    }
    
    const val PROVIDER_REGEXP = "(【.+?】|\\[.+?\\])"
    
    data class DatabaseSms(val sms: String, val databaseId: Int = -1)
}