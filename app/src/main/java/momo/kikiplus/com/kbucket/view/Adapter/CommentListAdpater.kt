package momo.kikiplus.com.kbucket.view.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.utils.StringUtils
import momo.kikiplus.com.kbucket.view.Bean.Comment
import java.util.*

/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : CommentListAdpater
 * @Description : 리스트 목록 어뎁터
 * @since 2015. 1. 6.
 */
class CommentListAdpater
/**
 * 생성자
 */
(context: Context, res: Int, list: ArrayList<Comment>,
 /**
  * 클릭 리스너
  */
 private val mClickListener: View.OnClickListener) : BaseAdapter() {

    /**
     * 컨텍스트
     */
    private var mContext: Context? = null

    /**
     * 리소스 아이디
     */
    private var mRes = -1

    /**
     * 리스트 아이템
     */
    private var mListItem: ArrayList<Comment>? = null

    init {
        mContext = context
        mRes = res
        mListItem = list
        Collections.reverse(mListItem)
    }

    override fun getCount(): Int {
        return if (mListItem != null) {
            mListItem!!.size
        } else -1
    }

    override fun getItem(position: Int): Any? {
        return if (mListItem != null) {
            mListItem!![position]
        } else null
    }

    override fun getItemId(position: Int): Long {
        return if (mListItem != null) {
            position.toLong()
        } else 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = convertView
        if (view == null) {
            val inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(mRes, null)
        }

        val date = mListItem!![position].date
        var convertDate: String?
        try {
            convertDate = StringUtils.convertTime(date!!)
        } catch (e: Exception) {
            convertDate = date
        }

        val data = mListItem!![position].content + "/" + convertDate
        (view!!.findViewById<View>(R.id.comment_list_text) as TextView).text = data
        (view.findViewById<View>(R.id.comment_list_nickname) as TextView).text = mListItem!![position].nickName
        return view
    }

    /**
     * 데이타 리스트 변경 메소드
     */
    fun setDataList(list: ArrayList<Comment>) {
        mListItem = list
        notifyDataSetChanged()
    }
}
