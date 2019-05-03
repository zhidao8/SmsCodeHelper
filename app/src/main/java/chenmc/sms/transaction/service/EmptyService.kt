package chenmc.sms.transaction.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by 明明 on 2017/7/21.
 */

class EmptyService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        stopSelf(startId)
        return Service.START_NOT_STICKY
    }
}
