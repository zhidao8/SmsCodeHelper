package chenmc.sms.utils.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author 明明
 * Created on 2017/7/21.
 */

public class PreferenceUtil {
    
    private SharedPreferences mSharedPreferences;
    
    private PreferenceUtil(Context context, String preferenceFileName) {
        if (TextUtils.isEmpty(preferenceFileName)) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        } else {
            mSharedPreferences = context.getSharedPreferences(preferenceFileName, Context.MODE_PRIVATE);
        }
    }
    
    public static PreferenceUtil init(Context context) {
        return new PreferenceUtil(context, null);
    }
    
    public static PreferenceUtil init(Context context, String preferenceName) {
        return new PreferenceUtil(context, preferenceName);
    }
    
    /**
     * *************** get ******************
     */
    
    public String get(String key, String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }
    
    public boolean get(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }
    
    public float get(String key, float defValue) {
        return mSharedPreferences.getFloat(key, defValue);
    }
    
    public int get(String key, int defValue) {
        return mSharedPreferences.getInt(key, defValue);
    }
    
    public long get(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }
    
    public Set<String> get(String key, Set<String> defValues) {
        Set<String> stringSet = mSharedPreferences.getStringSet(key, defValues);
        return new TreeSet<>(stringSet);
    }
    
    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }
    
    /**
     * *************** put ******************
     */
    
    public PreferenceUtil put(String key, String value) {
        if (value == null) {
            remove(key);
        } else {
            mSharedPreferences.edit().putString(key, value).apply();
        }
        return this;
    }
    
    public PreferenceUtil put(String key, Set<String> value) {
        if (value == null) {
            remove(key);
        } else {
            mSharedPreferences.edit().putStringSet(key, value).apply();
        }
        return this;
    }
    
    public PreferenceUtil put(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).apply();
        return this;
    }
    
    public PreferenceUtil put(String key, float value) {
        mSharedPreferences.edit().putFloat(key, value).apply();
        return this;
    }
    
    public PreferenceUtil put(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
        return this;
    }
    
    public PreferenceUtil put(String key, int value) {
        mSharedPreferences.edit().putInt(key, value).apply();
        return this;
    }
    
    public PreferenceUtil remove(String key) {
        mSharedPreferences.edit().remove(key).apply();
        return this;
    }
}
