package chenmc.sms.data;

/**
 * 自定义的验证码匹配规则
 * @author 明 明
 *         Created on 2017-4-27.
 */

public class SmsMatchRuleBean implements Cloneable {
    // 数据库的_id
    private int mId;
    // 短信
    private String mSms;
    // 验证码
    private String mVerificationCode;
    // 验证码匹配规则
    private String mRegExp;

    public SmsMatchRuleBean() {
    }

    public SmsMatchRuleBean(String sms) {
        mSms = sms;
    }

    public SmsMatchRuleBean(String sms, String verificationCode) {
        mSms = sms;
        mVerificationCode = verificationCode;
    }

    public SmsMatchRuleBean(String sms, String verificationCode, String regExp) {
        mSms = sms;
        mVerificationCode = verificationCode;
        mRegExp = regExp;
    }
    
    public int getId() {
        return mId;
    }
    
    public void setId(int id) {
        mId = id;
    }
    
    public String getSms() {
        return mSms;
    }

    public void setSms(String sms) {
        mSms = sms;
    }

    public String getVerificationCode() {
        return mVerificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        mVerificationCode = verificationCode;
    }

    public String getRegExp() {
        return mRegExp;
    }

    public void setRegExp(String regExp) {
        mRegExp = regExp;
    }

    @Override
    public String toString() {
        return mSms;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        
        SmsMatchRuleBean newObject = new SmsMatchRuleBean(mSms, mVerificationCode, mRegExp);
        newObject.setId(mId);
        return newObject;
    }
}
