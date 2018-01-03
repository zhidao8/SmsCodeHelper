package chenmc.sms.transaction;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import chenmc.sms.code.helper.R;
import chenmc.sms.utils.ToastUtil;

/**
 * 将短信设置为“已读”的服务
 * @author 明 明
 *         Created on 2017-4-21.
 */

public class SetReadSmsService extends Service {
    private Handler mHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new ServiceHandler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        // 延迟 3 秒，等待默认的短信应用将短信保存到数据库
        mHandler.sendMessageDelayed(msg, 3000);

        return START_NOT_STICKY;
    }

    private class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Intent intent = (Intent) msg.obj;
            if (intent.hasExtra("body")) {
                String body = intent.getStringExtra("body");
                ContentResolver contentResolver = getContentResolver();
                ContentValues values = new ContentValues();
                // 将短信标记为已读，将数据库中的表头 read 写为 1
                values.put("read", 1);
                int count = contentResolver.update(Uri.parse("content://sms/"), values,
                    "body like ?", new String[]{"%" + body + "%"});
                if (count == 0) {
                    ToastUtil.showToast(getString(R.string.set_read_fail), Toast.LENGTH_SHORT);
                }
            }
            stopSelfResult(msg.arg1);
        }
    }
}
