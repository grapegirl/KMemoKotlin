package momo.kikiplus.com.kbucket.Utils

import android.content.res.Resources

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
}
