package chenmc.sms.ui.main.customrules

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.SmsCodeRegex
import chenmc.sms.data.storage.SmsCodeRegexDao
import chenmc.sms.ui.interfaces.OnBackPressedActivity
import chenmc.sms.ui.interfaces.OnBackPressedFragment
import chenmc.sms.ui.interfaces.OnItemClickListener
import chenmc.sms.ui.interfaces.OnItemLongClickListener
import chenmc.sms.ui.main.codesmsclear.VH
import com.melnykov.fab.FloatingActionButton

/**
 * @author 明明
 * Created on 2017/8/11.
 */
class CustomRulesFragment : Fragment(), OnBackPressedFragment {

    private lateinit var mFab: FloatingActionButton
    private lateinit var mProgressBar: ProgressBar

    private val mAdapter: CustomRuleAdapter = CustomRuleAdapter()

    private lateinit var mDao: SmsCodeRegexDao

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
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
        return inflater.inflate(R.layout.fragment_code_match_rules, container, false)
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
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = mAdapter
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
                    val clickListener = DialogInterface.OnClickListener { _, _ ->
                        deleteDbData(mAdapter.selectedItems)
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
                if (mAdapter.selectedCount== 0) {
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

    private fun openEditDialog(data: SmsCodeRegex? = null) {
        CustomRuleDialogFragment(data).apply {
            onDeliverResult = mOnDeliverResult
        }.show(fragmentManager, CustomRuleDialogFragment::class.java.simpleName)
    }

    private val mOnDeliverResult = object : CustomRuleDialogFragment.OnDeliverResult {
        override fun deliverResult(data: SmsCodeRegex, isNew: Boolean) {
            if (isNew) {
                mDao.insert(data)
            } else {
                mDao.update(data)
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

    private fun updateActionModeTitle() {
        mListener.actionMode?.title = "${mAdapter.selectedCount}/${mAdapter.itemCount}"
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    companion object {
        private const val REQUEST_CODE_READ_STORAGE = 0
        private const val REQUEST_CODE_WRITE_STORAGE = 1

        private const val LOADER_ID_CUSTOM_RULES = 0
    }
}
