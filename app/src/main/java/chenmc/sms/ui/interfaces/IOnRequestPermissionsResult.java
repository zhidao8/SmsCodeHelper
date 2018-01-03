package chenmc.sms.ui.interfaces;

/**
 * @author 明明
 *         Created on 2017/8/11.
 */

public interface IOnRequestPermissionsResult {
    /**
     * 请求权限被允许时回调的方法
     * @param requestCode 请求码
     * @param grantedPermissions 被允许的权限；grantedPermissions != null && grantedPermissions.length > 0
     */
    void onPermissionGranted(int requestCode, String[] grantedPermissions);
    
    /**
     * 请求权限被允许时回调的方法
     * @param requestCode 请求码
     * @param deniedPermissions 被拒绝的权限。!= null && length > 0
     * @param deniedAlways 是否被用户勾选了“不再提示”。!= null && length > 0
     */
    void onPermissionDenied(int requestCode, String[] deniedPermissions,
        boolean[] deniedAlways);
}
