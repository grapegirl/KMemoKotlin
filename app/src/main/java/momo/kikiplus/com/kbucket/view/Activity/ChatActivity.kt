package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import momo.kikiplus.com.kbucket.Managers.http.HttpUrlTaskManager
import momo.kikiplus.com.kbucket.Managers.http.IHttpReceive
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.*
import momo.kikiplus.com.kbucket.Utils.sqlite.SQLQuery
import momo.kikiplus.com.kbucket.view.Adapter.ChatListAdpater
import momo.kikiplus.com.kbucket.view.Bean.Chat
import momo.kikiplus.com.kbucket.view.Object.KProgressDialog
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : ChatActivity
 * @Description :공유 싱세 화면
 * @since 2016-12-11.
 */
class ChatActivity : Activity(), IHttpReceive, View.OnClickListener, Handler.Callback {

    private var mHandler: Handler? = null
    private var mList: ArrayList<Chat>? = null
    private var mSaveDBList: ArrayList<Chat>? = null
    private var mListAdapter: ChatListAdpater? = null
    private var mListView: ListView? = null
    private val mBucketNo = -1
    private var mUserNickname: String? = null
    private var mLastSeq: String? = "0"

    private val TOAST_MASSEGE = 10
    private val LOAD_CHAT_LIST = 20
    private val SERVER_LOADING_FAIL = 30
    private val SET_CHAT_LIST = 40

