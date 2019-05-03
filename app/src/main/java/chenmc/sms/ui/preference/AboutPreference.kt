package chenmc.sms.ui.preference

import android.annotation.TargetApi
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.Toast
import androidx.preference.Preference
import chenmc.sms.code.helper.R

/**
 * @author 明 明
 * Created on 2017-4-27.
 */

class AboutPreference : Preference {

    private var times: Int = 0

    var isDeveloperMode: Boolean = false
        private set

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context)

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getBoolean(index, false)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        isDeveloperMode =
                if (restorePersistedValue || defaultValue == null)
                    getPersistedBoolean(false)
                else defaultValue as Boolean
    }

    override fun onClick() {
        times++
        if (!isDeveloperMode && times == 10) {
            Toast.makeText(context, R.string.advanced_preference_on, Toast.LENGTH_LONG).show()
            isDeveloperMode = !isDeveloperMode
            persistBoolean(isDeveloperMode)
            Handler(Looper.getMainLooper()).postDelayed({ callChangeListener(isDeveloperMode) }, 1000)
            notifyDependencyChange(shouldDisableDependents())
            notifyChanged()
        } else if (isDeveloperMode && times == 3) {
            Toast.makeText(context, R.string.advanced_preference_off, Toast.LENGTH_LONG).show()
            isDeveloperMode = !isDeveloperMode
            persistBoolean(isDeveloperMode)
            Handler(Looper.getMainLooper()).postDelayed({ callChangeListener(isDeveloperMode) }, 1000)
            notifyDependencyChange(shouldDisableDependents())
            notifyChanged()

            // 将变量置为 10 防止触发第一条 if
            times = 10
        }
    }
}
