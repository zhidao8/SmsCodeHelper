package chenmc.sms.ui.interfaces

/**
 * @author 明明
 * Created on 2017/8/11.
 */

interface IOnRequestPermissionsResult {

    /**
     * 请求权限被允许时回调的方法
     * @param requestCode 请求码
     * @param grantedPermissions 被允许的权限；grantedPermissions != null && grantedPermissions.length > 0
     */
    fun onPermissionGranted(requestCode: Int, grantedPermissions: Array<String>)

    /**
     * 请求权限被允许时回调的方法
     * @param requestCode 请求码
     * @param deniedPermissions 被拒绝的权限。!= null && length > 0
     * @param deniedAlways 是否被用户勾选了“不再提示”。!= null && length > 0
     */
    fun onPermissionDenied(requestCode: Int, deniedPermissions: Array<String>, deniedAlways: BooleanArray)
}
