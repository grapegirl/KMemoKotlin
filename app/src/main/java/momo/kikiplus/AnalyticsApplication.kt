package momo.kikiplus

import android.app.Application
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import momo.kikiplus.com.kbucket.data.finally.NetworkConst

/**
 * Created by mihyeKim on 2016-11-13.
 */
class AnalyticsApplication : Application() {
    private var mTracker: Tracker? = null

    /**
     * Gets the default [Tracker] for this [Application].
     *
     * @return tracker
     */
    // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
    val defaultTracker: Tracker
        @Synchronized get() {
            if (mTracker == null) {
                val analytics = GoogleAnalytics.getInstance(this)
                mTracker = analytics.newTracker(NetworkConst.KEY_ANALYTICS)
            }
            return mTracker!!
        }

}
