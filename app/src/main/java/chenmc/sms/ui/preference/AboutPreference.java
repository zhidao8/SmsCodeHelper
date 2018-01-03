package chenmc.sms.ui.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.Toast;

import chenmc.sms.code.helper.R;

/**
 * @author 明 明
 *         Created on 2017-4-27.
 */

public class AboutPreference extends Preference {
    private int mTimes;

    private boolean mDeveloperMode;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AboutPreference(Context context, AttributeSet attrs,
        int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AboutPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AboutPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AboutPreference(Context context) {
        super(context);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, false);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        mDeveloperMode = restorePersistedValue ? getPersistedBoolean(false) : (boolean) defaultValue;
    }

    public boolean isDeveloperMode() {
        return mDeveloperMode;
    }

    @Override
    protected void onClick() {
        mTimes++;
        if (!mDeveloperMode && mTimes == 10) {
            Toast.makeText(getContext(), R.string.developer_mode_on, Toast.LENGTH_LONG).show();
            persistBoolean(mDeveloperMode = !mDeveloperMode);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    callChangeListener(mDeveloperMode);
                }
            }, 1000);
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        } else if (mDeveloperMode && mTimes == 3) {
            Toast.makeText(getContext(), R.string.developer_mode_off, Toast.LENGTH_LONG).show();
            persistBoolean(mDeveloperMode = !mDeveloperMode);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    callChangeListener(mDeveloperMode);
                }
            }, 1000);
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();

            // 将变量置为 10 防止触发第一条 if
            mTimes = 10;
        }
    }
}
