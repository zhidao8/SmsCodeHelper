package chenmc.sms.data.storage

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * 自定义的验证码匹配规则
 *
 * @author Carter
 * Created on 2018-02-12
 */
@Entity(tableName = AppDatabaseContract.SmsCodeRegex.TABLE)
class SmsCodeRegex {
    
    // 数据库的_id
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = AppDatabaseContract.SmsCodeRegex.ID, typeAffinity = ColumnInfo.INTEGER)
    var id: Int = 0
    
    // 短信
    @ColumnInfo(name = AppDatabaseContract.SmsCodeRegex.SMS)
    var sms: String? = null
    
    // 验证码
    @ColumnInfo(name = AppDatabaseContract.SmsCodeRegex.CODE)
    var verificationCode: String? = null
    
    // 验证码匹配规则
    @ColumnInfo(name = AppDatabaseContract.SmsCodeRegex.REGEX)
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
