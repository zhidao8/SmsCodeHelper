package chenmc.sms.data;

/**
 * Created by 明明 on 2017/7/1.
 */

public abstract class CodeSms {
    // 该验证码短信的来源，如【中国移动】、[Google]
    private String mServiceProvider = "";
    private String mCode;
    
    public CodeSms() {
    }
    
    public CodeSms(String code) {
        mCode = code;
    }
    
    public CodeSms(String serviceProvider, String code) {
        mServiceProvider = serviceProvider;
        mCode = code;
    }
    
    public void setServiceProvider(String serviceProvider) {
        mServiceProvider = serviceProvider;
    }
    
    public String getServiceProvider() {
        return mServiceProvider;
    }
    
    public void setCode(String code) {
        mCode = code;
    }
    
    public String getCode() {
        return mCode;
    }
}
