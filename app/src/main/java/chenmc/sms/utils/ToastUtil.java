package chenmc.sms.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import chenmc.sms.ui.app.App;

/**
 * @author 明 明
 *         Created on 2017-4-30.
 */

public class ToastUtil {
    private static final String KEY_TEXT = "text";
    private static final String KEY_DURATION = "duration";
    
    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
    
            Toast.makeText(App.appContext, data.getString(KEY_TEXT),
                data.getInt(KEY_DURATION) == Toast.LENGTH_SHORT ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG
            ).show();
        }
    };

    public static void showToast(String text, int duration) {
        Message message = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TEXT, text);
        bundle.putInt(KEY_DURATION, duration);
        message.setData(bundle);

        mHandler.sendMessage(message);
    }

    public static void showToast(int textRes, int duration) {
        showToast(App.appContext.getString(textRes), duration);
    }

}
