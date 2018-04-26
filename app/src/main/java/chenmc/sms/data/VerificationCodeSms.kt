package chenmc.sms.data

/**
 * 验证码短信
 * Created by 明明 on 2017/7/1.
 */

class VerificationCodeSms : CodeSms {
    
    constructor()
    
    constructor(code: String) : super(code)
    
    constructor(serviceProvider: String, verificationCode: String) : super(serviceProvider, verificationCode)
    
    constructor(serviceProvider: String, code: String, content: String) : super(serviceProvider, code, content)
}
