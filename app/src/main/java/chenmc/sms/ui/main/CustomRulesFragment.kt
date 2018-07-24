package chenmc.sms.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.CustomRulesBackuper
import chenmc.sms.data.storage.SmsCodeRegex
import chenmc.sms.data.storage.SmsCodeRegexDao
import chenmc.sms.ui.app.PermissionFragment
import chenmc.sms.ui.interfaces.IOnBackPressedActivity
import chenmc.sms.ui.interfaces.IOnBackPressedFragment
import chenmc.sms.ui.interfaces.IOnRequestPermissionsResult
import chenmc.sms.util.FileChooserDialog
import chenmc.sms.util.SmsMatchRuleUtil
import chenmc.sms.util.ToastUtil
import com.melnykov.fab.FloatingActionButton
import java.lang.ref.WeakReference

/**
 * @author 明明
 * Created on 2017/8/11.
 */
class CustomRulesFragment : PermissionFragment(), IOnRequestPermissionsResult, IOnBackPressedFragment {

    private lateinit var fab: FloatingActionButton
    // 添加规则界面的短信内容输入框
    private lateinit var etSms: EditText
    // 添加规则界面的短信验证码输入框
    private lateinit var etCode: EditText
    // 添加规则界面中容纳上面的 EditText 的 ViewGroup
    private lateinit var addRuleLayout: LinearLayout
    // 显示上面 EditText 的内容错误提示的 TextView
    private lateinit var tvTips: TextView
    // 添加规则界面的透明灰色背景
    private lateinit var addRuleLayoutBg: View
    // 当前 Activity 界面中的 ListView 的 Adapter
    private val adapter = ListViewAdapter()
    // 正在修改中的 ListView Item 在 ListView 中的位置
    private var currentItemPosition: Int = -1

    private lateinit var smsCodeRegexDao: SmsCodeRegexDao

