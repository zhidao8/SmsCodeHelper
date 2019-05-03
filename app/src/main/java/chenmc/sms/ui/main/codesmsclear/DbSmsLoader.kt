package chenmc.sms.ui.main.codesmsclear

import android.content.Context
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.net.Uri
import android.widget.Toast
import androidx.loader.content.AsyncTaskLoader
import chenmc.sms.code.helper.R
import chenmc.sms.transaction.SmsAnalyzer
import chenmc.sms.util.ToastUtil
import java.util.*

class DbSmsLoader(context: Context) : AsyncTaskLoader<MutableList<DbSms>>(context) {

    private var mData: MutableList<DbSms>? = null

    override fun loadInBackground(): MutableList<DbSms> {
        val list = ArrayList<DbSms>()

        val cr = context.contentResolver
        // 获取接收到的短信（type = 1）

        val cur = cr.query(
            Uri.parse("content://sms/"),
            arrayOf("_id", "body"), "type = ?", arrayOf(1.toString()), "date desc"
        ) ?: return list

        try {
            cur.use {
                val smsAnalyzer = SmsAnalyzer(context)

                while (cur.moveToNext()) {
                    val sms = cur.getString(cur.getColumnIndex("body"))

                    val vcs = smsAnalyzer.analyseVerificationSms(sms)
                    val ecs = if (vcs == null) smsAnalyzer.analyseExpressSms(sms) else null

                    if (vcs != null || ecs != null) {
                        list.add(
                            DbSms(
                                cur.getInt(cur.getColumnIndex("_id")),
                                sms,
                                vcs ?: ecs!!
                            )
                        )
                    }
                }
            }
        } catch (ex: SQLiteCantOpenDatabaseException) {
            ToastUtil.showSingletonToast(R.string.error_occurred_while_read_sms, Toast.LENGTH_SHORT)
        }

        return list
    }

    override fun deliverResult(data: MutableList<DbSms>?) {
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
