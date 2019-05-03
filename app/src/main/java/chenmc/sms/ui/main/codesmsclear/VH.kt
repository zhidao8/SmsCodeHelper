package chenmc.sms.ui.main.codesmsclear

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import chenmc.sms.code.helper.R

class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val tvSms: TextView = itemView.findViewById(R.id.tvSms)
    val vMask: View = itemView.findViewById(R.id.mask)
}