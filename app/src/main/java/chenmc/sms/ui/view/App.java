package chenmc.sms.ui.view;

import android.app.Application;
import android.content.Context;

import chenmc.sms.utils.CrashHandler;

/**
 * @author 明 明
 *         Created on 2017-5-1.
 */

public class App extends Application {
    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();

        // 初始化程序崩溃时的异常处理器
        CrashHandler.init(appContext);
    }
}
