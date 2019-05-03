package chenmc.sms.ui.main.codesmsclear

import chenmc.sms.data.CodeSms

data class DbSms(
    var databaseId: Int,
    var sms: String,
    var codeSms: CodeSms
)