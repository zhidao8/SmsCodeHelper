package chenmc.sms.utils.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.util.Map;
import java.util.Set;

/**
 * Created by 明明 on 2017/7/21.
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
    
    public static PreferenceUtil init(Context context, String preferenceFileName) {
        return new PreferenceUtil(context, preferenceFileName);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defValue) {
        if (defValue instanceof Boolean) {
            Boolean a = mSharedPreferences.getBoolean(key, (Boolean) defValue);
            return (T) a;
        } else if (defValue instanceof Float) {
            Float a = mSharedPreferences.getFloat(key, (Float) defValue);
            return (T) a;
        } else if (defValue instanceof Integer) {
            Integer a = mSharedPreferences.getInt(key, (Integer) defValue);
            return (T) a;
        } else if (defValue instanceof Long) {
            Long a = mSharedPreferences.getLong(key, (Long) defValue);
            return (T) a;
        } else if (defValue instanceof String) {
            String a = mSharedPreferences.getString(key, (String) defValue);
            return (T) a;
        } else if (defValue instanceof Set) {
            Set<String> a = mSharedPreferences.getStringSet(key, (Set<String>) defValue);
            return (T) a;
        } else {
            return defValue;
        }
    }
    
    public Map<String, ?> getAll() {
        return mSharedPreferences.getAll();
    }
    
    public Editor edit() {
        return new Editor();
    }
    
    public class Editor {
        
        private SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        
        private Editor() {
        }
        
        @SuppressWarnings("unchecked")
        public <T> Editor put(String key, T value) {
            if (value instanceof Boolean) {
                mEditor.putBoolean(key, (Boolean) value);
            } else if (value instanceof Float) {
                mEditor.putFloat(key, ((Float) value));
            } else if (value instanceof Integer) {
                mEditor.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                mEditor.putLong(key, (Long) value);
            } else if (value instanceof String) {
                mEditor.putString(key, (String) value);
            } else if (value instanceof Set) {
                mEditor.putStringSet(key, (Set<String>) value);
            }
            return this;
        }
        
        public void apply() {
            mEditor.apply();
        }
    }
}
