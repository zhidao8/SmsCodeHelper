package chenmc.sms.ui.main.customrules

import android.content.Context
import androidx.loader.content.AsyncTaskLoader
import chenmc.sms.data.storage.SmsCodeRegex
import chenmc.sms.data.storage.SmsCodeRegexDao

class CustomRulesLoader(context: Context) : AsyncTaskLoader<MutableList<SmsCodeRegex>>(context) {
    private var mData: MutableList<SmsCodeRegex>? = null

    override fun loadInBackground(): MutableList<SmsCodeRegex> {
        return SmsCodeRegexDao.getInstance(context).selectAll()
    }

    override fun deliverResult(data: MutableList<SmsCodeRegex>?) {
        super.deliverResult(data)
        mData = data
    }

    override fun onStartLoading() {
        if (mData != null) {
            deliverResult(mData)
        }

        if (takeContentChanged() || mData == null) {
            forceLoad()
        }
    }
}