package chenmc.sms.transaction.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import chenmc.sms.transaction.SmsExtractor
import chenmc.sms.transaction.SmsHandlerExecutor

/**
 * 此 Service 用来监听短信数据库的变化
 *
 * @author 明 明
 * Created on 2017-2-14.
 */

class SmsObserverService : Service() {

    private lateinit var smsObserver: SmsObserver

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        smsObserver = SmsObserver(Handler(Looper.getMainLooper()))
        // 注册短信数据库监听
        contentResolver.registerContentObserver(
            Uri.parse("content://sms/"), true,
            smsObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        // 取消注册短信数据库监听
        contentResolver.unregisterContentObserver(smsObserver)

//        Log.i(javaClass.canonicalName, "onDestroy")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private inner class SmsObserver internal constructor(handler: Handler) : ContentObserver(handler) {

        // 上一次处理的短信在数据库中的 _id
        private var previousId = -1
        private val smsHandlerExecutor: SmsHandlerExecutor = SmsHandlerExecutor(this@SmsObserverService)

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)

            val context = this@SmsObserverService

            //每当有新短信到来时，使用我们获取短消息的方法
            val (sms, databaseId) = SmsExtractor.extractFromDatabase(context)
            // 有新短信来时，该 onChange 方法会被多次调用，
            // 如果上次处理的数据库 _id 跟这次的相同，即当前处理的短信与上一次的相同，则不处理
            if (previousId == databaseId) return

            previousId = databaseId
            // 短信执行器并执行
            smsHandlerExecutor.execute(sms)

//            Log.i(javaClass.canonicalName, "$databaseId: $sms")
        }
    }

    companion object {

        fun startThisService(context: Context) {
            context.startService(Intent(context, SmsObserverService::class.java))
        }

        fun stopThisService(context: Context) {
            context.stopService(Intent(context, SmsObserverService::class.java))
        }
    }
}
