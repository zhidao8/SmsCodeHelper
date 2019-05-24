package chenmc.sms.ui.main.codesmsclear

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chenmc.sms.code.helper.R
import chenmc.sms.data.storage.AppPreference
import chenmc.sms.ui.interfaces.OnBackPressedActivity
import chenmc.sms.ui.interfaces.OnBackPressedFragment
import chenmc.sms.ui.interfaces.OnItemClickListener
import chenmc.sms.ui.interfaces.OnItemLongClickListener
import chenmc.sms.ui.main.PreferenceActivity
import chenmc.sms.util.ToastUtil
import java.util.*

/**
 * @author 明明
 * Created on 2017/8/11.
 */

class CodeSmsClearFragment : Fragment(), ActionMode.Callback, OnBackPressedFragment {

    private var mActionMode: ActionMode? = null

    // 当前 Activity 界面中的 ListView 的 Adapter
    private val mAdapter: DbSmsAdapter = DbSmsAdapter()

    private lateinit var mProgressBar: ProgressBar

    private val attachActivity: PreferenceActivity?
        get() = activity as PreferenceActivity?

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_code_sms_clear, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        mAdapter.apply {
            onItemClickListener = mItemListener
            onItemLongClickListener = mItemListener
            registerAdapterDataObserver(mAdapterDataObserver)
        }

        view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = mAdapter
            itemAnimator = DefaultItemAnimator()
            val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            addItemDecoration(dividerItemDecoration)
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom)
        }

        mProgressBar = view.findViewById(R.id.progressBar)
    }

    private val mItemListener = object : OnItemClickListener<VH>, OnItemLongClickListener<VH> {
        override fun onClick(holder: VH) {
            val position = holder.adapterPosition

            // 如果当前处于非多选模式
            if (mActionMode == null) {
                mAdapter.toggleExpansion(position)
                // 如果当前处于多选模式
            } else {
                mAdapter.toggleSelection(position)
                if (mAdapter.selectedCount == 0) {
                    mActionMode?.finish()
                } else {
                    updateActionModeTitle()
                }
            }
        }

        override fun onLongClick(holder: VH): Boolean {
            val position = holder.adapterPosition

            if (mActionMode == null) {
                mActionMode = activity?.startActionMode(this@CodeSmsClearFragment)
                mAdapter.toggleSelection(position)
                updateActionModeTitle()
                return true
            }
            return false
        }
    }

    private val mAdapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            // invalidate options menu when data changed
            activity?.invalidateOptionsMenu()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LoaderManager.getInstance(this).initLoader(LOADER_ID_DB_SMS, null, mLoaderCallbacks)
    }

    private val mLoaderCallbacks =
        object : LoaderManager.LoaderCallbacks<MutableList<DbSms>> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<MutableList<DbSms>> {
                mProgressBar.visibility = View.VISIBLE
                return DbSmsLoader(activity!!)
            }

            override fun onLoadFinished(loader: Loader<MutableList<DbSms>>, data: MutableList<DbSms>) {
                mAdapter.data = data
                mProgressBar.visibility = View.GONE
            }

            override fun onLoaderReset(loader: Loader<MutableList<DbSms>>) {}
        }

    override fun onStart() {
        super.onStart()

        activity?.let { activity ->
            if (activity is OnBackPressedActivity) {
                activity.setFocusFragment(this)
            }

            when (activity) {
                is AppCompatActivity -> {
                    activity.supportActionBar?.run {
                        setTitle(R.string.enable_clear_sms)
                        setDisplayHomeAsUpEnabled(true)
                    }
                }
                else -> {
                    activity.actionBar?.run {
                        setTitle(R.string.enable_clear_sms)
                        setDisplayHomeAsUpEnabled(true)
                    }
                }
            }
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_code_sms_clear, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (mAdapter.itemCount == 0) {
            menu.findItem(R.id.menu_delete).isVisible = false
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            try {
                val defaultSmsApp = AppPreference.defaultSmsApp
                // 尝试获取应用信息，如果抛出异常，则该应用不存在
                activity?.packageManager?.getApplicationInfo(defaultSmsApp, 0)

                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp)
                startActivity(intent)

                ToastUtil.showSingletonToast(R.string.change_default_mms_to_default, Toast.LENGTH_LONG)
            } catch (e: PackageManager.NameNotFoundException) {
                // ignored
            }
        }
        super.onDestroy()
    }

    private fun updateActionModeTitle() {
        mActionMode?.title = "${mAdapter.selectedCount}/${mAdapter.itemCount}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> attachActivity?.onBackPressed()
            R.id.menu_delete -> {
                deleteSMSes(mAdapter.data)
            }
        }
        return super.onOptionsItemSelected(item)
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
                mAdapter.toggleSelectionAll()
                updateActionModeTitle()
            }
            R.id.menu_select_all -> {
                mAdapter.setSelectionAll(true)
                updateActionModeTitle()
            }
            R.id.menu_delete -> {
                deleteSMSes(mAdapter.selectedItems)
            }
        }
        return false
    }

    private fun deleteSMSes(list: List<DbSms>) {
        val clickListener = DialogInterface.OnClickListener { _, _ ->
            mActionMode?.finish()

            context?.let { context ->
                val resultList = deleteCodeSmsFromDatabase(context, list)
                if (resultList.size != list.size) {
                    ToastUtil.showSingletonToast(R.string.delete_fail, Toast.LENGTH_SHORT)
                }
            }

            LoaderManager.getInstance(this).restartLoader(LOADER_ID_DB_SMS, null, mLoaderCallbacks)
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_delete_sms)
            .setMessage(getString(R.string.dialog_message_delete_sms, list.size))
            .setPositiveButton(R.string.delete, clickListener)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .show()
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        mActionMode = null
        mAdapter.setSelectionAll(false)
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    /**
     * 删除系统中的验证码和取件码短信
     * @param context 上下文
     * @param deleteList 将要删除的包含验证码和取件码短信的线性表
     * @return 删除成功的 List<SmsHandler>
     */
    private fun deleteCodeSmsFromDatabase(context: Context, deleteList: List<DbSms>): List<DbSms> {
        val deleteSuccessList = ArrayList<DbSms>(deleteList.size)

        val contentResolver = context.contentResolver

        for (it in deleteList) {
            val count = contentResolver.delete(
                Uri.parse("content://sms/"), "_id = ?",
                arrayOf(it.databaseId.toString())
            )

            if (count > 0) {
                deleteSuccessList.add(it)
            }
        }

        return deleteSuccessList
    }

    companion object {
        private const val LOADER_ID_DB_SMS = 0
    }
}
