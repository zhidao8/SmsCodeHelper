package chenmc.sms.ui.interfaces

import androidx.recyclerview.widget.RecyclerView

interface OnItemClickListener<VH : RecyclerView.ViewHolder> {
    fun onClick(holder: VH)
}