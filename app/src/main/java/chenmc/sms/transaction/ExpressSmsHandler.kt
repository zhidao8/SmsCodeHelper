package chenmc.sms.transaction

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
        val notificationId = notificationId
        val title = context.getString(
            R.string.express_is,
            codeSms.serviceProvider
        )
        val isContentEmpty = TextUtils.isEmpty(codeSms.extra)
        val contentText = codeSms.code + if (isContentEmpty) "" else " " + codeSms.extra
        val bigText = codeSms.code + if (isContentEmpty) "" else "\n${codeSms.extra}"
        val copyText = "$title\n$bigText"

        // 通知按钮：点击复制通知内容
        val copyContentActionPi = PendingIntent.getService(
            context, requestCode,
            Intent(context, CopyTextService::class.java)
                .putExtra(CopyTextService.EXTRA_TEXT, copyText),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        // 通知按钮：点击复制取件码
        val copyCodeActionPi = PendingIntent.getService(
            context, requestCode,
            Intent(context, CopyTextService::class.java)
                .putExtra(CopyTextService.EXTRA_EXPRESS, codeSms.code),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, NotificationContract.CHANNEL_ID_EXPRESS)
            .setContentTitle(title) // 通知标题
            .setTicker(copyText) // 通知出现时的滚动文字
            .setContentText(contentText) // 通知详细内容
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS) // 通知声音和呼吸灯
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE) // 通知显示级别
            .setSmallIcon(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    R.drawable.ic_notification else R.mipmap.ic_launcher
            ) // 小通知图标
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary)) // 小通知图标颜色
            .setWhen(System.currentTimeMillis()) // 通知出现时间
            .setShowWhen(true) // 显示通知出现时间
            .setAutoCancel(true) // 用户点击通知后自动取消通知，实测无效
            .setContentIntent(
                PendingIntent.getService(
                    context, requestCode, Intent(),
                    PendingIntent.FLAG_CANCEL_CURRENT
                )
            ) // 用户点击通知后自动取消通知
            .addAction(0, context.getString(R.string.copy_content), copyContentActionPi)
            .addAction(0, context.getString(R.string.copy_express_code), copyCodeActionPi)

        val notification = builder.build().apply {
            // 通知不可一键清除
            flags = flags or NotificationCompat.FLAG_NO_CLEAR
        }
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(notificationId, notification)
    }

    private var _notificationId = 0

    private val notificationId: Int
        get() {
            _notificationId = System.currentTimeMillis().toInt()
            _requestCode = _notificationId
            return _notificationId
        }

    private var _requestCode = 0

    private val requestCode
        get() = _requestCode++
}