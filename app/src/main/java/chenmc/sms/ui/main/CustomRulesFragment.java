package chenmc.sms.ui.main;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import chenmc.sms.code.helper.R;
import chenmc.sms.data.SimpleWrapper;
import chenmc.sms.data.CustomRuleBean;
import chenmc.sms.ui.app.PermissionFragment;
import chenmc.sms.ui.interfaces.IOnBackPressedActivity;
import chenmc.sms.ui.interfaces.IOnBackPressedFragment;
import chenmc.sms.ui.interfaces.IOnRequestPermissionsResult;
import chenmc.sms.utils.FileChooserDialog;
import chenmc.sms.utils.FileHelper;
import chenmc.sms.utils.SmsMatchRuleUtil;
import chenmc.sms.utils.ToastUtil;
import chenmc.sms.utils.storage.DatabaseHelper;
import chenmc.sms.utils.storage.SmsMatchRulesDBDao;

/**
 * @author 明明
 *         Created on 2017/8/11.
 */
public class CustomRulesFragment extends PermissionFragment implements
    IOnRequestPermissionsResult,
    IOnBackPressedFragment {
    
    private static final int REQUEST_CODE_READ_STORAGE = 0;
    private static final int REQUEST_CODE_WRITE_STORAGE = 1;
    
    // 添加规则界面的短信内容输入框
    private EditText mEtSms;
    // 添加规则界面的短信验证码输入框
    private EditText mEtCode;
    // 添加规则界面中容纳上面的 EditText 的 ViewGroup
    private LinearLayout mAddRuleLayout;
    // 显示上面 EditText 的内容错误提示的 TextView
    private TextView mTvTips;
    // 添加规则界面的透明灰色背景
    private View mAddRuleLayoutBg;
    // 当前 Activity 界面中的 ListView 的 Adapter
    private ListViewAdapter mListAdapter;
    // 正在修改中的 ListView Item 的克隆对象
    private CustomRuleBean mCurrentItemClone;
    // 正在修改中的 ListView Item 在 ListView 中的位置
    private int mCurrentItemPosition;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_code_match_rules, container, false);
        init(root);
        return root;
    }
    
    private void init(View root) {
        // 初始化编辑面板
        mEtSms = (EditText) root.findViewById(R.id.et_sms);
        mEtCode = (EditText) root.findViewById(R.id.et_code);
        mTvTips = (TextView) root.findViewById(R.id.tv_tips);
        mAddRuleLayoutBg = root.findViewById(R.id.add_rule_layout_bg);
        mAddRuleLayout = (LinearLayout) root.findViewById(R.id.add_rule_layout);
        mAddRuleLayoutBg.setOnClickListener(mListener);
        // 先隐藏编辑面板
        mAddRuleLayoutBg.setVisibility(View.INVISIBLE);
        mAddRuleLayout.setVisibility(View.INVISIBLE);
        mTvTips.setVisibility(View.GONE);
        
        // 初始化悬浮按钮
        ImageButton btnAdd = (ImageButton) root.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(mListener);
        
        mEtSms.setOnFocusChangeListener(mListener);
        // 使 EditText 可以响应输入键盘按钮点击事件
        mEtCode.setOnEditorActionListener(mListener);
        
        mEtSms.addTextChangedListener(mListener);
        mEtCode.addTextChangedListener(mListener);
        
        // 初始化 ListView
        ListView listView = (ListView) root.findViewById(R.id.list_view);
        mListAdapter = new ListViewAdapter(getActivity());
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(mListener);
        listView.setOnItemLongClickListener(mListener);
        
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // 初始化 ActionBar
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.pref_custom_rules);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (getActivity() instanceof IOnBackPressedActivity) {
            ((IOnBackPressedActivity) getActivity()).setFocusFragment(this);
        }
    }
    
    @Override
    public void onPermissionGranted(int requestCode, String[] grantedPermissions) {
        handleRequestPermissionsResult(requestCode);
    }
    
    @Override
    public void onPermissionDenied(int requestCode, String[] deniedPermissions,
        boolean[] deniedAlways) {
        handleRequestPermissionsResult(requestCode);
    }
    
    private void handleRequestPermissionsResult(int requestCode) {
        switch (requestCode) {
            case REQUEST_CODE_READ_STORAGE:
                new FileChooserDialog.Builder(getActivity())
                    .setTitle(R.string.choose_import_dir)
                    .setChooseType(FileChooserDialog.TYPE_FILE)
                    .setFileType("db")
                    .setOnClickListener(new FileChooserDialog.OnClickListener() {
                        @Override
                        public void onClick(int which, File chooseFile) {
                            File databaseDir = getActivity().getDatabasePath(
                                DatabaseHelper.DATABASE_NAME).getParentFile();
                            if (chooseFile != null) {
                                String chooseFileName = chooseFile.getName();
                                File dest = new File(chooseFile.getParentFile(), DatabaseHelper.DATABASE_NAME);
                                boolean renameSuccess = chooseFile.renameTo(dest);
                                int textId =
                                    renameSuccess && FileHelper.copyFile(dest, databaseDir) ?
                                        R.string.import_success : R.string.import_fail;
                                ToastUtil.showToast(textId, Toast.LENGTH_SHORT);
                                
                                if (renameSuccess)
                                    FileHelper.renameFile(dest, chooseFileName);
                                
                                mListAdapter.init();
                            } else {
                                ToastUtil.showToast(getString(R.string.file_not_choose), Toast.LENGTH_SHORT);
                            }
                            
                        }
                    })
                    .create()
                    .show();
                break;
            case REQUEST_CODE_WRITE_STORAGE:
                new FileChooserDialog.Builder(getActivity())
                    .setChooseType(FileChooserDialog.TYPE_DIR)
                    .setTitle(R.string.choose_export_dir)
                    .setOnClickListener(new FileChooserDialog.OnClickListener() {
                        @Override
                        public void onClick(int which, File chooseFile) {
                            File database = getActivity().getDatabasePath(
                                DatabaseHelper.DATABASE_NAME);
                            int textId = FileHelper.copyFile(database, chooseFile) ?
                                R.string.export_success : R.string.export_fail;
                            ToastUtil.showToast(textId, Toast.LENGTH_SHORT);
                        }
                    })
                    .create()
                    .show();
                break;
        }
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_code_match_rules, menu);
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        if (mAddRuleLayout.getVisibility() == View.VISIBLE) {
            // 如果编辑界面的是可见的，则隐藏与编辑无关的菜单选项
            menu.setGroupVisible(R.id.menu_group_backup, false);
        } else {
            // 如果编辑界面的是不可见的，则隐藏与编辑相关的菜单选项
            menu.findItem(R.id.menu_finish).setVisible(false);
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            case R.id.menu_import_rules:
                // 导入规则
                // 请求读取存储权限
                String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
                }
                requestPermissions(REQUEST_CODE_READ_STORAGE,
                    new String[]{READ_EXTERNAL_STORAGE}, this);
                return true;
            case R.id.menu_export_rules:
                // 导出规则
                // 请求写入存储权限
                requestPermissions(REQUEST_CODE_WRITE_STORAGE,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, this);
                return true;
            case R.id.menu_finish:
                // 完成并保存当前的编辑
                mListener.onEditorAction(mEtCode, EditorInfo.IME_ACTION_DONE, null);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mListAdapter.close();
    }
    
    /*
     * 显示添加规则界面
     */
    private boolean showAddRuleLayout() {
        if (mAddRuleLayoutBg.getVisibility() == View.VISIBLE)
            return false;
        
        //region 显示界面时伴随的动画
        Animation translateAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_top_bottom);
        Animation alphaAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_show);
        mAddRuleLayoutBg.startAnimation(alphaAnim);
        mAddRuleLayout.startAnimation(translateAnim);
        //endregion
        
        mAddRuleLayoutBg.setVisibility(View.VISIBLE);
        mAddRuleLayoutBg.setClickable(true);
        mAddRuleLayout.setVisibility(View.VISIBLE);
        
        translateAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // 动画结束时，为输入短信的 EditText 获取焦点
                mEtSms.requestFocus();
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        return true;
    }
    
    /*
     * 隐藏添加规则界面
     */
    private boolean hideAddRuleLayout() {
        if (mAddRuleLayoutBg.getVisibility() != View.VISIBLE)
            return false;
        
        //region 隐藏界面时伴随的动画
        Animation translateAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_bottom_top);
        Animation alphaAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_hide);
        mAddRuleLayoutBg.startAnimation(alphaAnim);
        mAddRuleLayout.startAnimation(translateAnim);
        //endregion
        
        // 这里隐藏界面使用 View.INVISIBLE 而不使用 View.GONE，是因为在使用 View.GONE 时，
        // 在第一次将 View 设置为 View.VISIBLE 的时候，获取 View 的尺寸的结果将会返回 0
        mAddRuleLayoutBg.setVisibility(View.INVISIBLE);
        // 防止播放动画过程中被点击，触发点击事件
        mAddRuleLayoutBg.setClickable(false);
        mAddRuleLayout.setVisibility(View.INVISIBLE);
        mTvTips.setVisibility(View.GONE);
        
        // 隐藏界面的同时隐藏键盘
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mAddRuleLayoutBg.getWindowToken(), 0);
        
        translateAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // 动画结束时，将所有的 EditText 的内容清除
                mEtSms.setText("");
                mEtCode.setText("");
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        return true;
    }
    
    private void updateActionModeTitle() {
        mListener.actionMode.setTitle(mListAdapter.getItemCheckedCount() +
            "/" + mListAdapter.getCount());
    }
    
    @Override
    public boolean onBackPressed() {
        // 隐藏编辑界面并重新创建 Menu
        if (hideAddRuleLayout()) {
            getActivity().invalidateOptionsMenu();
            mCurrentItemClone = null;
            return true;
        }
        return false;
    }
    
    private Listener mListener = new Listener();
    
    private class Listener implements
        TextWatcher,
        View.OnClickListener,
        View.OnFocusChangeListener,
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        TextView.OnEditorActionListener,
        ActionMode.Callback {
        
        // ListView 的 Item 长按时出现的 ActionMode
        ActionMode actionMode;
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
        @Override
        public void afterTextChanged(Editable s) {
            // 隐藏提示
            mTvTips.setVisibility(View.GONE);
        }
        
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_add:
                    // 如果当前处于多选模式，则先退出多选模式
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                    showAddRuleLayout();
                    getActivity().invalidateOptionsMenu();
                    break;
                // 点击编辑界面背景时模拟返回按钮点击事件
                case R.id.add_rule_layout_bg:
                    onBackPressed();
                    break;
            }
        }
        
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // 输入短信的 EditText 获得焦点，把光标移动到末尾，并显示输入键盘
            if (v.getId() == R.id.et_sms && hasFocus) {
                mEtSms.setSelection(mEtSms.getText().length());
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        }
        
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // 如果当前处于非编辑模式
            if (actionMode == null) {
                mCurrentItemPosition = position;
                CustomRuleBean item = (CustomRuleBean) mListAdapter.getItem(position);
                try {
                    // 克隆一份 Item 对象
                    mCurrentItemClone = (CustomRuleBean) item.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                
                mEtSms.setText(item.getSms());
                mEtCode.setText(item.getVerificationCode());
                
                showAddRuleLayout();
                getActivity().invalidateOptionsMenu();
                
                // 如果当前处于编辑模式
            } else {
                mListAdapter.toggleItemChecked(position);
                if (mListAdapter.getItemCheckedCount() == 0) {
                    actionMode.finish();
                } else {
                    updateActionModeTitle();
                }
            }
        }
        
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // 当前未处于多选模式
            if (actionMode == null) {
                // ListView 中的 Item 被长按时进入多选模式
                actionMode = getActivity().startActionMode(this);
                mListAdapter.toggleItemChecked(position);
                updateActionModeTitle();
                
                return true;
            }
            return false;
        }
        
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            switch (v.getId()) {
                case R.id.et_code:
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        int resultCode;
                        // 当前正在修改 ListView 中的某个 Item
                        if (mCurrentItemClone != null) {
                            mCurrentItemClone.setSms(mEtSms.getText().toString());
                            mCurrentItemClone.setVerificationCode(mEtCode.getText().toString());
                            resultCode = SmsMatchRuleUtil.handleItem(mCurrentItemClone);
                            
                            if (SmsMatchRuleUtil.HANDLE_RESULT_SUCCESS == resultCode) {
                                mListAdapter.update(mCurrentItemPosition, mCurrentItemClone);
                            }
                            
                            // 当前正在给 ListView 添加一个 Item
                        } else {
                            CustomRuleBean bean = new CustomRuleBean(
                                mEtSms.getText().toString(), mEtCode.getText().toString());
                            resultCode = SmsMatchRuleUtil.handleItem(bean);
                            
                            if (SmsMatchRuleUtil.HANDLE_RESULT_SUCCESS == resultCode) {
                                mListAdapter.add(bean);
                            }
                        }
                        
                        switch (resultCode) {
                            case SmsMatchRuleUtil.HANDLE_ERROR_EMPTY_CONTENT:
                                mTvTips.setVisibility(View.VISIBLE);
                                mTvTips.setText(R.string.edit_text_no_content);
                                break;
                            case SmsMatchRuleUtil.HANDLE_ERROR_NO_CONTAINS:
                                mTvTips.setVisibility(View.VISIBLE);
                                mTvTips.setText(R.string.sms_not_contains_code);
                                break;
                            case SmsMatchRuleUtil.HANDLE_RESULT_SUCCESS:
                            default:
                                mCurrentItemClone = null;
                                onBackPressed();
                        }
                    }
                    return true;
            }
            
            return false;
        }
        
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_list_action, menu);
            return true;
        }
        
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_select_all:
                    mListAdapter.setAllItemsChecked(true);
                    updateActionModeTitle();
                    return true;
                case R.id.menu_invert_select:
                    mListAdapter.toggleAllItemChecked();
                    updateActionModeTitle();
                    return true;
                case R.id.menu_delete:
                    DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListAdapter.removeAllCheckedItems();
                            actionMode.finish();
                        }
                    };
                    
                    new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.dialog_title_delete_all_checked_rules)
                        .setMessage(R.string.dialog_message_delete_all_checked_rules)
                        .setPositiveButton(R.string.ok, clickListener)
                        .setNegativeButton(R.string.cancel, null)
                        .create()
                        .show();
                    return true;
                default:
                    return false;
            }
        }
        
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            // 退出多选模式的同时取消所有的选择
            mListAdapter.setAllItemsChecked(false);
        }
    }
    
    private class ListViewAdapter extends BaseAdapter {
        
        private Context mContext;
        private java.util.List<SimpleWrapper<CustomRuleBean, Boolean>> mList;
        private SmsMatchRulesDBDao mSmsMatchRulesDBDao;
        
        // 当前被选择的 Item 的个数
        private int mItemCheckedCount;
        
        ListViewAdapter(Context context) {
            mContext = context;
            init();
        }
        
        /**
         * 完全初始化
         */
        void init() {
            new ReadDataTask().execute();
        }
        
        void close() {
            mSmsMatchRulesDBDao.close();
        }
        
        @Override
        public int getCount() {
            return mList.size();
        }
        
        @Override
        public Object getItem(int position) {
            return mList.get(position).target;
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                TextView tv = (TextView) convertView.findViewById(R.id.list_item_text);
                viewHolder = new ViewHolder(tv);
                
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            
            SimpleWrapper<CustomRuleBean, Boolean> beanWrapper = mList.get(position);
            viewHolder.text.setText(beanWrapper.target.getSms());
            // 当前的 Item 被处于被选取状态，改变 Item 的背景颜色
            if (beanWrapper.attr) {
                ((ViewGroup) viewHolder.text.getParent()).setBackgroundResource(R.color.listItemChecked);
            } else {
                ((ViewGroup) viewHolder.text.getParent()).setBackgroundResource(android.R.color.transparent);
            }
            
            return convertView;
        }
        
        /**
         * 添加一个 Item
         *
         * @param bean 待添加的 Item
         */
        void add(CustomRuleBean bean) {
            // 更新数据库
            mSmsMatchRulesDBDao.insert(bean);
            
            mList.add(new SimpleWrapper<>(bean, false));
            
            notifyDataSetChanged();
        }
        
        /**
         * 修改 position 位置的 Item
         *
         * @param position Item 在 ListView 中的位置
         * @param newBean 新的 Item
         */
        void update(int position, CustomRuleBean newBean) {
            CustomRuleBean oldBean = mList.get(position).target;
            oldBean.setSms(newBean.getSms());
            oldBean.setVerificationCode(newBean.getVerificationCode());
            oldBean.setRegExp(newBean.getRegExp());
            
            mSmsMatchRulesDBDao.update(oldBean);
            
            notifyDataSetChanged();
        }
        
        /**
         * 删除所有被选取的 Item
         */
        void removeAllCheckedItems() {
            List<CustomRuleBean> beanList = new ArrayList<>();
            for (int i = mList.size() - 1; i >= 0; i--) {
                if (mList.get(i).attr) {
                    beanList.add(mList.get(i).target);
                    mList.remove(i);
                }
            }
            CustomRuleBean[] been = new CustomRuleBean[beanList.size()];
            mSmsMatchRulesDBDao.delete(beanList.toArray(been));
            
            notifyDataSetChanged();
        }
        
        /**
         * 统一设置所有 Item 的选取状态
         *
         * @param checked true 则设置所有 Item 为选取状态，false 则设置所有 Item 为未选取状态
         */
        void setAllItemsChecked(boolean checked) {
            for (SimpleWrapper<CustomRuleBean, Boolean> beanWrapper : mList) {
                beanWrapper.attr = checked;
            }
            if (checked) {
                mItemCheckedCount = mList.size();
            } else {
                mItemCheckedCount = 0;
            }
            
            notifyDataSetChanged();
        }
        
        /**
         * 更改给定位置的 Item 的选取状态
         *
         * @param position Item 在 ListView 中的位置
         */
        void toggleItemChecked(int position) {
            SimpleWrapper<CustomRuleBean, Boolean> beanWrapper = mList.get(position);
            beanWrapper.attr = !beanWrapper.attr;
            
            if (beanWrapper.attr) {
                mItemCheckedCount++;
            } else {
                mItemCheckedCount--;
            }
            
            notifyDataSetChanged();
        }
        
        /**
         * 反转所有的 Item 的选取状态
         */
        void toggleAllItemChecked() {
            for (SimpleWrapper<CustomRuleBean, Boolean> beanWrapper : mList) {
                beanWrapper.attr = !beanWrapper.attr;
                
                if (beanWrapper.attr) {
                    mItemCheckedCount++;
                } else {
                    mItemCheckedCount--;
                }
            }
            
            notifyDataSetChanged();
        }
        
        /**
         * 获取处于选取状态的 Item 数量
         *
         * @return 处于选取状态的 Item 数量
         */
        int getItemCheckedCount() {
            return mItemCheckedCount;
        }
        
        private class ReadDataTask extends AsyncTask<Void, Void, List<SimpleWrapper<CustomRuleBean, Boolean>>> {
            @Override
            protected List<SimpleWrapper<CustomRuleBean, Boolean>> doInBackground(
                Void... params) {
                mSmsMatchRulesDBDao = new SmsMatchRulesDBDao(mContext);
                List<CustomRuleBean> smsMatchRuleBeen = mSmsMatchRulesDBDao.selectAll();
                ArrayList<SimpleWrapper<CustomRuleBean, Boolean>> list = new ArrayList<>(
                    smsMatchRuleBeen.size() + 1);
                
                for (CustomRuleBean bean : smsMatchRuleBeen) {
                    list.add(new SimpleWrapper<>(bean, false));
                }
                
                return list;
            }
            
            @Override
            protected void onPreExecute() {
                mList = new ArrayList<>(0);
            }
            
            @Override
            protected void onPostExecute(List<SimpleWrapper<CustomRuleBean, Boolean>> list) {
                mList = list;
                notifyDataSetChanged();
            }
        }
        
        private class ViewHolder {
            TextView text;
            
            ViewHolder(TextView text) {
                this.text = text;
            }
        }
        
    }
}
