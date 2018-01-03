package chenmc.sms.transaction.handler;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.mingle.headsUp.HeadsUp;
import com.mingle.headsUp.HeadsUpManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chenmc.sms.code.helper.R;
import chenmc.sms.data.CodeSms;
import chenmc.sms.data.CustomRuleBean;
import chenmc.sms.data.ExpressCodeSms;
import chenmc.sms.data.VerificationCodeSms;
import chenmc.sms.transaction.CopyCodeService;
import chenmc.sms.transaction.SetReadSmsService;
import chenmc.sms.utils.ToastUtil;
import chenmc.sms.utils.storage.PrefKey;
import chenmc.sms.utils.storage.PreferenceUtil;
import chenmc.sms.utils.storage.SmsMatchRulesDBDao;

/**
 * @author 明明
 *         Created on 2017-10-03
 */

public class SmsHandler {
    
    private Context mContext;
    
    // 获取服务商信息
    private static final String sSourceRegExp = "(【.+?】|\\[.+?\\])";
    
    // 短信在数据库中的 _id，如果有
    private int mDatabaseId;
    
    // 短信内容
    private String mSms;
    
    private CodeSms mCodeSms;
    
    public SmsHandler(Context context, String sms, int databaseId) {
        this(context, sms);
        mDatabaseId = databaseId;
    }
    
    public SmsHandler(Context context, String sms) {
        mContext = context.getApplicationContext();
        mSms = sms;
    }
    
    public int getDatabaseId() {
        return mDatabaseId;
    }
    
    public String getSms() {
        return mSms;
    }
    
    public CodeSms getCodeSms() {
        return mCodeSms;
    }
    
    public boolean isVerificationSms() {
        return mCodeSms instanceof VerificationCodeSms;
    }
    
    public boolean isExpressSms() {
        return mCodeSms instanceof ExpressCodeSms;
    }
    
    // 处理验证码
    private void handleCode() {
        // 获取系统剪切板
        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        
        // 这是验证码
        if (mCodeSms instanceof VerificationCodeSms) {
            PreferenceUtil preferenceUtil = PreferenceUtil.init(mContext);
            Set<String> valuesSet = new HashSet<>(2);
            Collections.addAll(
                valuesSet,
                mContext.getResources().getStringArray(R.array.pref_def_values_sms_handle_ways)
            );
            String[] entryValues = mContext.getResources().getStringArray(R.array.pref_entry_values_sms_handle_ways);
            valuesSet = preferenceUtil.get(PrefKey.KEY_SMS_HANDLE_WAYS, valuesSet);
            // 应用开启了自动复制验证码
            if (valuesSet.contains(entryValues[0])) {
                clipboardManager.setPrimaryClip(
                    ClipData.newPlainText("code", mCodeSms.getCode())
                );
                ToastUtil.showToast(
                    mContext.getString(R.string.code_have_been_copied, mCodeSms.getCode()),
                    Toast.LENGTH_LONG);
            }
            // 应用开启了通知栏显示，在通知栏显示验证码
            if (valuesSet.contains(entryValues[1])) {
                notifyNotification();
            }
            
            // 这是取件码
        } else if (mCodeSms instanceof ExpressCodeSms) {
            notifyNotification();
        }
    }
    
    // 在通知栏显示验证码和服务商
    private void notifyNotification() {
        HeadsUpManager headsUpManager = HeadsUpManager.getInstant(mContext);
        HeadsUp.Builder headsUpBuilder = new HeadsUp.Builder(mContext)
            .setContentText(mCodeSms.getContent())
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            headsUpBuilder.setSmallIcon(R.drawable.ic_notification)
                .setColor(mContext.getResources().getColor(R.color.colorPrimary));
        } else {
            headsUpBuilder.setSmallIcon(R.mipmap.ic_launcher);
        }
        
