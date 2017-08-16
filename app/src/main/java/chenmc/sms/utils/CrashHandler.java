package chenmc.sms.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 *
 * @author user
 *
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";

    private static boolean HAVE_INSTANCE = false;
    
    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    //程序的Context对象
    private Context mContext;

    //用来存储设备信息和异常信息
    private Map<String, String> mInfos = new HashMap<>();

    /** 保证只有一个CrashHandler实例 */
    private CrashHandler(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    
        HAVE_INSTANCE = true;
    }
    
    public static void init(Context context) {
        if (HAVE_INSTANCE)
            throw new UnsupportedOperationException("This method can not invoke more than once.");
        new CrashHandler(context);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);
        //让系统默认的异常处理器再处理一遍
//        mDefaultHandler.uncaughtException(thread, ex);
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex Exception
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        //收集设备参数信息
        collectDeviceInfo(mContext);
        //保存日志文件
        final File logFile = saveCrashInfoToFile(ex);
        if (logFile != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Log: " + logFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        return true;
    }

    /**
     * 收集设备参数信息
     * @param ctx Context
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = String.valueOf(pi.versionCode);
                mInfos.put("versionName", versionName);
                mInfos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "An error occurred when collect package info: ", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                mInfos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "An error occurred when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex Exception
     * @return  返回文件,便于将文件传送到服务器
     */
    private File saveCrashInfoToFile(Throwable ex) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : mInfos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
    
        File externalFilesDir = mContext.getExternalFilesDir("");
        if (externalFilesDir == null) {
            Log.e(TAG, "Context.getExternalFilesDir(\"\") == null");
            return null;
        }
        
        long timestamp = System.currentTimeMillis();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String time = formatter.format(new Date(timestamp));
        String fileSimpleName = "log-" + time + "-" + (timestamp % 1000) + ".log";
    
        File logFile = new File(new File(externalFilesDir, "log"), fileSimpleName);
        
        // 父文件夹不存在且创建失败
        if (!logFile.getParentFile().exists() && !logFile.getParentFile().mkdirs()) {
            Log.e(TAG, "Failed to execute 'File.getParentFile().mkdirs()'");
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(logFile);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "An error occurred while writing file...\n", e);
        }
        return logFile;
    }
}