package chenmc.sms.transaction.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import chenmc.sms.util.LogUtil

/**
 * @author Carter
 * Created on 2018-09-25
 */
class CancelNotificationService : IntentService(CancelNotificationService::class.java.simpleName) {

    override fun onHandleIntent(intent: Intent?) {
        intent ?: return
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, NO_NOTIFICATION_ID)
        if (notificationId != NO_NOTIFICATION_ID) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(notificationId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.i(this.javaClass.simpleName, "${this.javaClass.simpleName} onDestroy")
    }

    companion object {
        const val NOTIFICATION_ID = "NOTIFICATION_ID"
        private const val NO_NOTIFICATION_ID = -1
    }
}