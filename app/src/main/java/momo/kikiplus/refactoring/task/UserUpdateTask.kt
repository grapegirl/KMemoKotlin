package momo.kikiplus.refactoring.task

import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Message
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.KLog
import momo.kikiplus.refactoring.utils.StringUtils
import momo.kikiplus.com.kbucket.http.HttpUrlTaskManager
import momo.kikiplus.com.kbucket.http.IHttpReceive
import momo.kikiplus.com.kbucket.view.Bean.MobileUser
import java.util.*

/**
 * @author grape gril
 * @version 1.0
 * @Class Name : UserUpdateTask
 * @Description : 사용자 버전 업데이트 Task
 * @since 2015-10-08
 */
class UserUpdateTask(private val mUser: MobileUser) : AsyncTask<Void, Void, Void>(), IHttpReceive, android.os.Handler.Callback {

    private val TAG = this.javaClass.simpleName

    private val mHandler: Handler = Handler(this)

    override fun onPreExecute() {}

    override fun doInBackground(vararg params: Void): Void? {
        mHandler.sendEmptyMessage(0)
        return null
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        if (actionId == IHttpReceive.UPDATE_USER) {
            if (type == IHttpReceive.HTTP_OK) {
                KLog.d(TAG, "@@ Update User Success !")
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        //서버에 내 정보 업데이트
        val mHttpUrlTaskManager = HttpUrlTaskManager(ContextUtils.KBUCKET_UPDATE_USER, true, this, IHttpReceive.UPDATE_USER)
        val map = HashMap<String, Any>()
        map["OS"] = mUser.os
        map["NICKNAME"] = mUser.userNickName!!
        map["PHONE"] = mUser.phone
        map["VERSION_NAME"] = mUser.versionName!!
        map["MARKET"] = mUser.market
        map["LANG"] = mUser.lanuage!!
        map["COUNTY"] = mUser.country!!
        map["GCM_TOKEN"] = mUser.gcmToken
        map["OS_VERSION"] = Build.VERSION.RELEASE
        map["TEL_GBN"] = Build.MODEL
        mHttpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
        return false
    }
}
