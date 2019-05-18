package momo.kikiplus.com.kbucket.Utils

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


    fun getConnectivityStatus(context: Context): Int {
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        if (null != activeNetwork) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI
            if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE
        }
        return TYPE_NOT_CONNECTED
    }

    fun isConnectivityStatus(context: Context): Boolean {
        val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        if (null != activeNetwork) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI)
                return true
            if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
                return true
        }
        return false
    }
}
