package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.WriteBucketLayoutBinding
import momo.kikiplus.com.kbucket.sqlite.SQLQuery
import momo.kikiplus.com.kbucket.view.Bean.PostData
import momo.kikiplus.com.kbucket.view.KBucketSort
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.SharedPreferenceUtils
import momo.kikiplus.refactoring.util.DateUtils
import momo.kikiplus.refactoring.util.KLog
import momo.kikiplus.refactoring.view.recycler.RecyclerViewAdapter
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : WriteActivity
 * @Description : 버킷 작성 클래스
 */
class WriteActivity : Activity(), View.OnClickListener, View.OnKeyListener {

    /**
     * 모든 버킷 목록
     */
    private var mBucketDataList: ArrayList<PostData>? = null
    private var mDataList: ArrayList<String>? = null

    private var mAdapter: RecyclerViewAdapter? = null
    private var mListView: RecyclerView? = null

    private var mSqlQuery: SQLQuery? = null

    private lateinit var mBinding : WriteBucketLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinding = WriteBucketLayoutBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setBackgroundColor()

        mDataList = ArrayList()
        mBucketDataList = ArrayList()

        mBinding.writeLayoutAddBtn.setOnClickListener(this)
        mBinding.sortDate.setOnClickListener(this)
        mBinding.sortDeadline.setOnClickListener(this)
        mBinding.sortMemo.setOnClickListener(this)
        mBinding.writeLayoutTitleView.setOnKeyListener(this)

        mSqlQuery = SQLQuery()


        var layoutMgr : LinearLayoutManager = LinearLayoutManager(this)
        mListView = findViewById(R.id.write_list_listview)
        mListView!!.layoutManager = layoutMgr
        mAdapter = RecyclerViewAdapter(this)
        mListView!!.adapter = mAdapter

