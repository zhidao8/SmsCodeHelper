package chenmc.sms.data.storage

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

/**
 * [SmsCodeRegex] 的数据库访问对象
 *
 * @author Carter
 * Created on 2018-04-20
 */
@Dao
interface SmsCodeRegexDao {
    @Query("SELECT * FROM ${AppDatabaseContract.SmsCodeRegex.TABLE}")
    fun loadAll(): List<SmsCodeRegex>
    
    /**
     * 返回每一条插入到数据库中的记录的 rowId
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg smsCodeRegexes: SmsCodeRegex): Array<Long>
    
    /**
     * 返回更新的记录的数量
     */
    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(vararg smsCodeRegexes: SmsCodeRegex): Int
    
    /**
     * 返回删除的记录的数量
     */
    @Delete
    fun delete(vararg smsCodeRegexes: SmsCodeRegex): Int
}