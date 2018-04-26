package chenmc.sms.transaction

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import chenmc.sms.code.helper.R
import chenmc.sms.data.ExpressCodeSms
import chenmc.sms.transaction.service.CopyTextService

/**
 * @author Carter
 * Created on 2018-02-06
 */
class ExpressSmsHandler : ISmsHandler {
    private lateinit var context: Context
    
    override fun handle(context: Context, sms: String): Boolean {
        this.context = context.applicationContext
    
        val codeSms = SmsAnalyzer(context).analyseExpressSms(sms) ?: return false
    
        // 处理取件码
        handleCode(codeSms)
        return true
    }
    
    private fun handleCode(codeSms: ExpressCodeSms) {
        notifyNotification(codeSms)
    }
    
    // 在通知栏显示验证码和服务商
    private fun notifyNotification(codeSms: ExpressCodeSms) {
        val notificationId = System.currentTimeMillis().toInt()
        val title = context.getString(R.string.express_is, codeSms.serviceProvider, codeSms.code)
        val copyText = title + if (codeSms.content != null) "\n${codeSms.content}" else ""
    
        val builder = NotificationCompat.Builder(context, NotificationContract.CHANNEL_ID_EXPRESS)
            .setContentTitle(title)
            .setTicker(copyText)
            .setContentText(codeSms.content)
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .addAction(0, context.getString(R.string.copy_content), PendingIntent.getService(
                    context, notificationId + 1,
                    Intent(context, CopyTextService::class.java)
                        .putExtra(CopyTextService.EXTRA_TEXT, copyText),
                    PendingIntent.FLAG_UPDATE_CURRENT))
            .addAction(0, context.getString(R.string.copy_express_code),
                    PendingIntent.getService(context, notificationId + 2,
                    Intent(context, CopyTextService::class.java)
                        .putExtra(CopyTextService.EXTRA_EXPRESS, codeSms.code),
                    PendingIntent.FLAG_UPDATE_CURRENT))
    
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.ic_notification)
                .color = ContextCompat.getColor(context, R.color.colorPrimary)
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher)
        }
    
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }
}