        // 这是验证码
        if (mCodeSms instanceof VerificationCodeSms) {
            String title = mContext.getString(
                R.string.code_is,
                mCodeSms.getServiceProvider(),
                mCodeSms.getCode());
            
            Intent copyIntent = new Intent(mContext, CopyCodeService.class);
            copyIntent.putExtra(CopyCodeService.EXTRA_CODE, mCodeSms.getCode());
            
            headsUpBuilder.setContentTitle(title)
                .setTicker(title)
                .setContentIntent(PendingIntent.getService(
                    mContext, 0, copyIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
            
            // 这是取件码
        } else if (mCodeSms instanceof ExpressCodeSms) {
            String title = mContext.getString(
                R.string.express_is,
                mCodeSms.getServiceProvider(),
                mCodeSms.getCode());
            
            headsUpBuilder.setContentTitle(title)
                .setTicker(title + "\n" + mCodeSms.getContent());
        }
        
        //noinspection deprecation
        headsUpManager.notify((int) System.currentTimeMillis(), headsUpBuilder.buildHeadUp());
    }
    
    public void analyseAndHandle() {
        if (analyse()) {
            handle();
        }
    }
    
    public void handle() {
        if (mCodeSms != null) {
            handleCode();
        
            PreferenceUtil preferenceUtil = PreferenceUtil.init(mContext);
            // 应用开启了“自动将短信标记为已读”
            if (preferenceUtil.get(
                PrefKey.KEY_READ_AUTOMATICALLY,
                mContext.getResources().getBoolean(R.bool.pref_def_value_read_automatically)
            )) {
                // 启动一个 Service 标记短信为“已读”
                Intent readIntent = new Intent(mContext, SetReadSmsService.class);
                readIntent.putExtra("body", mSms);
                mContext.startService(readIntent);
            }
        }
    }
    
    // 分析短信内容，解析出短信中的验证码
    public boolean analyse() {
        PreferenceUtil preferenceUtil = PreferenceUtil.init(mContext);
        
        // 尝试分析处理短信
        analyseVerificationSms();
        if (mCodeSms == null) {
            // 如果应用开启了解析快递取件码短信
            if (preferenceUtil.get(PrefKey.KEY_EXPRESS, false)) {
                // 尝试分析处理取件码短信
                analyseExpressSms();
            }
        }
        
        return mCodeSms != null;
    }
    
    // 分析短信，提取短信中的验证码，如果有
    private void analyseVerificationSms() {
        SmsMatchRulesDBDao smsMatchRulesDBDao = new SmsMatchRulesDBDao(mContext);
        List<CustomRuleBean> customRuleBeanList = smsMatchRulesDBDao.selectAll();
        for (CustomRuleBean bean : customRuleBeanList) {
            Pattern pattern = Pattern.compile(bean.getRegExp());
            Matcher matcher = pattern.matcher(mSms);
            if (matcher.find()) {
                mCodeSms = new VerificationCodeSms();
                // 获取短信中的验证码
                mCodeSms.setCode(matcher.group());
                Pattern paSource = Pattern.compile(sSourceRegExp);
                Matcher maSource = paSource.matcher(mSms);
                if (maSource.find()) {
                    // 获取短信中的服务商
                    mCodeSms.setServiceProvider(maSource.group());
                }
                mCodeSms.setContent(mContext.getString(R.string.click_to_copy));
                return;
            }
        }
        
        PreferenceUtil preferenceUtil = PreferenceUtil.init(mContext);
        String smsContains = preferenceUtil.get(
            PrefKey.KEY_SMS_CONTAINS,
            mContext.getString(R.string.pref_def_value_sms_contains)
        );
        String regExp = preferenceUtil.get(
            PrefKey.KEY_REGEXP,
            mContext.getString(R.string.pref_def_value_regexp)
        );
        
        if (mSms.matches("(.|\n)*(" + smsContains + ")(.|\n)*")) {
            Pattern pattern = Pattern.compile(regExp);
            Matcher matcher = pattern.matcher(mSms);
            if (matcher.find()) {
                // 获取短信中所有可能是验证码的字符串
                ArrayList<String> codeList = new ArrayList<>(3);
                do {
                    codeList.add(matcher.group());
                } while (matcher.find());
                
                mCodeSms = new VerificationCodeSms(getBestCode(mSms, codeList));
                
                Pattern paSource = Pattern.compile(sSourceRegExp);
                Matcher maSource = paSource.matcher(mSms);
                if (maSource.find()) {
                    // 获取短信中的验证码
                    mCodeSms.setServiceProvider(maSource.group());
                }
                mCodeSms.setContent(mContext.getString(R.string.click_to_copy));
            }
        }
    }
    
    // 当从短信中匹配得到多个验证码时，通过赋予每个验证码不同的优先级（验证码的可能性），
    // 选出最有可能是验证码的字符串
    private String getBestCode(String sms, List<String> codeList) {
        if (codeList.size() == 1) return codeList.get(0);
        
        class CodeWrapper {
            private String code;
            private int priority;
            
            private CodeWrapper(String code) {
                this.code = code;
            }
        }
        ArrayList<CodeWrapper> codeWrapperList = new ArrayList<>(codeList.size());
        for (String s : codeList) {
            codeWrapperList.add(new CodeWrapper(s));
        }
        
        for (CodeWrapper codeWrapper : codeWrapperList) {
            final String code = codeWrapper.code;
            
            int codeIndex = sms.indexOf(codeWrapper.code);
            String prefixCode = codeIndex > 4 ?
                sms.substring(codeIndex - 5, codeIndex + code.length()) :
                sms.substring(0, codeIndex + code.length());
            
            if (prefixCode.matches("(.|\n)*(是|为|為|is|は|:|：|『|「|【|〖|（|\\(|\\[| )(.|\n)*")) {
                // 如果包含触发字，该验证码的优先级+1
                codeWrapper.priority++;
            }
            
            if (prefixCode.matches("(.|\n)*(码|碼|代码|代碼|号码|密码|口令|code|コード)(.|\n)*")) {
                // 如果包含“码”字，该验证码的优先级+2
                codeWrapper.priority += 2;
            }
            
            // 判断是否包含字母
            boolean hasLetter = false;
            for (int i = 0; i < code.length(); i++) {
                if (Character.isLetter(code.charAt(i))) {
                    hasLetter = true;
                    break;
                }
            }
            if (hasLetter) {
                // 如果包含字母，该验证码的优先级+1
                codeWrapper.priority++;
            } else {
                try {
                    // 可能该字符串是一个年份，尝试排除这种情况
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    if (Math.abs(currentYear - Integer.valueOf(code)) > 1) {
                        // 如果不是当前的年份±1，该验证码的优先级+1
                        codeWrapper.priority++;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        
        CodeWrapper bestCodeWrapper = codeWrapperList.get(0);
        for (CodeWrapper codeWrapper : codeWrapperList) {
            if (codeWrapper.priority > bestCodeWrapper.priority) {
                bestCodeWrapper = codeWrapper;
            }
        }
        
        return bestCodeWrapper.code;
    }
    
    // 处理快递短信
    private void analyseExpressSms() {
        PreferenceUtil preferenceUtil = PreferenceUtil.init(mContext);
        String smsContains = preferenceUtil.get(
            PrefKey.KEY_EXPRESS_SMS_CONTAINS,
            mContext.getString(R.string.pref_def_value_express_sms_contains)
        );
        String regExp = preferenceUtil.get(
            PrefKey.KEY_EXPRESS_REGEXP,
            mContext.getString(R.string.pref_def_value_express_regexp)
        );
        
        if (mSms.matches("(.|\n)*(" + smsContains + ")(.|\n)*")) {
            Matcher codeMatcher = Pattern.compile(regExp).matcher(mSms);
            if (codeMatcher.find()) {
                // 获取短信中的取件码
                mCodeSms = new ExpressCodeSms(codeMatcher.group());
                
                Matcher sourceMatcher = Pattern.compile(sSourceRegExp).matcher(mSms);
                if (sourceMatcher.find()) {
                    // 获取短信中的服务商
                    mCodeSms.setServiceProvider(sourceMatcher.group());
                }
                Matcher addressMatcher = Pattern.compile(
                    "(?<=快递已到)\\w*(?=[,.，。])"
                ).matcher(mSms);
                if (addressMatcher.find()) {
                    // 获取短信中的地址
                    mCodeSms.setContent(addressMatcher.group());
                }
            }
        }
    }
    
    /**
     * 从接收短信的 Receiver 的 Intent 中获取对象
     *
     * @param context 上下文
     * @param intent Receiver 接收的 Intent
     * @return {@link SmsHandler} 对象
     */
    public static SmsHandler createFromIntent(Context context, Intent intent) {
        
        StringBuilder sb = new StringBuilder();
        
        Bundle bundle = intent.getExtras();
        if (bundle == null) return new SmsHandler(context, "");
        
        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return new SmsHandler(context, "");
        
        SmsMessage[] messages = new SmsMessage[pdus.length];
        for (int i = 0; i < pdus.length; i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], intent.getStringExtra("format"));
            } else {
                //noinspection deprecation
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
        }
        for (SmsMessage message : messages) {
            sb.append(message.getDisplayMessageBody());
        }
        return new SmsHandler(context, sb.toString());
    }
    
    /**
     * 从系统短信数据库中获取对象
     *
     * @param context 上下文
     * @return {@link SmsHandler} 对象
     */
    public static SmsHandler createFromDatabase(Context context) {
        
        ContentResolver cr = context.getContentResolver();
        // 获取接收到的短信（type = 1），并且只获取 5 秒以内的消息
        String where = "type = 1 and date > " + (System.currentTimeMillis() - 5000);
        Cursor cur = cr.query(Uri.parse("content://sms/"),
            new String[]{"_id", "body"}, where, null, "date desc");
        if (cur == null) return new SmsHandler(context, "");
        
        SmsHandler smsHandler = null;
        if (cur.moveToFirst()) {
            smsHandler = new SmsHandler(
                context,
                cur.getString(cur.getColumnIndex("body")),
                cur.getInt(cur.getColumnIndex("_id"))
            );
            smsHandler.mSms = cur.getString(cur.getColumnIndex("body"));
            smsHandler.mDatabaseId = cur.getInt(cur.getColumnIndex("_id"));
        }
        cur.close();
        return smsHandler != null ? smsHandler : new SmsHandler(context, "");
    }
}
