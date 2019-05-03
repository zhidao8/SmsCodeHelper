package chenmc.sms.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import chenmc.sms.ui.app.App

object ActivityUtil {
    fun showApplicationDetail(activity: Activity, requestCode: Int): Boolean {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        return if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivityForResult(intent, requestCode)
            true
        } else {
            false
        }
    }

    // 判断当前应用是不是短信默认应用
    val isSmsDefaultApp: Boolean
        get() = Settings.Secure.getString(App.context.contentResolver, "sms_default_application") ==
                App.context.packageName
}