package chenmc.sms.transaction;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import chenmc.sms.code.helper.R;
import chenmc.sms.utils.ToastUtil;

/**
 * 复制验证码（取件码）的服务
 * @author 明 明
 *         Created on 2017-4-20.
 */

public class CopyCodeService extends Service {
    public static final String EXTRA_CODE = "code";
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent.hasExtra(EXTRA_CODE)) {
            String verificationCode = intent.getStringExtra(EXTRA_CODE);

            // 获取剪切板
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            // 复制到剪切板
            clipboardManager.setPrimaryClip(
                ClipData.newPlainText(EXTRA_CODE, verificationCode)
            );
            ToastUtil.showToast(
                getString(R.string.code_have_been_copied, verificationCode),
                Toast.LENGTH_LONG);
        }

        stopSelfResult(startId);
        return START_NOT_STICKY;
    }
}
