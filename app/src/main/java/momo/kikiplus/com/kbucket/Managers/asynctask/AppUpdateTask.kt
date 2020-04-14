package momo.kikiplus.com.kbucket.Managers.asynctask

import android.content.Context
import android.os.AsyncTask
import android.os.Message
import android.widget.Toast
import momo.kikiplus.com.kbucket.Managers.http.HttpUrlTaskManager
import momo.kikiplus.com.kbucket.Managers.http.IHttpReceive
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.AppUtils
import momo.kikiplus.com.kbucket.Utils.ContextUtils
import momo.kikiplus.com.kbucket.Utils.KLog
import momo.kikiplus.com.kbucket.Utils.StringUtils
import momo.kikiplus.com.kbucket.view.Bean.Version
import momo.kikiplus.com.kbucket.view.popup.BasicPopup
import momo.kikiplus.com.kbucket.view.popup.ConfirmPopup
import momo.kikiplus.com.kbucket.view.popup.OnPopupEventListener
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * @author grape gril
 * @version 1.0
 * @Class Name : AppUpdateTask
 * @Description : App 업데이트 Task
 * @since 2016-01-28
 */
class AppUpdateTask(private val mContext: Context) : AsyncTask<Void, Void, Void>(), IHttpReceive, android.os.Handler.Callback, OnPopupEventListener {

    private val TAG = this.javaClass.simpleName
    private var mVersion: Version? = null
    private val mHandler: android.os.Handler

    private val START_VERSION = 10
    private val CHECK_VERSION = 20
    private val FAIL_VERSION = 30
    private val TOAST_MESSAGE = 40

    private var mBasicPopup: BasicPopup? = null
    private var mConfirmPopup: ConfirmPopup? = null

    init {
        mHandler = android.os.Handler(this)
    }

    override fun onPreExecute() {}



    override fun doInBackground(vararg params: Void): Void? {
        mHandler.sendEmptyMessage(START_VERSION)
        return null
    }

    override fun onPostExecute(aVoid: Void?) {
        super.onPostExecute(aVoid)
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d(this.javaClass.simpleName, " @@ onHttpReceive type:$type, object: $obj")
        if (actionId == IHttpReceive.UPDATE_VERSION) {
            if (type == IHttpReceive.HTTP_OK) {
                val mData = obj as String
                try {
                    val json = JSONObject(mData)
                    val versionCode = json.getInt("versionCode")
                    val versionName = json.getString("versionName")
                    val forceYN = json.getString("forceYN")

                    mVersion = Version()
                    mVersion!!.forceYN = forceYN
                    mVersion!!.versionCode = versionCode
                    mVersion!!.versionName = versionName

                    mHandler.sendEmptyMessage(CHECK_VERSION)

                } catch (e: JSONException) {
                    KLog.e(TAG, "@@ jsonException message : " + e.message)
                    mHandler.sendEmptyMessage(FAIL_VERSION)
                }

            } else {
                mHandler.sendEmptyMessage(FAIL_VERSION)
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {

        when (msg.what) {
            START_VERSION -> {
                val urlTaskManager = HttpUrlTaskManager(ContextUtils.KBUCKET_VERSION_UPDATE_URL, true, this, IHttpReceive.UPDATE_VERSION)
                KLog.d(TAG, "@@ URL : " + ContextUtils.KBUCKET_VERSION_UPDATE_URL)
                val map = HashMap<String, Any>()
                map["version"] = AppUtils.getVersionName(mContext)!!
                KLog.d(TAG, "@@ StringUtils.getHTTPPostSendData(map) : " + StringUtils.getHTTPPostSendData(map))
                urlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            CHECK_VERSION -> {
                val currentVersionName = AppUtils.getVersionName(mContext)
                val serverVersionName = mVersion!!.versionName
                KLog.d(TAG, "@@ currentVersion : " + currentVersionName!!)
                KLog.d(TAG, "@@ serverVersionName : " + serverVersionName!!)
                if (serverVersionName != null && serverVersionName != "null") {
                    if (StringUtils.compareVersion(currentVersionName, serverVersionName) > 0) {
                        if (mVersion!!.forceYN == "Y") {
                            val title = mContext.getString(R.string.update_popup_title)
                            val content = mContext.getString(R.string.update_popup_content_y)
                            mBasicPopup = BasicPopup(mContext, title, content, R.layout.popup_basic, this, OnPopupEventListener.POPUP_UPDATE_FORCE)
                            mBasicPopup!!.showDialog()
                        } else {
                            val title = mContext.getString(R.string.update_popup_title)
                            val content = mContext.getString(R.string.update_popup_content_n)
                            mConfirmPopup = ConfirmPopup(mContext, title, content, R.layout.popup_confirm, this, OnPopupEventListener.POPUP_UPDATE_SELECT)
                            mConfirmPopup!!.showDialog()
                        }
                    } else {
                        mHandler.sendEmptyMessage(TOAST_MESSAGE)
                    }
                }
            }
            FAIL_VERSION -> KLog.d(TAG, "@@ Fail Version Check")
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
            OnPopupEventListener.POPUP_UPDATE_FORCE -> {
                if (what == OnPopupEventListener.POPUP_BTN_OK) {
                    val message = mContext.getString(R.string.version_update_string)
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                    AppUtils.locationMarket(mContext, mContext.packageName)
                }
                mBasicPopup!!.closeDialog()
            }
            OnPopupEventListener.POPUP_UPDATE_SELECT -> {
                if (what == OnPopupEventListener.POPUP_BTN_OK) {
                    val message = mContext.getString(R.string.version_update_string)
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
                    AppUtils.locationMarket(mContext, mContext.packageName)
                }
                mConfirmPopup!!.closeDialog()
            }
        }
    }
}
