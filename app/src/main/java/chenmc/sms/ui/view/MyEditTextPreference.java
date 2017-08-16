package chenmc.sms.ui.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import chenmc.sms.code.helper.R;
import chenmc.sms.utils.database.PrefKey;

/**
 * @author 明 明
 *         Created on 2017-4-20.
 */

public class MyEditTextPreference extends EditTextPreference {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MyEditTextPreference(Context context, AttributeSet attrs,
        int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MyEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditTextPreference(Context context) {
        super(context);
    }
    
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        final TextView summaryView = (TextView) view.findViewById(
            android.R.id.summary);
        summaryView.setSingleLine(true);
        summaryView.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);

        builder.setNeutralButton(R.string.dialog_default, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String s;
                switch (getKey()) {
                    case PrefKey.KEY_SMS_CONTAINS:
                        s = getContext().getString(R.string.pref_def_value_sms_contains);
                        break;
                    case PrefKey.KEY_REGEXP:
                        s = getContext().getString(R.string.pref_def_value_regexp);
                        break;
                    case PrefKey.KEY_EXPRESS_SMS_CONTAINS:
                        s = getContext().getString(R.string.pref_def_value_express_sms_contains);
                        break;
                    case PrefKey.KEY_EXPRESS_REGEXP:
                        s = getContext().getString(R.string.pref_def_value_express_regexp);
                        break;
                    default:
                        s = getText();
                }
                setText(s);
                setSummary(s);
            }
        });
    }
}
