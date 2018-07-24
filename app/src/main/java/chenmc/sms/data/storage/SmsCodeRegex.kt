package chenmc.sms.data.storage

/**
 * @author Carter
 * Created on 2018-07-17
 */

class SmsCodeRegex {

    var id: Int = 0

    // 短信
    var sms: String? = null

    // 验证码
    var verificationCode: String? = null

    // 验证码匹配规则
    var regex: String? = null

    constructor()

    constructor(id: Int = 0, sms: String? = null, verificationCode: String? = null, regex: String? = null) {
        this.id = id
        this.sms = sms
        this.verificationCode = verificationCode
        this.regex = regex
    }

    override fun toString(): String {
        return sms.toString()
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is SmsCodeRegex && other.id == this.id
    }

    override fun hashCode(): Int {
        return id
    }
}
