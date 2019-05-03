package chenmc.sms.util

import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import chenmc.sms.ui.app.App

object ToastUtil {

    private val mToast = Toast.makeText(App.context, "", Toast.LENGTH_SHORT)

    @IntDef(value = [Toast.LENGTH_SHORT, Toast.LENGTH_LONG])
    @Retention(AnnotationRetention.SOURCE)
    annotation class Duration

    fun showSingletonToast(text: String, @Duration duration: Int) {
        mToast.setText(text)
        mToast.duration = duration
        mToast.show()
    }

    fun showSingletonToast(@StringRes textRes: Int, @Duration duration: Int) {
        mToast.setText(textRes)
        mToast.duration = duration
        mToast.show()
    }

    fun showSingletonShortToast(text: String) {
        showSingletonToast(text, Toast.LENGTH_SHORT)
    }

    fun showSingletonShortToast(@StringRes textRes: Int) {
        showSingletonToast(textRes, Toast.LENGTH_SHORT)
    }

    fun showSingletonLongToast(text: String) {
        showSingletonToast(text, Toast.LENGTH_LONG)
    }

    fun showSingletonLongToast(@StringRes textRes: Int) {
        showSingletonToast(textRes, Toast.LENGTH_LONG)
    }

    fun showToast(text: String, @Duration duration: Int) {
        Toast.makeText(App.context, text, duration).show()
    }

    fun showToast(@StringRes textRes: Int, @Duration duration: Int) {
        Toast.makeText(App.context, textRes, duration).show()
    }

    fun showShortToast(text: String) {
        showToast(text, Toast.LENGTH_SHORT)
    }

    fun showShortToast(@StringRes textRes: Int) {
        showToast(textRes, Toast.LENGTH_SHORT)
    }

    fun showLongToast(text: String) {
        showToast(text, Toast.LENGTH_LONG)
    }

    fun showLongToast(@StringRes textRes: Int) {
        showToast(textRes, Toast.LENGTH_LONG)
    }
}