package chenmc.sms.transaction

import android.content.Context
import android.content.Intent
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.transaction.service.SetReadService

/**
 * @author Carter
 * Created on 2018-02-06
 */
class SmsHandlerExecutor(private val context: Context, private val sms: String) {
    
    fun execute() {
        var handled = VerificationSmsHandler().handle(context, sms)
        
        // 如果应用开启了解析快递取件码短信
        if (AppPreference.expressEnable) {
            // 添加取件码短信处理器
            handled = ExpressSmsHandler().handle(context, sms)
        }
        
        if (handled) {
            context.startService(Intent(context, SetReadService::class.java)
                .putExtra(SetReadService.EXTRA_SMS, sms))
        }
    }
}