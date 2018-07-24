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
    
    /*---------高级设置页面----------*/

    const val defaultProviderRegex: String = "(【.+?】|\\[.+?\\])"

    const val defaultSmsKeyword: String = "(验证|確認|驗證|校验|动态|确认|随机|激活|兑换|认证|交易|授权|操作|提取|安全|登陆|登录|verification |confirmation )(码|碼|代码|代碼|号码|密码|code|コード)|口令|Steam"

    const val defaultSmsRegex: String = "((?<!\\d|(联通|尾号|金额|支付|末四位)(为)?)(G-)?\\d{4,8}(?!\\d|年|账|动画))|((?<=(code is|码|碼|コードは)[是为為]?[『「【〖（(：: ]?)(?<![a-zA-Z0-9])[a-zA-Z0-9]{4,8}(?![a-zA-Z0-9]))|((?<!\\w)\\w{4,8}(?!\\w)(?= is your))"

    const val defaultExpressKeyword: String = "快递|快件|单号|订单|包裹"

    const val defaultExpressRegex: String = "((?<=(取件码|密码|货码|暗号|请凭|号码)[『「【〖（(:：“\\\" ]?)(?<![a-zA-Z0-9-])[a-zA-Z0-9-]{4,10}(?![a-zA-Z0-9-]))|货号[0-9]{1,}"

    const val defaultExpressPlaceRegex: String = "(?<=到|至|速来|[取|提]货地址：)\\w{3,}?(?=(领取|取件|取货)|[,.)，。）])"

    var smsKeyword: String
        get() = sp.getString(context.getString(R.string.pref_key_sms_contains), "")
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_sms_contains), value)
            .apply()
    
    var smsRegex: String
        get() = sp.getString(context.getString(R.string.pref_key_regexp), "")
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_regexp), value)
            .apply()
    
    var expressKeyword: String
        get() = sp.getString(context.getString(R.string.pref_key_express_sms_contains), "")
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_express_sms_contains), value)
            .apply()
    
    var expressRegex: String
        get() = sp.getString(context.getString(R.string.pref_key_express_regexp), "")
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_express_regexp), value)
            .apply()

    var expressPlaceRegex: String
        get() = sp.getString(context.getString(R.string.pref_key_express_place_regexp), "")
        set(value) = sp.edit()
            .putString(context.getString(R.string.pref_key_express_place_regexp), value)
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