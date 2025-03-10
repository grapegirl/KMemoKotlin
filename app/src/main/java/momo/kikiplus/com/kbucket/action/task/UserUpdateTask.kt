package momo.kikiplus.com.kbucket.action.task

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import momo.kikiplus.data.http.HttpUrlTaskManager
import momo.kikiplus.data.http.IHttpReceive
import momo.kikiplus.com.common.util.*
import momo.kikiplus.com.kbucket.data.finally.NetworkConst
import momo.kikiplus.com.kbucket.data.finally.PreferConst
import momo.kikiplus.com.kbucket.data.vo.MobileUser
import java.util.*

/**
 * @author grape gril
 * @version 1.0
 * @Class Name : UserUpdateTask
 * @Description : 사용자 버전 업데이트 Task
 * @since 2015-10-08
 */
class UserUpdateTask(private  val mContext : Context) : AsyncTask<Void, Void, Void>(), IHttpReceive, Handler.Callback {

    private val mHandler: Handler = Handler(Looper.getMainLooper(), this)

    private lateinit var mUserData: MobileUser

    @Deprecated("Deprecated in Java", ReplaceWith("setUserData()"))
    override fun onPreExecute() {
        setUserData()
    }

    /**
     * 사용자 정보업데이트 가공 데이타 만드는 메소드
     *
     * @return 사용자 정보
     */

    private fun setUserData() {
        val mobileUser = MobileUser()
        val userNickName = SharedPreferenceUtils.read(mContext, PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        mobileUser.userNickName = userNickName
        mobileUser.versionName = AppUtils.getVersionName(mContext)
        mobileUser.lanuage = AppUtils.getUserPhoneLanuage(mContext)
        mobileUser.country = AppUtils.getUserPhoneCoutry(mContext)
        val date = DateUtils.getStringDateFormat(DateUtils.DATE_YYMMDD_PATTER, Date())
        mobileUser.createDt = date
        val gcmToken = SharedPreferenceUtils.read(mContext, PreferConst.KEY_USER_FCM, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        mobileUser.token = gcmToken!!

        mUserData = mobileUser
    }


    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void): Void? {
        mHandler.sendEmptyMessage(0)
        return null
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        if (actionId == IHttpReceive.UPDATE_USER) {
            if (type == IHttpReceive.HTTP_OK) {
                KLog.d( "@@ Update User Success !")
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        //서버에 내 정보 업데이트
        val mHttpUrlTaskManager = HttpUrlTaskManager(NetworkConst.KBUCKET_UPDATE_USER, true, this, IHttpReceive.UPDATE_USER)
        val map = HashMap<String, Any>()
        map["OS"] = mUserData.os
        map["NICKNAME"] = mUserData.userNickName!!
        map["PHONE"] = mUserData.phone
        map["VERSION_NAME"] = mUserData.versionName!!
        map["MARKET"] = mUserData.market
        map["LANG"] = mUserData.lanuage!!
        map["COUNTY"] = mUserData.country!!
        map["GCM_TOKEN"] = mUserData.token
        map["OS_VERSION"] = Build.VERSION.RELEASE
        map["TEL_GBN"] = Build.MODEL
        mHttpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
        return false
    }
}
