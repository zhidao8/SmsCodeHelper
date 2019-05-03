package chenmc.sms.data

/**
 * Created by 明明 on 2017/7/1.
 */

abstract class CodeSms @JvmOverloads constructor(
    // 短信原文
    val raw: String,
    // 该验证码短信的来源，如【中国移动】、[Google]
    val serviceProvider: String = "",
    // 码
    val code: String = "",
    // 额外的内容
    val extra: String = ""
)