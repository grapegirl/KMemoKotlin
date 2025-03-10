package momo.kikiplus.com.kbucket.ui.view.fragment.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import momo.kikiplus.data.sqlite.SQLQuery
import momo.kikiplus.com.common.util.ByteUtils
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.kbucket.data.vo.Bucket
import java.util.*

class DetailViewModel : ViewModel() {

    val mSqlQuery = SQLQuery()

    fun loadDBData(context : Context, contents : String) : LinkedHashMap<String, String>?{
        val memoMap = mSqlQuery.selectKbucket(context, contents)
        if (memoMap.isNullOrEmpty()) {
            return null
        }
        return memoMap
    }

    fun loadDBImage(context : Context, contents: String, date : String) : ByteArray?{
        val bytes = mSqlQuery.selectImage(context, contents, date)
        KLog.d( "@@ loadDBImage bytes : " + bytes)
        KLog.d( "@@ loadDBImage contents : " + contents + ", data : " + date)
        return bytes
    }

    fun updateDBDate(context : Context, contents : String, bucket : Bucket) {
        KLog.d( "@@ updateDBDate Data : " + bucket)
        KLog.d( "@@ updateDBDate bucket.imageUrl : " + bucket.imageUrl)

        mSqlQuery.updateMemoContent(context, bucket.content!!,  contents,
            bucket.completeYN, bucket!!.date, bucket.imageUrl, bucket!!.deadLine)
        //수정한 상태지만 이미지는 이미 없는 상태
        if (bucket.imageUrl.length == 0) {
            mSqlQuery.deleteMemoImage(context, contents, bucket.date)
        } else {
            //신규로 추가한 경우
            val bitmaps = ByteUtils.getByteArrayFromFile(context, bucket.imageUrl)
            KLog.d("@@ bitmaps : " + bitmaps)
            if(bitmaps != null){
                mSqlQuery.updateMemoImage(context, contents, bucket.date, bitmaps)
            }
        }
    }

    fun removeDBData(context : Context, content: String, date : String) {
        KLog.d( "@@ remove Data Contents : " + content)
        mSqlQuery.deleteUserBucket(context, content)
        mSqlQuery.deleteMemoImage(context, content, date)
    }

}
