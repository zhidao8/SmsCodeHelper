package chenmc.sms.transaction.service

import android.app.Service
import android.content.Intent
import android.os.*
import chenmc.sms.transaction.SmsExtractor
import chenmc.sms.transaction.SmsHandlerExecutor

/**
 * @author 明 明
 * Created on 2017-4-28.
 */

class SmsReceiveService : Service() {

    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // 新开一个线程进行处理
        val handlerThread = HandlerThread(
            javaClass.simpleName,
            Process.THREAD_PRIORITY_MORE_FAVORABLE
        )
        handlerThread.start()

        serviceHandler = ServiceHandler(handlerThread.looper)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val msg = serviceHandler.obtainMessage()
        // 保存 startId
        msg.arg1 = startId
        // 保存 intent 到 msg.obj
        msg.obj = intent
        serviceHandler.sendMessage(msg)

        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        // Service 停止时停止线程
        serviceHandler.looper.quit()
    }

    private inner class ServiceHandler internal constructor(looper: Looper) : Handler(looper) {

        private val smsHandlerExecutor: SmsHandlerExecutor = SmsHandlerExecutor(this@SmsReceiveService)

        override fun handleMessage(msg: Message) {
            val intent = msg.obj as Intent

            // 从 Intent 中获取短信内容
            val sms = SmsExtractor.extractFromIntent(intent)
            // 短信执行器执行
            smsHandlerExecutor.execute(sms)

            // 使用 startId 停止 Service
            stopSelfResult(msg.arg1)
        }
    }
}
