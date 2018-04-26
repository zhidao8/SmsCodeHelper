package chenmc.sms.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.view.MenuItem
import chenmc.sms.code.helper.R
import chenmc.sms.ui.preference.AboutPreference

/**
 * @author 明明
 * Created on 2017-10-01
 */

class AboutFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.about)
        setHasOptionsMenu(true)
        init()
    }
    
    override fun onStart() {
        super.onStart()
        // 初始化 ActionBar
        val actionBar = activity.actionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.pref_about)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun init() {
        //region 关于
        val prefAbout = findPreference(getString(R.string.pref_key_about)) as AboutPreference
        try {
            val pi = activity.packageManager
                .getPackageInfo(activity.packageName, 0)
            prefAbout.summary = getString(R.string.pref_about_summary, pi.versionName, pi.versionCode)
        } catch (ex: PackageManager.NameNotFoundException) {
            // ignored
        }
        //endregion
    }
    
    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.pref_key_source_address) -> {
                browseUrl("https://github.com/zhidao8/SmsCodeHelper")
                return true
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }
    
    private fun browseUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(Intent.createChooser(intent, url))
    }
}
