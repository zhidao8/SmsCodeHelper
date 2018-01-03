package chenmc.sms.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import chenmc.sms.code.helper.R;
import chenmc.sms.utils.storage.PrefKey;
import chenmc.sms.utils.storage.PreferenceUtil;

/**
 * 数据库升级帮助类
 *
 * @author 明 明
 *         Created on 2017-5-15.
 */

public class DataUpdater {
    private Context mContext;
    
    private static final String KEY_VERSION_CODE = "version_code";
    
    public DataUpdater(Context context) {
        mContext = context;
    }
    
    public void updateSharedPreference() {
        PreferenceUtil formerSP = PreferenceUtil.init(mContext, "FormerPreference");
        // 如果上一版本的 SharedPreferences 中的 version_code 跟当前版本的应用相同
        // 则不需要进行更新 SharedPreferences 中值
        if (formerSP.get(KEY_VERSION_CODE, -1) == getAppVersionCode()) {
            return;
        }
        
        PreferenceUtil userSP = PreferenceUtil.init(mContext);
        
        // 更新 KEY_SMS_CONTAINS
        checkByKey(formerSP, userSP, PrefKey.KEY_SMS_CONTAINS,
            mContext.getString(R.string.pref_def_value_sms_contains));
        
        // 更新 KEY_REGEXP
        checkByKey(formerSP, userSP, PrefKey.KEY_REGEXP,
            mContext.getString(R.string.pref_def_value_regexp));
        
        // 更新 KEY_EXPRESS_SMS_CONTAINS
        checkByKey(formerSP, userSP, PrefKey.KEY_EXPRESS_SMS_CONTAINS,
            mContext.getString(R.string.pref_def_value_express_sms_contains));
        
        // 更新 KEY_EXPRESS_REGEXP
        checkByKey(formerSP, userSP, PrefKey.KEY_EXPRESS_REGEXP,
            mContext.getString(R.string.pref_def_value_express_regexp));
        
        // 更新 version_code
        formerSP.put(KEY_VERSION_CODE, getAppVersionCode());
    }
    
    private void checkByKey(PreferenceUtil formerSP,
        PreferenceUtil userSP, String key, String currentValue) {
        String str = formerSP.get(key, "");
        // 检查用户是否有更改这个 key 的值，如果没有则马上更新这个值
        if (str.equals(userSP.get(key, (String) null))) {
            userSP.put(key, currentValue);
        }
        formerSP.put(key, currentValue);
    }
    
    // 返回当前程序版本号
    private int getAppVersionCode() {
        int versionCode = 0;
        try {
            // ---get the package info---
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (Exception e) {
            Log.e(getClass().getName(), "Exception: ", e);
        }
        return versionCode;
    }
}
