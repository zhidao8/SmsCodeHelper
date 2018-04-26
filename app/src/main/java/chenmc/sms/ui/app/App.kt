package chenmc.sms.ui.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.utils.CrashHandler
import chenmc.sms.utils.LogUtil

/**
 * @author 明 明
 * Created on 2017-5-1.
 */

class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        
        // 初始化程序崩溃时的异常处理器
        CrashHandler.init(context)
    
        LogUtil.init(this)
        
        AppPreference.init(context)
    }
    
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}
