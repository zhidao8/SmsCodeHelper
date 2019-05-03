package chenmc.sms.ui.interfaces

import androidx.recyclerview.widget.RecyclerView

interface OnItemLongClickListener<VH : RecyclerView.ViewHolder> {
    fun onLongClick(holder: VH): Boolean
}