package chenmc.sms.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
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
import android.view.LayoutInflater;
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
import chenmc.sms.ui.activities.PreferenceActivity;
import chenmc.sms.ui.interfaces.OnRequestPermissionsResultListener;
import chenmc.sms.ui.view.AboutPreference;
import chenmc.sms.ui.view.DeveloperPreference;
import chenmc.sms.utils.DataUpdater;
import chenmc.sms.utils.database.PrefKey;
import chenmc.sms.utils.database.PreferenceUtil;

/**
 * Created by 明明 on 2017/8/9.
 */

public class MainPreferenceFragment extends PermissionPreferenceFragment implements
    Preference.OnPreferenceChangeListener,
    OnRequestPermissionsResultListener {
    
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
                    
                    showApplicationDetail(REQUEST_PERMISSIONS_RECEIVE_SMS);
                    break;
                case WHAT_REQUEST_PERMISSION:
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
    
        showPermissionAtFirstRun();
        
        init();
    }
    
    private void showPermissionAtFirstRun() {
        PreferenceUtil preferenceUtil = PreferenceUtil.init(getActivity());
        boolean firstRun = preferenceUtil.get(PrefKey.KEY_FIRST_LAUNCH, true);
        if (firstRun) {
            @SuppressLint("InflateParams")
            View dialogView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_permission, null);
            final WebView webView = (WebView) dialogView.findViewById(R.id.dialog_webView);
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
                        // 请求权限
                        mHandler.sendEmptyMessage(WHAT_REQUEST_PERMISSION);
                    }
                })
                .setCancelable(false)
                .create()
                .show();
            
            preferenceUtil.edit()
                .put(PrefKey.KEY_FIRST_LAUNCH, false)
                .apply();
        } else {
            // 请求权限
            mHandler.sendEmptyMessage(WHAT_REQUEST_PERMISSION);
        }
    }
    
    @Override
    public void onPermissionGranted(int requestCode, String[] grantedPermissions) {
        // ignored
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
                    mHandler.sendEmptyMessageDelayed(WHAT_SHOW_APP_DETAIL, 3000);
                }
                break;
            case REQUEST_PERMISSIONS_READ_SMS:
                Toast.makeText(getActivity(),
                    R.string.no_read_sms_permission_and_work_abnormal,
                    Toast.LENGTH_LONG).show();
                if (deniedAlways[0]) {
                    mHandler.sendEmptyMessageDelayed(WHAT_SHOW_APP_DETAIL, 3000);
                }
                break;
        }
    }
    
    private PreferenceActivity getThisActivity() {
        return (PreferenceActivity) getActivity();
    }
    
    private void init() {
        PreferenceUtil preferenceUtil = PreferenceUtil.init(getThisActivity());
        
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
        StringBuilder sbSummary = new StringBuilder();
        for (String value : valuesSet) {
            sbSummary.append(prefSmsHandleWays.getEntries()[Integer.valueOf(value)])
                .append(ENTRIES_CONNECTOR);
        }
        // 删除 sbSummary 中后面多余的 linkString
        if (sbSummary.length() >= ENTRIES_CONNECTOR.length()) {
            sbSummary.delete(sbSummary.length() - ENTRIES_CONNECTOR.length(), sbSummary.length());
        }
        prefSmsHandleWays.setSummary(sbSummary);
        
        prefSmsHandleWays.setOnPreferenceChangeListener(this);
        //endregion
        
        //region 关于
        AboutPreference prefAbout = (AboutPreference) findPreference(PrefKey.KEY_ABOUT);
        try {
            PackageInfo pi = getActivity().getPackageManager()
                .getPackageInfo(getActivity().getPackageName(), 0);
            prefAbout.setSummary(
                getString(R.string.pref_about_summary, pi.versionName, pi.versionCode)
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        prefAbout.setOnPreferenceChangeListener(this);
        //endregion
        
        // 如果开启了开发者模式
        if (prefAbout.isDeveloperMode()) {
            getPreferenceScreen().addPreference(new DeveloperPreference(getActivity()));
        }
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
        Preference preference) {
        switch (preference.getKey()) {
            case PrefKey.KEY_CUSTOM_RULES:
                getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit,
                        R.animator.fragment_pop_enter, R.animator.fragment_pop_exit)
                    .replace(R.id.fragment_container, new CodeMatchRulesFragment())
                    .addToBackStack(null)
                    .commit();
                return true;
            case PrefKey.KEY_DEVELOPER_MODE:
                getFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit,
                        R.animator.fragment_pop_enter, R.animator.fragment_pop_exit)
                    .replace(R.id.fragment_container, new DeveloperPreferenceFragment())
                    .addToBackStack(null)
                    .commit();
                break;
            case PrefKey.KEY_CLEAR_CODE_SMS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    !isMyAppLauncherDefault()) {
                    // 如果安卓版本大于等于 4.4，并且当前应用不是默认启动应用
                    
                    String smsDefaultApp = Settings.Secure.getString(
                        getActivity().getContentResolver(), "sms_default_application");
                    // 先将当前默认启动应用保存起来
                    PreferenceUtil.init(getActivity()).edit()
                        .put(PrefKey.KEY_SMS_DEFAULT_APPLICATION, smsDefaultApp)
                        .apply();
                    
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
                                .edit()
                                .put(PrefKey.FIRST_USE_CLEAR_CODE_SMS, false)
                                .apply();
                        }
                    };
                    
                    PreferenceUtil preferenceUtil = PreferenceUtil.init(getActivity());
                    if (preferenceUtil.get(PrefKey.FIRST_USE_CLEAR_CODE_SMS, true)) {
                        // 如果是第一次点击这个选项，则显示对话框
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
                    getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit,
                            R.animator.fragment_pop_enter, R.animator.fragment_pop_exit)
                        .replace(R.id.fragment_container, new CodeSmsClearFragment())
                        .addToBackStack(null)
                        .commit();
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
                break;
            case PrefKey.KEY_ABOUT:
                PreferenceScreen preferenceScreen = getPreferenceScreen();
                if ((boolean) newValue) {
                    preferenceScreen.addPreference(new DeveloperPreference(getActivity()));
                } else {
                    preferenceScreen.removePreference(findPreference(PrefKey.KEY_DEVELOPER_MODE));
                }
                break;
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
                break;
        }
        
        return true;
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
                    getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.animator.fragment_enter, R.animator.fragment_exit,
                            R.animator.fragment_pop_enter, R.animator.fragment_pop_exit)
                        .replace(R.id.fragment_container, new CodeSmsClearFragment())
                        .addToBackStack(null)
                        .commit();
                }
                break;
        }
        
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        SmsObserverService.stopThisService(getActivity());
        
        // 如果开启了兼容模式，重启后台进程
        if (PreferenceUtil.init(getActivity()).get(PrefKey.KEY_COMPAT_MODE,
            getResources().getBoolean(R.bool.pref_def_value_compat_mode))) {
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
