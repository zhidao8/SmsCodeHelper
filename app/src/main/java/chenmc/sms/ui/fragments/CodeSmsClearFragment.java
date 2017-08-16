package chenmc.sms.ui.fragments;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import chenmc.sms.code.helper.R;
import chenmc.sms.data.SimpleWrapper;
import chenmc.sms.transaction.handler.CodeSmsClearHandler;
import chenmc.sms.transaction.handler.SmsHandler;
import chenmc.sms.ui.activities.PreferenceActivity;
import chenmc.sms.utils.database.PrefKey;
import chenmc.sms.utils.database.PreferenceUtil;

/**
 * @author 明明
 *         Created on 2017/8/11.
 */

public class CodeSmsClearFragment extends PermissionFragment implements
    View.OnClickListener,
    ActionMode.Callback,
    ListView.OnItemClickListener,
    ListView.OnItemLongClickListener,
    PreferenceActivity.OnActivityBackPressedListener {
    
    private CharSequence mPreviousActionBarTitle;
    
    // 显示短信内容的 TextView
    private TextView mTvSms;
    
    // 显示短信内容界面中容纳上面的 TextView 的 ViewGroup
    private LinearLayout mViewSmsLayout;
    
    // 显示短信内容界面的透明灰色背景
    private View mViewSmsLayoutBg;
    
    // 当前 Activity 界面中的 ListView 的 Adapter
    private ListViewAdapter mAdapter;
    
    // ListView 的 Item 长按时出现的 ActionMode
    private ActionMode mActionMode;
    
    // 验证码短信加载进度条
    private ProgressBar mProgressBar;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_code_sms_clear, container, false);
        init(root);
    
        return root;
    }
    
    private void init(View root) {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            mPreviousActionBarTitle = actionBar.getTitle();
            actionBar.setTitle(R.string.enable_clear_sms);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    
        mProgressBar = (ProgressBar) root.findViewById(R.id.progress_bar);
        
        mTvSms = (TextView) root.findViewById(R.id.tv_sms);
        mViewSmsLayoutBg = root.findViewById(R.id.view_sms_layout_bg);
        mViewSmsLayout = (LinearLayout) root.findViewById(R.id.view_sms_layout);
        ListView listView = (ListView) root.findViewById(R.id.list_view);
        
        mViewSmsLayoutBg.setOnClickListener(this);
        
        mAdapter = new ListViewAdapter(getActivity());
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        
        setHasOptionsMenu(true);
        getAttachActivity().addOnBackPressedListener(this);
    }
    
    private PreferenceActivity getAttachActivity() {
        return (PreferenceActivity) getActivity();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_code_sms_clear, menu);
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mAdapter.getCount() == 0) {
            menu.findItem(R.id.menu_delete).setVisible(false);
        }
    }
    
    @Override
    public void onDetach() {
        getAttachActivity().removeOnBackPressedListener(this);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mPreviousActionBarTitle);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        super.onDetach();
    }
    
    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String smsDefaultApp = PreferenceUtil.init(getActivity()).get(
                PrefKey.KEY_SMS_DEFAULT_APPLICATION, "com.android.mms");
            
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, smsDefaultApp);
            startActivity(intent);
            
            Toast.makeText(getActivity(), R.string.change_default_mms_to_default, Toast.LENGTH_LONG).show();
        }
        super.onDestroy();
    }
    
    private void updateActionModeTitle() {
        mActionMode.setTitle(mAdapter.getItemCheckedCount() +
            "/" + mAdapter.getCount());
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getAttachActivity().onBackPressed();
                break;
            case R.id.menu_delete:
                DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAdapter.setAllItemsChecked(true);
                        mAdapter.removeAllCheckedItems();
                    }
                };
                
                new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_delete_all_sms)
                    .setMessage(R.string.dialog_message_delete_all_sms)
                    .setPositiveButton(R.string.ok, clickListener)
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onActivityBackPressed() {
        if (mViewSmsLayoutBg.getVisibility() == View.VISIBLE) {
            hideViewSmsLayout();
            return true;
        }
        return false;
    }
    
    private boolean showViewSmsLayout() {
        if (mViewSmsLayoutBg.getVisibility() == View.INVISIBLE) {
            //region 显示界面时伴随的动画
            Animation translateAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_top_bottom);
            Animation alphaAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_show);
            mViewSmsLayoutBg.startAnimation(alphaAnim);
            mViewSmsLayout.startAnimation(translateAnim);
            //endregion
            
            mViewSmsLayoutBg.setVisibility(View.VISIBLE);
            mViewSmsLayoutBg.setClickable(true);
            mViewSmsLayout.setVisibility(View.VISIBLE);
            
            return true;
        } else {
            return false;
        }
    }
    
    /*
     * 隐藏添加规则界面
     */
    private boolean hideViewSmsLayout() {
        if (mViewSmsLayoutBg.getVisibility() == View.VISIBLE) {
            //region 隐藏界面时伴随的动画
            Animation translateAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_bottom_top);
            Animation alphaAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha_hide);
            mViewSmsLayoutBg.startAnimation(alphaAnim);
            mViewSmsLayout.startAnimation(translateAnim);
            //endregion
            
            // 这里隐藏界面使用 View.INVISIBLE 而不使用 View.GONE，是因为在使用 View.GONE 时，
            // 在第一次将 View 设置为 View.VISIBLE 的时候，获取 View 的尺寸的结果将会返回 0
            mViewSmsLayoutBg.setVisibility(View.INVISIBLE);
            // 防止播放动画过程中被点击，触发点击事件
            mViewSmsLayoutBg.setClickable(false);
            mViewSmsLayout.setVisibility(View.INVISIBLE);
            
            return true;
        } else {
            return false;
        }
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
            case R.id.menu_invert_select:
                mAdapter.toggleAllItemChecked();
                updateActionModeTitle();
                break;
            case R.id.menu_select_all:
                mAdapter.setAllItemsChecked(true);
                updateActionModeTitle();
                break;
            case R.id.menu_delete:
                DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success = mAdapter.removeAllCheckedItems();
                        mActionMode.finish();
                        if (!success) {
                            Toast.makeText(getActivity(), R.string.delete_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                
                new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_title_delete_all_checked_sms)
                    .setMessage(R.string.dialog_message_delete_all_checked_sms)
                    .setPositiveButton(R.string.ok, clickListener)
                    .setNegativeButton(R.string.cancel, null)
                    .create()
                    .show();
                break;
        }
        return false;
    }
    
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        mAdapter.setAllItemsChecked(false);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_sms_layout_bg:
                getAttachActivity().onBackPressed();
                break;
        }
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 如果当前处于非多选模式
        if (mActionMode == null) {
            SmsHandler item = (SmsHandler) mAdapter.getItem(position);
            mTvSms.setText(item.getSms());
            showViewSmsLayout();
            
            // 如果当前处于多选模式
        } else {
            mAdapter.toggleItemChecked(position);
            if (mAdapter.getItemCheckedCount() == 0) {
                mActionMode.finish();
            } else {
                updateActionModeTitle();
            }
        }
    }
    
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mActionMode == null) {
            mActionMode = getActivity().startActionMode(this);
            mAdapter.toggleItemChecked(position);
            updateActionModeTitle();
            
            return true;
        }
        
        return false;
    }
    
    private class ListViewAdapter extends BaseAdapter {
        
        private Context mContext;
        private List<SimpleWrapper<SmsHandler, Boolean>> mList;
        
        // 当前被选择的 Item 的个数
        private int mItemCheckedCount;
        
        ListViewAdapter(Context context) {
            mContext = context;
            
            new ReadDataTask().execute();
            
            registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    getActivity().invalidateOptionsMenu();
                }
            });
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
            
            SimpleWrapper<SmsHandler, Boolean> beanWrapper = mList.get(position);
            String text = "";
            if (beanWrapper.target.isVerificationSms()) {
                text = mContext.getString(
                    R.string.code_is,
                    beanWrapper.target.getServiceProvider(), beanWrapper.target.getCode()
                );
            } else if (beanWrapper.target.isExpressSms()) {
                text = mContext.getString(
                    R.string.express_is,
                    beanWrapper.target.getServiceProvider(), beanWrapper.target.getCode()
                );
            }
            viewHolder.text.setText(text);
            // 当前的 Item 被处于被选取状态，改变 Item 的背景颜色
            if (beanWrapper.attr) {
                ((ViewGroup) viewHolder.text.getParent()).setBackgroundResource(R.color.listItemChecked);
            } else {
                ((ViewGroup) viewHolder.text.getParent()).setBackgroundResource(android.R.color.transparent);
            }
            
            return convertView;
        }
        
        /**
         * 删除所有被选取的 Item
         *
         * @return 删除成功返回 true，否则返回 false
         */
        boolean removeAllCheckedItems() {
            List<SmsHandler> beanList = new ArrayList<>();
            for (int i = mList.size() - 1; i >= 0; i--) {
                if (mList.get(i).attr) {
                    beanList.add(mList.get(i).target);
                }
            }
            boolean deleteSuccess = CodeSmsClearHandler.deleteCodeSmsFromDatabase(
                mContext, beanList);
            if (deleteSuccess) {
                for (int i = mList.size() - 1; i >= 0; i--) {
                    if (mList.get(i).attr) {
                        mList.remove(i);
                    }
                }
            }
            
            notifyDataSetChanged();
            
            return deleteSuccess;
        }
        
        /**
         * 统一设置所有 Item 的选取状态
         *
         * @param checked true 则设置所有 Item 为选取状态，false 则设置所有 Item 为未选取状态
         */
        void setAllItemsChecked(boolean checked) {
            for (SimpleWrapper<SmsHandler, Boolean> beanWrapper : mList) {
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
            SimpleWrapper<SmsHandler, Boolean> beanWrapper = mList.get(position);
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
            for (SimpleWrapper<SmsHandler, Boolean> beanWrapper : mList) {
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
        
        private class ReadDataTask extends AsyncTask<Void, Void, List<SimpleWrapper<SmsHandler, Boolean>>> {
            @Override
            protected List<SimpleWrapper<SmsHandler, Boolean>> doInBackground(Void... params) {
                List<SmsHandler> smsHandlerList = CodeSmsClearHandler.getCodeSmsFromDatabase(mContext);
                
                ArrayList<SimpleWrapper<SmsHandler, Boolean>> list = new ArrayList<>(smsHandlerList.size() + 1);
                for (SmsHandler smsHandler : smsHandlerList) {
                    list.add(new SimpleWrapper<>(smsHandler, false));
                }
    
                return list;
            }
            
            @Override
            protected void onPreExecute() {
                mList = new ArrayList<>(0);
            }
            
            @Override
            protected void onPostExecute(List<SimpleWrapper<SmsHandler, Boolean>> list) {
                mProgressBar.setVisibility(View.GONE);
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
