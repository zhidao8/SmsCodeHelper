package chenmc.sms.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import chenmc.sms.code.helper.R
import java.lang.ref.WeakReference

/**
 * @author Carter
 * Created on 2018-04-20
 */
object AppPreference {
    
    private lateinit var contextWR: WeakReference<Context>
    private lateinit var sp: SharedPreferences
    
    fun init(context: Context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context)
        contextWR = WeakReference(context)
    }
    
    private val context: Context
        get() = contextWR.get()!!
    
    var mode: String
        get() = sp.getString(context.getString(R.string.pref_key_mode),
                context.getString(R.string.pref_def_value_mode))
        set(value) = sp.edit()
                .putString(context.getString(R.string.pref_key_mode), value)
                .apply()
    
    val isDefaultMode: Boolean
        get() = mode == context.resources.getStringArray(R.array.pref_entry_values_mode)[0]
    
    val isCompatMode: Boolean
        get() = mode == context.resources.getStringArray(R.array.pref_entry_values_mode)[1]
    
    var smsHandleWays: MutableSet<String>
        get() = sp.getStringSet(context.getString(R.string.pref_key_sms_handle_ways),
                mutableSetOf(*context.resources.getStringArray(R.array.pref_def_values_sms_handle_ways)))
        set(value) = sp.edit()
                .putStringSet(context.getString(R.string.pref_key_sms_handle_ways), LinkedHashSet(value))
                .apply()
    
    var expressEnable: Boolean
        get() = sp.getBoolean(context.getString(R.string.pref_key_express),
                context.resources.getBoolean(R.bool.pref_def_value_express))
        set(value) = sp.edit()
            .putBoolean(context.getString(R.string.pref_key_express), value)
            .apply()
    
    var isDeveloperMode: Boolean
        get() = sp.getBoolean(context.getString(R.string.pref_key_about),
                context.resources.getBoolean(R.bool.pref_def_value_about))
        set(value) = sp.edit()
            .putBoolean(context.getString(R.string.pref_key_about), value)
            .apply()
    
    /*---------开发者页面----------*/
    
    var smsKeyword: String
        get() = sp.getString(context.getString(R.string.pref_key_sms_contains),
                context.getString(R.string.pref_def_value_sms_contains))
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_sms_contains), value)
            .apply()
    
    var smsRegex: String
        get() = sp.getString(context.getString(R.string.pref_key_regexp),
                context.getString(R.string.pref_def_value_regexp))
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_regexp), value)
            .apply()
    
    var expressKeyword: String
        get() = sp.getString(context.getString(R.string.pref_key_express_sms_contains),
                context.getString(R.string.pref_def_value_express_sms_contains))
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_express_sms_contains), value)
            .apply()
    
    var expressRegex: String
        get() = sp.getString(context.getString(R.string.pref_key_express_regexp),
                context.getString(R.string.pref_def_value_express_regexp))
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_express_regexp), value)
            .apply()
    
    /*-------其他-------*/
    
    var defaultSmsApp: String
        get() = sp.getString(context.getString(R.string.pref_key_def_sms_app),
                context.getString(R.string.pref_key_value_def_sms_app))
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_def_sms_app), value)
            .apply()
    
    var isFirstUseClearSms: Boolean
        get() = sp.getBoolean(context.getString(R.string.pref_key_first_use_clear_code_sms), true)
        set(value) = sp.edit()
            .putBoolean(context.getString(R.string.pref_key_first_use_clear_code_sms), value)
            .apply()
    
    var isFirstRun: Boolean
        get() = sp.getBoolean(context.getString(R.string.pref_key_first_launch), true)
        set(value) = sp.edit()
            .putBoolean(context.getString(R.string.pref_key_first_launch), value)
            .apply()
}