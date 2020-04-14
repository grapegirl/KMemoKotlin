package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import momo.kikiplus.com.kbucket.Managers.http.HttpUrlTaskManager
import momo.kikiplus.com.kbucket.Managers.http.IHttpReceive
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.*
import momo.kikiplus.com.kbucket.view.Adapter.RankListAdpater
import momo.kikiplus.com.kbucket.view.Bean.BucketRank
import momo.kikiplus.com.kbucket.view.Object.KProgressDialog
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : RankListActivity
 * @Description :버킷 랭킹 목록
 * @since 2016-09-06.
 */
class RankListActivity : Activity(), IHttpReceive, View.OnClickListener, Handler.Callback {

    private var mHandler: Handler? = null
    private var mBucketDataList: ArrayList<BucketRank>? = null
    private var mListAdapter: RankListAdpater? = null
    private var mListView: ListView? = null
    private var mBucketRankComment: Int = 0
    private var mBucketRankIdx = -1

    private val TOAST_MASSEGE = 10
    private val SERVER_LOADING_FAIL = 20
    private val LOAD_BUCKET_RANK = 30
    private val SET_LIST = 40
    private val SEND_BUCKET_RANK = 50
    private val CHECK_NETWORK = 70

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.rank_list_activity)
        setBackgroundColor()
        setTextPont()
        mHandler = Handler(this)
        mBucketDataList = ArrayList()
        mHandler!!.sendEmptyMessage(CHECK_NETWORK)
        AppUtils.sendTrackerScreen(this, "버킷랭킹화면")
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.share_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive actionId: $actionId")
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive  type: $type")
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive  obj: $obj")
        val mData = obj as String
        var isValid = false
        if (mData.isNotEmpty()) {
            try {
                val json = JSONObject(mData)
                isValid = json.getBoolean("isValid")
            } catch (e: JSONException) {
                KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
            }

        }
        if (actionId == IHttpReceive.Companion.RANK_LIST) {
            KProgressDialog.setDataLoadingDialog(this, false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                try {
                    val json = JSONObject(mData)
                    val jsonArray = json.getJSONArray("bucketList")
                    KLog.d(this.javaClass.simpleName, "@@ jsonArray :   $jsonArray")
                    val size = jsonArray.length()
                    mBucketDataList!!.clear()
                    for (i in 0 until size) {
                        val jsonObject = jsonArray.get(i) as JSONObject
                        val bucket = BucketRank()
                        bucket.categoryCode = jsonObject.getInt("categoryCode")
                        bucket.content = jsonObject.getString("content")
                        bucket.idx = jsonObject.getInt("idx")
                        bucket.bestCnt = jsonObject.getInt("bestCnt")
                        bucket.goodCnt = jsonObject.getInt("goodCnt")
                        bucket.soSoCnt = jsonObject.getInt("ssoCnt")
                        bucket.userComment = jsonObject.getInt("comment")
                        mBucketDataList!!.add(bucket)
                    }
                    mHandler!!.sendEmptyMessage(SET_LIST)
                } catch (e: JSONException) {
                    KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
                    mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
                }

            } else {
                mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
            }
        } else if (actionId == IHttpReceive.Companion.RANK_UPDATE_COMMENT) {
            KProgressDialog.setDataLoadingDialog(this, false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                mBucketDataList!!.clear()
                mHandler!!.sendEmptyMessage(LOAD_BUCKET_RANK)
            } else {
                mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
            }

        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            TOAST_MASSEGE -> Toast.makeText(applicationContext, msg.obj as String, Toast.LENGTH_LONG).show()
            SERVER_LOADING_FAIL -> {
                KLog.d(ContextUtils.TAG, "@@ SERVER_LOADING_FAIL")
                val message = getString(R.string.server_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                finish()
            }
            LOAD_BUCKET_RANK -> {
                KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string), true)
                var userNickName: String = (SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?)!!
                var httpUrlTaskManager = HttpUrlTaskManager(ContextUtils.KBUCKET_RANK_LIST_URL, true, this, IHttpReceive.RANK_LIST)
                var map = HashMap<String, Any>()
                map["pageNm"] = "1"
                map["nickname"] = userNickName
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            SET_LIST -> {
                mListView = findViewById<View>(R.id.rank_list_listview) as ListView
                mListAdapter = RankListAdpater(this, R.layout.rank_list_line, mBucketDataList!!, this)
                mListView!!.adapter = mListAdapter
            }
            SEND_BUCKET_RANK -> {
                KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string), true)
                val userNickName = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
                val httpUrlTaskManager = HttpUrlTaskManager(ContextUtils.KBUCKET_RANK_COMMENT, true, this, IHttpReceive.RANK_UPDATE_COMMENT)
                val map = HashMap<String, Any>()
                map.put("idx", mBucketRankIdx)
                map.put("comment", mBucketRankComment)
                map.put("nickname", userNickName!!)
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            CHECK_NETWORK -> {
                val isConnect = NetworkUtils.isConnectivityStatus(this)
                if (!isConnect) {
                    val connectMsg = getString(R.string.check_network)
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, connectMsg))
                } else {
                    mHandler!!.sendEmptyMessage(LOAD_BUCKET_RANK)
                }
            }
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            //최고에요
            R.id.rank_btn1 -> mBucketRankComment = 3
            //좋아요
            R.id.rank_btn2 -> mBucketRankComment = 2
            //괜찮네요
            R.id.rank_btn3 -> mBucketRankComment = 1
        }

        val nIndex = v.tag as Int
        val isSendServer = getCommentCount(nIndex)
        // 의견이 없으면 서버에 반영
        if (!isSendServer) {
            mHandler!!.sendEmptyMessage(SEND_BUCKET_RANK)
            mBucketRankIdx = nIndex
        } else {
            mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, "이미 의견을 반영했습니다! "))
        }
    }

    private fun setTextPont() {
        val typeFace = DataUtils.getHannaFont(applicationContext)
        (findViewById<View>(R.id.rank_list_text) as Button).typeface = typeFace
    }

    private fun getCommentCount(index: Int): Boolean {
        if (mBucketDataList != null) {
            for (i in mBucketDataList!!.indices) {
                if (mBucketDataList!![i].idx == index) {
                    val comment = mBucketDataList!![i].userComment
                    if (comment != 0) {
                        return true
                    }
                }
            }
        }
        return false
    }
}
