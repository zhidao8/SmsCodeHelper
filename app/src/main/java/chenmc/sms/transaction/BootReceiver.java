package chenmc.sms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import chenmc.sms.code.helper.R;
import chenmc.sms.utils.storage.PrefKey;
import chenmc.sms.utils.storage.PreferenceUtil;

/**
 * 接收开机完成广播的接收器
 *
 * @author 明 明
 *         Created on 2017-2-18.
 */

public class BootReceiver extends BroadcastReceiver {
    /**
     * 开机后自动启动{@link SmsObserverService}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        PreferenceUtil preferenceUtil = PreferenceUtil.init(context);
        // 如果用户开启了兼容模式，自动启动短信监视服务
        if (preferenceUtil.get(PrefKey.KEY_COMPAT_MODE,
            context.getResources().getBoolean(R.bool.pref_def_value_compat_mode))) {
            SmsObserverService.startThisService(context);
        }
    }
}
