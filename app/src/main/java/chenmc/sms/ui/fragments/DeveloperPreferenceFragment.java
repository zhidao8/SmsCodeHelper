package chenmc.sms.ui.fragments;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import chenmc.sms.code.helper.R;
import chenmc.sms.utils.database.PrefKey;
import chenmc.sms.utils.database.PreferenceUtil;

/**
 * Created by 明明 on 2017/8/9.
 */

public class DeveloperPreferenceFragment extends PreferenceFragment implements
    Preference.OnPreferenceChangeListener {
    
    private CharSequence mPreviousActionBarTitle;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_developer);
        
        setHasOptionsMenu(true);
        init();
    }
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            mPreviousActionBarTitle = actionBar.getTitle();
            actionBar.setTitle(R.string.pref_developer_mode);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mPreviousActionBarTitle);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }
    
    private void init() {
        PreferenceUtil preferenceUtil = PreferenceUtil.init(getActivity());
        
        //region 短信包含关键词
        Preference prefSmsContains = findPreference(PrefKey.KEY_SMS_CONTAINS);
        prefSmsContains.setSummary(
            preferenceUtil.get(
                PrefKey.KEY_SMS_CONTAINS,
                getString(R.string.pref_def_value_sms_contains))
        );
        prefSmsContains.setOnPreferenceChangeListener(this);
        //endregion
        
        //region 验证码匹配规则
        Preference prefRegexp = findPreference(PrefKey.KEY_REGEXP);
        prefRegexp.setSummary(
            preferenceUtil.get(
                PrefKey.KEY_REGEXP,
                getString(R.string.pref_def_value_regexp))
        );
        prefRegexp.setOnPreferenceChangeListener(this);
        //endregion
        
        //region 快递短信包含关键词
        Preference prefExpressSmsContains = findPreference(PrefKey.KEY_EXPRESS_SMS_CONTAINS);
        prefExpressSmsContains.setSummary(
            preferenceUtil.get(
                PrefKey.KEY_EXPRESS_SMS_CONTAINS,
                getString(R.string.pref_def_value_express_sms_contains))
        );
        prefExpressSmsContains.setOnPreferenceChangeListener(this);
        //endregion
        
        //region 取件码匹配规则
        Preference prefExpressRegexp = findPreference(PrefKey.KEY_EXPRESS_REGEXP);
        prefExpressRegexp.setSummary(
            preferenceUtil.get(
                PrefKey.KEY_EXPRESS_REGEXP,
                getString(R.string.pref_def_value_express_regexp)
            )
        );
        prefExpressRegexp.setOnPreferenceChangeListener(this);
        //endregion
        
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary(newValue.toString());
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
}
