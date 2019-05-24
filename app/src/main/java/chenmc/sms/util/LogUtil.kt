package chenmc.sms.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log

/**
 * @author Carter
 * Created on 2018-02-10
 */
object LogUtil {
    // 标记当前应用是否是 debug
    private var debuggable = true
    // 是否强制 debug 以输出 log
    private var forceDebuggable = false

    @JvmStatic
    fun init(context: Context) {
        debuggable = (context.applicationContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    @JvmStatic
    fun v(tag: String?, msg: Any?) {
        if (debuggable || forceDebuggable)
            Log.v(tag.toString(), msg.toString())
    }

    @JvmStatic
    fun d(tag: String?, msg: Any?) {
        if (debuggable || forceDebuggable)
            Log.d(tag.toString(), msg.toString())
    }

    @JvmStatic
    fun i(tag: String?, msg: Any?) {
        if (debuggable || forceDebuggable)
            Log.i(tag.toString(), msg.toString())
    }

    @JvmStatic
    fun w(tag: String?, msg: Any?) {
        if (debuggable || forceDebuggable)
            Log.w(tag.toString(), msg.toString())
    }

    @JvmStatic
    fun e(tag: String?, msg: Any?) {
        if (debuggable || forceDebuggable)
            Log.e(tag.toString(), msg.toString())
    }
}