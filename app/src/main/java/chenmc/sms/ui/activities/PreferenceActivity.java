package chenmc.sms.ui.activities;

import android.os.Bundle;

import java.util.ArrayList;

import chenmc.sms.code.helper.R;
import chenmc.sms.ui.fragments.MainPreferenceFragment;

/**
 * @author 明 明
 *         Created on 2017-2-13.
 */

public class PreferenceActivity extends PermissionActivity {
    
    private ArrayList<OnActivityBackPressedListener> mOnBackPressedListenerList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, new MainPreferenceFragment())
            .commit();
    }
    
    @Override
    protected int getContentViewRes() {
        return R.layout.activity_preference;
    }
    
    @Override
    public void onBackPressed() {
        boolean b = false;
        if (mOnBackPressedListenerList != null) {
            for (OnActivityBackPressedListener l : mOnBackPressedListenerList) {
                if (l != null && l.onActivityBackPressed()) {
                    b = true;
                }
            }
        }
        if (!b) {
            super.onBackPressed();
        }
    }
    
    public synchronized void addOnBackPressedListener(OnActivityBackPressedListener listener) {
        if (mOnBackPressedListenerList == null) {
            mOnBackPressedListenerList = new ArrayList<>(2);
        }
        mOnBackPressedListenerList.add(listener);
    }
    
    public synchronized void removeOnBackPressedListener(OnActivityBackPressedListener listener) {
        if (mOnBackPressedListenerList == null) return;
        mOnBackPressedListenerList.remove(listener);
    }
    
    public interface OnActivityBackPressedListener {
        boolean onActivityBackPressed();
    }
}
