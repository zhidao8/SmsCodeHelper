package chenmc.sms.transaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.transaction.service.SmsObserverService

/**
 * 接收开机完成广播的接收器
 *
 * @author 明 明
 * Created on 2017-2-18.
 */

class BootReceiver : BroadcastReceiver() {
    /**
     * 开机后自动启动[SmsObserverService]
     */
    override fun onReceive(context: Context, intent: Intent?) {
        if (AppPreference.isCompatMode) {
            // 如果短信处理方式是通过监听短信数据库变化读取短信内容，自动启动短信监视服务
            SmsObserverService.startThisService(context)
        }
    }
}
