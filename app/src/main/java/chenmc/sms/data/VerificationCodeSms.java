package chenmc.sms.data;

/**
 * 验证码短信
 * Created by 明明 on 2017/7/1.
 */

public class VerificationCodeSms extends CodeSms {
    public VerificationCodeSms() {
    }
    
    public VerificationCodeSms(String code) {
        super(code);
    }
    
    public VerificationCodeSms(String serviceProvider, String verificationCode) {
        super(serviceProvider, verificationCode);
    }
    
    public VerificationCodeSms(String serviceProvider, String code, String content) {
        super(serviceProvider, code, content);
    }
}
