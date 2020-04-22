package momo.kikiplus.refactoring.view.fragment.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.WriteFragmentBinding
import momo.kikiplus.com.kbucket.sqlite.SQLQuery
import momo.kikiplus.com.kbucket.view.Activity.WriteDetailActivity
import momo.kikiplus.com.kbucket.view.KBucketSort
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.SharedPreferenceUtils
import momo.kikiplus.refactoring.model.Bucket
import momo.kikiplus.refactoring.util.DateUtils
import momo.kikiplus.refactoring.util.KLog
import momo.kikiplus.refactoring.view.fragment.ui.viewmodel.WriteViewModel
import momo.kikiplus.refactoring.view.recycler.RecyclerViewAdapter
import java.util.*

class WriteFragment : Fragment(), View.OnClickListener, View.OnKeyListener {

    companion object {
        fun newInstance() = WriteFragment()
    }

    private lateinit var viewModel: WriteViewModel
    private lateinit var mBinding : WriteFragmentBinding

    private var mBucketDataList: ArrayList<Bucket>? = null
    private var mDataList: ArrayList<String>? = null

    private var mAdapter: RecyclerViewAdapter? = null
    private var mListView: RecyclerView? = null

    private var mSqlQuery: SQLQuery? = null
    private var mActivity : Activity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.write_fragment, container, false)
        mBinding = WriteFragmentBinding.bind(view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WriteViewModel::class.java)
        mActivity = activity

        setBackgroundColor()

        mDataList = ArrayList()
        mBucketDataList = ArrayList()

        mBinding.writeLayoutAddBtn.setOnClickListener(this)
        mBinding.sortDate.setOnClickListener(this)
        mBinding.sortDeadline.setOnClickListener(this)
        mBinding.sortMemo.setOnClickListener(this)
        mBinding.writeLayoutTitleView.setOnKeyListener(this)

        mSqlQuery = SQLQuery()

        var layoutMgr : LinearLayoutManager = LinearLayoutManager(mActivity)
        mBinding.writeListListview.layoutManager = layoutMgr
        mAdapter = RecyclerViewAdapter(this)
        mBinding.writeListListview.adapter = mAdapter

        setListData()
    }


    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(mActivity!!.applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            mBinding.writeBackColor.setBackgroundColor(color)
        }
    }

    override fun onClick(v: View) {
        KLog.d(ContextUtils.TAG, "@@ onClick id: $v.id")
        when (v.id) {
            R.id.write_layout_addBtn -> {
                val editText = mBinding.writeLayoutTitleView.text.toString()
                if (checkduplicateData(editText)) {
                    val message = getString(R.string.check_input_bucket_string)
                    Toast.makeText(mActivity!!.applicationContext, message, Toast.LENGTH_LONG).show()
                } else {
                    addDBData(editText)
                }
                mBinding.writeLayoutTitleView.setText("")
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
                val intent = Intent(mActivity!!.applicationContext, WriteDetailActivity::class.java)
                intent.putExtra("CONTENTS", mDataList!![index])
                intent.putExtra("BACK", ContextUtils.VIEW_WRITE)
                startActivity(intent)

                //fragmentManager!!.popBackStack();

            }
            //메모 정렬순
            R.id.sort_memo -> {
                SharedPreferenceUtils.write(mActivity!!.applicationContext, ContextUtils.KBUCKET_SORT_KEY, ContextUtils.SORT_MEMO)
                sort()
            }
            //날짜 정렬순
            R.id.sort_date -> {
                SharedPreferenceUtils.write(mActivity!!.applicationContext, ContextUtils.KBUCKET_SORT_KEY, ContextUtils.SORT_DATE)
                sort()
            }
            //기한 정렬순
            R.id.sort_deadline -> {
                SharedPreferenceUtils.write(mActivity!!.applicationContext, ContextUtils.KBUCKET_SORT_KEY, ContextUtils.SORT_DEADLINE)
                sort()
            }
        }
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //엔터입력시 할일 처리

                val editText = mBinding.writeLayoutTitleView.text.toString()
                if (checkduplicateData(editText)) {
                    val message = getString(R.string.check_input_bucket_string)
                    Toast.makeText(mActivity!!.applicationContext, message, Toast.LENGTH_LONG).show()
                } else {
                    addDBData(editText)
                }
                mBinding.writeLayoutTitleView.setText("")
                mBinding.writeLayoutTitleView.nextFocusDownId = R.id.write_layout_titleView
            }

        }
        return false
    }

    /**
     * DB 데이타 불러와서 데이타 표시하기
     */
    private fun setListData() {
        val map = mSqlQuery!!.selectKbucket(mActivity!!.applicationContext) ?: return
        KLog.d(ContextUtils.TAG, "@@ setListData map: $map")
        for (i in map.indices) {
            val memoMap = map[i]
            val postData = Bucket("", memoMap["contents"]!!, memoMap["date"]!!, i)
            postData.imageUrl = memoMap["image_path"]
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
        mSqlQuery!!.deleteUserBucket(mActivity!!.applicationContext, Content)
    }

    /**
     * DB 데이타 동기화하기(추가)
     *
     * @param Content 내용
     */
    private fun addDBData(Content: String) {
        mDataList!!.add(Content)
        mDataList!!.reverse()

        if(mAdapter == null){
            mAdapter = RecyclerViewAdapter(this)
            mListView!!.adapter = mAdapter
        }
        mAdapter!!.updateItems(mDataList!!)

        val dateTime = Date()
        val date = DateUtils.getStringDateFormat(DateUtils.DATE_YYMMDD_PATTER, dateTime)
        mSqlQuery!!.insertUserSetting(mActivity!!.applicationContext, Content, date, "N", "")

        val postData = Bucket("", Content, date, mBucketDataList!!.size)
        postData.imageUrl = ""
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
            if (mBucketDataList!![i].content == Content) {
                return true
            }
        }
        return false
    }

    /**
     * 정렬 기능
     */
    private fun sort() {
        val sort = SharedPreferenceUtils.read(mActivity!!.applicationContext, ContextUtils.KBUCKET_SORT_KEY, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
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
            mDataList!!.add(data.content!!)
        }
        if (sort == ContextUtils.SORT_DATE) {
            mDataList!!.reverse()
        }
        mAdapter!!.updateItems(mDataList!!)
    }


    override fun onStop() {
        super.onStop()
        mDataList = null
        mBucketDataList = null
    }
}
