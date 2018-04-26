package chenmc.sms.transaction

import android.content.Context

/**
 * @author Carter
 * Created on 2018-02-06
 */
interface ISmsHandler {
    fun handle(context: Context, sms: String): Boolean
}