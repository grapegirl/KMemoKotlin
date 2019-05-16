package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.*
import momo.kikiplus.com.kbucket.Utils.sqlite.SQLQuery
import momo.kikiplus.com.kbucket.view.Adapter.ListAdpater
import momo.kikiplus.com.kbucket.view.Bean.PostData
import momo.kikiplus.com.kbucket.view.popup.ConfirmPopup
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : AddBucketActivity
 * @Description : 버킷 추가 클래스
 */
class AddBucketActivity : Activity(), View.OnClickListener {

    private var mBucketDataList: ArrayList<PostData>? = null
    private var mDataList: ArrayList<String>? = null
    private var mListAdapter: ListAdpater? = null

    private var mSqlQuery: SQLQuery? = null
    private var mbVisible = true
    private val mConfirmPopup: ConfirmPopup? = null

    internal var mListView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.interest_bucket_list_activity)

        initialize()
        mBucketDataList = ArrayList()
        mSqlQuery = SQLQuery()
        mListAdapter = ListAdpater(this, R.layout.interest_bucket_list_line, mDataList!!, this)
        mListView!!.adapter = mListAdapter
        AppUtils.sendTrackerScreen(this, "관심버킷추가화면")

        setBtnClickListener()
    }

    private fun initialize() {
        setBackgroundColor()
        setTextPont()
        setListData()
        mListView = findViewById(R.id.interest_bucket_list_listview) as ListView
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.bucketlist_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun setBtnClickListener(){
        val btn1 = findViewById(R.id.interest_bucket_list_add) as Button
        btn1.setOnClickListener(this);
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.interest_bucket_list_add -> {
                mbVisible = !mbVisible

                if (mbVisible) {
                    (findViewById<View>(R.id.interest_bucket_list_add) as Button).setText(R.string.interest_bucket_list_add)
                } else {
                    (findViewById<View>(R.id.interest_bucket_list_add) as Button).setText(R.string.interest_bucket_list_view)
                }
                mListAdapter!!.setDataVisible(mbVisible)
            }
            R.id.bucket_list_modifyBtn//추가
            -> {
                var index = Integer.valueOf(v.tag as String)
                var data = mDataList!![index]
                addDBData(data)
            }
            R.id.bucket_list_deleteBtn//삭제
            -> {
                val index = Integer.valueOf(v.tag as String)
                val data = mDataList!![index]
                removeDBData(data)
            }
        }
    }

    private fun setListData() {
        mDataList = ArrayList()
        val strArray = resources.getStringArray(R.array.dream100)
        for (i in strArray.indices) {
            mDataList!!.add(strArray[i])
        }
    }

    /**
     * DB 데이타 동기화하기(삭제)
     */
    private fun removeDBData(Content: String) {
        mSqlQuery!!.deleteUserBucket(applicationContext, Content)
        val inContainsBucket = mSqlQuery!!.containsKbucket(applicationContext, Content)
        if (!inContainsBucket) {
            val message = getString(R.string.share_delete_popup_ok)
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * DB 데이타 동기화하기(추가)
     *
     * @param Content 내용
     */
    private fun addDBData(Content: String) {
        val inContainsBucket = mSqlQuery!!.containsKbucket(applicationContext, Content)
        val message: String
        if (!inContainsBucket) {
            val dateTime = Date()
            val date = DateUtils.getStringDateFormat(DateUtils.DATE_YYMMDD_PATTER, dateTime)
            mSqlQuery!!.insertUserSetting(applicationContext, Content, date, "N", "")
            message = getString(R.string.share_add_popup_ok)
        } else {
            message = getString(R.string.check_input_bucket_string)
        }
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    override fun onStop() {
        super.onStop()
        mDataList = null
        mBucketDataList = null
    }

    private fun setTextPont() {
        val typeFace = DataUtils.getHannaFont(applicationContext)
        (findViewById<View>(R.id.interest_bucket_list_text) as Button).typeface = typeFace
        (findViewById<View>(R.id.interest_bucket_list_add) as Button).typeface = typeFace
    }
}