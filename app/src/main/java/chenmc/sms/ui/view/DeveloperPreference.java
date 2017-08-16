package chenmc.sms.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;

import chenmc.sms.code.helper.R;
import chenmc.sms.utils.database.PrefKey;

/**
 * @author 明 明
 *         Created on 2017-4-27.
 */

public class DeveloperPreference extends Preference {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DeveloperPreference(Context context, AttributeSet attrs,
        int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public DeveloperPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DeveloperPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DeveloperPreference(Context context) {
        super(context);
        setOrder(7);
        setKey(PrefKey.KEY_DEVELOPER_MODE);
        setTitle(R.string.pref_developer_mode);
    }
}
