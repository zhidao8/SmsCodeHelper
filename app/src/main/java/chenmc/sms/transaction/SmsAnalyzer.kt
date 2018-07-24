package chenmc.sms.transaction

import android.content.Context
import chenmc.sms.code.helper.R
import chenmc.sms.data.ExpressCodeSms
import chenmc.sms.data.VerificationCodeSms
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.data.storage.SmsCodeRegexDao
import java.lang.ref.WeakReference
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

/**
 * @author Carter
 * Created on 2018-02-06
 */
class SmsAnalyzer(context: Context) {

    private val context = context.applicationContext
    private val keywordList = ArrayList<String>(2)
    private val regexList = ArrayList<String>(2)
    private val codeList = ArrayList<String>(5)

    /**
     * 根据短信内容分析是否是验证码短信
     * 如果是返回 [VerificationCodeSms] ，如果不是返回 null
     */
    // 分析短信，提取短信中的验证码，如果有
    fun analyseVerificationSms(sms: String): VerificationCodeSms? {

        // 使用缓存
        val codeRegexList = daoWR?.get()?.selectAll() ?: kotlin.run {
            val dao = SmsCodeRegexDao(context)
            daoWR = WeakReference(dao)
            dao.selectAll()
        }

        // 短信内容与逐个模板进行匹配
        for (bean in codeRegexList) {
            val pattern = Pattern.compile(bean.regex)
            val matcher = pattern.matcher(sms)
            // 如果验证码匹配成功
            if (matcher.find()) {
                val codeSms = VerificationCodeSms()
                // 获取短信中的验证码
                codeSms.code = matcher.group()

                // 提取短信中的服务商
                codeSms.serviceProvider = extractProvider(sms)
                codeSms.content = context.getString(R.string.click_to_copy)

                return codeSms
            }
        }

        // 使用前先将里面储存的数据清除
        keywordList.clear()
        regexList.clear()
        // 获取验证码短信关键词
        val smsKeyword = AppPreference.smsKeyword
        // 获取验证码短信正则匹配
        val smsRegex = AppPreference.smsRegex

        if (smsKeyword.trim().isNotEmpty()) keywordList.add(smsKeyword)
        if (smsRegex.trim().isNotEmpty()) regexList.add(smsRegex)
        keywordList.add(AppPreference.defaultSmsKeyword)
        regexList.add(AppPreference.defaultSmsRegex)

        // 使用正则表达式匹配短信内容
        for (keyword in keywordList) {
            if (sms.matches("(.|\n)*($keyword)(.|\n)*".toRegex())) {

                for (regex in regexList) {
                    val matcher = Pattern.compile(regex).matcher(sms)
                    if (matcher.find()) {
                        // 获取短信中所有可能是验证码的字符串
                        codeList.clear()
                        do {
                            codeList.add(matcher.group())
                        } while (matcher.find())

                        // 获取最佳（可能）的验证码
                        val codeSms = VerificationCodeSms(getBestCode(sms, codeList))
                        // 提取短信中的服务商
                        codeSms.serviceProvider = extractProvider(sms)
                        codeSms.content = context.getString(R.string.click_to_copy)

                        return codeSms
                    }
                }
            }
        }

        return null
    }

    // 提取短信中的服务商
    private fun extractProvider(sms: String): String {
        // 匹配短信中的服务商
        val matcher = Pattern.compile(AppPreference.defaultProviderRegex).matcher(sms)
        if (matcher.find()) {
            // 获取短信中的验证码
            return matcher.group()
        }
        return ""
    }

    private class CodeWrapper(val code: String) {
        var priority = 0
    }

    private val codeWrapperList = ArrayList<CodeWrapper>(5)

    // 当从短信中匹配得到多个验证码时，通过赋予每个验证码不同的优先级（验证码的可能性），
    // 选出最有可能是验证码的字符串
    private fun getBestCode(sms: String, codeList: List<String>): String {
        if (codeList.size == 1) return codeList[0]

        // 使用前先清除里面储存的数据
        codeWrapperList.clear()
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
                } catch (_: NumberFormatException) {
                    // ignored
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

    private val placeRegexList = ArrayList<String>(2)
    /**
     * 根据短信内容分析是否是取件码短信
     * 如果是返回 [ExpressCodeSms] ，如果不是返回 null
     */
    fun analyseExpressSms(sms: String): ExpressCodeSms? {
        // 取件码短信关键词
        // 使用前先清除里面的数据
        keywordList.clear()
        val expressKeyword = AppPreference.expressKeyword
        if (expressKeyword.trim().isNotEmpty()) keywordList.add(expressKeyword)
        keywordList.add(AppPreference.defaultExpressKeyword)

        // 取件码短信取件码匹配正则
        // 使用前先清除里面的数据
        regexList.clear()
        val expressRegex = AppPreference.expressRegex
        if (expressRegex.trim().isNotEmpty()) regexList.add(expressRegex)
        regexList.add(AppPreference.defaultExpressRegex)

        // 取件码短信地址匹配正则
        // 使用前先清除里面的数据
        placeRegexList.clear()
        val expressPlaceRegex = AppPreference.expressPlaceRegex
        if (expressPlaceRegex.trim().isNotEmpty()) placeRegexList.add(expressPlaceRegex)
        placeRegexList.add(AppPreference.defaultExpressPlaceRegex)

        for (keyword in keywordList) {
            if (sms.matches("(.|\n)*($keyword)(.|\n)*".toRegex())) {

                for (regex in regexList) {
                    val codeMatcher = Pattern.compile(regex).matcher(sms)
                    if (codeMatcher.find()) {
                        // 获取短信中的取件码
                        val codeSms = ExpressCodeSms(codeMatcher.group())

                        // 获取短信中的服务商
                        codeSms.serviceProvider = extractProvider(sms)

                        for (placeRegex in placeRegexList) {
                            val addressMatcher = Pattern.compile(placeRegex).matcher(sms)
                            if (addressMatcher.find()) {
                                // 获取短信中的地址
                                codeSms.content = addressMatcher.group()
                                break
                            }
                        }

                        return codeSms
                    }
                }
            }
        }

        return null
    }

    private companion object {
        // 缓存
        private var daoWR: WeakReference<SmsCodeRegexDao>? = null
    }
}