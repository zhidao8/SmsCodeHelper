package chenmc.sms.ui.preference;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import chenmc.sms.code.helper.R;

/**
 * 这个类跟它的父类 {@link EditTextPreference} 不同的地方一是 Summary 只显示一行，末尾超出部分以...代替；
 * 二是弹出的对话框显示 NeutralButton，文本为“默认”，用于恢复当前 Preference 默认的值
 *
 * @author 明 明
 *         Created on 2017-4-20.
 */

public class DefaultEditTextPreference extends EditTextPreference {
    
    private String mDefValue;
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DefaultEditTextPreference(Context context, AttributeSet attrs,
        int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    
    public DefaultEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    public DefaultEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public DefaultEditTextPreference(Context context) {
        super(context);
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        mDefValue = (String) super.onGetDefaultValue(a, index);
        return mDefValue;
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
                if (mDefValue != null) {
                    setText(mDefValue);
                    setSummary(mDefValue);
                }
            }
        });
    }
}
