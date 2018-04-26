package chenmc.sms.transaction

import android.content.Context
import chenmc.sms.code.helper.R
import chenmc.sms.data.ExpressCodeSms
import chenmc.sms.data.VerificationCodeSms
import chenmc.sms.data.storage.AppDatabaseWrapper
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.data.storage.SmsCodeRegex
import java.util.*
import java.util.regex.Pattern

/**
 * @author Carter
 * Created on 2018-02-06
 */
class SmsAnalyzer(context: Context) {

    private val mContext = context.applicationContext

    private companion object {
        // 缓存
        private var regexListCache: List<SmsCodeRegex>? = null
        private var updateTime: Long = 0
    }

    /**
     * 根据短信内容分析是否是验证码短信
     * 如果是返回 [VerificationCodeSms] ，如果不是返回 null
     */
    // 分析短信，提取短信中的验证码，如果有
    fun analyseVerificationSms(sms: String): VerificationCodeSms? {

        // 从数据库查询验证码匹配模板
        val regexList = if (regexListCache == null || System.currentTimeMillis() - updateTime > 10000) {
            // 如果缓存已超过10秒，更新缓存；缓存保留10秒是为了防止频繁访问数据库
            val database = AppDatabaseWrapper(mContext).database
            val all = database.smsCodeRegexDao().loadAll()
            database.close()
            regexListCache = all
            all
        } else regexListCache!!

        // 短信内容与逐个模板进行匹配
        for (bean in regexList) {
            val pattern = Pattern.compile(bean.regex)
            val matcher = pattern.matcher(sms)
            // 如果验证码匹配成功
            if (matcher.find()) {
                val codeSms = VerificationCodeSms()
                // 获取短信中的验证码
                codeSms.code = matcher.group()

                // 提取短信中的服务商
                codeSms.serviceProvider = extractProvider(sms)
                codeSms.content = mContext.getString(R.string.click_to_copy)

                return codeSms
            }
        }

        // 使用正则表达式匹配短信内容
        val smsKeyword = AppPreference.smsKeyword
        val smsRegex = AppPreference.smsRegex
        if (sms.matches("(.|\n)*($smsKeyword)(.|\n)*".toRegex())) {
            val pattern = Pattern.compile(smsRegex)
            val matcher = pattern.matcher(sms)
            if (matcher.find()) {
                // 获取短信中所有可能是验证码的字符串
                val codeList = ArrayList<String>(3)
                do {
                    codeList.add(matcher.group())
                } while (matcher.find())

                // 获取最佳（可能）的验证码
                val codeSms = VerificationCodeSms(getBestCode(sms, codeList))
                // 提取短信中的服务商
                codeSms.serviceProvider = extractProvider(sms)
                codeSms.content = mContext.getString(R.string.click_to_copy)

                return codeSms
            }
        }

        return null
    }

    // 提取短信中的服务商
    private fun extractProvider(sms: String): String {
        // 匹配短信中的服务商
        val paSource = Pattern.compile(SmsExtractor.PROVIDER_REGEXP)
        val maSource = paSource.matcher(sms)
        if (maSource.find()) {
            // 获取短信中的验证码
            return maSource.group()
        }
        return ""
    }

    // 当从短信中匹配得到多个验证码时，通过赋予每个验证码不同的优先级（验证码的可能性），
// 选出最有可能是验证码的字符串
    private fun getBestCode(sms: String, codeList: List<String>): String {
        if (codeList.size == 1) return codeList[0]

        class CodeWrapper constructor(val code: String) {
            var priority = 0
        }

        val codeWrapperList = ArrayList<CodeWrapper>(codeList.size)
        codeList.mapTo(codeWrapperList) { CodeWrapper(it) }

        for (codeWrapper in codeWrapperList) {
            val code = codeWrapper.code

            val codeIndex = sms.indexOf(codeWrapper.code)
            val prefixCode = if (codeIndex > 4)
                sms.substring(codeIndex - 5, codeIndex + code.length)
            else
                sms.substring(0, codeIndex + code.length)

            if (prefixCode.matches("(.|\n)*(是|为|為|is|は|:|：|『|「|【|〖|（|\\(|\\[| )(.|\n)*".toRegex())) {
                // 如果包含触发字，该验证码的优先级+1
                codeWrapper.priority++
            }

            if (prefixCode.matches("(.|\n)*(码|碼|代码|代碼|号码|密码|口令|code|コード)(.|\n)*".toRegex())) {
                // 如果包含“码”字，该验证码的优先级+2
                codeWrapper.priority += 2
            }

            // 判断是否包含字母
            val hasLetter = (0 until code.length).any { Character.isLetter(code[it]) }
            if (hasLetter) {
                // 如果包含字母，该验证码的优先级+1
                codeWrapper.priority++
            } else {
                try {
                    // 可能该字符串是一个年份，尝试排除这种情况
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    if (Math.abs(currentYear - Integer.valueOf(code)) > 1) {
                        // 如果不是当前的年份±1，该验证码的优先级+1
                        codeWrapper.priority++
                    }
                } catch (ignored: NumberFormatException) {
                }
            }
        }

        var bestCodeWrapper = codeWrapperList[0]
        for (codeWrapper in codeWrapperList) {
            if (codeWrapper.priority > bestCodeWrapper.priority) {
                bestCodeWrapper = codeWrapper
            }
        }

        return bestCodeWrapper.code
    }

    /**
     * 根据短信内容分析是否是取件码短信
     * 如果是返回 [ExpressCodeSms] ，如果不是返回 null
     */
    fun analyseExpressSms(sms: String): ExpressCodeSms? {
        val expressKeyword = AppPreference.expressKeyword
        val expressRegex = AppPreference.expressRegex

        if (sms.matches("(.|\n)*($expressKeyword)(.|\n)*".toRegex())) {
            val codeMatcher = Pattern.compile(expressRegex).matcher(sms)
            if (codeMatcher.find()) {
                // 获取短信中的取件码
                val codeSms = ExpressCodeSms(codeMatcher.group())

                val sourceMatcher = Pattern.compile(SmsExtractor.PROVIDER_REGEXP).matcher(sms)
                if (sourceMatcher.find()) {
                    // 获取短信中的服务商
                    codeSms.serviceProvider = sourceMatcher.group()
                }
                val addressMatcher = Pattern.compile(
                        "(?<=快递已到)\\w*(?=[,.，。])"
                ).matcher(sms)
                if (addressMatcher.find()) {
                    // 获取短信中的地址
                    codeSms.content = addressMatcher.group()
                }

                return codeSms
            }
        }

        return null
    }
}