        setListData()

    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.write_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(v: View) {
        KLog.d(ContextUtils.TAG, "@@ onClick id: $v.id")
        when (v.id) {
            R.id.write_layout_addBtn -> {
                val editText = (findViewById<View>(R.id.write_layout_titleView) as EditText).text.toString()
                if (checkduplicateData(editText)) {
                    val message = getString(R.string.check_input_bucket_string)
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                } else {
                    addDBData(editText)
                }
                (findViewById<View>(R.id.write_layout_titleView) as EditText).setText("")
            }
            // 삭제 버튼
            R.id.bucket_list_deleteBtn -> {
                var index = Integer.valueOf(v.tag as String)
                removeDBData(mDataList!![index])
                mDataList!!.removeAt(index)
                mAdapter!!.removeItems(index)
            }
            // 수정 버튼
            R.id.bucket_list_modifyBtn -> {
                val index = Integer.valueOf(v.tag as String)
                val intent = Intent(this, WriteDetailActivity::class.java)
                intent.putExtra("CONTENTS", mDataList!![index])
                intent.putExtra("BACK", ContextUtils.VIEW_WRITE)
                startActivity(intent)
                finish()
            }
            //메모 정렬순
            R.id.sort_memo -> {
                SharedPreferenceUtils.write(applicationContext, ContextUtils.KBUCKET_SORT_KEY, ContextUtils.SORT_MEMO)
                sort()
            }
            //날짜 정렬순
            R.id.sort_date -> {
                SharedPreferenceUtils.write(applicationContext, ContextUtils.KBUCKET_SORT_KEY, ContextUtils.SORT_DATE)
                sort()
            }
            //기한 정렬순
            R.id.sort_deadline -> {
                SharedPreferenceUtils.write(applicationContext, ContextUtils.KBUCKET_SORT_KEY, ContextUtils.SORT_DEADLINE)
                sort()
            }
        }
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //엔터입력시 할일 처리
                val editText = (findViewById<View>(R.id.write_layout_titleView) as EditText).text.toString()
                if (checkduplicateData(editText)) {
                    val message = getString(R.string.check_input_bucket_string)
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                } else {
                    addDBData(editText)
                }
                (findViewById<View>(R.id.write_layout_titleView) as EditText).setText("")
                (findViewById<View>(R.id.write_layout_titleView) as EditText).nextFocusDownId = R.id.write_layout_titleView
            }

        }
        return false
    }

    /**
     * DB 데이타 불러와서 데이타 표시하기
     */
    private fun setListData() {
        val map = mSqlQuery!!.selectKbucket(applicationContext) ?: return
        KLog.d(ContextUtils.TAG, "@@ setListData map: $map")
        for (i in map.indices) {
            val memoMap = map[i]
            val postData = PostData("", memoMap["contents"]!!, memoMap["date"]!!, i)
            postData.imageName = memoMap["image_path"]
            postData.completeYN = memoMap["complete_yn"]
            postData.deadLine = memoMap["deadline"]
            mBucketDataList!!.add(postData)

            if (memoMap["complete_yn"] == "Y") {
                continue
            }
            mDataList!!.add(memoMap["contents"]!!)
        }
        sort()
    }

    /**
     * DB 데이타 동기화하기(삭제)
     */
    private fun removeDBData(Content: String) {
        KLog.d(this.javaClass.simpleName, "@@ remove Data Contents : $Content")
        mSqlQuery!!.deleteUserBucket(applicationContext, Content)
    }

    /**
     * DB 데이타 동기화하기(추가)
     *
     * @param Content 내용
     */
    private fun addDBData(Content: String) {
        mDataList!!.add(Content)
        Collections.reverse(mDataList)

        if(mAdapter == null){
            mAdapter = RecyclerViewAdapter(this)
            mListView!!.adapter = mAdapter
        }
        mAdapter!!.updateItems(mDataList!!)

        val dateTime = Date()
        val date = DateUtils.getStringDateFormat(DateUtils.DATE_YYMMDD_PATTER, dateTime)
        mSqlQuery!!.insertUserSetting(applicationContext, Content, date, "N", "")

        val postData = PostData("", Content, date, mBucketDataList!!.size)
        postData.imageName = ""
        postData.completeYN = "N"
        mBucketDataList!!.add(postData)
    }

    /**
     * 중복 데이타 검사 메소드
     *
     * @param Content 추가할 내용
     * @return 중복 데이타 여부(true- 중복된 데이타 있음, false - 없음)
     */
    private fun checkduplicateData(Content: String): Boolean {
        for (i in mBucketDataList!!.indices) {
            if (mBucketDataList!![i].contents == Content) {
                return true
            }
        }
        return false
    }

    /**
     * 정렬 기능
     */
    private fun sort() {
        val sort = SharedPreferenceUtils.read(applicationContext, ContextUtils.KBUCKET_SORT_KEY, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        KLog.d(ContextUtils.TAG, "@@ sort sort: $sort")
        if (sort == null) {
            if(mAdapter != null){
                mAdapter!!.updateItems(mDataList!!)
            }
            return
        }
        if (sort == ContextUtils.SORT_DATE) {
            Collections.sort(mBucketDataList, KBucketSort.DATE_SORT)
        } else if (sort == ContextUtils.SORT_MEMO) {
            Collections.sort(mBucketDataList, KBucketSort.MEMO_SORT)
        } else {
            Collections.sort(mBucketDataList, KBucketSort.DEADLINE_SORT)
        }

        mDataList!!.clear()
        for (i in mBucketDataList!!.indices) {
            val data = mBucketDataList!![i]
            if (data.completeYN == "Y") {
                continue
            }
            mDataList!!.add(data.contents!!)
        }
        if (sort == ContextUtils.SORT_DATE) {

            Collections.reverse(mDataList)
        }
        mAdapter!!.updateItems(mDataList!!)
    }

    override fun onStop() {
        super.onStop()
        mDataList = null
        mBucketDataList = null
    }
}