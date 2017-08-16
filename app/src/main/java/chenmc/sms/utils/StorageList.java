package chenmc.sms.utils;

import android.content.Context;
import android.os.storage.StorageManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by 明明 on 2017/7/21.
 */

public class StorageList {
    private static final String TAG = "StorageList";
    
    private StorageManager mStorageManager;
    private Method mMethodGetPaths;
    
    public StorageList(Context context) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            mMethodGetPaths = mStorageManager.getClass().getMethod("getVolumePaths");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    
    public String[] getVolumePaths() {
        String[] paths = null;
        try {
            mMethodGetPaths.setAccessible(true);
            paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTarget", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccess", e);
        }
        
        return paths;
    }
    
    
}
