package chenmc.sms.ui.main.customrules

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.CustomRulesBackuper
import chenmc.sms.data.storage.SmsCodeRegex
import chenmc.sms.data.storage.SmsCodeRegexDao
import chenmc.sms.ui.interfaces.OnBackPressedActivity
import chenmc.sms.ui.interfaces.OnBackPressedFragment
import chenmc.sms.ui.interfaces.OnItemClickListener
import chenmc.sms.ui.interfaces.OnItemLongClickListener
import chenmc.sms.ui.main.codesmsclear.VH
import chenmc.sms.util.FileChooserDialog
import chenmc.sms.util.ToastUtil
import com.melnykov.fab.FloatingActionButton
import com.tbruyelle.rxpermissions2.RxPermissions

/**
 * @author 明明
 * Created on 2017/8/11.
 */
class CustomRulesFragment : Fragment(), OnBackPressedFragment {

    private lateinit var mFab: FloatingActionButton
    private lateinit var mProgressBar: ProgressBar

    private val mAdapter: CustomRuleAdapter = CustomRuleAdapter()

    private lateinit var mDao: SmsCodeRegexDao

    private lateinit var mRxPermissions: RxPermissions

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mRxPermissions = RxPermissions(this)

        activity?.let { activity ->
            mDao = SmsCodeRegexDao.getInstance(activity)
            LoaderManager.getInstance(this).initLoader(LOADER_ID_CUSTOM_RULES, null, loaderCallbacks)
        }
    }

    private val loaderCallbacks =
        object : LoaderManager.LoaderCallbacks<MutableList<SmsCodeRegex>> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<MutableList<SmsCodeRegex>> {
                mProgressBar.visibility = View.VISIBLE
                return CustomRulesLoader(activity!!)
            }

            override fun onLoadFinished(loader: Loader<MutableList<SmsCodeRegex>>, data: MutableList<SmsCodeRegex>) {
                mAdapter.setData(data)
                mProgressBar.visibility = View.GONE
            }

            override fun onLoaderReset(loader: Loader<MutableList<SmsCodeRegex>>) {}
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_code_match_rules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = view.context

        // 初始化悬浮按钮
        mFab = view.findViewById(R.id.fab)
        mFab.setOnClickListener(mListener)

        mProgressBar = view.findViewById(R.id.progressBar)

        mAdapter.onItemClickListener = mListener
        mAdapter.onItemLongClickListener = mListener

        // 初始化列表
        view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom)
        }
    }

    private val mListener = object : View.OnClickListener, ActionMode.Callback,
        OnItemClickListener<VH>, OnItemLongClickListener<VH> {

        override fun onClick(v: View?) {
            openEditDialog()
        }

        // ListView 的 DbSms 长按时出现的 ActionMode
        var actionMode: ActionMode? = null

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
                    mAdapter.setSelectionAll(true)
                    updateActionModeTitle()
                    return true
                }
                R.id.menu_invert_select -> {
                    mAdapter.toggleSelectionAll()
                    updateActionModeTitle()
                    return true
                }
                R.id.menu_delete -> {
                    val list = mAdapter.selectedItems

                    val clickListener = DialogInterface.OnClickListener { _, _ ->
                        deleteDbData(list)
                        actionMode?.finish()
                    }

                    AlertDialog.Builder(activity)
                        .setTitle(R.string.dialog_title_delete_custom_rules)
                        .setMessage(getString(R.string.dialog_message_delete_custom_rules, list.size))
                        .setPositiveButton(R.string.delete, clickListener)
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
            mAdapter.setSelectionAll(false)
        }

        override fun onClick(holder: VH) {
            val position = holder.adapterPosition

            if (actionMode == null) {
                // 如果当前处于非多选模式
                openEditDialog(mAdapter.getItem(position))
            } else {
                // 如果当前处于多选模式
                mAdapter.toggleSelection(position)
                if (mAdapter.selectedCount == 0) {
                    actionMode?.finish()
                } else {
                    updateActionModeTitle()
                }
            }
        }

        override fun onLongClick(holder: VH): Boolean {
            val position = holder.adapterPosition

            if (actionMode == null) {
                actionMode = activity?.startActionMode(this)
                mAdapter.toggleSelection(position)
                updateActionModeTitle()
                return true
            }
            return false
        }
    }

    private fun updateActionModeTitle() {
        mListener.actionMode?.title = "${mAdapter.selectedCount}/${mAdapter.itemCount}"
    }

    private fun openEditDialog(data: SmsCodeRegex? = null) {
        CustomRuleDialogFragment(data).apply {
            onDeliverResult = mOnDeliverResult
        }.show(fragmentManager, CustomRuleDialogFragment::class.java.simpleName)
    }

    private val mOnDeliverResult = object : CustomRuleDialogFragment.OnDeliverResult {
        override fun deliverResult(data: SmsCodeRegex, isNew: Boolean) {
            if (isNew) {
                val result = mDao.insert(data)
                if (result.isEmpty() || result[0] == -1L) {
                    ToastUtil.showSingletonShortToast(R.string.rule_repetition)
                }
            } else {
                if (mDao.update(data) == 0) {
                    ToastUtil.showSingletonShortToast(R.string.rule_repetition)
                }
            }
            LoaderManager.getInstance(this@CustomRulesFragment)
                .restartLoader(LOADER_ID_CUSTOM_RULES, null, loaderCallbacks)
        }
    }

    private fun deleteDbData(data: List<SmsCodeRegex>) {
        mDao.delete(*data.toTypedArray())
        LoaderManager.getInstance(this@CustomRulesFragment)
            .restartLoader(LOADER_ID_CUSTOM_RULES, null, loaderCallbacks)
    }

    override fun onStart() {
        super.onStart()
        // 初始化 ActionBar
        activity?.let { activity ->
            when (activity) {
                is AppCompatActivity -> {
                    activity.supportActionBar?.run {
                        setTitle(R.string.pref_custom_rules)
                        setDisplayHomeAsUpEnabled(true)
                    }
                }
                else -> {
                    activity.actionBar?.run {
                        setTitle(R.string.pref_custom_rules)
                        setDisplayHomeAsUpEnabled(true)
                    }
                }
            }
        }
        setHasOptionsMenu(true)

        if (activity is OnBackPressedActivity) {
            (activity as OnBackPressedActivity).setFocusFragment(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_custom_rules, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            R.id.menu_import_rules -> {
                // 导入规则
                // 请求读取存储权限
                mRxPermissions
                    .requestEach(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        else
                            "android.permission.READ_EXTERNAL_STORAGE"
                    )
                    .subscribe { permission ->
                        if (permission.granted) {
                            FileChooserDialog.Builder(activity)
                                .setTitle(R.string.choose_import_dir)
                                .setChooseType(FileChooserDialog.TYPE_FILE)
                                .setOnClickListener { _, chooseFile ->
                                    if (chooseFile != null) {
                                        val array = CustomRulesBackuper(mDao).restore(chooseFile)
                                        if (array != null) {
                                            var skipSize = 0
                                            array.forEach {
                                                if (it == -1L) {
                                                    skipSize++
                                                }
                                            }
                                            LoaderManager.getInstance(this@CustomRulesFragment)
                                                .restartLoader(LOADER_ID_CUSTOM_RULES, null, loaderCallbacks)
                                            if (skipSize > 0) {
                                                ToastUtil.showSingletonShortToast(
                                                    getString(R.string.import_success_with_skip, skipSize)
                                                )
                                            } else {
                                                ToastUtil.showSingletonShortToast(R.string.import_success)
                                            }
                                        } else {
                                            ToastUtil.showSingletonShortToast(R.string.import_fail)
                                        }
                                    } else {
                                        ToastUtil.showSingletonShortToast(R.string.file_not_choose)
                                    }
                                }
                                .create()
                                .show()
                        } else {
                            ToastUtil.showSingletonLongToast(R.string.permission_rationale_read_external_storage)
                        }
                    }
                true
            }
            R.id.menu_export_rules -> {
                // 导出规则
                // 请求写入存储权限
                mRxPermissions
                    .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe {
                        if (it.granted) {
                            FileChooserDialog.Builder(activity)
                                .setChooseType(FileChooserDialog.TYPE_DIR)
                                .setTitle(R.string.choose_export_dir)
                                .setOnClickListener { _, chooseFile ->
                                    if (chooseFile != null && chooseFile.exists()) {
                                        CustomRulesBackuper(mDao).backup(chooseFile)
                                        ToastUtil.showSingletonToast(R.string.export_success, Toast.LENGTH_SHORT)
                                    } else {
                                        ToastUtil.showSingletonToast(R.string.export_fail, Toast.LENGTH_SHORT)
                                    }
                                }
                                .create()
                                .show()
                        } else {
                            ToastUtil.showSingletonLongToast(R.string.permission_rationale_write_external_storage)
                        }
                    }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    companion object {
        private const val LOADER_ID_CUSTOM_RULES = 0
    }
}
