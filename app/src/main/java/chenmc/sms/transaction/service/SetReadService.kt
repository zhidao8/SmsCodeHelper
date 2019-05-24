package chenmc.sms.transaction.service

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.core.content.ContextCompat

/**
 * @author Carter
 * Created on 2018-04-21
 */
class SetReadService : Service() {

    private val handler: Handler = object : Handler(Looper.myLooper()) {
        override fun handleMessage(msg: Message) {
            val sms = msg.obj as String
            val newValues = ContentValues()
            newValues.put("read", 1)
            contentResolver.update(Uri.parse("content://sms/"), newValues, "body like ?", arrayOf("%$sms%"))
            stopSelf(msg.what)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_SMS") ==
            PackageManager.PERMISSION_GRANTED
        ) {

            val message = handler.obtainMessage(startId, intent.getStringExtra(EXTRA_SMS))
            // 延迟 3 秒，等待短信写入数据库
            handler.sendMessageDelayed(message, 3 * 1000)
        } else stopSelf(startId)

        return Service.START_NOT_STICKY
    }

    companion object {
        const val EXTRA_SMS = "EXTRA_SMS"
    }
}