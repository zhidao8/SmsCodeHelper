package chenmc.sms.ui.preference

import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.preference.EditTextPreference
import android.util.AttributeSet
import chenmc.sms.code.helper.R

/**
 * 这个类跟它的父类 [EditTextPreference] 不同的地方一是 Summary 只显示一行，末尾超出部分以...代替；
 * 二是弹出的对话框显示 NeutralButton，文本为“默认”，用于恢复当前 Preference 默认的值
 *
 * @author 明 明
 * Created on 2017-4-20.
 */

class DefaultEditTextPreference : EditTextPreference {
    
    private var defValue: String? = null
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    
    constructor(context: Context) : super(context)
    
    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        defValue = super.onGetDefaultValue(a, index) as String
        return defValue
    }
    /*
    override fun onBindView(view: View) {
        super.onBindView(view)
        val summaryView = view.findViewById<View>(
                android.R.id.summary) as TextView
        summaryView.setSingleLine(true)
        summaryView.ellipsize = TextUtils.TruncateAt.END
    }
    */
    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        builder.setPositiveButton(R.string.ok, this)
        builder.setNegativeButton(R.string.cancel, this)
        
        builder.setNeutralButton(R.string.dialog_default) { _, _ ->
            if (defValue != null) {
                this.text = defValue
                this.summary = defValue
            }
        }
    }
}
