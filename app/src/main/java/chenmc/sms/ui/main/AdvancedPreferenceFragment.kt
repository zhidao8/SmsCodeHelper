package chenmc.sms.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.provider.Settings
import android.provider.Telephony
import android.view.MenuItem
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.AppPreference
import java.util.regex.PatternSyntaxException

/**
 * Created by 明明 on 2017/8/9.
 */

class AdvancedPreferenceFragment : PreferenceFragment(), Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference_advanced)
        
        init()
    }
    
    override fun onStart() {
        super.onStart()
        val actionBar = activity.actionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.pref_advanced)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
    }
    
    private fun init() {
        findPreference(getString(R.string.pref_key_notification_listener)).apply {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                this.isEnabled = false
                this.summary =  getString(R.string.pref_notification_listener_summary_api_19)
            }
            this.onPreferenceClickListener  = this@AdvancedPreferenceFragment
        }
    
        (findPreference(getString(R.string.pref_key_default_sms_app)) as SwitchPreference).apply {
            this.isChecked = isSmsDefaultApp
            this.onPreferenceChangeListener = this@AdvancedPreferenceFragment
        }
        
        // 短信包含关键词
        findPreference(getString(R.string.pref_key_sms_contains)).apply {
            this.summary = AppPreference.smsKeyword
            this.onPreferenceChangeListener = this@AdvancedPreferenceFragment
        }

        // 验证码匹配规则
        findPreference(getString(R.string.pref_key_regexp)).apply {
            this.summary = AppPreference.smsRegex
            this.onPreferenceChangeListener = this@AdvancedPreferenceFragment
        }
        
        // 快递短信包含关键词
        findPreference(getString(R.string.pref_key_express_sms_contains)).apply {
            this.summary = AppPreference.expressKeyword
            this.onPreferenceChangeListener = this@AdvancedPreferenceFragment
        }

        // 取件码匹配规则
        findPreference(getString(R.string.pref_key_express_regexp)).apply {
            this.summary = AppPreference.expressRegex
            this.onPreferenceChangeListener = this@AdvancedPreferenceFragment
        }

        // 取件地址匹配正则
        findPreference(getString(R.string.pref_key_express_place_regexp)).apply {
            this.summary = AppPreference.expressPlaceRegex
            this.onPreferenceChangeListener = this@AdvancedPreferenceFragment
        }
    }
    
    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
    
        when (preference.key) {
            getString(R.string.pref_key_default_sms_app) -> {
                val newPackage: String = if (newValue as Boolean) {
    
                    // 先将当前默认启动应用保存起来
                    AppPreference.defaultSmsApp = Settings.Secure.getString(
                            activity.contentResolver, "sms_default_application")
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
                if (intent?.resolveActivity(activity.packageManager) != null)
                    startActivityForResult(intent, REQUEST_CHANGE_DEFAULT_SMS_APP)
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
            android.R.id.home -> activity.onBackPressed()
        }
        
        return super.onOptionsItemSelected(item)
    }
    
    override fun onPreferenceClick(preference: Preference): Boolean {
        
        when (preference.key) {
            getString(R.string.pref_key_notification_listener) -> {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                } else Intent(Settings.ACTION_SETTINGS)
                if (intent.resolveActivity(activity.packageManager) != null) {
                    startActivity(intent)
                }
                return true
            }
        }
        
        return false
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHANGE_DEFAULT_SMS_APP -> if (resultCode != Activity.RESULT_OK) {
                // 如果用户没有修改默认短信，将开关反转
                val preference = findPreference(getString(R.string.pref_key_default_sms_app)) as SwitchPreference
                preference.isChecked = !preference.isChecked
            }
        }
        
        super.onActivityResult(requestCode, resultCode, data)
    }

    // 判断当前应用是不是短信默认应用
    private val isSmsDefaultApp: Boolean
        get() {
            return Settings.Secure.getString(
                    activity.contentResolver, "sms_default_application") == activity.packageName
        }
    
    companion object {
        
        // 请求更改默认短信的请求码
        private const val REQUEST_CHANGE_DEFAULT_SMS_APP = 0
    }
}
