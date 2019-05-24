package chenmc.sms.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Telephony
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.util.ActivityUtil
import chenmc.sms.util.ToastUtil
import java.util.regex.PatternSyntaxException

/**
 * Created by 明明 on 2017/8/9.
 */

class AdvancedPreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_advanced)
        init()
    }

    private fun init() {
        val listener = this

        findPreference(getString(R.string.pref_key_app_main_switch)).apply {
            this.onPreferenceChangeListener = listener
        }

        findPreference(getString(R.string.pref_key_notification_listener)).apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                this.isEnabled = false
                this.summary = getString(R.string.pref_notification_listener_summary_api_19)
            }
        }

        (findPreference(getString(R.string.pref_key_default_sms_app)) as SwitchPreference).apply {
            this.isChecked = ActivityUtil.isSmsDefaultApp
            this.onPreferenceChangeListener = listener
        }

        // 短信包含关键词
        findPreference(getString(R.string.pref_key_sms_contains)).apply {
            this.summary = AppPreference.smsKeyword
            this.onPreferenceChangeListener = listener
        }

        // 验证码匹配规则
        findPreference(getString(R.string.pref_key_regexp)).apply {
            this.summary = AppPreference.smsRegex
            this.onPreferenceChangeListener = listener
        }

        // 快递短信包含关键词
        findPreference(getString(R.string.pref_key_express_sms_contains)).apply {
            this.summary = AppPreference.expressKeyword
            this.onPreferenceChangeListener = listener
        }

        // 取件码匹配规则
        findPreference(getString(R.string.pref_key_express_regexp)).apply {
            this.summary = AppPreference.expressRegex
            this.onPreferenceChangeListener = listener
        }

        // 取件地址匹配正则
        findPreference(getString(R.string.pref_key_express_place_regexp)).apply {
            this.summary = AppPreference.expressPlaceRegex
            this.onPreferenceChangeListener = listener
        }
    }

    override fun onStart() {
        super.onStart()

        activity?.let { activity ->
            when (activity) {
                is AppCompatActivity -> {
                    activity.supportActionBar?.run {
                        setTitle(R.string.pref_advanced)
                        setDisplayHomeAsUpEnabled(true)
                    }
                }
                else -> {
                    activity.actionBar?.run {
                        setTitle(R.string.pref_advanced)
                        setDisplayHomeAsUpEnabled(true)
                    }
                }
            }
        }
        setHasOptionsMenu(true)

        // 设置通知栏使用权后返回需要进行的判断
        val appFeaturesEnabled = AppPreference.isAppFeaturesEnabled
        findPreference(getString(R.string.pref_key_notification_listener)).apply {
            this.isEnabled = appFeaturesEnabled
            (this as SwitchPreference).isChecked = notificationListenerEnabled
        }
        // 应用的所有功能已被禁用，并且通知栏使用权已开启
        if (!appFeaturesEnabled && notificationListenerEnabled) {
            // 提醒用户关闭通知栏使用权
            tipOffNotificationListenerSettings()
        }
    }

    private val notificationListenerEnabled
        get(): Boolean = activity?.let { activity ->
            NotificationManagerCompat.getEnabledListenerPackages(activity).contains(activity.packageName)
        } ?: false

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when (preference.key) {
            getString(R.string.pref_key_app_main_switch) -> {
                findPreference(getString(R.string.pref_key_notification_listener)).isEnabled = (newValue as Boolean)

                mHandler.removeCallbacks(mOpenNotificationListenerSettingsRun)

                if (!newValue && notificationListenerEnabled) {
                    tipOffNotificationListenerSettings()
                }
            }
            getString(R.string.pref_key_default_sms_app) -> {
                activity?.let { activity ->
                    val newPackage: String = if (newValue as Boolean) {

                        // 先将当前默认启动应用保存起来
                        AppPreference.defaultSmsApp = Settings.Secure.getString(
                            activity.contentResolver, "sms_default_application"
                        )
                        activity.packageName
                    } else {
                        AppPreference.defaultSmsApp
                    }

                    // 请求更改默认短信应用
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                            .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, newPackage)
                    } else {
                        null
                    }
                    if (intent?.resolveActivity(activity.packageManager) != null) {
                        startActivityForResult(intent, REQUEST_CHANGE_DEFAULT_SMS_APP)
                    }
                }
            }
            getString(R.string.pref_key_sms_contains),
            getString(R.string.pref_key_regexp),
            getString(R.string.pref_key_express_sms_contains),
            getString(R.string.pref_key_express_regexp),
            getString(R.string.pref_key_express_place_regexp) -> {
                try {
                    newValue.toString().toRegex()
                } catch (e: PatternSyntaxException) {
                    val dialog = AlertDialog.Builder(activity)
                        .setTitle(R.string.dialog_title_regex_incorrect)
                        .setMessage(e.message)
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()

                    return false
                }

                preference.summary = newValue.toString()
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            getString(R.string.pref_key_notification_listener) -> {
                return openNotificationListenerSettings()
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private val mOpenNotificationListenerSettingsRun: Runnable = Runnable { openNotificationListenerSettings() }

    private fun tipOffNotificationListenerSettings() {
        ToastUtil.showSingletonToast(R.string.off_notification_listener_manually, Toast.LENGTH_LONG)
        mHandler.postDelayed(mOpenNotificationListenerSettingsRun, 3000)
    }

    private fun openNotificationListenerSettings(): Boolean {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        } else Intent(Settings.ACTION_SETTINGS)

        return activity?.let { activity ->
            if (intent.resolveActivity(activity.packageManager) != null) {
                startActivity(intent)
                true
            } else false
        } ?: false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHANGE_DEFAULT_SMS_APP -> {
                if (resultCode != Activity.RESULT_OK) {
                    // 如果用户没有修改默认短信，将开关反转
                    val preference = findPreference(getString(R.string.pref_key_default_sms_app)) as SwitchPreference
                    preference.isChecked = !preference.isChecked
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        // 请求更改默认短信的请求码
        private const val REQUEST_CHANGE_DEFAULT_SMS_APP = 0
    }
}
