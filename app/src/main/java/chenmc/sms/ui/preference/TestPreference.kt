package chenmc.sms.ui.preference

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.preference.EditTextPreference
import android.util.AttributeSet
import android.widget.Toast
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.transaction.SmsAnalyzer
import chenmc.sms.transaction.SmsHandlerExecutor

/**
 * @author 明 明
 * Created on 2017-5-12.
 */

class TestPreference : EditTextPreference {
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    
    constructor(context: Context) : super(context)
    
    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        editText.setHint(R.string.sms_content)
        builder.setNegativeButton(R.string.cancel, this)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            val context = context
            
            val sms = editText.text.toString()
            
            // 创建一个短信执行器
            val executor = SmsHandlerExecutor(context, sms)
            
            // 分析文本内容是否符合验证码短信或取件码短信的格式
            val smsAnalyzer = SmsAnalyzer(context)
            val verificationCodeSms = smsAnalyzer.analyseVerificationSms(sms)
            val expressCodeSms = smsAnalyzer.analyseExpressSms(sms)
            
            if (verificationCodeSms != null || AppPreference.expressEnable && expressCodeSms != null) {
                executor.execute()
            } else {
                Toast.makeText(context, R.string.can_not_analyse_sms, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
}
