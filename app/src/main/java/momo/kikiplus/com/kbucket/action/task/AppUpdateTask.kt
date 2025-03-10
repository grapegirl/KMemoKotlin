package momo.kikiplus.com.kbucket.action.task

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.common.util.AppUtils
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.common.util.StringUtils
import momo.kikiplus.com.common.view.popup.BasicPopup
import momo.kikiplus.com.common.view.popup.ConfirmPopup
import momo.kikiplus.com.common.view.popup.IPopupReceive
import momo.kikiplus.com.kbucket.action.net.NetRetrofit
import momo.kikiplus.com.kbucket.action.net.Version
import momo.kikiplus.com.kbucket.data.finally.PopupConst
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author grape gril
 * @version 1.0
 * @Class Name : AppUpdateTask
 * @Description : App 업데이트 Task
 * @since 2020-01-28
 */
class AppUpdateTask(private val mContext: Context) : AsyncTask<Void, Void, Void>(), Handler.Callback,
    IPopupReceive {

    private var mVersion: Version? = null
    private val mHandler: Handler = Handler(Looper.getMainLooper(), this)

    private val START_VERSION : Int = 10
    private val CHECK_VERSION : Int = 20
    private val FAIL_VERSION : Int = 30
    private val TOAST_MESSAGE : Int = 40

    private var mBasicPopup: BasicPopup? = null
    private var mConfirmPopup: ConfirmPopup? = null

    @Deprecated("Deprecated in Java")
    override fun onPreExecute() {}

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void): Void? {
        mHandler.sendEmptyMessage(START_VERSION)
        return null
    }

    override fun handleMessage(msg: Message): Boolean {

        when (msg.what) {
            START_VERSION -> {

                val strSender = AppUtils.getVersionName(mContext)!!
                KLog.d("@@ StringUtils.getHTTPPostSendData(map) : $strSender")

                val res = NetRetrofit.instance.service.getVersion(strSender)
                res.enqueue(object : Callback<Version>{
                    override fun onResponse(call: Call<Version>, response: Response<Version>) {
                        KLog.d("@@ onRecv ok : $response")
                        KLog.d( "@@ onRecv response body: " + response.body()!!.toString())

                        if (response.body()!!.bIsValid) {
                            mVersion = Version()
                            mVersion!!.forceYN = response.body()!!.forceYN
                            mVersion!!.versionCode = response.body()!!.versionCode
                            mVersion!!.versionName = response.body()!!.versionName
                            KLog.d( "@@ onRecv forceYN : " + response.body()!!.forceYN)
                            KLog.d( "@@ onRecv versionCode : " + response.body()!!.versionCode)
                            KLog.d( "@@ onRecv versionName : " + mVersion!!.versionName)
                            mHandler.sendEmptyMessage(CHECK_VERSION)
                        }
                    }

                    override fun onFailure(call: Call<Version>, t: Throwable) {
                        KLog.d( "@@ onRecv fail : " + t.localizedMessage)
                        mHandler.sendEmptyMessage(FAIL_VERSION)
                    }
                })

            }
            CHECK_VERSION -> {

               if(mVersion == null){
                   KLog.d( "@@ check version mVesion is null")
                   return false
               }

                val currentVersionName = AppUtils.getVersionName(mContext)
                val serverVersionName = mVersion!!.versionName
                if(currentVersionName == null || serverVersionName == null){
                    KLog.d( "@@ check version currentVersionName or serverVersionName is null")
                    KLog.d("@@ currentVersionName : $currentVersionName")
                    KLog.d("@@ serverVersisonName : $serverVersionName")
                    return false
                }
                if(mVersion!!.versionCode > 0)
                    if (StringUtils.compareVersion(currentVersionName, serverVersionName) > 0) {
                        if (mVersion!!.forceYN == "Y") {
                            val title = mContext.getString(R.string.update_popup_title)
                            val content = mContext.getString(R.string.update_popup_content_y)
                            mBasicPopup =
                                BasicPopup(
                                    mContext,
                                    title,
                                    content,
                                    R.layout.popup_basic,
                                    this,
                                    PopupConst.POPUP_UPDATE_FORCE
                                )
                            mBasicPopup!!.showDialog()
                        } else {
                            val title = mContext.getString(R.string.update_popup_title)
                            val content = mContext.getString(R.string.update_popup_content_n)
                            mConfirmPopup =
                                ConfirmPopup(
                                    mContext,
                                    title,
                                    content,
                                    R.layout.popup_confirm,
                                    this,
                                    PopupConst.POPUP_UPDATE_SELECT
                                )
                            mConfirmPopup!!.showDialog()
                        }
                    } else {
                        mHandler.sendEmptyMessage(TOAST_MESSAGE)
                    }

            }
            FAIL_VERSION -> KLog.d( "@@ Fail Version Check")
            TOAST_MESSAGE -> {
                val message = mContext.getString(R.string.check_version_lasted)
                Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
            }
        }//                realmMgr realmMgr = new realmMgr();
        //                RealmResults<Version> versionInfo = realmMgr.selectVersion();
        //                if (versionInfo != null) {
        //                    if (versionInfo.size() > 0) {
        //                        mVersion = versionInfo.get(0);
        //                        if (mVersion != null) {
        //                            mHandler.sendEmptyMessage(CHECK_VERSION);
        //                        }
        //                    }
        //                }
        return false
    }

    override fun onPopupAction(popId: Int, what: Int, obj: Any?) {
        when (popId) {
            PopupConst.POPUP_UPDATE_FORCE -> {
                if (what == IPopupReceive.POPUP_BTN_OK) {
                    val message = mContext.getString(R.string.version_update_string)
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                    AppUtils.locationMarket(mContext, mContext.packageName)
                }
                mBasicPopup!!.closeDialog()
            }
            PopupConst.POPUP_UPDATE_SELECT -> {
                if (what == IPopupReceive.POPUP_BTN_OK) {
                    val message = mContext.getString(R.string.version_update_string)
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                    AppUtils.locationMarket(mContext, mContext.packageName)
                }
                mConfirmPopup!!.closeDialog()
            }
        }
    }
}
