package chenmc.sms.ui.main.customrules

import android.app.AlertDialog
import android.content.Context

class AbortDismissDialog : AlertDialog {
    private var isAbortDismiss: Boolean = false

    constructor(context: Context) : super(context)

    override fun dismiss() {
        if (!isAbortDismiss) {
            super.dismiss()
        }
        isAbortDismiss = false
    }

    fun abortDismiss() {
        isAbortDismiss = true
    }
}