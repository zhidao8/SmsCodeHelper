package chenmc.sms.utils;

import android.text.TextUtils;

import java.util.Locale;

import chenmc.sms.data.storage.SmsCodeRegex;

/**
 * 处理验证码匹配规则的工具类
 * @author 明 明
 *         Created on 2017-4-27.
 */
public class SmsMatchRuleUtil {
    
    /**
     * 对 SmsMatchRuleBean 的实例的处理成功的标志
     *
     * @see #handleItem(SmsCodeRegex)
     */
    public static final int HANDLE_RESULT_SUCCESS = 0;
    
    /**
     * 对 SmsMatchRuleBean 的实例的处理失败的标志。失败原因：包含空内容
     *
     * @see #handleItem(SmsCodeRegex)
     */
    public static final int HANDLE_ERROR_EMPTY_CONTENT = -1;
    
    /**
     * 对 SmsMatchRuleBean 的实例的处理失败的标志。失败原因：短信内容不包含给定的验证码
     *
     * @see #handleItem(SmsCodeRegex)
     */
    public static final int HANDLE_ERROR_NO_CONTAINS = -2;
    
    
    /**
     * 处理 {@link SmsCodeRegex} 实例，分析并生成短信验证码规则
     * @param item SmsMatchRuleBean 实例
     * @return 处理结果码。值可以为 {@link #HANDLE_ERROR_EMPTY_CONTENT}，
     * {@link #HANDLE_ERROR_NO_CONTAINS}，{@link #HANDLE_RESULT_SUCCESS}
     */
    public static int handleItem(SmsCodeRegex item) {
        String sms = item.getSms();
        String verificationCode = item.getVerificationCode();
       
        if (TextUtils.isEmpty(sms) || TextUtils.isEmpty(verificationCode)) {
            return HANDLE_ERROR_EMPTY_CONTENT;
        }
        
        if (!sms.contains(verificationCode)) {
            return HANDLE_ERROR_NO_CONTAINS;
        }
        
        int startCode = sms.indexOf(verificationCode);
        int endCode = startCode + verificationCode.length();
        int index1 = startCode;
        int index2 = endCode;
        for (int i = 0, len = 5; i < len; i++) {
            index1--;
            if (index1 < 0) {
                index1 = 0;
                break;
            }
            // 遇到数字就不再向前检索字符了，因为数字可能是年份之类的可变数字
            if (Character.isDigit(sms.charAt(index1))) {
                index1++;
                break;
            } else if (i == 1 && Character.isLetter(sms.charAt(index1))) {
                // 遇到字母，则这条短信很大可能是英文短信，把检索长度 len 提高到 16
                len = 16;
            }
        }
        for (int i = 0, len = 5; i < len; i++) {
            if (index2 == sms.length()) {
                break;
            }
            // 遇到数字就不再向前检索字符了，因为数字可能是年份之类的可变数字
            if (Character.isDigit(sms.charAt(index2))) {
                break;
            } else if (i == 1 && Character.isLetter(sms.charAt(index2))) {
                // 遇到字母，则这条短信很大可能是英文短信，把检索长度 len 提高到 16
                len = 16;
            }
            index2++;
        }
        String s1 = sms.substring(index1, startCode)
            .replaceAll("\\.", "\\\\.")
            .replaceAll("\\[", "\\\\[")
            .replaceAll("\\]", "\\\\]");
        String s2 = sms.substring(endCode, index2)
            .replaceAll("\\.", "\\\\.")
            .replaceAll("\\[", "\\\\[")
            .replaceAll("\\]", "\\\\]");

        String regExp1 = index1 < startCode ?
            String.format("(?<=%s)", s1) : "";
        String regExp2 = index2 > endCode ?
            String.format("(?=%s)", s2) : "";
        String regCode = String.format(Locale.getDefault(), ".{%d}", verificationCode.length());
        
        item.setRegex(regExp1 + regCode + regExp2);
        
        return HANDLE_RESULT_SUCCESS;
    }
}
