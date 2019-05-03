package chenmc.sms.data

/**
 * 取件码短信
 * Created by 明明 on 2017/7/1.
 */

class ExpressCodeSms : CodeSms {

    constructor()

    constructor(code: String) : super(code)

    constructor(serviceProvider: String, expressCode: String) : super(serviceProvider, expressCode)

    constructor(serviceProvider: String, code: String, content: String) : super(serviceProvider, code, content)
}
