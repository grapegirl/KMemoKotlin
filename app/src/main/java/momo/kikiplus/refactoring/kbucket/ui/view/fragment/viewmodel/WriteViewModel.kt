package momo.kikiplus.refactoring.kbucket.ui.view.fragment.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import momo.kikiplus.deprecated.sqlite.SQLQuery
import momo.kikiplus.refactoring.common.util.DateUtils
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.vo.Bucket
import java.util.*
import kotlin.Comparator

class WriteViewModel : ViewModel() {

    private var mBucketDataList: ArrayList<Bucket> = ArrayList()

    private var mSqlQuery: SQLQuery =
        SQLQuery()

    fun initLocalData(context : Context) {
        val map = mSqlQuery.selectKbucket(context) ?: return
        mBucketDataList.clear()
        KLog.d("@@ setListData map: $map size : ${map.size}" )
        for (i in map.indices) {
            val memoMap = map[i]
            val bucket = Bucket("", memoMap["contents"]!!, memoMap["date"]!!, i)
            bucket.imageUrl = memoMap["image_path"]
            bucket.completeYN = memoMap["complete_yn"]
            bucket.deadLine = memoMap["deadline"]

            if (memoMap["complete_yn"] == "Y") {
                continue
            }
            mBucketDataList.add(bucket)
        }
    }

    fun addData(Content : String, context: Context){
        KLog.log("@@ remove Data Contents : $Content")
        val dateTime = Date()
        val date = DateUtils.getStringDateFormat(DateUtils.DATE_YYMMDD_PATTER, dateTime)
        mSqlQuery.insertUserSetting(context, Content, date, "N", "")
    }

    fun removeData(Content : String, context: Context){
        KLog.log("@@ remove Data Contents : $Content")
        mSqlQuery.deleteUserBucket(context, Content)
    }


    fun getListDoing() : ArrayList<String> {
        var list = ArrayList<String>()
        for (i in mBucketDataList.indices) {
            val data = mBucketDataList[i]
            if (data.completeYN == "Y") {
                continue
            }
            list.add(data.content!!)
        }
        return list
    }

    /**
     * 중복 데이타 검사 메소드
     *
     * @param Content 추가할 내용
     * @return 중복 데이타 여부(true- 중복된 데이타 있음, false - 없음)
     */
    fun checkduplicateData(Content: String): Boolean {
        for (i in mBucketDataList.indices) {
            if (mBucketDataList[i].content == Content) {
                return true
            }
        }
        return false
    }

    fun sort(sorting : String){
        if (sorting == DataConst.SORT_DATE) {
            Collections.sort(mBucketDataList, DATE_SORT)
        } else if (sorting == DataConst.SORT_MEMO) {
            Collections.sort(mBucketDataList, MEMO_SORT)
        } else {
            Collections.sort(mBucketDataList, DEADLINE_SORT)
        }
    }

    //날짜순 정렬
    var DATE_SORT: java.util.Comparator<Bucket> = Comparator { lhs, rhs -> DateUtils.getCompareDate(
        DateUtils.DATE_YYMMDD_PATTER, lhs.date!!, rhs.date!!) }

    //내용순 정렬
    var MEMO_SORT: java.util.Comparator<Bucket> = Comparator { lhs, rhs -> lhs.content!!.compareTo(rhs.content!!) }

    //기한순 정렬
    var DEADLINE_SORT: java.util.Comparator<Bucket> = Comparator { lhs, rhs ->
        val lhsDate = if (lhs.deadLine != null) lhs.deadLine else lhs.date
        val rhsDate = if (rhs.deadLine != null) rhs.deadLine else rhs.date
        // KLog.d("Sort", "@@ Sort lhsDate: " + lhsDate!!)
        // KLog.d("Sort", "@@ Sort rhsDate: " + rhsDate!!)
        DateUtils.getCompareDate(DateUtils.DATE_YYMMDD_PATTER, lhsDate!!, rhsDate!!)
    }
}
