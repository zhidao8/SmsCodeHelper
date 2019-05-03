package chenmc.sms.transaction.service

import android.annotation.TargetApi
import android.app.Notification
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import chenmc.sms.transaction.SmsAnalyzer

/**
 * @author Carter
 * Created on 2018-04-21
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
class SmsNotificationListenerService : NotificationListenerService() {
    
    private val context: Context = this

    // API 21 之前，父类的这个方法是 abstract 的
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        when (sbn.packageName) {
            "com.android.mms",
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging" -> {
                val extras = sbn.notification.extras

                val smsAnalyzer = SmsAnalyzer(context)
                val verificationCodeSms =
                    smsAnalyzer.analyseVerificationSms(extras[Notification.EXTRA_TITLE]?.toString() ?: "")
                    ?: smsAnalyzer.analyseVerificationSms(extras[Notification.EXTRA_TEXT]?.toString() ?: "")
                if (verificationCodeSms != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        cancelNotification(sbn.key)
                    } else {
                        @Suppress("DEPRECATION")
                        cancelNotification(sbn.packageName, sbn.tag, sbn.id)
                    }
                }
            }
        }
    }

    // API 21 之前，父类的这个方法是 abstract 的
    override fun onNotificationRemoved(sbn: StatusBarNotification) = Unit

    override fun onListenerConnected() {
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
    }
}