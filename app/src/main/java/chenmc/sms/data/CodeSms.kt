package chenmc.sms.data

/**
 * Created by 明明 on 2017/7/1.
 */

abstract class CodeSms {
    
    // 该验证码短信的来源，如【中国移动】、[Google]
    var serviceProvider = ""
    var code: String? = null
    // 额外的内容
    var content: String? = null
    
    constructor()
    
    constructor(code: String) {
        this.code = code
    }
    
    constructor(serviceProvider: String, code: String) {
        this.serviceProvider = serviceProvider
        this.code = code
    }
    
    constructor(serviceProvider: String, code: String, content: String) {
        this.serviceProvider = serviceProvider
        this.code = code
        this.content = content
    }
}
