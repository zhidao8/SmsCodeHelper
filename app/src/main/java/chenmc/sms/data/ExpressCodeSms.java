package chenmc.sms.data;

/**
 * 取件码短信
 * Created by 明明 on 2017/7/1.
 */

public class ExpressCodeSms extends CodeSms {
    public ExpressCodeSms() {
    }
    
    public ExpressCodeSms(String code) {
        super(code);
    }
    
    public ExpressCodeSms(String serviceProvider, String expressCode) {
        super(serviceProvider, expressCode);
    }
}
