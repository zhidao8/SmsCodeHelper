package chenmc.sms.transaction;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import chenmc.sms.transaction.handler.SmsHandler;

/**
 * 此 Service 用来监听短信数据库的变化
 *
 * @author 明 明
 *         Created on 2017-2-14.
 */

public class SmsObserverService extends Service {

    // 上一次处理的短信在数据库中的 _id
    private int mOld_id = -1;
    private SmsObserver mSmsObserver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSmsObserver = new SmsObserver(new Handler(Looper.getMainLooper()));
        // 注册短信数据库监听
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true,
            mSmsObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 取消注册短信数据库监听
        getContentResolver().unregisterContentObserver(mSmsObserver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static void startThisService(Context context) {
        context.startService(new Intent(context, SmsObserverService.class));
    }

    public static void stopThisService(Context context) {
        context.stopService(new Intent(context, SmsObserverService.class));
    }
    
    private class SmsObserver extends ContentObserver {

        SmsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //每当有新短信到来时，使用我们获取短消息的方法
            SmsHandler smsHandler = SmsHandler.createFromDatabase(SmsObserverService.this);
            // 有新短信来时，该 onChange 方法会被多次调用，
            // 如果上次处理的数据库 _id 跟这次的相同，即当前处理的短信与上一次的相同，则不处理
            if (mOld_id == smsHandler.getDatabaseId())
                return;
            
            mOld_id = smsHandler.getDatabaseId();
    
            // 处理短信
            smsHandler.analyseAndHandle();
        }
    }
}
