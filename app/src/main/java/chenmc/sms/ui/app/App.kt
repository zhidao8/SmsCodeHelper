package chenmc.sms.ui.app

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.transaction.NotificationContract
import chenmc.sms.utils.CrashHandler
import chenmc.sms.utils.LogUtil

/**
 * @author 明 明
 * Created on 2017-5-1.
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        // 初始化程序崩溃时的异常处理器
        CrashHandler.init(context)

        LogUtil.init(this)

        AppPreference.init(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannels(mutableListOf(
                        NotificationChannel(NotificationContract.CHANNEL_ID_VERIFICATION,
                                getString(R.string.notification_channel_verification_code),
                                NotificationManager.IMPORTANCE_HIGH),
                        NotificationChannel(NotificationContract.CHANNEL_ID_EXPRESS,
                                getString(R.string.notification_channel_express_code),
                                NotificationManager.IMPORTANCE_HIGH)
                ))
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}
