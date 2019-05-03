package chenmc.sms.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import chenmc.sms.code.helper.R
import chenmc.sms.ui.interfaces.OnBackPressedActivity
import chenmc.sms.ui.interfaces.OnBackPressedFragment

/**
 * @author 明 明
 * Created on 2017-2-13.
 */

class PreferenceActivity : AppCompatActivity(), OnBackPressedActivity {

    private var mOnBackPressedFragment: OnBackPressedFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainPreferenceFragment())
            .commit()
    }

    override fun setFocusFragment(fragment: OnBackPressedFragment) {
        mOnBackPressedFragment = fragment
    }

    override fun onBackPressed() {
        if (mOnBackPressedFragment?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }
}
