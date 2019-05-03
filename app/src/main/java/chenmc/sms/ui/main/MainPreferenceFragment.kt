package chenmc.sms.ui.main

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Telephony
import android.view.*
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.*
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.transaction.SmsAnalyzer
import chenmc.sms.transaction.SmsHandlerExecutor
import chenmc.sms.transaction.service.SmsObserverService
import chenmc.sms.ui.main.customrules.CustomRulesFragment
import chenmc.sms.ui.main.codesmsclear.CodeSmsClearFragment
import chenmc.sms.util.ActivityUtil
import chenmc.sms.util.ToastUtil

/**
 * Created by 明明 on 2017/8/9.
 */

class MainPreferenceFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener {

    private val mHandler = Handler(Looper.myLooper()) { msg ->
        when (msg.what) {
            WHAT_SHOW_APP_DETAIL -> {
                ToastUtil.showSingletonToast(R.string.click_permission_allow_permission, Toast.LENGTH_LONG)
                // 显示应用详情
                activity?.run {
                    ActivityUtil.showApplicationDetail(this, msg.data.getInt(DATA_REQUEST_CODE))
                } ?: false
            }
            WHAT_REQUEST_RECEIVE_SMS -> {
                requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS), REQUEST_PERMISSIONS_RECEIVE_SMS)
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.apply {
            setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_main)
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
            summary = summarySB.toString()
            onPreferenceChangeListener = listener
        }

