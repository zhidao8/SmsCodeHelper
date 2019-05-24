package chenmc.sms.ui.main.codesmsclear

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chenmc.sms.code.helper.R
import chenmc.sms.ui.interfaces.OnItemClickListener
import chenmc.sms.ui.interfaces.OnItemLongClickListener
import chenmc.sms.ui.main.adapter.SelectableAdapter

class DbSmsAdapter : SelectableAdapter<VH>() {

    private var mData: MutableList<DbSms> = mutableListOf()
    private val mExpandedItems = SparseBooleanArray(0)

    var data: List<DbSms>
        get () = mData
        set(value) {
            mData = if (value is MutableList) value else value.toMutableList()
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
        val item = mData[position]

        val codeSms = item.codeSms
        val text = codeSms.raw
        holder.tvSms.text = text

        // 当前的 DbSms 被处于被选取状态，改变 DbSms 的背景颜色
        holder.vMask.visibility = if (isSelected(position)) View.VISIBLE else View.INVISIBLE
        holder.tvSms.maxLines = if (isExpanded(position)) Int.MAX_VALUE else 1
    }

    val selectedItems: List<DbSms>
        get() = selectedItemsPosition.map { mData[it] }

    fun setSelectionAll(selected: Boolean = true) {
        mData.forEachIndexed { index, _ ->
            setSelection(index, selected)
        }
    }

    fun toggleSelectionAll() {
        mData.forEachIndexed { index, _ ->
            toggleSelection(index)
        }
    }

    fun isExpanded(position: Int): Boolean = mExpandedItems.get(position, false)

    fun toggleExpansion(position: Int) {
        if (mExpandedItems.get(position, false)) {
            mExpandedItems.delete(position)
        } else {
            mExpandedItems.put(position, true)
        }
        notifyItemChanged(position)
    }
}