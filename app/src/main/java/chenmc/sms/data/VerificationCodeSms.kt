package chenmc.sms.data

/**
 * 验证码短信
 * Created by 明明 on 2017/7/1.
 */

class VerificationCodeSms @JvmOverloads constructor(
    raw: String,
    serviceProvider: String = "",
    code: String = "",
    extra: String = ""
) : CodeSms(raw, serviceProvider, code, extra)