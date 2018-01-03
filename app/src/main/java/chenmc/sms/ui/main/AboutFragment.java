package chenmc.sms.ui.main;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import chenmc.sms.code.helper.R;
import chenmc.sms.ui.preference.AboutPreference;
import chenmc.sms.utils.storage.PrefKey;

/**
 * @author 明明
 *         Created on 2017-10-01
 */

public class AboutFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);
        setHasOptionsMenu(true);
        init();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // 初始化 ActionBar
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.pref_about);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void init() {
        //region 关于
        AboutPreference prefAbout = (AboutPreference) findPreference(PrefKey.KEY_ABOUT);
        try {
            PackageInfo pi = getActivity().getPackageManager()
                .getPackageInfo(getActivity().getPackageName(), 0);
            prefAbout.setSummary(
                getString(R.string.pref_about_summary, pi.versionName, pi.versionCode)
            );
        } catch (PackageManager.NameNotFoundException ex) {
            // ignored
        }
        //endregion
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case PrefKey.KEY_SOURCE_ADDRESS:
                browseUrl("https://github.com/zhidao8/SmsCodeHelper");
                return true;
            case PrefKey.KEY_LIBRARY:
                browseUrl("https://github.com/zzz40500/HeadsUp");
                return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    private void browseUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(Intent.createChooser(intent, url));
    }
}
