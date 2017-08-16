package chenmc.sms.ui.view;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import chenmc.sms.code.helper.R;
import chenmc.sms.transaction.handler.SmsHandler;
import chenmc.sms.utils.ToastUtil;

/**
 * @author 明 明
 *         Created on 2017-5-12.
 */

public class TestPreference extends EditTextPreference {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TestPreference(Context context, AttributeSet attrs,
        int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TestPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TestPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestPreference(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        getEditText().setHint(R.string.sms_content);
        builder.setNegativeButton(R.string.cancel, this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sms = getEditText().getText().toString();
                SmsHandler smsHandler = new SmsHandler(getContext(), sms);

                if (!smsHandler.handleSms()) {
                    ToastUtil.showToast(R.string.can_not_analyse_sms, Toast.LENGTH_SHORT);
                }
                
            }
        });
    }
}
