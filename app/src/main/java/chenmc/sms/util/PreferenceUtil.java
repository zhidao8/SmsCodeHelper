package chenmc.sms.util;

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
    
    public void put(String key, String value) {
        edit().put(key, value).apply();
    }
    
    public void put(String key, Set<String> value) {
        edit().put(key, value).apply();
    }
    
    public void put(String key, boolean value) {
        edit().put(key, value).apply();
    }
    
    public void put(String key, float value) {
        edit().put(key, value).apply();
    }
    
    public void put(String key, long value) {
        edit().put(key, value).apply();
    }
    
    public void put(String key, int value) {
        edit().put(key, value).apply();
    }
    
    public void remove(String key) {
        edit().remove(key).apply();
    }
    
    private Editor mEditor;
    
    public Editor edit() {
        if (mEditor == null) mEditor = new Editor();
        return mEditor;
    }
    
    public class Editor {
        private SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        
        private Editor() {}
    
        public Editor put(String key, String value) {
            if (value == null) {
                remove(key);
            } else {
                mEditor.putString(key, value);
            }
            return this;
        }
    
        public Editor put(String key, Set<String> value) {
            if (value == null) {
                remove(key);
            } else {
                mEditor.putStringSet(key, value);
            }
            return this;
        }
    
        public Editor put(String key, boolean value) {
            mEditor.putBoolean(key, value);
            return this;
        }
    
        public Editor put(String key, float value) {
            mEditor.putFloat(key, value);
            return this;
        }
    
        public Editor put(String key, long value) {
            mEditor.putLong(key, value);
            return this;
        }
    
        public Editor put(String key, int value) {
            mEditor.putInt(key, value);
            return this;
        }
    
        public Editor remove(String key) {
            mEditor.remove(key);
            return this;
        }
        
        public void apply() {
            mEditor.apply();
        }
    }
}
