package chenmc.sms.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.preference.ListPreference
import android.preference.MultiSelectListPreference
import android.preference.Preference
import android.preference.PreferenceScreen
import android.provider.Settings
import android.provider.Telephony
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.Toast
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.transaction.SmsAnalyzer
import chenmc.sms.transaction.SmsHandlerExecutor
import chenmc.sms.transaction.service.SmsObserverService
import chenmc.sms.ui.app.PermissionPreferenceFragment
import chenmc.sms.ui.interfaces.IOnRequestPermissionsResult

/**
 * Created by 明明 on 2017/8/9.
 */

class MainPreferenceFragment : PermissionPreferenceFragment(),
        Preference.OnPreferenceChangeListener, IOnRequestPermissionsResult {

    private val mHandler = object : Handler(Looper.myLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.what) {
                WHAT_SHOW_APP_DETAIL -> {
                    Toast.makeText(activity,
                            R.string.click_permission_allow_permission,
                            Toast.LENGTH_LONG).show()
                    // 显示应用详情
                    showApplicationDetail(REQUEST_PERMISSIONS_RECEIVE_SMS)
                }
                WHAT_REQUEST_PERMISSION ->
                    // 请求权限
                    requestPermissions(REQUEST_PERMISSIONS_RECEIVE_SMS,
                            arrayOf(Manifest.permission.RECEIVE_SMS), this@MainPreferenceFragment)
            }
        }
    }

    // 判断当前应用是不是短信默认应用
    private val isSmsDefaultApp: Boolean
        get() {
            return Settings.Secure.getString(
                    activity.contentResolver, "sms_default_application") == activity.packageName
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preference_main)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_permission_explanation -> {
                showPermissionExplanation()
                return true
            }
            R.id.menu_about -> {
                replaceFragment(AboutFragment())
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        SmsObserverService.stopThisService(activity)

        val context = activity
        if (AppPreference.isCompatMode && AppPreference.isAppFeaturesEnabled) {
            /*
             * 如果短信处理方式是通过监听短信数据库变化读取短信内容（兼容模式），并且没有禁用应用所有功能，
             * 自动启动短信数据库监听服务
             * Android P 以上不支持兼容模式
             */
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                SmsObserverService.startThisService(context)
            else {
                Toast.makeText(activity, R.string.p_not_support_compat_mode, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val actionBar = activity.actionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name)
            actionBar.setDisplayHomeAsUpEnabled(false)
        }
        // Fragment 有菜单项
        setHasOptionsMenu(true)

        // 关闭应用总开关后的操作
        val isAppFeaturesEnabled = AppPreference.isAppFeaturesEnabled
        findPreference(getString(R.string.pref_key_mode)).isEnabled = isAppFeaturesEnabled
        findPreference(getString(R.string.pref_key_sms_handle_ways)).isEnabled = isAppFeaturesEnabled
        findPreference(getString(R.string.pref_key_express)).isEnabled = isAppFeaturesEnabled
    }

    // 第一次运行应用的一些初始化操作
    private fun showPermissionFirstRun() {
        if (AppPreference.isFirstRun) {
            showPermissionExplanation()
            // 保存一个 false 值标记应用已经运行过了
            AppPreference.isFirstRun = false
        } else {
            // 不是第一次运行应用，直接请求权限
            mHandler.sendEmptyMessage(WHAT_REQUEST_PERMISSION)
        }
    }

    // 显示权限说明
    @SuppressLint("InflateParams")
    private fun showPermissionExplanation() {
        // 第一次运行显示应用的权限说明
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_webview, null)
        val webView = dialogView.findViewById<View>(R.id.dialog_webView) as WebView
        // 设置 WebView 背景透明
        webView.setBackgroundColor(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val layoutParams = webView.layoutParams as LinearLayout.LayoutParams
            layoutParams.topMargin = this.resources.getDimensionPixelSize(R.dimen.activity_padding)
            webView.layoutParams = layoutParams
        }
        webView.loadUrl("file:///android_asset/permission.html")

        AlertDialog.Builder(activity)
            .setView(dialogView)
            .setTitle(getString(R.string.pref_permission))
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.cancel()
                // 点击确定后，请求权限
                mHandler.sendEmptyMessage(WHAT_REQUEST_PERMISSION)
            }
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onPermissionGranted(requestCode: Int, grantedPermissions: Array<String>) {
        // 权限被允许，不做任何操作
    }

    override fun onPermissionDenied(requestCode: Int, deniedPermissions: Array<String>,
                                    deniedAlways: BooleanArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS_RECEIVE_SMS -> {
                Toast.makeText(activity,
                        R.string.no_receive_sms_permission_and_work_abnormal,
                        Toast.LENGTH_LONG).show()
                if (deniedAlways[0]) {
                    // 请求权限时用户选择了不再提醒，{@link SHOW_APP_DETAIL_DELAY} 毫秒后显示应用详情，
                    // 引导用户再次授权
                    mHandler.sendEmptyMessageDelayed(WHAT_SHOW_APP_DETAIL, SHOW_APP_DETAIL_DELAY.toLong())
                }
            }
            REQUEST_PERMISSIONS_READ_SMS -> {
                Toast.makeText(activity,
                        R.string.no_read_sms_permission_and_work_abnormal,
                        Toast.LENGTH_LONG).show()
                if (deniedAlways[0]) {
                    // 请求权限时用户选择了不再提醒，{@link SHOW_APP_DETAIL_DELAY} 毫秒后显示应用详情，
                    // 引导用户再次授权
                    mHandler.sendEmptyMessageDelayed(WHAT_SHOW_APP_DETAIL, SHOW_APP_DETAIL_DELAY.toLong())
                }
            }
        }
    }

    private fun init() {
        // 显示权限说明
        showPermissionFirstRun()
        // 初始化 Preference
        initPreference()
    }

    private fun initPreference() {
        val listener = this

        // 模式
        (findPreference(getString(R.string.pref_key_mode)) as ListPreference).apply {
            summary = entries[value.toInt()]
            onPreferenceChangeListener = listener
        }

        // 验证码处理方式
        (findPreference(getString(R.string.pref_key_sms_handle_ways)) as MultiSelectListPreference).apply {
            // 这里要重新创建一个对象而不能简单地引用，否则在后面给 MultiSelectListPreference
            // setValue() 会出错。感兴趣可以看一下 setValue() 的源代码
            val valuesSet = LinkedHashSet(values)
            val summarySB = StringBuilder()
            for (value in valuesSet) {
                summarySB.append(entries[Integer.valueOf(value)])
                    .append(ENTRIES_CONNECTOR)
            }
            // 删除 summarySB 中后面多余的 linkString
            if (summarySB.length >= ENTRIES_CONNECTOR.length) {
                summarySB.delete(summarySB.length - ENTRIES_CONNECTOR.length, summarySB.length)
            }
            this.summary = summarySB.toString()
            this.onPreferenceChangeListener = listener
        }

        findPreference(getString(R.string.pref_key_test_rules)).apply {
            onPreferenceChangeListener = listener
        }
    }

    override fun onResume() {
        super.onResume()
        val isDeveloperMode = AppPreference.isDeveloperMode
        var prefDeveloper: Preference? = findPreference(getString(R.string.pref_key_developer_mode))

        if (prefDeveloper == null && isDeveloperMode) {
            prefDeveloper = Preference(activity).apply {
                this.key = this.context.getString(R.string.pref_key_developer_mode)
                this.title = this.context.getString(R.string.pref_advanced)
                // 将“开发者模式”插入到现在“清除验证码短信”的位置的下面
                this.order = findPreference(getString(R.string.pref_key_clear_code_sms)).order + 1
            }
            // 开发者模式不存在，并且当前处于开发者模式
            preferenceScreen.addPreference(prefDeveloper)
        } else if (prefDeveloper != null && !isDeveloperMode) {
            // 开发者模式存在，并且当前不处于开发者模式
            preferenceScreen.removePreference(prefDeveloper)
        }
    }

    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen,
                                       preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.pref_key_custom_rules) -> {
                // 自定义规则
                replaceFragment(CustomRulesFragment())
                return true
            }
            getString(R.string.pref_key_developer_mode) -> {
                // 开发者模式
                replaceFragment(AdvancedPreferenceFragment())
                return true
            }
            getString(R.string.pref_key_clear_code_sms) -> {
                // 清除所有验证码短信
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !isSmsDefaultApp) {
                    // 如果安卓版本大于等于 4.4，并且当前应用不是默认启动应用

                    // 先将当前默认启动应用保存起来
                    AppPreference.defaultSmsApp = Settings.Secure.getString(
                            activity.contentResolver, "sms_default_application") ?: ""

                    // Android 4.4 及以上的版本中，需要设置默认短信应用才能删除短信
                    val confirmListener = DialogInterface.OnClickListener { _, which ->
                        // 请求更改默认短信应用
                        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                                .putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                        activity.packageName)
                        } else {
                            null
                        }
                        if (intent?.resolveActivity(activity.packageManager) != null)
                            startActivityForResult(intent,
                                    REQUEST_CHANGE_DEFAULT_SMS_APP)

                        // 如果用户指定了不再提示，记住选择
                        if (which == DialogInterface.BUTTON_NEUTRAL) {
                            AppPreference.isFirstUseClearSms = false
                        }
                    }

                    // 如果用户没有指定不再提示，显示对话框提示用户
                    if (AppPreference.isFirstUseClearSms) {
                        AlertDialog.Builder(activity)
                            .setTitle(R.string.pref_permission)
                            .setMessage(R.string.need_set_default_mms)
                            .setNegativeButton(R.string.cancel, null)
                            .setNeutralButton(R.string.i_know_not_prompt_again, confirmListener)
                            .setPositiveButton(R.string.ok, confirmListener)
                            .setCancelable(false)
                            .create()
                            .show()
                    } else {
                        confirmListener.onClick(null, DialogInterface.BUTTON_POSITIVE)
                    }
                } else {
                    replaceFragment(CodeSmsClearFragment())
                }
                return true
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {

        return when (preference.key) {
            getString(R.string.pref_key_mode) -> {
                val prefMode = preference as ListPreference
                val possibleValues = activity.resources.getStringArray(R.array.pref_entry_values_mode)
                when (newValue as String) {
                    possibleValues[0] -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(REQUEST_PERMISSIONS_RECEIVE_SMS, arrayOf(Manifest.permission.RECEIVE_SMS), this)
                        }
                    }
                    possibleValues[1] -> {
                        AlertDialog.Builder(activity)
                            .setTitle(activity.resources.getStringArray(R.array.pref_entries_mode)[1])
                            .setMessage(R.string.pref_compat_mode_desc)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                // 请求权限
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(REQUEST_PERMISSIONS_READ_SMS, arrayOf(Manifest.permission.READ_SMS), this)
                                }
                            }
                            .create()
                            .show()
                    }
                }
                prefMode.summary = prefMode.entries[newValue.toInt()]
                true
            }
            getString(R.string.pref_key_sms_handle_ways) -> {
                @Suppress("UNCHECKED_CAST")
                val values = newValue as MutableSet<String>
                val iterator = values.iterator()
                val sbSummary = StringBuilder()
                if (iterator.hasNext()) {
                    while (true) {
                        val i = Integer.valueOf(iterator.next())
                        sbSummary.append((preference as MultiSelectListPreference).entries[i])
                        if (iterator.hasNext()) {
                            sbSummary.append(ENTRIES_CONNECTOR)
                        } else {
                            break
                        }
                    }
                    preference.setSummary(sbSummary.toString())
                } else {
                    // 用户没有选择任何选项，不显示设置项的 summary
                    preference.summary = null
                }
                true
            }
            getString(R.string.pref_key_test_rules) -> {
                activity?.let { context ->
                    val sms = newValue.toString()

                    // 创建一个短信执行器
                    val executor = SmsHandlerExecutor(context)

                    // 分析文本内容是否符合验证码短信或取件码短信的格式
                    val smsAnalyzer = SmsAnalyzer(context)
                    val verificationCodeSms = smsAnalyzer.analyseVerificationSms(sms)
                    val expressCodeSms = smsAnalyzer.analyseExpressSms(sms)

                    if (verificationCodeSms != null || AppPreference.expressEnable && expressCodeSms != null) {
                        executor.execute(sms)
                    } else {
                        Toast.makeText(context, R.string.can_not_analyse_sms, Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PERMISSIONS_RECEIVE_SMS -> if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity,
                        R.string.no_receive_sms_permission_and_work_abnormal,
                        Toast.LENGTH_LONG).show()
            }
            REQUEST_PERMISSIONS_READ_SMS -> if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity, R.string.no_read_sms_permission_and_work_abnormal, Toast.LENGTH_LONG).show()
            }
            REQUEST_CHANGE_DEFAULT_SMS_APP -> if (resultCode == Activity.RESULT_OK) {
                replaceFragment(CodeSmsClearFragment())
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun replaceFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit,
                    R.animator.fragment_pop_enter, R.animator.fragment_pop_exit)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        // 请求获取接收短信权限的请求码
        private const val REQUEST_PERMISSIONS_RECEIVE_SMS = 0

        // 请求获取查看短信权限的请求码
        private const val REQUEST_PERMISSIONS_READ_SMS = 1

        // 请求更改默认短信的请求码
        private const val REQUEST_CHANGE_DEFAULT_SMS_APP = 2

        private const val ENTRIES_CONNECTOR = " + "

        // Handler Message what
        private const val SHOW_APP_DETAIL_DELAY = 3000
        private const val WHAT_SHOW_APP_DETAIL = 0
        private const val WHAT_REQUEST_PERMISSION = 1
    }
}
