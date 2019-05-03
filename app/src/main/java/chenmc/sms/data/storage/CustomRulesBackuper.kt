package chenmc.sms.data.storage

import android.content.Context
import chenmc.sms.ui.app.App.Companion.context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Carter
 * Created on 2018-05-07
 */
class CustomRulesBackuper {

    // 数据库访问对象
    private val dao: SmsCodeRegexDao
    // Gson 实例
    private val gson: Gson = Gson()
    // 时间格式化模版
    private val timeFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSS", Locale.getDefault())

    constructor(context: Context) {
        dao = SmsCodeRegexDao.getInstance(context)
    }

    constructor(dao: SmsCodeRegexDao) {
        this.dao = dao
    }

    /**
     * 将数据库中的数据持久化到 json 文件中，文件保存到文件夹 [dir] 中
     */
    fun backup(dir: File) {
        val list = dao.selectAll()
        val jsonElement = toJsonElement(list)

        val packageName = context.packageName
        val timeString = timeFormatter.format(Date())
        val file = File(dir, "$timeString.$packageName")

        // 将 JsonElement 写入到文件中
        val jsonWriter = JsonWriter(FileWriter(file))
        gson.toJson(jsonElement, jsonWriter)
        jsonWriter.flush()
    }

    // 将 List 转换为 JsonElement
    private fun toJsonElement(list: MutableList<SmsCodeRegex>): JsonElement {
        val result = JsonArray(list.size)
        list.forEach {
            val jsonObject = JsonObject()
            jsonObject.addProperty(SMS, it.sms)
            jsonObject.addProperty(CODE, it.verificationCode)
            jsonObject.addProperty(REGEX, it.regex)
            result.add(jsonObject)
        }
        return result
    }

    /**
     * 从 json 文件 [file] 中读取数据写入到数据库中。
     * 文件格式错误返回 false，否则返回 true
     */
    fun restore(file: File): Boolean {
        try {
            val result = fromFile(file)
            dao.insert(*result.toTypedArray())
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            return false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    private fun fromFile(file: File): MutableList<SmsCodeRegex> {
        val result = mutableListOf<SmsCodeRegex>()

        val jsonArray = gson.fromJson(FileReader(file), JsonArray::class.java)
        jsonArray.forEach {
            val jsonObject = it.asJsonObject
            val sms = jsonObject[SMS]
            val code = jsonObject[CODE]
            val regex = jsonObject[REGEX]
            if (sms == null || code == null || regex == null) {
                // json 对象中没有这些属性，即文件格式错误，抛出异常
                throw IllegalStateException("There are no $SMS, $CODE or $REGEX property in json")
            }
            result.add(SmsCodeRegex(
                    sms = sms.asString,
                    verificationCode = code.asString,
                    regex = regex.asString))
        }

        return result
    }

    private companion object {
        private const val SMS = "sms"
        private const val CODE = "code"
        private const val REGEX = "regex"
    }
}