package chenmc.sms.ui.main.customrules

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.SmsCodeRegex
import chenmc.sms.data.storage.SmsCodeRegexDao
import chenmc.sms.util.SmsMatchRuleUtil

class CustomRuleDialogFragment(data: SmsCodeRegex?) : DialogFragment() {
    private val mData = data

    private lateinit var mEtSms: EditText
    private lateinit var mEtCode: EditText
    private lateinit var mTvTips: TextView
    private lateinit var mDao: SmsCodeRegexDao

    var onDeliverResult: OnDeliverResult? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_costom_rule, null)
        mDao = SmsCodeRegexDao.getInstance(context!!)
        initView(view)

        return AbortDismissDialog(activity!!).apply {
            setTitle(if (mData == null) R.string.dialog_title_add_custom_rule else R.string.dialog_title_edit_custom_rule)
            setView(view)
            setButton(
                DialogInterface.BUTTON_NEGATIVE,
                getString(R.string.cancel),
                null as DialogInterface.OnClickListener?
            )
            setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok)) { _, _ ->

                val bean = SmsCodeRegex(mData?.id ?: 0, mEtSms.text.toString(), mEtCode.text.toString(), null)

                when (SmsMatchRuleUtil.handleItem(bean)) {
                    SmsMatchRuleUtil.HANDLE_RESULT_SUCCESS -> {
                        onDeliverResult?.deliverResult(bean, mData == null)
                    }
                    SmsMatchRuleUtil.HANDLE_ERROR_EMPTY_CONTENT -> {
                        mTvTips.visibility = View.VISIBLE
                        mTvTips.setText(R.string.edit_text_no_content)
                        abortDismiss()
                    }
                    SmsMatchRuleUtil.HANDLE_ERROR_NO_CONTAINS -> {
                        mTvTips.visibility = View.VISIBLE
                        mTvTips.setText(R.string.sms_not_contains_code)
                        abortDismiss()
                    }
                }
            }
        }
    }

    private fun initView(view: View) {
        mEtSms = view.findViewById<EditText>(R.id.etSms).apply {
            setText(mData?.sms)
            setSelection(text.length)
            addTextChangedListener(mTextWatcher)
            requestFocus()
        }

        mEtCode = view.findViewById<EditText>(R.id.etCode).apply {
            setText(mData?.verificationCode)
            setSelection(text.length)
            addTextChangedListener(mTextWatcher)
        }

        mTvTips = view.findViewById(R.id.tvTips)
    }

    private val mTextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            mTvTips.visibility = View.GONE
        }
    }

    interface OnDeliverResult {
        fun deliverResult(data: SmsCodeRegex, isNew: Boolean)
    }
}