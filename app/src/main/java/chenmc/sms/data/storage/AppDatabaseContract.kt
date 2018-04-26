package chenmc.sms.data.storage

/**
 * @author Carter
 * Created on 2018-02-12
 */
object AppDatabaseContract {
    const val DATABASE = "MainDatabase.db"
    
    object SmsCodeRegex {
        const val TABLE = "sms_code_regex"
    
        const val ID = "_id"
        const val SMS = "sms"
        const val CODE = "code"
        const val REGEX = "regex"
    }
}