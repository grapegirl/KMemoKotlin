package momo.kikiplus.refactoring.common.util

import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.util.Base64
import com.google.android.gms.analytics.HitBuilders
import momo.kikiplus.AnalyticsApplication
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author mihye kim
 * @version 1.0
 * @Class Name : AppUtils
 * @Description : 앱 관련 유틸 클래스
 * @since 2015-08-03.
 */
object AppUtils {

    /**
     * 현재 사용자에 설정된 시간정보 가져오기
     *
     * @return 시간 정보
     */
    val userPhoneTimezone: String
        get() {
            var timeZone = TimeZone.getDefault()
            timeZone= TimeZone.getTimeZone(timeZone.id)

            val date = Date()

            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            df.timeZone = timeZone

            return df.format(date)
        }

    /**
     * 사용자 정보 출력 메소드
     *
     * @param context 컨텍스트
     */
    fun printUserPhoneInfo(context: Context) {
        val locale = context.resources.configuration.locale
        val displayCountry = locale.displayCountry
        val country = locale.country
        val launage = locale.language

        KLog.d( "@@ displayCountry => $displayCountry")
        KLog.d( "@@ County => $country")
        KLog.d("@@ launage => $launage")
    }

    /**
     * 현재 사용자에 설정된 언어 가져오기
     *
     * @param context 컨텍스트
     * @return 언어 정보
     */
    fun getUserPhoneLanuage(context: Context): String {
        val locale = context.resources.configuration.locale
        return locale.language
    }

    /**
     * 현재 사용자에 설정된 국가 정보 가져오기
     *
     * @param context 컨텍스트
     * @return 국가 정보
     */
    fun getUserPhoneCoutry(context: Context): String {
        val locale = context.resources.configuration.locale
        return locale.country
    }


    /***
     * 타임존 시간 출력 메소드
     */
    fun printUserPoneTimezone() {
        var tz: TimeZone
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)")

        tz = TimeZone.getTimeZone("Asia/Seoul")
        df.timeZone = tz
        //System.out.println("@@ timezone : " + tz.getDisplayName() + "," + df.format(date));


        tz = TimeZone.getTimeZone("Greenwich")
        df.timeZone = tz
        //System.out.println("@@ timezone : " + tz.getDisplayName() + "," + df.format(date));


        tz = TimeZone.getTimeZone("America/Los_Angeles")
        df.timeZone = tz
        //System.out.println("@@ timezone : " + tz.getDisplayName() + "," + df.format(date));


        tz = TimeZone.getTimeZone("America/New_York")
        df.timeZone = tz
        //System.out.println("@@ timezone : " + tz.getDisplayName() + "," + df.format(date));


        tz = TimeZone.getTimeZone("Pacific/Honolulu")
        df.timeZone = tz
        //System.out.println("@@ timezone : " + tz.getDisplayName() + "," + df.format(date));


        tz = TimeZone.getTimeZone("Asia/Shanghai")
        df.timeZone = tz
        //System.out.println("@@ timezone : " + tz.getDisplayName() + "," + df.format(date));
    }

    /**
     * 언어 설정 메소드
     *
     * @param context 컨텍스트
     * @param locale  국가
     */
    fun setLocale(context: Context, locale: Locale) {
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.locale = locale
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }


    /**
     * 서비스가 실행중인지 반환하는 메소드
     *
     * @param context     컨텍스트
     * @param serviceName 서비스명
     * @return 실행중이면 true, 아니면 false
     */
    fun getRunningService(context: Context, serviceName: String): Boolean {
        val manager = context.getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
        

        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * 앱 버전 코드 반환 메소드
     *
     * @param context 컨텍스트
     * @return 앱 버전(int형) 예외상황 -1값 반환
     */
    fun getVersionCode(context: Context): Int {
        val packageManager = context.packageManager
        val pkgName = context.packageName
        var versionCode: Int
        try {
            val packageInfo = packageManager.getPackageInfo(pkgName, 0) as PackageInfo
            versionCode = packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            versionCode = -1
        }

        return versionCode
    }

    /**
     * 앱 버전 네임 반환 메소드
     *
     * @param context 컨텍스트
     * @return 앱 버전 네임(string형) 예외상황 null 반환
     */
    fun getVersionName(context: Context): String? {
        val packageManager = context.packageManager
        val pkgName = context.packageName
        var versionName: String?
        try {
            val packageInfo = packageManager.getPackageInfo(pkgName, 0) as PackageInfo
            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            versionName = null
        }

        return versionName
    }


    /**
     * 패키지명으로 실행중인 앱 여부 반환 메소드
     *
     * @param context  컨텍스트
     * @param packName 패키지명
     * @return 실행 여부(true - 실행중, false - 실행안됨)
     */
    fun getServiceTaskName(context: Context, packName: String): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = am.runningAppProcesses
        for (rap in list) {
            if (rap.processName == packName) {
                return true
            }
        }
        return false
    }


    /**
     * 앱 hash 값 반환 메소드
     *
     * @param context 컨텍스트
     * @return hash 키값
     */
    /*
    tjkim
    카카오로그인 키해시생성 코드 , 알아본바에 openssl 버전에 따라 생성되는 키해시값이 달라 , 카카오와 연동이 안되는 경우가 있었음
    이 메소드 현재 배포된 키싸인의 해쉬코드를 가져오는것이기에 , 불편할순있지만 , 카카오 계정 해시코드는 이걸로 사용을 하며 , 다른 sns 로그인에도 필요하면 사용하도록하자.
    fun getAppKeyHash(context: Context): String? {
        var hashValue: String? = null
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures!!) {
                val md: MessageDigest
                md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                hashValue = String(Base64.encode(md.digest(), 0))
                KLog.d( "@@ Hash key : $hashValue")
            }
        } catch (e: Exception) {
            KLog.d( "@@ name not found : " + e.toString())
        }

        return hashValue
    }
     */

    /**
     * 패키지로 앱 설치 유무 반환 메소드
     *
     * @param packageName 패키지명
     * @return 앱 설치 유무
     */
    fun checkAppPakcage(context: Context, packageName: String): Boolean {
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            val appInfo = pi.applicationInfo

            if(appInfo != null){
                return true
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // 패키지가 없을 경우.
            return false
        }
        return true
    }

    /**
     * 마켓 이동 메소드
     *
     * @param context      컨텍스트
     * @param packangeName 마켓 이동할 패키지명
     */
    fun locationMarket(context: Context, packangeName: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=$packangeName")
        context.startActivity(intent)
    }


    fun restart(context: Context, intent: Intent) {
        val mPendingIntentId = 123456
        val mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
        System.exit(0)
    }

    fun sendTrackerScreen(context: Activity, screenName: String) {
        val application = context.application as AnalyticsApplication
        val mTracker = application.defaultTracker
        mTracker.setScreenName(screenName)
        mTracker.send(HitBuilders.ScreenViewBuilder().build())
        KLog.d("@@ sendTrackerScreen name : $screenName")
    }

}
