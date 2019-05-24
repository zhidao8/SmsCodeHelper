package chenmc.sms.ui.app;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import chenmc.sms.ui.interfaces.IOnRequestPermissionsResult;

import java.util.ArrayList;

/**
 * @author 明明
 * Created on 2017/8/11.
 */

public abstract class PermissionFragment extends Fragment {

    private SparseArray<IOnRequestPermissionsResult> mListeners = new SparseArray<>(2);

    @SuppressLint("NewApi")
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
            Context ctx = getContext();
            if (ctx != null) {
                if (ContextCompat.checkSelfPermission(ctx, permission) != PackageManager.PERMISSION_GRANTED) {
                    // 至少有一个权限没有被系统授予
                    grantedAll = false;
                    break;
                }
            } else {
                grantedAll = false;
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

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        IOnRequestPermissionsResult listener = mListeners.get(requestCode);
        if (listener != null) {
            // 使用线性表将系统允许的权限和不被系统允许的权限分别保存起来
            ArrayList<String> deniedList = new ArrayList<>(2);
            ArrayList<String> grantedList = new ArrayList<>(2);
            ArrayList<Boolean> alwaysList = new ArrayList<>(2);
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedList.add(permissions[i]);
                } else {
                    deniedList.add(permissions[i]);
                    alwaysList.add(!shouldShowRequestPermissionRationale(permissions[i]));
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

    protected boolean showApplicationDetail(int requestCode) {
        FragmentActivity act = getActivity();
        if (act != null) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", act.getPackageName(), null);
            intent.setData(uri);
            if (intent.resolveActivity(act.getPackageManager()) != null) {
                startActivityForResult(intent, requestCode);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

}
