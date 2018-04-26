package chenmc.sms.transaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import chenmc.sms.transaction.service.SmsReceiveService

/**
 * 接收短信广播的接收器
 * @author 明 明
 * Created on 2017-2-20.
 */

class SmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                // 打开一个服务进行相关处理
                intent.setClass(context, SmsReceiveService::class.java)
                context.startService(intent)
            }
        }
    }
}
