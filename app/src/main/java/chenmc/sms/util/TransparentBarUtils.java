package chenmc.sms.util;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * @author 明 明
 * Created on 2017-1-26.
 */

public class TransparentBarUtils {
    private Activity mActivity;
    private LinearLayout mStatusBar;
    private int mStatusBarColor = -1;
    private LinearLayout mNavigationBar;
    private int mNavigationBarColor = -1;
    private boolean mRemoveMask;

    public TransparentBarUtils(Activity activity) {
        this.mActivity = activity;
    }

    public void commit() {
        // 如果用户没有设置颜色，则保持默认
        if (mStatusBarColor == -1 && mNavigationBarColor == -1) {
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // 安卓版本等于 4.4
            // 状态栏透明
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 导航栏透明
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } else {
            if (mStatusBarColor != -1)
                mActivity.getWindow().setStatusBarColor(mStatusBarColor);
            if (mNavigationBarColor != -1)
                mActivity.getWindow().setNavigationBarColor(mNavigationBarColor);
            return;
        }

        final FrameLayout rootFrame = getRootFrame(mActivity);
        if (rootFrame == null) {
            throw new UnsupportedOperationException("Unknown error: The view(id = android.R.id.content) is null");
        }

        rootFrame.post(new Runnable() {
            @Override
            public void run() {
                if (rootFrame.getChildCount() < 1) {
                    // 如果 Activity 中没有添加任何 View，则不做任何事
                    return;
                } else if (rootFrame.getChildCount() > 1) {
                    throw new IllegalStateException("Using 'merge' in your layout resource is not support");
                }

                View rootView = rootFrame.getChildAt(0);
                if (!(rootView instanceof ViewGroup)) {
                    throw new IllegalStateException("Using 'merge' in your layout resource is not support");
                }

                ViewGroup v1 = (ViewGroup) rootView;
                if (v1.getChildCount() == 3
                        && "StatusBar".equals(v1.getChildAt(0).getTag())
                        && v1 instanceof LinearLayout
                        && "NavigationBar".equals(v1.getChildAt(2).getTag())) {
                    // 可重用
                    // v1 就是下面的 baseView
                    mStatusBar = (LinearLayout) v1.getChildAt(0);
                    mNavigationBar = (LinearLayout) v1.getChildAt(2);
                } else {
                    // 不可重用
                    rootFrame.removeAllViews();

                    //region 原来的最底层的 View
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, 0, 2
                    );
                    rootView.setLayoutParams(lp);
                    //endregion

                    //region 新建的作为最底层的 View
                    LinearLayout baseView = new LinearLayout(mActivity);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                    );
                    baseView.setLayoutParams(params);
                    baseView.setOrientation(LinearLayout.VERTICAL);
                    //endregion

                    //region StatusBar
                    mStatusBar = new LinearLayout(mActivity);
                    mStatusBar.setTag("StatusBar");
                    //endregion

                    //region NavigationBar
                    mNavigationBar = new LinearLayout(mActivity);
                    mNavigationBar.setTag("NavigationBar");
                    //endregion

                    baseView.addView(mStatusBar);
                    baseView.addView(rootView);
                    baseView.addView(mNavigationBar);

                    rootFrame.addView(baseView);
                }

                //region StatusBar
                int statusBarHeight = isFullScreen(mActivity) ? 0 : getStatusBarHeight(mActivity);
                ActionBar actionBar = mActivity.getActionBar();
                int actionBarHeight = actionBar == null || !actionBar.isShowing() ?
                        0 : getActionBarHeight(mActivity);
                LinearLayout.LayoutParams statusBarParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        statusBarHeight + actionBarHeight
                );
                mStatusBar.setLayoutParams(statusBarParams);
                if (mStatusBarColor == -1)
                    mStatusBar.setBackgroundColor(Color.BLACK);
                else
                    mStatusBar.setBackgroundColor(mStatusBarColor);
                //endregion

                //region NavigationBar
                LinearLayout.LayoutParams navigationBarParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        hasSoftKeys(mActivity) ? getNavigationBarHeight(mActivity) : 0
                );
                mNavigationBar.setLayoutParams(navigationBarParams);
                if (mNavigationBarColor == -1)
                    mNavigationBar.setBackgroundColor(Color.BLACK);
                else
                    mNavigationBar.setBackgroundColor(mNavigationBarColor);
                //endregion
            }
        });

    }

    private void setTransparent() {
        // 安卓版本小于 4.4，不支持状态栏透明
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        // 安卓版本大于等于 5.0，去除状态的半透明遮罩
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mRemoveMask) {
                // 下面的代码是直接从网上复制的
                Window window = mActivity.getWindow();
                window.clearFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                );
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.setNavigationBarColor(Color.TRANSPARENT);
            }
        } else {
            // 安卓版本等于 4.4
            // 状态栏透明
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 导航栏透明
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void removeSemitransparentMask(boolean bool) {
        mRemoveMask = bool;
    }

    /**
     * 设置状态栏的颜色
     *
     * @param colorRes 颜色资源 id
     */
    public void setStatusBarColorRes(int colorRes) {
        //noinspection deprecation
        mStatusBarColor = mActivity.getResources().getColor(colorRes);
    }

    /**
     * 设置状态栏的颜色
     *
     * @param color 颜色
     */
    public void setStatusBarColor(int color) {
        mStatusBarColor = color;
    }

    /**
     * 设置导航栏的颜色
     *
     * @param colorRes 颜色资源 id
     */
    public void setNavigationBarColorRes(int colorRes) {
        //noinspection deprecation
        mNavigationBarColor = mActivity.getResources().getColor(colorRes);
    }

    /**
     * 设置导航栏的颜色
     *
     * @param color 颜色
     */
    public void setNavigationBarColor(int color) {
        mNavigationBarColor = color;
    }

    /**
     * 获取Activity的底层布局FrameLayout
     *
     * @param activity Activity
     * @return 底层布局FrameLayout
     */
    public static FrameLayout getRootFrame(Activity activity) {
        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null && rootView instanceof FrameLayout)
            return (FrameLayout) rootView;
        else
            return null;
    }

    /**
     * 获取ActionBar的高度
     *
     * @param activity Activity
     * @return ActionBar的高度
     */
    public static int getActionBarHeight(Activity activity) {
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null)
            return actionBar.getHeight();
        else
            return 0;
    }

    /**
     * 获取StatusBar的高度
     *
     * @param context Context
     * @return StatusBar的高度
     */
    public static int getStatusBarHeight(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId != 0)
            return res.getDimensionPixelSize(resourceId);
        else
            return 0;
    }

    /**
     * 获取NavigationBar的高度
     *
     * @param context Context
     * @return NavigationBar的高度
     */
    public static int getNavigationBarHeight(Context context) {
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("navigation_bar_height",
                "dimen", "android");
        if (resourceId != 0)
            return res.getDimensionPixelSize(resourceId);
        else
            return 0;
    }

    /**
     * 判断该手机是否有虚拟导航栏（即NavigationBar）
     *
     * @param activity 当前 Activity
     * @return boolean 类型的结果
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean hasSoftKeys(Activity activity) {
        return hasSoftKeys(activity.getWindowManager());
    }

    /**
     * 判断该手机是否有虚拟导航栏（即NavigationBar）
     *
     * @param windowManager WindowManager，可通过Activity的getWindowManager()得到
     * @return boolean 类型的结果
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean hasSoftKeys(WindowManager windowManager) {
        Display display = windowManager.getDefaultDisplay();

        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        display.getRealMetrics(realDisplayMetrics);
        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    /**
     * 判断屏幕是否全屏
     *
     * @param activity 当前的 Activity
     * @return boolean 类型
     */
    public static boolean isFullScreen(Activity activity) {
        return (activity.getWindow().getAttributes().flags
                & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }
}
