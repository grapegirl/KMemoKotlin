package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.InterestBucketListActivityBinding
import momo.kikiplus.com.kbucket.view.Adapter.ListAdpater
import momo.kikiplus.modify.sqlite.SQLQuery
import momo.kikiplus.refactoring.common.util.AppUtils
import momo.kikiplus.refactoring.common.util.DateUtils
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.common.view.popup.ConfirmPopup
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.data.vo.Bucket
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : AddBucketActivity
 * @Description : 버킷 추가 클래스
 */
class AddBucketActivity : Activity(), View.OnClickListener {

    private var mBucketDataList: ArrayList<Bucket> =ArrayList()
    private var mDataList: ArrayList<String> = ArrayList()
    private var mListAdapter: ListAdpater? = null

    private var mSqlQuery: SQLQuery? = null
    private var mbVisible = true
    private val mConfirmPopup: ConfirmPopup? = null

    private lateinit var mBinder : InterestBucketListActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinder = InterestBucketListActivityBinding.inflate(layoutInflater)
        setContentView(mBinder.root)

        initialize()
        mSqlQuery = SQLQuery()
        mListAdapter = ListAdpater(this, R.layout.interest_bucket_list_line, mDataList, this)
        mBinder.interestBucketListListview.adapter = mListAdapter
        mBinder.interestBucketListAdd.setOnClickListener(this)

        AppUtils.sendTrackerScreen(this, "관심버킷추가화면")
    }

    private fun initialize() {
        setBackgroundColor()
        val strArray = resources.getStringArray(R.array.dream100)
        for (i in strArray.indices) {
            mDataList.add(strArray[i])
        }
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.bucketlist_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.interest_bucket_list_add -> {
                mbVisible = !mbVisible

                if (mbVisible) {
                    mBinder.interestBucketListAdd.setText(R.string.interest_bucket_list_add)
                } else {
                    mBinder.interestBucketListAdd.setText(R.string.interest_bucket_list_view)
                }
                mListAdapter!!.setDataVisible(mbVisible)
            }
            R.id.bucket_list_modifyBtn//추가
            -> {
                var index = Integer.valueOf(v.tag as String)
                var data = mDataList[index]
                addDBData(data)
            }
            R.id.bucket_list_deleteBtn//삭제
            -> {
                val index = Integer.valueOf(v.tag as String)
                val data = mDataList[index]
                removeDBData(data)
            }
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
        mDataList.clear()
        mBucketDataList.clear()
    }
}