    private val listener = Listener()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_code_match_rules, container, false)
        init(root)
        return root
    }

    private fun init(root: View) {
        // 初始化编辑面板
        etSms = root.findViewById(R.id.et_sms)
        etCode = root.findViewById(R.id.et_code)
        tvTips = root.findViewById(R.id.tv_tips)
        addRuleLayoutBg = root.findViewById(R.id.add_rule_layout_bg)
        addRuleLayout = root.findViewById(R.id.add_rule_layout)
        addRuleLayoutBg.setOnClickListener(listener)
        // 先隐藏编辑面板
        addRuleLayoutBg.visibility = View.INVISIBLE
        addRuleLayout.visibility = View.INVISIBLE
        tvTips.visibility = View.GONE

        // 初始化悬浮按钮
        fab = root.findViewById(R.id.fab)
        fab.setOnClickListener(listener)

        etSms.onFocusChangeListener = listener
        // 使 EditText 可以响应输入键盘按钮点击事件
        etCode.setOnEditorActionListener(listener)

        etSms.addTextChangedListener(listener)
        etCode.addTextChangedListener(listener)

        // 初始化 ListView
        val listView = root.findViewById<ListView>(R.id.list_view)
        listView.adapter = adapter
        listView.onItemClickListener = listener
        listView.onItemLongClickListener = listener

        smsCodeRegexDao = SmsCodeRegexDao(activity)
        refreshData()
    }

    private fun refreshData() {
        ReadDataTask(adapter, smsCodeRegexDao).execute()
    }

    override fun onStart() {
        super.onStart()
        // 初始化 ActionBar
        val actionBar = activity.actionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.pref_custom_rules)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)

        if (activity is IOnBackPressedActivity) {
            (activity as IOnBackPressedActivity).setFocusFragment(this)
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPermissionGranted(requestCode: Int, grantedPermissions: Array<String>) {
        handleRequestPermissionsResult(requestCode)
    }

    override fun onPermissionDenied(requestCode: Int, deniedPermissions: Array<String>,
                                    deniedAlways: BooleanArray) {
        handleRequestPermissionsResult(requestCode)
    }

    private fun handleRequestPermissionsResult(requestCode: Int) {
        when (requestCode) {
            REQUEST_CODE_READ_STORAGE -> {
                FileChooserDialog.Builder(activity)
                    .setTitle(R.string.choose_import_dir)
                    .setChooseType(FileChooserDialog.TYPE_FILE)
                    .setOnClickListener { _, chooseFile ->
                        if (chooseFile != null) {
                            val successful = CustomRulesBackuper(smsCodeRegexDao).restore(chooseFile)
                            if (successful) {
                                refreshData()
                                ToastUtil.showToast(R.string.import_success, Toast.LENGTH_SHORT)
                            } else {
                                ToastUtil.showToast(R.string.import_fail, Toast.LENGTH_SHORT)
                            }
                        } else {
                            ToastUtil.showToast(R.string.file_not_choose, Toast.LENGTH_SHORT)
                        }
                    }
                    .create()
                    .show()
            }
            REQUEST_CODE_WRITE_STORAGE -> {
                FileChooserDialog.Builder(activity)
                    .setChooseType(FileChooserDialog.TYPE_DIR)
                    .setTitle(R.string.choose_export_dir)
                    .setOnClickListener { _, chooseFile ->
                        if (chooseFile != null && chooseFile.exists()) {
                            CustomRulesBackuper(smsCodeRegexDao).backup(chooseFile)
                            ToastUtil.showToast(R.string.export_success, Toast.LENGTH_SHORT)
                        } else {
                            ToastUtil.showToast(R.string.export_fail, Toast.LENGTH_SHORT)
                        }
                    }
                    .create()
                    .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_custom_rules, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        if (addRuleLayout.visibility == View.VISIBLE) {
            // 如果编辑界面的是可见的，则隐藏与编辑无关的菜单选项
            menu.setGroupVisible(R.id.menu_group_backup, false)
        } else {
            // 如果编辑界面的是不可见的，则隐藏与编辑相关的菜单选项
            menu.findItem(R.id.menu_finish).isVisible = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity.onBackPressed()
                return true
            }
            R.id.menu_import_rules -> {
                // 导入规则
                // 请求读取存储权限
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                } else {
                    "android.permission.READ_EXTERNAL_STORAGE"
                }
                requestPermissions(REQUEST_CODE_READ_STORAGE,
                        arrayOf(permission), this)
                return true
            }
            R.id.menu_export_rules -> {
                // 导出规则
                // 请求写入存储权限
                requestPermissions(REQUEST_CODE_WRITE_STORAGE,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), this)
                return true
            }
            R.id.menu_finish -> {
                // 完成并保存当前的编辑
                listener.onEditorAction(etCode, EditorInfo.IME_ACTION_DONE, null)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /*
     * 显示添加规则界面
     */
    private fun showAddRuleLayout() {
        if (addRuleLayoutBg.visibility == View.VISIBLE)
            return

        //region 显示界面时伴随的动画
        val translateAnim = AnimationUtils.loadAnimation(activity, R.anim.translate_top_bottom)
        val alphaAnim = AnimationUtils.loadAnimation(activity, R.anim.alpha_show)
        addRuleLayoutBg.startAnimation(alphaAnim)
        addRuleLayout.startAnimation(translateAnim)
        //endregion

        addRuleLayoutBg.visibility = View.VISIBLE
        addRuleLayoutBg.isClickable = true
        addRuleLayout.visibility = View.VISIBLE

        translateAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // 动画结束时，为输入短信的 EditText 获取焦点
                etSms.requestFocus()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        fab.hide()
    }

    /*
     * 隐藏添加规则界面
     */
    private fun hideAddRuleLayout(): Boolean {
        if (addRuleLayoutBg.visibility != View.VISIBLE)
            return false

        //region 隐藏界面时伴随的动画
        val translateAnim = AnimationUtils.loadAnimation(activity, R.anim.translate_bottom_top)
        val alphaAnim = AnimationUtils.loadAnimation(activity, R.anim.alpha_hide)
        addRuleLayoutBg.startAnimation(alphaAnim)
        addRuleLayout.startAnimation(translateAnim)
        //endregion

        // 这里隐藏界面使用 View.INVISIBLE 而不使用 View.GONE，是因为在使用 View.GONE 时，
        // 在第一次将 View 设置为 View.VISIBLE 的时候，获取 View 的尺寸的结果将会返回 0
        addRuleLayoutBg.visibility = View.INVISIBLE
        // 防止播放动画过程中被点击，触发点击事件
        addRuleLayoutBg.isClickable = false
        addRuleLayout.visibility = View.INVISIBLE
        tvTips.visibility = View.GONE

        // 隐藏界面的同时隐藏键盘
        val inputMethodManager = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(addRuleLayoutBg.windowToken, 0)

        translateAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // 动画结束时，将所有的 EditText 的内容清除
                etSms.setText("")
                etCode.setText("")
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })


        fab.show()
        return true
    }

    private fun updateActionModeTitle() {
        listener.actionMode?.title = adapter.itemCheckedCount.toString() +
                "/" + adapter.count
    }

    override fun onBackPressed(): Boolean {
        // 隐藏编辑界面并重新创建 Menu
        if (hideAddRuleLayout()) {
            activity.invalidateOptionsMenu()
            currentItemPosition = -1
            return true
        }
        return false
    }

    private inner class Listener : TextWatcher, View.OnClickListener, View.OnFocusChangeListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, TextView.OnEditorActionListener, ActionMode.Callback {

        // ListView 的 Item 长按时出现的 ActionMode
        internal var actionMode: ActionMode? = null

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable) {
            // 隐藏提示
            tvTips.visibility = View.GONE
        }

        override fun onClick(v: View) {
            when (v.id) {
                R.id.fab -> {
                    // 如果当前处于多选模式，则先退出多选模式
                    actionMode?.finish()
                    showAddRuleLayout()
                    activity.invalidateOptionsMenu()
                }
            // 点击编辑界面背景时模拟返回按钮点击事件
                R.id.add_rule_layout_bg -> onBackPressed()
            }
        }

        override fun onFocusChange(v: View, hasFocus: Boolean) {
            // 输入短信的 EditText 获得焦点，把光标移动到末尾，并显示输入键盘
            if (v.id == R.id.et_sms && hasFocus) {
                etSms.setSelection(etSms.text.length)
                val inputMethodManager = activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            // 如果当前处于非编辑模式
            if (actionMode == null) {
                currentItemPosition = position
                val item = adapter.getItem(position)

                etSms.setText(item.sms)
                etCode.setText(item.verificationCode)

                showAddRuleLayout()
                activity.invalidateOptionsMenu()

                // 如果当前处于编辑模式
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
            // 当前未处于多选模式
            if (actionMode == null) {
                // ListView 中的 Item 被长按时进入多选模式
                actionMode = activity.startActionMode(this)
                adapter.toggleItemChecked(position)
                updateActionModeTitle()

                return true
            }
            return false
        }

        override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
            when (v.id) {
                R.id.et_code -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        val resultCode: Int
                        if (currentItemPosition >= 0) {
                            // 当前正在修改 ListView 中的某个 Item
                            val item = adapter.getItem(currentItemPosition)
                            val itemClone = SmsCodeRegex(id = item.id).apply {
                                this.sms = etSms.text.toString()
                                this.verificationCode = etCode.text.toString()
                            }
                            resultCode = SmsMatchRuleUtil.handleItem(itemClone)

                            if (SmsMatchRuleUtil.HANDLE_RESULT_SUCCESS == resultCode) {
                                smsCodeRegexDao.update(itemClone)
                                adapter.update(currentItemPosition, itemClone)
                            }
                        } else {
                            // 当前正在给 ListView 添加一个 Item
                            val bean = SmsCodeRegex(0,
                                    etSms.text.toString(), etCode.text.toString(), null)
                            resultCode = SmsMatchRuleUtil.handleItem(bean)

                            if (SmsMatchRuleUtil.HANDLE_RESULT_SUCCESS == resultCode) {
                                smsCodeRegexDao.insert(bean)
                                adapter.add(bean)
                            }
                        }

                        when (resultCode) {
                            SmsMatchRuleUtil.HANDLE_ERROR_EMPTY_CONTENT -> {
                                tvTips.visibility = View.VISIBLE
                                tvTips.setText(R.string.edit_text_no_content)
                            }
                            SmsMatchRuleUtil.HANDLE_ERROR_NO_CONTAINS -> {
                                tvTips.visibility = View.VISIBLE
                                tvTips.setText(R.string.sms_not_contains_code)
                            }
                            SmsMatchRuleUtil.HANDLE_RESULT_SUCCESS -> {
                                currentItemPosition = -1
                                onBackPressed()
                            }
                            else -> {
                                currentItemPosition = -1
                                onBackPressed()
                            }
                        }
                    }
                    return true
                }
            }

            return false
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
                R.id.menu_select_all -> {
                    adapter.setAllItemsChecked(true)
                    updateActionModeTitle()
                    return true
                }
                R.id.menu_invert_select -> {
                    adapter.toggleAllItemChecked()
                    updateActionModeTitle()
                    return true
                }
                R.id.menu_delete -> {
                    val clickListener = DialogInterface.OnClickListener { _, _ ->
                        adapter.removeAllCheckedItems()
                        actionMode?.finish()
                    }

                    AlertDialog.Builder(activity)
                        .setTitle(R.string.dialog_title_delete_all_checked_rules)
                        .setMessage(R.string.dialog_message_delete_all_checked_rules)
                        .setPositiveButton(R.string.ok, clickListener)
                        .setNegativeButton(R.string.cancel, null)
                        .create()
                        .show()
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            // 退出多选模式的同时取消所有的选择
            adapter.setAllItemsChecked(false)
        }
    }

    private inner class ListViewAdapter internal constructor() : BaseAdapter() {

        internal var data: MutableList<WrappedItem> = ArrayList(0)

        // 当前被选择的 Item 的个数
        /**
         * 获取处于选取状态的 Item 数量
         *
         * @return 处于选取状态的 Item 数量
         */
        internal var itemCheckedCount: Int = 0
            private set

        override fun getCount(): Int {
            return data.size
        }

        override fun getItem(position: Int): SmsCodeRegex {
            return data[position].item
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var itemView = convertView
            val viewHolder: ViewHolder
            if (itemView == null) {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
                val tv = itemView.findViewById<TextView>(R.id.list_item_text)
                viewHolder = ViewHolder(tv)

                itemView.tag = viewHolder
            } else {
                viewHolder = itemView.tag as ViewHolder
            }

            val beanWrapper = data[position]
            viewHolder.text.text = beanWrapper.item.sms
            // 当前的 Item 被处于被选取状态，改变 Item 的背景颜色
            if (beanWrapper.isSelected) {
                (viewHolder.text.parent as ViewGroup).setBackgroundResource(R.color.listItemChecked)
            } else {
                (viewHolder.text.parent as ViewGroup).setBackgroundResource(android.R.color.transparent)
            }

            return itemView
        }

        /**
         * 添加一个 Item
         *
         * @param bean 待添加的 Item
         */
        internal fun add(bean: SmsCodeRegex) {
            data.add(WrappedItem(bean, false))

            notifyDataSetChanged()
        }

        /**
         * 修改 position 位置的 Item
         *
         * @param position Item 在 ListView 中的位置
         * @param newBean  新的 Item
         */
        internal fun update(position: Int, newBean: SmsCodeRegex) {
            data[position].item = newBean
            notifyDataSetChanged()
        }

        /**
         * 删除所有被选取的 Item
         */
        internal fun removeAllCheckedItems() {
            val beanList = ArrayList<SmsCodeRegex>()
            for (i in data.indices.reversed()) {
                if (data[i].isSelected) {
                    beanList.add(data[i].item)
                    data.removeAt(i)
                }
            }
            smsCodeRegexDao.delete(*beanList.toTypedArray())

            notifyDataSetChanged()
        }

        /**
         * 统一设置所有 Item 的选取状态
         *
         * @param checked true 则设置所有 Item 为选取状态，false 则设置所有 Item 为未选取状态
         */
        internal fun setAllItemsChecked(checked: Boolean) {
            for (beanWrapper in data) {
                beanWrapper.isSelected = checked
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
            val beanWrapper = data[position]
            beanWrapper.isSelected = !beanWrapper.isSelected

            if (beanWrapper.isSelected) {
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
            for (beanWrapper in data) {
                beanWrapper.isSelected = !beanWrapper.isSelected

                if (beanWrapper.isSelected) {
                    itemCheckedCount++
                } else {
                    itemCheckedCount--
                }
            }

            notifyDataSetChanged()
        }

        private inner class ViewHolder internal constructor(internal var text: TextView)

    }

    private class WrappedItem internal constructor(var item: SmsCodeRegex, var isSelected: Boolean)

    private class ReadDataTask(adapter: ListViewAdapter, val dao: SmsCodeRegexDao) :
            AsyncTask<Any, Int, List<SmsCodeRegex>>() {

        private val wrAdapter: WeakReference<ListViewAdapter> = WeakReference(adapter)

        override fun doInBackground(vararg args: Any): List<SmsCodeRegex> {
            return dao.selectAll()
        }

        override fun onPostExecute(smsCodeRegexes: List<SmsCodeRegex>) {
            wrAdapter.get()?.apply {
                this.data = smsCodeRegexes.mapTo(ArrayList(smsCodeRegexes.size)) {
                    WrappedItem(it, false)
                }
                this.notifyDataSetChanged()
            }
        }
    }

    companion object {

        private const val REQUEST_CODE_READ_STORAGE = 0
        private const val REQUEST_CODE_WRITE_STORAGE = 1
    }
}
