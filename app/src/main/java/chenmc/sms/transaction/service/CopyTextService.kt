package chenmc.sms.transaction.service

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.Toast

import chenmc.sms.code.helper.R
import chenmc.sms.util.ToastUtil

/**
 * 复制验证码（取件码）的服务
 * @author 明 明
 * Created on 2017-4-20.
 */

class CopyTextService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val text = intent.getStringExtra(EXTRA_VERIFICATION) ?: intent.getStringExtra(EXTRA_EXPRESS)
                   ?: intent.getStringExtra(EXTRA_TEXT)

        if (text != null) {
            // 获取剪切板
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // 复制到剪切板
            clipboardManager.primaryClip = ClipData.newPlainText(javaClass.simpleName, text)

            when {
                intent.hasExtra(EXTRA_VERIFICATION) -> ToastUtil.showToast(
                        getString(R.string.sms_code_have_been_copied, text),
                        Toast.LENGTH_LONG)
                intent.hasExtra(EXTRA_EXPRESS) -> ToastUtil.showToast(
                        getString(R.string.express_code_have_been_copied, text),
                        Toast.LENGTH_LONG)
                intent.hasExtra(EXTRA_TEXT) -> ToastUtil.showToast(
                        getString(R.string.text_have_been_copied, text),
                        Toast.LENGTH_LONG)
            }
        }

        stopSelfResult(startId)
        return Service.START_NOT_STICKY
    }

    companion object {
        const val EXTRA_VERIFICATION = "verification"
        const val EXTRA_EXPRESS = "express"
        const val EXTRA_TEXT = "text"
    }
}