        findPreference(getString(R.string.pref_key_test_rules)).apply {
            onPreferenceChangeListener = listener
        }
    }

    override fun onStart() {
        super.onStart()

        activity?.let { activity ->
            when (activity) {
                is AppCompatActivity -> {
                    activity.supportActionBar?.run {
                        setTitle(R.string.app_name)
                        setDisplayHomeAsUpEnabled(false)
                    }
                }
                else -> {
                    activity.actionBar?.run {
                        setTitle(R.string.app_name)
                        setDisplayHomeAsUpEnabled(false)
                    }
                }
            }
        }
        setHasOptionsMenu(true)

        // 关闭应用总开关后的操作
        AppPreference.isAppFeaturesEnabled.let { enabled ->
            findPreference(getString(R.string.pref_key_mode)).isEnabled = enabled
            findPreference(getString(R.string.pref_key_sms_handle_ways)).isEnabled = enabled
            findPreference(getString(R.string.pref_key_express)).isEnabled = enabled
        }

        showPermissionFirstRun()
    }

    // 第一次运行应用的一些初始化操作
    private fun showPermissionFirstRun() {
        val pref = findPreference(getString(R.string.pref_key_mode)) as ListPreference
        val values = resources.getStringArray(R.array.pref_entry_values_mode)
        val permission = when (pref.value) {
            values[0] -> Pair(Manifest.permission.RECEIVE_SMS, REQUEST_PERMISSIONS_RECEIVE_SMS)
            values[1] -> Pair(Manifest.permission.READ_SMS, REQUEST_PERMISSIONS_READ_SMS)
            else -> null
        }
        val action: () -> Unit = {
            permission?.let { (permission, requestCode) ->
                requestPermissionIfNeed(permission, requestCode)
            }
        }
        if (AppPreference.isFirstRun) {
            showPermissionExplanation(action)
            // 保存一个 false 值标记应用已经运行过了
            AppPreference.isFirstRun = false
        } else {
            action()
        }
    }

    private fun requestPermissionIfNeed(permission: String, requestCode: Int, delay: Long = 0) {
        activity?.let { activity ->
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(permission, requestCode, delay)
            }
        }
    }

    private fun requestPermission(permission: String, requestCode: Int, delay: Long = 0) {
        val action: () -> Unit = {
            requestPermissions(arrayOf(permission), requestCode)
        }
        if (delay >= 0) {
            mHandler.postDelayed(action, delay)
        } else {
            action()
        }
    }

    // 显示权限说明
    private fun showPermissionExplanation(action: (() -> Unit)?) {
        activity?.let { activity ->
            // 第一次运行显示应用的权限说明
            val dialogView = activity.layoutInflater.inflate(
                R.layout.dialog_permission, view as ViewGroup?, false
            )
            val webView = dialogView.findViewById<WebView>(R.id.webView)

            webView.setBackgroundColor(Color.TRANSPARENT)
            webView.loadUrl("file:///android_asset/permission.html")

            AlertDialog.Builder(activity)
                .setView(dialogView)
                .setTitle(getString(R.string.pref_permission))
                .setPositiveButton(R.string.ok) { _, _ ->
                    if (action != null) {
                        action()
                    }
                }
                .setCancelable(false)
                .create()
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_permission_explanation -> {
                showPermissionExplanation(null)
                true
            }
            R.id.menu_about -> {
                replaceFragment(AboutFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.let { activity ->
            SmsObserverService.stopThisService(activity)

            if (AppPreference.isCompatMode && AppPreference.isAppFeaturesEnabled) {
                /*
                 * 如果短信处理方式是通过监听短信数据库变化读取短信内容（兼容模式），并且没有禁用应用所有功能，
                 * 自动启动短信数据库监听服务
                 * Android P 以上因后台限制，不支持兼容模式
                 */
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                    SmsObserverService.startThisService(activity)
                else {
                    ToastUtil.showSingletonToast(R.string.p_not_support_compat_mode, Toast.LENGTH_LONG)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PERMISSIONS_RECEIVE_SMS -> {
                for ((i, result) in grantResults.withIndex()) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ToastUtil.showSingletonToast(R.string.no_receive_sms_permission_and_work_abnormal, Toast.LENGTH_LONG)

                        if (!shouldShowRequestPermissionRationale(permissions[i])) {
                            // 请求权限时用户选择了不再提醒，{@link DELAY_SHOW_APP_DETAIL} 毫秒后显示应用详情，
                            // 引导用户再次授权
                            val message = mHandler.obtainMessage(WHAT_SHOW_APP_DETAIL)
                            message.data = Bundle(1).apply {
                                putInt(DATA_REQUEST_CODE, REQUEST_PERMISSIONS_RECEIVE_SMS)
                            }
                            mHandler.sendMessageDelayed(message, DELAY_SHOW_APP_DETAIL)
                        }
                        break
                    }
                }
            }
            REQUEST_PERMISSIONS_READ_SMS -> {
                for ((i, result) in grantResults.withIndex()) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ToastUtil.showSingletonToast(R.string.no_read_sms_permission_and_work_abnormal, Toast.LENGTH_LONG)

                        if (!shouldShowRequestPermissionRationale(permissions[i])) {
                            // 请求权限时用户选择了不再提醒，{@link DELAY_SHOW_APP_DETAIL} 毫秒后显示应用详情，
                            // 引导用户再次授权
                            val message = mHandler.obtainMessage(WHAT_SHOW_APP_DETAIL)
                            message.data = Bundle(1).apply {
                                putInt(DATA_REQUEST_CODE, REQUEST_PERMISSIONS_READ_SMS)
                            }
                            mHandler.sendMessageDelayed(message, DELAY_SHOW_APP_DETAIL)
                        }
                        break
                    }
                }
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            getString(R.string.pref_key_custom_rules) -> {
                // 自定义规则
                replaceFragment(CustomRulesFragment())
                true
            }
            getString(R.string.pref_key_developer_mode) -> {
                // 开发者模式
                replaceFragment(AdvancedPreferenceFragment())
                true
            }
            getString(R.string.pref_key_clear_code_sms) -> {
                // 清除所有验证码短信
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !ActivityUtil.isSmsDefaultApp) {
                    // 如果安卓版本大于等于 4.4，并且当前应用不是默认启动应用

                    activity?.let { activity ->
                        // 先将当前默认启动应用保存起来
                        AppPreference.defaultSmsApp = Settings.Secure.getString(
                            activity.contentResolver, "sms_default_application"
                        ) ?: ""

                        // Android 4.4 及以上的版本中，需要设置默认短信应用才能删除短信
                        val confirmListener = DialogInterface.OnClickListener { _, which ->
                            // 请求更改默认短信应用
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                                    .putExtra(
                                        Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                        activity.packageName
                                    )
                            } else {
                                null
                            }
                            if (intent?.resolveActivity(activity.packageManager) != null) {
                                startActivityForResult(
                                    intent,
                                    REQUEST_CHANGE_DEFAULT_SMS_APP
                                )
                            }

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
                    }
                } else {
                    replaceFragment(CodeSmsClearFragment())
                }
                true
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {

        when (preference.key) {
            getString(R.string.pref_key_mode) -> {
                val prefMode = preference as ListPreference
                val possibleValues = resources.getStringArray(R.array.pref_entry_values_mode)
                when (newValue as String) {
                    possibleValues[0] -> {
                        requestPermissionIfNeed(Manifest.permission.RECEIVE_SMS, REQUEST_PERMISSIONS_RECEIVE_SMS, -1)
                    }
                    possibleValues[1] -> {
                        AlertDialog.Builder(activity)
                            .setTitle(resources.getStringArray(R.array.pref_entries_mode)[1])
                            .setMessage(R.string.pref_compat_mode_desc)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                // 请求权限
                                requestPermissionIfNeed(Manifest.permission.READ_SMS, REQUEST_PERMISSIONS_READ_SMS, -1)
                            }
                            .create()
                            .show()
                    }
                }
                prefMode.summary = prefMode.entries[newValue.toInt()]
                return true
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
                return true
            }
            getString(R.string.pref_key_test_rules) -> {
                context?.let { context ->
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
                        ToastUtil.showSingletonShortToast(R.string.can_not_analyse_sms)
                    }
                }
                return true
            }
        }

        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PERMISSIONS_RECEIVE_SMS -> {
                activity?.let { activity ->
                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.RECEIVE_SMS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ToastUtil.showSingletonToast(R.string.no_receive_sms_permission_and_work_abnormal, Toast.LENGTH_LONG)
                    }
                }
            }
            REQUEST_PERMISSIONS_READ_SMS -> {
                activity?.let { activity ->
                    if (ActivityCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.READ_SMS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ToastUtil.showSingletonToast(R.string.no_read_sms_permission_and_work_abnormal, Toast.LENGTH_LONG)
                    }
                }
            }
            REQUEST_CHANGE_DEFAULT_SMS_APP -> {
                if (resultCode == Activity.RESULT_OK) {
                    replaceFragment(CodeSmsClearFragment())
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun replaceFragment(fragment: Fragment) {
        fragmentManager?.run {
            beginTransaction()
                .setCustomAnimations(
                    R.animator.fragment_enter, R.animator.fragment_exit,
                    R.animator.fragment_pop_enter, R.animator.fragment_pop_exit
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
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
        private const val DELAY_SHOW_APP_DETAIL = 3000L
        private const val WHAT_SHOW_APP_DETAIL = 0
        private const val WHAT_REQUEST_RECEIVE_SMS = 1

        private const val DATA_REQUEST_CODE = "REQUEST_CODE"
    }

}
