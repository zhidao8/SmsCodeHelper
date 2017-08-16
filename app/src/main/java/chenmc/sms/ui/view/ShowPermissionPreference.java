package chenmc.sms.ui.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import chenmc.sms.code.helper.R;

/**
 * @author 明 明
 *         Created on 2017-4-25.
 */

public class ShowPermissionPreference extends Preference {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShowPermissionPreference(Context context, AttributeSet attrs,
        int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ShowPermissionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ShowPermissionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShowPermissionPreference(Context context) {
        super(context);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, true);
    }

    @Override
    protected void onClick() {
        @SuppressLint("InflateParams")
        View dialogView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.dialog_permission, null);
        final WebView webView = (WebView) dialogView.findViewById(R.id.dialog_webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) webView.getLayoutParams();
            layoutParams.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.activity_padding);
            webView.setLayoutParams(layoutParams);
        }
        webView.loadUrl("file:///android_asset/permission.html");

        AlertDialog dialog = new AlertDialog.Builder(getContext())
            .setView(dialogView)
            .setTitle(getTitle())
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setCancelable(true)
            .create();
        
        dialog.show();
    }
}
