package chenmc.sms.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import chenmc.sms.code.helper.R;

/**
 * Created by 明明 on 2017/7/20.
 */

public class FileChooserDialog implements ListView.OnItemClickListener,
        View.OnClickListener {

    public static final int TYPE_DIR = 0;

    public static final int TYPE_FILE = 1;

    private Context mContext;
    private String mTitle;
    private int mChooseType;

    private File mChooseFile;

    private String mFileType = "";

    private OnClickListener mOnClickListener;

    private FileAdapter mAdapter;

    public FileChooserDialog(Context context) {
        mContext = context;
    }

    public void setTitle(int textId) {
        mTitle = mContext.getString(textId);
    }

    public void setChooseType(int type) {
        mChooseType = type;
    }

    public void setFileType(String fileType) {
        mFileType = fileType;
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void show() {
        @SuppressLint("InflateParams")
        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_file_chooser, null);
        TextView button = dialogView.findViewById(R.id.btn_up_to_parent);
        button.setOnClickListener(this);

        File dir = Environment.getExternalStorageDirectory();

        mAdapter = new FileAdapter(mContext, dir);
        ListView listView = dialogView.findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        ButtonOnClickListener clickListener = new ButtonOnClickListener();
        new AlertDialog.Builder(mContext)
                .setPositiveButton(R.string.ok, clickListener)
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .setTitle(mTitle)
                .setView(dialogView)
                .create()
                .show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.invalidate(position);
    }

    @Override
    public void onClick(View v) {
        mAdapter.upToParent();
    }

    private class ButtonOnClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            mOnClickListener.onClick(which, mChooseFile);
        }
    }

    public static class Builder {

        private FileChooserDialog mDialog;

        public Builder(Context context) {
            mDialog = new FileChooserDialog(context);
        }

        public Builder setTitle(int textId) {
            mDialog.setTitle(textId);
            return this;
        }

        public Builder setChooseType(int type) {
            mDialog.setChooseType(type);
            return this;
        }

        public Builder setFileType(String fileType) {
            mDialog.setFileType(fileType);
            return this;
        }

        public Builder setOnClickListener(OnClickListener listener) {
            mDialog.setOnClickListener(listener);
            return this;
        }

        public FileChooserDialog create() {
            return mDialog;
        }
    }

    public interface OnClickListener {
        void onClick(int which, File chooseFile);
    }

    private class FileAdapter extends BaseAdapter {

        private Context mContext;
        private List<FileWrapper> mList;

        private File mShowingDir;

        private int mOldChoosePosition = -1;

        FileAdapter(Context context, File dir) {
            mContext = context;
            mList = new ArrayList<>();

            if (listDir(dir)) {
                mShowingDir = dir;

                if (mChooseType == TYPE_DIR) {
                    mChooseFile = dir;
                }
            }
        }

        void invalidate(int position) {
            if (mList.get(position).file.isDirectory()) {
                mOldChoosePosition = -1;

                File dir = mList.get(position).file;

                if (listDir(dir)) {
                    if (mChooseType == TYPE_DIR) {
                        mChooseFile = dir;
                    }
                } else {
                    mChooseFile = null;
                }

                mShowingDir = dir;

                if (mChooseType == TYPE_FILE) {
                    mChooseFile = null;
                }
            } else {
                if (mChooseType == TYPE_FILE) {
                    if (mOldChoosePosition != -1) {
                        mList.get(mOldChoosePosition).checked = false;
                    }

                    mOldChoosePosition = position;
                    mChooseFile = mList.get(position).file;
                    mList.get(position).checked = true;
                }
            }

            notifyDataSetChanged();
        }

        void upToParent() {
            if (mShowingDir == null) return;

            File dir = mShowingDir.getParentFile();
            if (checkDirValid(dir)) {
                listDir(dir);
                mShowingDir = dir;
                if (mChooseType == TYPE_DIR) {
                    mChooseFile = dir;
                }
            }
            notifyDataSetChanged();
        }

        private boolean listDir(File dir) {
            mList.clear();
            if (checkDirValid(dir)) {

                for (File file : dir.listFiles()) {
                    mList.add(new FileWrapper(file));
                }
                handleList();

                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position).file;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.list_item, parent, false
                );
                int padding = convertView.getPaddingTop() / 2;
                convertView.setPadding(padding, padding, padding, padding);
                viewHolder = new ViewHolder((TextView) convertView.findViewById(R.id.list_item_text));
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            FileWrapper fileWrapper = mList.get(position);
            String text = fileWrapper.file.getName();
            if (fileWrapper.file.isDirectory()) {
                text += "/";
            }
            viewHolder.tv.setText(text);
            // 当前的 Item 被处于被选取状态，改变 Item 的背景颜色
            if (fileWrapper.checked) {
                ((ViewGroup) viewHolder.tv.getParent()).setBackgroundResource(R.color.listItemChecked);
            } else {
                ((ViewGroup) viewHolder.tv.getParent()).setBackgroundResource(android.R.color.transparent);
            }

            return convertView;
        }

        private boolean checkDirValid(File dir) {
            return !(dir.isFile() || !dir.exists()) && dir.listFiles() != null;
        }

        private void handleList() {
            if (mChooseType == TYPE_DIR) {
                // 不显示文件，只显示文件夹
                for (int i = mList.size() - 1; i >= 0; i--) {
                    if (mList.get(i).file.isFile()) {
                        mList.remove(i);
                    }
                }
            } else {
                // 只显示文件类型为 mFileType 的文件
                for (int i = mList.size() - 1; i >= 0; i--) {
                    File file = mList.get(i).file;
                    if (file.isFile() && mFileType != null && mFileType.length() != 0 &&
                        !file.getName().toLowerCase().endsWith("." + mFileType.toLowerCase())) {
                        mList.remove(i);
                    }
                }
            }

            sortList();
        }

        private void sortList() {
            Collections.sort(mList, new Comparator<FileWrapper>() {
                @Override
                public int compare(FileWrapper o1, FileWrapper o2) {
                    return o1.file.getName().compareToIgnoreCase(o2.file.getName());
                }
            });
        }

        private class ViewHolder {
            TextView tv;

            ViewHolder(TextView tv) {
                this.tv = tv;
            }
        }

        private class FileWrapper {
            File file;
            boolean checked;

            FileWrapper(File file) {
                this.file = file;
            }
        }
    }
}
