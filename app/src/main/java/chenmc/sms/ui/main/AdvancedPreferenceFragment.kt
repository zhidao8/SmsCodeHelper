package chenmc.sms.ui.main

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
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
import java.util.*

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
            this.isChecked = isMyAppLauncherDefault
            this.onPreferenceChangeListener = this@AdvancedPreferenceFragment
        }
        
        //region 短信包含关键词
        val prefSmsContains = findPreference(getString(R.string.pref_key_sms_contains))
        prefSmsContains.summary = AppPreference.smsKeyword
        prefSmsContains.onPreferenceChangeListener = this
        //endregion
        
        //region 验证码匹配规则
        val prefRegexp = findPreference(getString(R.string.pref_key_regexp))
        prefRegexp.summary = AppPreference.smsRegex
        prefRegexp.onPreferenceChangeListener = this
        //endregion
        
        //region 快递短信包含关键词
        val prefExpressSmsContains = findPreference(getString(R.string.pref_key_express_sms_contains))
        prefExpressSmsContains.summary = AppPreference.expressKeyword
        prefExpressSmsContains.onPreferenceChangeListener = this
        //endregion
        
        //region 取件码匹配规则
        val prefExpressRegexp = findPreference(getString(R.string.pref_key_express_regexp))
        prefExpressRegexp.summary = AppPreference.expressRegex
        prefExpressRegexp.onPreferenceChangeListener = this
        //endregion
        
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
            else -> preference.summary = newValue.toString()
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
    
    // 判断当前应用是不是默认应用
    private val isMyAppLauncherDefault: Boolean
        get() {
            val preferredActivities = ArrayList<ComponentName>()
            activity.packageManager.getPreferredActivities(
                    mutableListOf(IntentFilter(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }),
                    preferredActivities,
                    activity.packageName
            )
            return preferredActivities.size > 0
        }
    
    companion object {
        
        // 请求更改默认短信的请求码
        private const val REQUEST_CHANGE_DEFAULT_SMS_APP = 0
    }
}
