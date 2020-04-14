package momo.kikiplus.com.kbucket.view

import momo.kikiplus.com.kbucket.Utils.DateUtils
import momo.kikiplus.com.kbucket.view.Bean.PostData
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : KBucketSort
 * @Description : 버킷 정렬 클래스
 */
object KBucketSort {

    //날짜순 정렬
    var DATE_SORT: Comparator<PostData> = Comparator { lhs, rhs -> DateUtils.getCompareDate(DateUtils.DATE_YYMMDD_PATTER, lhs.date!!, rhs.date!!) }

    //내용순 정렬
    var MEMO_SORT: Comparator<PostData> = Comparator { lhs, rhs -> lhs.contents!!.compareTo(rhs.contents!!) }

    //기한순 정렬
    var DEADLINE_SORT: Comparator<PostData> = Comparator { lhs, rhs ->
        val lhsDate = if (lhs.deadLine != null) lhs.deadLine else lhs.date
        val rhsDate = if (rhs.deadLine != null) rhs.deadLine else rhs.date
       // KLog.d("Sort", "@@ Sort lhsDate: " + lhsDate!!)
       // KLog.d("Sort", "@@ Sort rhsDate: " + rhsDate!!)
        DateUtils.getCompareDate(DateUtils.DATE_YYMMDD_PATTER, lhsDate!!, rhsDate!!)
    }
}
