package chenmc.sms.ui.main

import android.os.Build
import android.os.Bundle
import chenmc.sms.code.helper.R
import chenmc.sms.ui.app.PermissionActivity
import chenmc.sms.ui.interfaces.IOnBackPressedActivity
import chenmc.sms.ui.interfaces.IOnBackPressedFragment

/**
 * @author 明 明
 * Created on 2017-2-13.
 */

class PreferenceActivity : PermissionActivity(), IOnBackPressedActivity {
    
    private var mOnBackPressedFragment: IOnBackPressedFragment? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar?.elevation = 0f
        }

        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainPreferenceFragment())
            .commit()
    }
    
    override fun getContentViewRes(): Int {
        return R.layout.activity_preference
    }
    
    override fun setFocusFragment(fragment: IOnBackPressedFragment) {
        mOnBackPressedFragment = fragment
    }
    
    override fun onBackPressed() {
        if (mOnBackPressedFragment?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
}
