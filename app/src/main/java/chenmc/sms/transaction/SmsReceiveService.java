package chenmc.sms.transaction;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import chenmc.sms.transaction.handler.SmsHandler;

/**
 * @author 明 明
 *         Created on 2017-4-28.
 */

public class SmsReceiveService extends Service {

    public Handler mServiceHandler;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 新开一个线程进行处理
        HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(),
            Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
    
        mServiceHandler = new ServiceHandler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mServiceHandler.getLooper().quit();
    }
    
    private class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Intent intent = (Intent) msg.obj;
            if (intent == null) return;
    
            SmsHandler smsHandler = SmsHandler.createFromIntent(
                SmsReceiveService.this, intent);
            // 处理短信
            smsHandler.analyseAndHandle();
            
            stopSelfResult(msg.arg1);
        }

    }
}
