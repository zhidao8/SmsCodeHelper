package chenmc.sms.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import chenmc.sms.code.helper.R

/**
 * @author 明明
 * Created on 2017-10-01
 */

class AboutFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_about)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { activity ->
            val prefAbout = findPreference(getString(R.string.pref_key_about))
            try {
                val pi = activity.packageManager
                    .getPackageInfo(activity.packageName, 0)
                val versionCode = PackageInfoCompat.getLongVersionCode(pi)
                prefAbout.summary = getString(R.string.pref_about_summary, pi.versionName, versionCode)
            } catch (ex: PackageManager.NameNotFoundException) {
                // ignored
            }
        }
    }

    override fun onStart() {
        super.onStart()

        activity?.let { activity ->
            when (activity) {
                is AppCompatActivity -> {
                    activity.supportActionBar?.run {
                        setTitle(R.string.pref_about)
                        setDisplayHomeAsUpEnabled(true)
                    }
                }
                else -> {
                    activity.actionBar?.run {
                        setTitle(R.string.pref_about)
                        setDisplayHomeAsUpEnabled(true)
                    }
                }
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            getString(R.string.pref_key_author) -> {
                browseUrl(AUTHOR_HOMEPAGE)
                true
            }
            getString(R.string.pref_key_store) -> {
                browseUrl(STORE_URL)
                true
            }
            getString(R.string.pref_key_source_address) -> {
                browseUrl(SOURCE_URL)
                true
            }
            getString(R.string.pref_key_alipay) -> {
                activity?.let { activity ->
                    val intent = Intent.parseUri(
                        ALIPAY_URI_FORMAT.replace("{urlCode}", ALIPAY_URL_CODE),
                        Intent.URI_INTENT_SCHEME
                    )
                    if (intent.resolveActivity(activity.packageManager) != null) {
                        startActivity(intent)
                    }
                }
                true
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    private fun browseUrl(url: String) {
        activity?.let { activity ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            if (intent.resolveActivity(activity.packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    private companion object {
        private const val AUTHOR_HOMEPAGE = "http://www.coolapk.com/u/506872"
        private const val STORE_URL = "https://www.coolapk.com/apk/chenmc.sms.code.helper"
        private const val SOURCE_URL = "https://github.com/zhidao8/SmsCodeHelper"
        private const val ALIPAY_URI_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
                "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" +
                "%3Dweb-other&_t=1472443966571#Intent;" + "scheme=alipayqr;package=com.eg.android.AlipayGphone;end"
        private const val ALIPAY_URL_CODE = "fkx086906hfpdb0tostqya3"
    }
}
