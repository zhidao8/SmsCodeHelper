package chenmc.sms.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import chenmc.sms.code.helper.R;
import chenmc.sms.transaction.SmsObserverService;
import chenmc.sms.ui.app.PermissionPreferenceFragment;
import chenmc.sms.ui.interfaces.IOnRequestPermissionsResult;
import chenmc.sms.ui.preference.DeveloperPreference;
import chenmc.sms.utils.DataUpdater;
import chenmc.sms.utils.storage.PrefKey;
import chenmc.sms.utils.storage.PreferenceUtil;

/**
 * Created by 明明 on 2017/8/9.
 */

public class MainPreferenceFragment extends PermissionPreferenceFragment implements
    Preference.OnPreferenceChangeListener,
    IOnRequestPermissionsResult {
    
    /*
     * 请求获取接收短信权限的请求码
     */
    private static final int REQUEST_PERMISSIONS_RECEIVE_SMS = 0;
    /*
     * 请求获取查看短信权限的请求码
     */
    private static final int REQUEST_PERMISSIONS_READ_SMS = 1;
    /*
     * 请求更改默认短信的请求码
     */
    private static final int REQUEST_CHANGE_DEFAULT_SMS_APP = 2;
    
    private static final String ENTRIES_CONNECTOR = " + ";
    
    private static final int SHOW_APP_DETAIL_DELAY = 3000;
    private static final int WHAT_SHOW_APP_DETAIL = 0;
    private static final int WHAT_REQUEST_PERMISSION = 1;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            switch (msg.what) {
                case WHAT_SHOW_APP_DETAIL:
                    Toast.makeText(getActivity(),
                        R.string.click_permission_allow_permission,
                        Toast.LENGTH_LONG).show();
                    // 显示应用详情
                    showApplicationDetail(REQUEST_PERMISSIONS_RECEIVE_SMS);
                    break;
                case WHAT_REQUEST_PERMISSION:
                    // 请求权限
                    requestPermissions(REQUEST_PERMISSIONS_RECEIVE_SMS,
                        new String[]{Manifest.permission.RECEIVE_SMS}, MainPreferenceFragment.this);
                    break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_main);
        // 更新数据
        DataUpdater dataUpdater = new DataUpdater(getActivity());
        dataUpdater.updateSharedPreference();
    
        showPermissionFirstRun();
        setHasOptionsMenu(true);
        init();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_permission_explanation:
                showPermissionExplanation();
                return true;
            case R.id.menu_about:
                replaceFragment(new AboutFragment());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_name);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }
    
    // 第一次运行应用的一些初始化操作
    private void showPermissionFirstRun() {
        PreferenceUtil preferenceUtil = PreferenceUtil.init(getActivity());
        boolean firstRun = preferenceUtil.get(PrefKey.KEY_FIRST_LAUNCH, true);
        if (firstRun) {
            showPermissionExplanation();
            // 保存一个 false 值标记应用已经运行过了
            preferenceUtil.put(PrefKey.KEY_FIRST_LAUNCH, false);
        } else {
            // 不是第一次运行应用，直接请求权限
            mHandler.sendEmptyMessage(WHAT_REQUEST_PERMISSION);
        }
    }
    
    // 显示权限说明
    private void showPermissionExplanation() {
        // 第一次运行显示应用的权限说明
        @SuppressLint("InflateParams")
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_permission, null);
        final WebView webView = (WebView) dialogView.findViewById(R.id.dialog_webView);
        // 设置 WebView 背景透明
        webView.setBackgroundColor(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) webView.getLayoutParams();
            layoutParams.topMargin = this.getResources().getDimensionPixelSize(R.dimen.activity_padding);
            webView.setLayoutParams(layoutParams);
        }
        webView.loadUrl("file:///android_asset/permission.html");
    
        new AlertDialog.Builder(getActivity())
            .setView(dialogView)
            .setTitle(getString(R.string.pref_permission))
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    // 点击确定后，请求权限
                    mHandler.sendEmptyMessage(WHAT_REQUEST_PERMISSION);
                }
            })
            .setCancelable(false)
            .create()
            .show();
    }
    
    @Override
    public void onPermissionGranted(int requestCode, String[] grantedPermissions) {
        // 权限被允许，不做任何操作
    }
    
    @Override
    public void onPermissionDenied(int requestCode, String[] deniedPermissions,
        boolean[] deniedAlways) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_RECEIVE_SMS:
                Toast.makeText(getActivity(),
                    R.string.no_receive_sms_permission_and_work_abnormal,
                    Toast.LENGTH_LONG).show();
                if (deniedAlways[0]) {
                    // 请求权限时用户选择了不再提醒，{@link SHOW_APP_DETAIL_DELAY} 毫秒后显示应用详情，
                    // 引导用户再次授权
                    mHandler.sendEmptyMessageDelayed(WHAT_SHOW_APP_DETAIL, SHOW_APP_DETAIL_DELAY);
                }
                break;
            case REQUEST_PERMISSIONS_READ_SMS:
                Toast.makeText(getActivity(),
                    R.string.no_read_sms_permission_and_work_abnormal,
                    Toast.LENGTH_LONG).show();
                if (deniedAlways[0]) {
                    // 请求权限时用户选择了不再提醒，{@link SHOW_APP_DETAIL_DELAY} 毫秒后显示应用详情，
                    // 引导用户再次授权
                    mHandler.sendEmptyMessageDelayed(WHAT_SHOW_APP_DETAIL, SHOW_APP_DETAIL_DELAY);
                }
                break;
        }
    }
    
    private void init() {
        PreferenceUtil preferenceUtil = PreferenceUtil.init(getActivity());
        
        //region 兼容模式
        if (preferenceUtil.get(PrefKey.KEY_COMPAT_MODE,
            getResources().getBoolean(R.bool.pref_def_value_compat_mode))) {
            // 如果兼容模式已开启，则启动监听短信数据库变化的 Service
            SmsObserverService.startThisService(getActivity());
        }
        findPreference(PrefKey.KEY_COMPAT_MODE).setOnPreferenceChangeListener(this);
        //endregion
        
        //region 验证码处理方式
        MultiSelectListPreference prefSmsHandleWays =
            (MultiSelectListPreference) findPreference(PrefKey.KEY_SMS_HANDLE_WAYS);
        // 这里要重新创建一个对象而不能简单地引用，否则在后面给 MultiSelectListPreference
        // setValue() 会出错。感兴趣可以看一下 setValue() 的源代码
        Set<String> valuesSet = new TreeSet<>(prefSmsHandleWays.getValues());
        if (valuesSet.size() == 0) {
            String[] defValues = getResources().getStringArray(R.array.pref_def_values_sms_handle_ways);
            Collections.addAll(valuesSet, defValues);
            prefSmsHandleWays.setValues(valuesSet);
        }
        StringBuilder summarySB = new StringBuilder();
        for (String value : valuesSet) {
            summarySB.append(prefSmsHandleWays.getEntries()[Integer.valueOf(value)])
                .append(ENTRIES_CONNECTOR);
        }
        // 删除 summarySB 中后面多余的 linkString
        if (summarySB.length() >= ENTRIES_CONNECTOR.length()) {
            summarySB.delete(summarySB.length() - ENTRIES_CONNECTOR.length(), summarySB.length());
        }
        prefSmsHandleWays.setSummary(summarySB);
        
        prefSmsHandleWays.setOnPreferenceChangeListener(this);
        //endregion
        
        findPreference(PrefKey.KEY_READ_AUTOMATICALLY).setOnPreferenceChangeListener(this);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        boolean isDeveloperMode = PreferenceUtil.init(getActivity())
            .get(PrefKey.KEY_ABOUT, getResources().getBoolean(R.bool.pref_def_value_about));
        Preference prefDeveloper = findPreference(PrefKey.KEY_DEVELOPER_MODE);
        if (prefDeveloper == null && isDeveloperMode) {
            // 开发者模式不存在，并且当前处于开发者模式
            DeveloperPreference preference = new DeveloperPreference(getActivity());
            // 将“开发者模式”插入到现在“清除验证码短信”的位置的下面
            preference.setOrder(findPreference(PrefKey.KEY_CLEAR_CODE_SMS).getOrder() + 1);
            getPreferenceScreen().addPreference(preference);
        } else if (prefDeveloper != null && !isDeveloperMode) {
            // 开发者模式存在，并且当前不处于开发者模式
            getPreferenceScreen().removePreference(prefDeveloper);
        }
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
        Preference preference) {
        switch (preference.getKey()) {
            case PrefKey.KEY_CUSTOM_RULES:
                // 自定义规则
                replaceFragment(new CustomRulesFragment());
                return true;
            case PrefKey.KEY_DEVELOPER_MODE:
                // 开发者模式
                replaceFragment(new DeveloperPreferenceFragment());
                return true;
            case PrefKey.KEY_CLEAR_CODE_SMS:
                // 清除所有验证码短信
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    !isMyAppLauncherDefault()) {
                    // 如果安卓版本大于等于 4.4，并且当前应用不是默认启动应用
                    
                    String smsDefaultApp = Settings.Secure.getString(
                        getActivity().getContentResolver(), "sms_default_application");
                    // 先将当前默认启动应用保存起来
                    PreferenceUtil.init(getActivity())
                        .put(PrefKey.KEY_SMS_DEFAULT_APPLICATION, smsDefaultApp);
                    
                    // Android 4.4 及以上的版本中，需要设置默认短信应用才能删除短信
                    DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 请求更改默认短信应用
                            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                                getActivity().getPackageName());
                            startActivityForResult(intent,
                                REQUEST_CHANGE_DEFAULT_SMS_APP);
                            
                            PreferenceUtil.init(getActivity())
                                .put(PrefKey.FIRST_USE_CLEAR_CODE_SMS, false);
                        }
                    };
                    
                    PreferenceUtil preferenceUtil = PreferenceUtil.init(getActivity());
                    if (preferenceUtil.get(PrefKey.FIRST_USE_CLEAR_CODE_SMS, true)) {
                        // 如果是第一次点击这个选项，则显示提示对话框
                        new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.pref_permission)
                            .setMessage(R.string.need_set_default_mms)
                            .setPositiveButton(R.string.ok, confirmListener)
                            .setCancelable(false)
                            .create()
                            .show();
                        
                    } else {
                        confirmListener.onClick(null, 0);
                    }
                } else {
                    replaceFragment(new CodeSmsClearFragment());
                }
                return true;
        }
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PrefKey.KEY_COMPAT_MODE:
                // Android 6.0 以上系统需要手动申请权限
                if ((Boolean) newValue) {
                    new AlertDialog.Builder(getActivity())
                        .setTitle(preference.getTitle())
                        .setMessage(R.string.if_copy_twice_not_compat_mode)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(REQUEST_PERMISSIONS_READ_SMS,
                            new String[]{Manifest.permission.READ_SMS}, this);
                        
                        requestPermissions(REQUEST_PERMISSIONS_READ_SMS, new String[]{Manifest.permission.READ_SMS}, this);
                    }
                }
                return true;
            case PrefKey.KEY_SMS_HANDLE_WAYS:
                @SuppressWarnings("unchecked")
                Set<String> values = (Set<String>) newValue;
                Iterator<String> iterator = values.iterator();
                StringBuilder sbSummary = new StringBuilder();
                if (iterator.hasNext()) {
                    while (true) {
                        Integer i = Integer.valueOf(iterator.next());
                        sbSummary.append(((MultiSelectListPreference) preference).getEntries()[i]);
                        if (iterator.hasNext()) {
                            sbSummary.append(ENTRIES_CONNECTOR);
                        } else {
                            break;
                        }
                    }
                    preference.setSummary(sbSummary.toString());
                } else {
                    // 如果用户没有选择任何选项，则设置默认选项
                    MultiSelectListPreference pref = (MultiSelectListPreference) preference;
                    String defValue = (String) pref.getEntryValues()[0];
                    values.add(defValue);
                    pref.setValues(values);
                    preference.setSummary(pref.getEntries()[Integer.valueOf(defValue)]);
                }
                return true;
            case PrefKey.KEY_READ_AUTOMATICALLY:
                if ((Boolean) newValue) {
                    new AlertDialog.Builder(getActivity())
                        .setTitle(preference.getTitle())
                        .setMessage(R.string.this_function_maybe_unusable)
                        .setPositiveButton(R.string.ok, null)
                        .create()
                        .show();
                }
                return true;
        }
        
        return false;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_RECEIVE_SMS:
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECEIVE_SMS) !=
                    PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(),
                        R.string.no_receive_sms_permission_and_work_abnormal,
                        Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_PERMISSIONS_READ_SMS:
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) !=
                    PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), R.string.no_read_sms_permission_and_work_abnormal, Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_CHANGE_DEFAULT_SMS_APP:
                if (resultCode == Activity.RESULT_OK) {
                    replaceFragment(new CodeSmsClearFragment());
                }
                break;
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void replaceFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
            .setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit,
                R.animator.fragment_pop_enter, R.animator.fragment_pop_exit)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        SmsObserverService.stopThisService(getActivity());
        
        // 如果开启了兼容模式，重启后台进程
        if (PreferenceUtil.init(getActivity()).get(PrefKey.KEY_COMPAT_MODE, false)) {
            SmsObserverService.startThisService(getActivity());
        }
    }
    
    /*
     * 判断当前应用是不是默认应用
     */
    private boolean isMyAppLauncherDefault() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        
        List<IntentFilter> filters = new ArrayList<>();
        filters.add(filter);
        
        List<ComponentName> preferredActivities = new ArrayList<>();
        String packageName = getActivity().getPackageName();
        
        PackageManager packageManager = getActivity().getPackageManager();
        packageManager.getPreferredActivities(filters, preferredActivities, packageName);
        
        return preferredActivities.size() > 0;
    }
    
}
