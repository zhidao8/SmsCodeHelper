package chenmc.sms.ui.main;

import android.os.Build;
import android.os.Bundle;

import chenmc.sms.code.helper.R;
import chenmc.sms.ui.app.PermissionActivity;
import chenmc.sms.ui.interfaces.IOnBackPressedActivity;
import chenmc.sms.ui.interfaces.IOnBackPressedFragment;

/**
 * @author 明 明
 *         Created on 2017-2-13.
 */

public class PreferenceActivity extends PermissionActivity implements IOnBackPressedActivity {
    
    private IOnBackPressedFragment mOnBackPressedFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActionBar().setElevation(0);
        }
        
        getFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, new MainPreferenceFragment())
            .commit();
    }
    
    @Override
    protected int getContentViewRes() {
        return R.layout.activity_preference;
    }
    
    @Override
    public void setFocusFragment(IOnBackPressedFragment fragment) {
        mOnBackPressedFragment = fragment;
    }
    
    @Override
    public void onBackPressed() {
        if (mOnBackPressedFragment == null || !mOnBackPressedFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
