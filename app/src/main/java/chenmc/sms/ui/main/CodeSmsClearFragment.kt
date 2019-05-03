package chenmc.sms.ui.main

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.DataSetObserver
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import chenmc.sms.code.helper.R
import chenmc.sms.data.CodeSms
import chenmc.sms.data.ExpressCodeSms
import chenmc.sms.data.VerificationCodeSms
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.transaction.SmsAnalyzer
import chenmc.sms.ui.app.PermissionFragment
import chenmc.sms.ui.interfaces.IOnBackPressedActivity
import chenmc.sms.ui.interfaces.IOnBackPressedFragment
import java.lang.ref.WeakReference
import java.util.*

/**
 * @author 明明
 * Created on 2017/8/11.
 */

class CodeSmsClearFragment : PermissionFragment(), View.OnClickListener, ActionMode.Callback,
        AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, IOnBackPressedFragment {

    private var mPreviousActionBarTitle: CharSequence? = null

    // 显示短信内容的 TextView
    private lateinit var tvSms: TextView

    // 显示短信内容界面中容纳上面的 TextView 的 ViewGroup
    private lateinit var viewSmsLayout: LinearLayout

    // 显示短信内容界面的透明灰色背景
    private lateinit var viewSmsLayoutBg: View

    // 当前 Activity 界面中的 ListView 的 Adapter
    private lateinit var adapter: ListViewAdapter

    // ListView 的 Item 长按时出现的 ActionMode
    private var actionMode: ActionMode? = null

    private val attachActivity: PreferenceActivity
        get() = activity as PreferenceActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_code_sms_clear, container, false)
        init(root)

        return root
    }

    private fun init(root: View) {
        activity.actionBar?.apply {
            mPreviousActionBarTitle = this.title
            this.setTitle(R.string.enable_clear_sms)
            this.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        tvSms = root.findViewById(R.id.tv_sms)
        viewSmsLayoutBg = root.findViewById(R.id.view_sms_layout_bg)
        viewSmsLayoutBg.setOnClickListener(this)
        viewSmsLayout = root.findViewById(R.id.view_sms_layout)

        adapter = ListViewAdapter(activity)

        val listView: ListView = root.findViewById(R.id.list_view)
        listView.adapter = adapter
        listView.onItemClickListener = this
        listView.onItemLongClickListener = this

        val progressBar = root.findViewById<ProgressBar>(R.id.progress_bar)
        ReadDataTask().execute(activity, progressBar, adapter)
    }

    override fun onStart() {
        super.onStart()
        if (activity is IOnBackPressedActivity) {
            (activity as IOnBackPressedActivity).setFocusFragment(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_code_sms_clear, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (adapter.count == 0) {
            menu.findItem(R.id.menu_delete).isVisible = false
        }
    }

    override fun onDetach() {
        val actionBar = activity.actionBar
        if (actionBar != null) {
            actionBar.title = mPreviousActionBarTitle
            actionBar.setDisplayHomeAsUpEnabled(false)
        }
        super.onDetach()
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            try {
                val defaultSmsApp = AppPreference.defaultSmsApp
                // 尝试获取应用信息，如果抛出异常，则该应用不存在
                activity.packageManager.getApplicationInfo(defaultSmsApp, 0)

                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp)
                startActivity(intent)

                Toast.makeText(activity, R.string.change_default_mms_to_default, Toast.LENGTH_LONG).show()
            } catch (e: PackageManager.NameNotFoundException) {
                // ignored
            }
        }
        super.onDestroy()
    }

    private fun updateActionModeTitle() {
        actionMode?.title = adapter.itemCheckedCount.toString() +
                            "/" + adapter.count
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> attachActivity.onBackPressed()
            R.id.menu_delete -> {
                val clickListener = DialogInterface.OnClickListener { _, _ ->
                    adapter.setAllItemsChecked(true)
                    adapter.removeAllCheckedItems()
                }

                AlertDialog.Builder(activity)
                    .setTitle(R.string.dialog_title_delete_all_sms)
                    .setMessage(R.string.dialog_message_delete_all_sms)
                    .setPositiveButton(R.string.ok, clickListener)
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showViewSmsLayout() {
        if (viewSmsLayoutBg.visibility == View.INVISIBLE) {
            //region 显示界面时伴随的动画
            val translateAnim = AnimationUtils.loadAnimation(activity, R.anim.translate_top_bottom)
            val alphaAnim = AnimationUtils.loadAnimation(activity, R.anim.alpha_show)
            viewSmsLayoutBg.startAnimation(alphaAnim)
            viewSmsLayout.startAnimation(translateAnim)
            //endregion

            viewSmsLayoutBg.visibility = View.VISIBLE
            viewSmsLayoutBg.isClickable = true
            viewSmsLayout.visibility = View.VISIBLE
        }
    }

    /*
     * 隐藏添加规则界面
     */
    private fun hideViewSmsLayout() {
        if (viewSmsLayoutBg.visibility == View.VISIBLE) {
            //region 隐藏界面时伴随的动画
            val translateAnim = AnimationUtils.loadAnimation(activity, R.anim.translate_bottom_top)
            val alphaAnim = AnimationUtils.loadAnimation(activity, R.anim.alpha_hide)
            viewSmsLayoutBg.startAnimation(alphaAnim)
            viewSmsLayout.startAnimation(translateAnim)
            //endregion

            // 这里隐藏界面使用 View.INVISIBLE 而不使用 View.GONE，是因为在使用 View.GONE 时，
            // 在第一次将 View 设置为 View.VISIBLE 的时候，获取 View 的尺寸的结果将会返回 0
            viewSmsLayoutBg.visibility = View.INVISIBLE
            // 防止播放动画过程中被点击，触发点击事件
            viewSmsLayoutBg.isClickable = false
            viewSmsLayout.visibility = View.INVISIBLE
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.menu_list_action, menu)

        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_invert_select -> {
                adapter.toggleAllItemChecked()
                updateActionModeTitle()
            }
            R.id.menu_select_all -> {
                adapter.setAllItemsChecked(true)
                updateActionModeTitle()
            }
            R.id.menu_delete -> {
                val clickListener = DialogInterface.OnClickListener { _, _ ->
                    val success = adapter.removeAllCheckedItems()
                    actionMode?.finish()
                    if (!success) {
                        Toast.makeText(activity, R.string.delete_fail, Toast.LENGTH_SHORT).show()
                    }
                }

                AlertDialog.Builder(activity)
                    .setTitle(R.string.dialog_title_delete_all_checked_sms)
                    .setMessage(R.string.dialog_message_delete_all_checked_sms)
                    .setPositiveButton(R.string.ok, clickListener)
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show()
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
        adapter.setAllItemsChecked(false)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.view_sms_layout_bg -> attachActivity.onBackPressed()
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        // 如果当前处于非多选模式
        if (actionMode == null) {
            val item = adapter.getItem(position)
            tvSms.text = item.sms
            showViewSmsLayout()

            // 如果当前处于多选模式
        } else {
            adapter.toggleItemChecked(position)
            if (adapter.itemCheckedCount == 0) {
                actionMode?.finish()
            } else {
                updateActionModeTitle()
            }
        }
    }

    override fun onItemLongClick(parent: AdapterView<*>, view: View, position: Int, id: Long): Boolean {
        if (actionMode == null) {
            actionMode = activity.startActionMode(this)
            adapter.toggleItemChecked(position)
            updateActionModeTitle()

            return true
        }

        return false
    }

    override fun onBackPressed(): Boolean {
        if (viewSmsLayoutBg.visibility == View.VISIBLE) {
            hideViewSmsLayout()
            return true
        }
        return false
    }

    private inner class ListViewAdapter internal constructor(private val context: Context) : BaseAdapter() {
        private val data = ArrayList<WrappedItem>(0)

        /**
         * 获取处于选取状态的 Item 数量
         *
         * @return 处于选取状态的 Item 数量
         */
        internal var itemCheckedCount: Int = 0
            private set

        init {
            registerDataSetObserver(object : DataSetObserver() {
                override fun onChanged() {
                    activity?.invalidateOptionsMenu()
                }
            })
        }

        internal fun setData(list: List<Item>) {
            data.clear()
            for (item in list) {
                data.add(WrappedItem(item, false))
            }
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(position: Int): Item {
            return data[position].item
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            val viewHolder: ViewHolder
            val itemView: View = if (convertView == null) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                val tv = view.findViewById<TextView>(R.id.list_item_text)
                viewHolder = ViewHolder(tv)

                view.tag = viewHolder
                view
            } else {
                viewHolder = convertView.tag as ViewHolder
                convertView
            }

            val wrappedItem = data[position]
            val text = when (val codeSms = wrappedItem.item.codeSms) {
                is VerificationCodeSms ->
                    context.getString(R.string.provider_code_value, codeSms.serviceProvider, codeSms.code)
                is ExpressCodeSms ->
                    context.getString(R.string.provider_express_value, codeSms.serviceProvider, codeSms.code)
                else -> ""
            }
            viewHolder.text.text = text
            // 当前的 Item 被处于被选取状态，改变 Item 的背景颜色
            if (wrappedItem.isSelected) {
                itemView.setBackgroundResource(R.color.listItemChecked)
            } else {
                itemView.setBackgroundResource(android.R.color.transparent)
            }

            return itemView
        }

        /**
         * 删除所有被选取的 Item
         *
         * @return 删除成功返回 true，否则返回 false
         */
        internal fun removeAllCheckedItems(): Boolean {
            val beanList = ArrayList<Item>()
            for (i in data.indices.reversed()) {
                if (data[i].isSelected) {
                    beanList.add(data[i].item)
                }
            }
            val deleteSuccessList = deleteCodeSmsFromDatabase(
                    context, beanList)
            for (e in deleteSuccessList) {
                for (wrapper in data) {
                    if (wrapper.item === e) {
                        data.remove(wrapper)
                        break
                    }
                }
            }

            notifyDataSetChanged()

            return deleteSuccessList.size == beanList.size
        }

        /**
         * 统一设置所有 Item 的选取状态
         *
         * @param checked true 则设置所有 Item 为选取状态，false 则设置所有 Item 为未选取状态
         */
        internal fun setAllItemsChecked(checked: Boolean) {
            for (wrapper in data) {
                wrapper.isSelected = checked
            }
            itemCheckedCount = if (checked) data.size else 0

            notifyDataSetChanged()
        }

        /**
         * 更改给定位置的 Item 的选取状态
         *
         * @param position Item 在 ListView 中的位置
         */
        internal fun toggleItemChecked(position: Int) {
            val wrapper = data[position]
            wrapper.isSelected = !wrapper.isSelected

            if (wrapper.isSelected) {
                itemCheckedCount++
            } else {
                itemCheckedCount--
            }

            notifyDataSetChanged()
        }

        /**
         * 反转所有的 Item 的选取状态
         */
        internal fun toggleAllItemChecked() {
            for (wrapper in data) {
                wrapper.isSelected = !wrapper.isSelected

                if (wrapper.isSelected) {
                    itemCheckedCount++
                } else {
                    itemCheckedCount--
                }
            }

            notifyDataSetChanged()
        }

        /**
         * 删除系统中的验证码和取件码短信
         * @param context 上下文
         * @param deleteList 将要删除的包含验证码和取件码短信的线性表
         * @return 删除成功的 List<SmsHandler>
        </SmsHandler> */
        private fun deleteCodeSmsFromDatabase(context: Context, deleteList: List<Item>): List<Item> {
            val deleteSuccessList = ArrayList<Item>(deleteList.size)

            val contentResolver = context.contentResolver

            for (it in deleteList) {
                val count = contentResolver.delete(Uri.parse("content://sms/"), "_id = ?",
                        arrayOf(it.databaseId.toString()))

                if (count > 0) {
                    deleteSuccessList.add(it)
                }
            }

            return deleteSuccessList
        }

        private inner class ViewHolder internal constructor(internal var text: TextView)

    }

    private class WrappedItem internal constructor(val item: Item, var isSelected: Boolean)

    private class Item(
        var databaseId: Int,
        var sms: String,
        var codeSms: CodeSms
    )

    private class ReadDataTask : AsyncTask<Any, Void, List<Item>>() {
        private lateinit var wrProgressBar: WeakReference<ProgressBar>
        private lateinit var wrAdapter: WeakReference<ListViewAdapter>

        override fun doInBackground(vararg params: Any): List<Item> {
            wrProgressBar = WeakReference(params[1] as ProgressBar)
            wrAdapter = WeakReference(params[2] as ListViewAdapter)

            return getCodeSmsFromDatabase(params[0] as Context)
        }

        override fun onPostExecute(list: List<Item>) {
            wrProgressBar.get()?.visibility = View.GONE
            wrAdapter.get()?.setData(list)
        }

        /**
         * 获取系统中所有的验证码和取件码短信
         * @param context 上下文
         * @return 包含所有验证码和取件码短信的线性表
         */
        private fun getCodeSmsFromDatabase(context: Context): List<Item> {
            val list = ArrayList<Item>()

            val cr = context.contentResolver
            // 获取接收到的短信（type = 1）
            val cur = cr.query(Uri.parse("content://sms/"),
                    arrayOf("_id", "body"), "type = ?", arrayOf(1.toString()), "date desc")
                      ?: return list

            try {
                val smsAnalyzer = SmsAnalyzer(context)

                while (cur.moveToNext()) {
                    val sms = cur.getString(cur.getColumnIndex("body"))

                    val vcs = smsAnalyzer.analyseVerificationSms(sms)
                    val ecs = smsAnalyzer.analyseExpressSms(sms)

                    if (vcs != null || ecs != null) {
                        list.add(Item(
                                cur.getInt(cur.getColumnIndex("_id")),
                                sms,
                                vcs ?: ecs!!
                        ))
                    }
                }
            } catch (ex: SQLiteCantOpenDatabaseException) {
                Toast.makeText(context, R.string.error_occurred_while_read_sms, Toast.LENGTH_SHORT).show()
            }

            cur.close()

            return list
        }

    }
}
