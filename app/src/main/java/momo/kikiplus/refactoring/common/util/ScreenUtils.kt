package momo.kikiplus.refactoring.common.util

import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.Window
import android.view.WindowManager

/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : ScreenUtils.java
 * @Description : 화면 유틸 클래스
 * @since 2017.04.15
 */
object ScreenUtils {

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun pxToDp(px: Int): Int {
        return (px * Resources.getSystem().displayMetrics.density).toInt()
    }

    /**
     * 해상도 pixel 가져오는 메소드
     *
     * @param windowManager
     * @return 화면 해상도 가로 세로
     */
    fun getDisplay(windowManager: WindowManager): String {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val ScreenWidth = metrics.widthPixels
        val ScreenHeight = metrics.heightPixels
        return ScreenWidth.toString() + "," + ScreenHeight
    }


    //화면 캡쳐 방지
    fun setSecure(window: Window, isSecure: Boolean) {
        if (isSecure) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            val wm = window.windowManager
            wm.removeViewImmediate(window.decorView)
            wm.addView(window.decorView, window.attributes)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            val wm = window.windowManager
            wm.removeViewImmediate(window.decorView)
            wm.addView(window.decorView, window.attributes)
        }
    }
}
