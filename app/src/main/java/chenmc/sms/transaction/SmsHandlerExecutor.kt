package chenmc.sms.transaction

import android.content.Context
import android.content.Intent
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.transaction.service.SetReadService

/**
 * @author Carter
 * Created on 2018-02-06
 */
class SmsHandlerExecutor(private val context: Context) {

    private val verificationSmsHandler: VerificationSmsHandler = VerificationSmsHandler()
    private var expressSmsHandler: ExpressSmsHandler? = null

    fun execute(sms: String) {
        var handled = verificationSmsHandler.handle(context, sms)

        // 如果应用开启了解析快递取件码短信
        if (AppPreference.expressEnable) {
            expressSmsHandler = ExpressSmsHandler()
            // 添加取件码短信处理器
            handled = expressSmsHandler?.handle(context, sms) ?: false

        } else expressSmsHandler = null

        if (handled) {
            context.startService(
                Intent(context, SetReadService::class.java)
                    .putExtra(SetReadService.EXTRA_SMS, sms)
            )
        }
    }
}