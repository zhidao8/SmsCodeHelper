package chenmc.sms.ui.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import chenmc.sms.code.helper.R;
import chenmc.sms.ui.interfaces.IOnRequestPermissionsResult;

import java.util.ArrayList;

/**
 * Created by 明明 on 2017/8/9.
 */

public abstract class PermissionActivity extends Activity {

    private SparseArray<IOnRequestPermissionsResult> mListeners = new SparseArray<>(2);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewRes());
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    protected abstract int getContentViewRes();

    public void requestPermissions(int requestCode, String[] permissions,
                                   IOnRequestPermissionsResult listener) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (listener != null) {
                listener.onPermissionGranted(requestCode, permissions);
            }
            return;
        }

        // 标记系统是否授予所有的权限
        boolean grantedAll = true;
        for (String permission : permissions) {
            if (checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                // 至少有一个权限没有被系统授予
                grantedAll = false;
                break;
            }
        }

        if (grantedAll) {
            if (listener != null) {
                listener.onPermissionGranted(requestCode, permissions);
            }
        } else {
            mListeners.put(requestCode, listener);
            // 包含不被系统允许的权限，则请求权限
            requestPermissions(permissions, requestCode);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        IOnRequestPermissionsResult listener = mListeners.get(requestCode);
        if (listener != null) {
            // 使用线性表将系统允许的权限和不被系统允许的权限分别保存起来
            ArrayList<String> grantedList = new ArrayList<>(2);
            ArrayList<String> deniedList = new ArrayList<>(2);
            ArrayList<Boolean> alwaysList = new ArrayList<>(2);
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedList.add(permissions[i]);
                } else {
                    deniedList.add(permissions[i]);
                    alwaysList.add(!shouldShowRequestPermissionRationale(permissions[0]));
                }
            }

            if (grantedList.size() > 0) {
                listener.onPermissionGranted(requestCode, grantedList.toArray(new String[0]));
            }

            if (deniedList.size() > 0) {
                boolean[] bAlways = new boolean[alwaysList.size()];
                for (int i = 0; i < alwaysList.size(); i++) {
                    bAlways[i] = alwaysList.get(i);
                }
                listener.onPermissionDenied(requestCode,
                        deniedList.toArray(new String[0]), bAlways);
            }
        }
        mListeners.remove(requestCode);
    }

    protected void showApplicationDetail(int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, requestCode);
    }

}
