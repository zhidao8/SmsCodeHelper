package chenmc.sms.ui.main.adapter

import android.util.SparseBooleanArray
import androidx.recyclerview.widget.RecyclerView

abstract class SelectableAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    private val mSelectedItems: SparseBooleanArray = SparseBooleanArray(0)

    val selectedCount: Int
        get() = mSelectedItems.size()

    val selectedItemsPosition: List<Int>
        get() = (0 until mSelectedItems.size()).map { mSelectedItems.keyAt(it) }

    fun isSelected(position: Int): Boolean {
        for (i in 0 until mSelectedItems.size()) {
            if (mSelectedItems.keyAt(i) == position) {
                return true
            }
        }
        return false
    }

    fun toggleSelection(position: Int) {
        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position)
        } else {
            mSelectedItems.put(position, true)
        }
        notifyItemChanged(position)
    }

    fun setSelection(position: Int, selected: Boolean = true) {
        if (selected) {
            mSelectedItems.put(position, true)
        } else {
            mSelectedItems.delete(position)
        }
        notifyItemChanged(position)
    }

    fun clearSelection() {
        mSelectedItems.clear()
        notifyDataSetChanged()
    }
}