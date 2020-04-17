package momo.kikiplus.refactoring.utils

import android.content.Context
import android.net.ConnectivityManager

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name :NetworkUtils
 * @Description : 네트워크 연결 관련 유틸
 * @since 2016-11-26.
 */
object NetworkUtils {

    var TYPE_WIFI = 1
    var TYPE_MOBILE = 2
    var TYPE_NOT_CONNECTED = 0


    fun isConnectivityStatus(context: Context): Boolean {
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        if ( activeNetwork != null) {
           return activeNetwork.isConnected

        }
        return false
    }
}
