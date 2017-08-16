package chenmc.sms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;

/**
 * 接收短信广播的接收器
 * @author 明 明
 *         Created on 2017-2-20.
 */

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Telephony.Sms.Intents.SMS_RECEIVED_ACTION:
                // 打开一个服务进行相关处理
                intent.setClass(context, SmsReceiveService.class);
                context.startService(intent);
                break;
        }
    }
}