    private var mSqlQuery: SQLQuery? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.chat_activity)
        setBackgroundColor()
        mSqlQuery = SQLQuery()
        mHandler = Handler(this)
        mList = ArrayList()
        mSaveDBList = ArrayList()
        mUserNickname = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        (findViewById<View>(R.id.chat_comment_layout_sendBtn) as Button).setOnClickListener(this)
        setData()
        loadChatDBDat()
        AppUtils.sendTrackerScreen(this, "채팅화면")
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.bucketdetail_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /**
     * 데이타 초기화
     */
    private fun setData() {
        val typeFace = DataUtils.getHannaFont(applicationContext)
        (findViewById<View>(R.id.chat_detail_text) as Button).typeface = typeFace
        (findViewById<View>(R.id.chat_comment_layout_sendBtn) as Button).typeface = typeFace
        (findViewById<View>(R.id.chat_comment_layout_text) as EditText).typeface = typeFace

    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive actionId: $actionId")
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive  type: $type")
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive  obj: $obj")
        val mData = obj as String
        var isValid = false

        try {
            val json = JSONObject(mData)
            isValid = json.getBoolean("isValid")
        } catch (e: JSONException) {
            KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
        }

        if (actionId == IHttpReceive.SELECT_CHAT) {
            KProgressDialog.setDataLoadingDialog(this, false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                try {
                    val json = JSONObject(mData)
                    val jsonArray = json.getJSONArray("chatVOList")
                    val size = jsonArray.length()
                    for (i in 0 until size) {
                        val jsonObject = jsonArray.get(i) as JSONObject
                        val chat = Chat()
                        chat.nickName = jsonObject.getString("nickname")
                        chat.content = jsonObject.getString("content")
                        chat.date = jsonObject.getString("date")
                        chat.seq = jsonObject.getInt("seq")

                        if (mLastSeq != null) {
                            val nLastSeq = Integer.valueOf(mLastSeq!!)
                            if (nLastSeq < jsonObject.getInt("seq")) {
                                mLastSeq = jsonObject.getInt("seq").toString()
                                mList!!.add(chat)
                                mSaveDBList!!.add(chat)
                            }
                        }
                    }
                    mHandler!!.sendEmptyMessage(SET_CHAT_LIST)
                } catch (e: JSONException) {
                    KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
                    mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
                }

            } else {
                mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
            }
        } else if (actionId == IHttpReceive.Companion.INSERT_CHAT) {
            KProgressDialog.setDataLoadingDialog(this, false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                mHandler!!.sendMessage(mHandler!!.obtainMessage(LOAD_CHAT_LIST, mBucketNo))
            } else {
                mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.chat_comment_layout_sendBtn -> {
                val text = (findViewById<View>(R.id.chat_comment_layout_text) as EditText).text.toString()
                if ("" == text.replace(" ".toRegex(), "")) {
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, "내용을 입력해주세요~"))
                    return
                }
                KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string), true)
                val httpUrlTaskManager = HttpUrlTaskManager(ContextUtils.INSERT_CHAT, true, this, IHttpReceive.INSERT_CHAT)
                val map = HashMap<String, Any>()
                map["NICKNAME"] = mUserNickname!!
                map["CONTENT"] = text
                map["CREATE_DT"] = DateUtils.getDateFormat(DateUtils.KBUCKET_DB_DATE_PATTER, 0)
                map["idx"] = DateUtils.getDateFormat(DateUtils.DATE_YYMMDD_PATTER2, 0)
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
                (findViewById<View>(R.id.chat_comment_layout_text) as EditText).setText("")
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            LOAD_CHAT_LIST -> {
                KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string), true)
                val httpUrlTaskManager = HttpUrlTaskManager(ContextUtils.SELECT_CHAT, true, this, IHttpReceive.SELECT_CHAT)
                val map = HashMap<String, Any>()
                map["idx"] = DateUtils.getDateFormat(DateUtils.DATE_YYMMDD_PATTER2, 0)
                map["NICKNAME"] = mUserNickname!!
                if (mLastSeq != null) {
                    map["seq"] = mLastSeq!!
                }
                KLog.d(ContextUtils.TAG, "@@ loadchat  : " + StringUtils.getHTTPPostSendData(map))
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            SET_CHAT_LIST -> {
                mListView = findViewById<View>(R.id.chat_listview) as ListView
                mListAdapter = ChatListAdpater(this, R.layout.chat_list_line, mList!!)
                mListView!!.adapter = mListAdapter
                mListView!!.divider = null
                mListView!!.setSelection(mList!!.size)
                addChatDBData()
            }
            TOAST_MASSEGE -> Toast.makeText(applicationContext, msg.obj as String, Toast.LENGTH_LONG).show()
            SERVER_LOADING_FAIL -> {
                KLog.d(ContextUtils.TAG, "@@ SERVER_LOADING_FAIL")
                val message = getString(R.string.server_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                finish()
            }
        }
        return false
    }

    /**
     * Chat DB 데이타 동기화하기(추가)
     */
    private fun addChatDBData() {
        val chatIdx = DateUtils.getDateFormat(DateUtils.DATE_YYMMDD_PATTER2, 0)
        KLog.d(ContextUtils.TAG, "@@ addChatDBData  mSaveDBList.size(): " + mSaveDBList!!.size)
        for (i in mSaveDBList!!.indices) {
            val contents = mSaveDBList!![i].content
            val date = mSaveDBList!![i].date
            val nickname = mSaveDBList!![i].nickName
            val seq = mSaveDBList!![i].seq.toString()
            val imagePath = mSaveDBList!![i].imageUrl
            try {
                mSqlQuery!!.insertChatting(applicationContext, contents!!, date!!, nickname, imagePath!!, seq, chatIdx)
            } catch (e: Exception) {
                KLog.d(ContextUtils.TAG, "@@ addChatDBData exception : " + e.message)
            }

        }
        mSaveDBList!!.clear()
    }

    private fun loadChatDBDat() {
        val chatIdx = DateUtils.getDateFormat(DateUtils.DATE_YYMMDD_PATTER2, 0)
        val map = mSqlQuery!!.selectChatTable(applicationContext, chatIdx)
        if (map == null) {
            mHandler!!.sendEmptyMessage(LOAD_CHAT_LIST)
            return
        }
        for (i in map.indices) {
            val chatMap = map[i]
            val chat = Chat()
            chat.content = chatMap["contents"]
            chat.nickName = chatMap["nickname"]!!
            chat.seq = Integer.valueOf(chatMap["seq"]!!)
            chat.date = chatMap["date"]

            mList!!.add(chat)
            mLastSeq = chatMap["seq"]
        }
        KLog.d(ContextUtils.TAG, "@@ loadChatDBDat mLastSeq : " + mLastSeq!!)
        mHandler!!.sendEmptyMessage(LOAD_CHAT_LIST)
    }
}
