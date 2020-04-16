package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ExpandableListView
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.ContextUtils
import momo.kikiplus.com.kbucket.Utils.KLog
import momo.kikiplus.com.kbucket.Utils.SharedPreferenceUtils
import momo.kikiplus.com.kbucket.net.NetRetrofit
import momo.kikiplus.com.kbucket.net.NoticeList
import momo.kikiplus.com.kbucket.view.Adapter.BaseExpandableAdapter
import momo.kikiplus.com.kbucket.view.Object.KProgressDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : NoticeActivity
 * @Description :공지사항 화면
 * @since 2015-12-27.
 */
class NoticeActivity : Activity(), Handler.Callback {

    private var mHandler: Handler? = null
    private var mList: ArrayList<NoticeList.Notice>? = null
    private var mExtendableListView: ExpandableListView? = null

    private val TOAST_MASSEGE = 10
    private val LOAD_NOTICE_LIST = 20
    private val SET_NOTICE_LIST = 30
    private val SERVER_LOADING_FAIL = 40

    private var mGroupList: ArrayList<String>? = null
    private var mChildList: ArrayList<ArrayList<String>>? = null
    private var mChildListContent: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.notice_extended)
        setBackgroundColor()

        mHandler = Handler(this)
        mList = ArrayList()
        mGroupList = ArrayList()
        mChildList = ArrayList()
        KProgressDialog.setDataLoadingDialog(this, true, this.getString(R.string.loading_string), true)

        mHandler!!.sendEmptyMessage(LOAD_NOTICE_LIST)

    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.notice_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            LOAD_NOTICE_LIST -> {

                val res = NetRetrofit.instance.service.updateList
                res.enqueue(object : Callback<NoticeList> {
                    override fun onResponse(call: Call<NoticeList>, response: Response<NoticeList>) {
                        if (response.body()!!.bIsValid) {
                            val size = response.body()!!.noticeList.size
                            mList!!.clear()
                            mGroupList!!.clear()
                            mChildList!!.clear()
                            mList!!.addAll(response.body()!!.noticeList)

                            for (i in 0 until size) {
                                val mTitle = mList!![i].mContents!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                                mGroupList!!.add(mTitle)
                                mChildListContent = ArrayList()
                                mChildListContent!!.add(mList!![i].mContents!!)
                                mChildList!!.add(mChildListContent!!)
                            }
                            mHandler!!.sendEmptyMessage(SET_NOTICE_LIST)
                        } else {
                            mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
                        }
                    }

                    override fun onFailure(call: Call<NoticeList>, t: Throwable) {
                        KLog.d(ContextUtils.TAG, "@@ NoticeList onFailure call : " + call.request())
                        KLog.d(ContextUtils.TAG, "@@ NoticeList onFailure message : " + t.message)
                        mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
                    }
                })
            }
            SET_NOTICE_LIST -> {
                KProgressDialog.setDataLoadingDialog(this, false, null, false)
                mExtendableListView = findViewById<View>(R.id.notice_listview_extended) as ExpandableListView?
                mExtendableListView!!.setAdapter(BaseExpandableAdapter(this, mGroupList!!, mChildList!!))
            }
            TOAST_MASSEGE -> Toast.makeText(applicationContext, msg.obj as String, Toast.LENGTH_LONG).show()
            SERVER_LOADING_FAIL -> {
                KProgressDialog.setDataLoadingDialog(this, false, null, false)
                KLog.d(ContextUtils.TAG, "@@ SERVER_LOADING_FAIL")
                val message = getString(R.string.server_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                finish()
            }
        }
        return false
    }
}
