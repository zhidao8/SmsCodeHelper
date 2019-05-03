package chenmc.sms.ui.main.customrules

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.SmsCodeRegex
import chenmc.sms.ui.interfaces.OnItemClickListener
import chenmc.sms.ui.interfaces.OnItemLongClickListener
import chenmc.sms.ui.main.adapter.SelectableAdapter
import chenmc.sms.ui.main.codesmsclear.VH

class CustomRuleAdapter : SelectableAdapter<VH>() {

    private var mData: MutableList<SmsCodeRegex> = mutableListOf()

    val selectedItems: List<SmsCodeRegex>
        get() = selectedItemsPosition.map { mData[it] }

    fun setData(data: MutableList<SmsCodeRegex>) {
        mData = data
        notifyDataSetChanged()
    }

    var onItemClickListener: OnItemClickListener<VH>? = null

    var onItemLongClickListener: OnItemLongClickListener<VH>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val vh = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_sms, parent, false))

        vh.itemView.setOnClickListener {
            onItemClickListener?.onClick(vh)
        }
        vh.itemView.setOnLongClickListener {
            onItemLongClickListener?.onLongClick(vh) ?: false
        }

        return vh
    }

    override fun getItemCount(): Int = mData.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tvSms.text = mData[position].sms
        holder.vMask.visibility = if (isSelected(position)) View.VISIBLE else View.INVISIBLE
    }

    fun getItem(position: Int): SmsCodeRegex = mData[position]

    fun setSelectionAll(selected: Boolean) {
        mData.forEachIndexed { index, _ -> setSelection(index, selected) }
    }

    fun toggleSelectionAll() {
        mData.forEachIndexed { index, _ -> toggleSelection(index) }
    }
}