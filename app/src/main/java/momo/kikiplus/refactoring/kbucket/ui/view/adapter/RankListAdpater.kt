package momo.kikiplus.refactoring.kbucket.ui.view.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.kbucket.data.vo.BucketRank
import java.util.*

/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : RankListAdpater
 * @Description : 리스트 목록 어뎁터
 * @since 2016. 9. 6
 */
class RankListAdpater
/**
 * 생성자
 */
(context: Context, res: Int, list: ArrayList<BucketRank>,
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
    private var mListItem: ArrayList<BucketRank>? = null

    init {
        mContext = context
        mRes = res
        mListItem = list
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
        (view!!.findViewById<View>(R.id.rank_list_text) as EditText).setText(mListItem!![position].bucket!!.content)
        (view.findViewById<View>(R.id.rank_btn1) as Button).setOnClickListener(mClickListener)
        (view.findViewById<View>(R.id.rank_btn2) as Button).setOnClickListener(mClickListener)
        (view.findViewById<View>(R.id.rank_btn3) as Button).setOnClickListener(mClickListener)


        val bestCount = mListItem!![position].bestCnt
        val goodCount = mListItem!![position].goodCnt
        val sosoCount = mListItem!![position].soSoCnt
        val comment = mListItem!![position].userComment


        if (comment == 3) {
            (view.findViewById<View>(R.id.rank_btn1) as Button).setBackgroundColor(Color.WHITE)
            (view.findViewById<View>(R.id.rank_btn1) as Button).setTextColor(Color.parseColor("#FFCC0000"))
            (view.findViewById<View>(R.id.rank_btn2) as Button).setBackgroundColor(Color.parseColor("#FF33B5E5"))
            (view.findViewById<View>(R.id.rank_btn2) as Button).setTextColor(Color.BLACK)
            (view.findViewById<View>(R.id.rank_btn3) as Button).setBackgroundColor(Color.parseColor("#FF99CC00"))
            (view.findViewById<View>(R.id.rank_btn3) as Button).setTextColor(Color.BLACK)
        } else if (comment == 2) {
            (view.findViewById<View>(R.id.rank_btn1) as Button).setBackgroundColor(Color.parseColor("#FFCC0000"))
            (view.findViewById<View>(R.id.rank_btn1) as Button).setTextColor(Color.BLACK)
            (view.findViewById<View>(R.id.rank_btn2) as Button).setBackgroundColor(Color.WHITE)
            (view.findViewById<View>(R.id.rank_btn2) as Button).setTextColor(Color.parseColor("#FF33B5E5"))
            (view.findViewById<View>(R.id.rank_btn3) as Button).setBackgroundColor(Color.parseColor("#FF99CC00"))
            (view.findViewById<View>(R.id.rank_btn3) as Button).setTextColor(Color.BLACK)

        } else if (comment == 1) {
            (view.findViewById<View>(R.id.rank_btn1) as Button).setBackgroundColor(Color.parseColor("#FFCC0000"))
            (view.findViewById<View>(R.id.rank_btn1) as Button).setTextColor(Color.BLACK)
            (view.findViewById<View>(R.id.rank_btn2) as Button).setBackgroundColor(Color.parseColor("#FF33B5E5"))
            (view.findViewById<View>(R.id.rank_btn2) as Button).setTextColor(Color.BLACK)
            (view.findViewById<View>(R.id.rank_btn3) as Button).setBackgroundColor(Color.WHITE)
            (view.findViewById<View>(R.id.rank_btn3) as Button).setTextColor(Color.parseColor("#FF99CC00"))
        } else {
            (view.findViewById<View>(R.id.rank_btn1) as Button).setBackgroundColor(Color.parseColor("#FFCC0000"))
            (view.findViewById<View>(R.id.rank_btn1) as Button).setTextColor(Color.BLACK)
            (view.findViewById<View>(R.id.rank_btn2) as Button).setBackgroundColor(Color.parseColor("#FF33B5E5"))
            (view.findViewById<View>(R.id.rank_btn2) as Button).setTextColor(Color.BLACK)
            (view.findViewById<View>(R.id.rank_btn3) as Button).setBackgroundColor(Color.parseColor("#FF99CC00"))
            (view.findViewById<View>(R.id.rank_btn3) as Button).setTextColor(Color.BLACK)
        }
        (view.findViewById<View>(R.id.rank_text1) as TextView).text = bestCount.toString() + ""
        (view.findViewById<View>(R.id.rank_text2) as TextView).text = goodCount.toString() + ""
        (view.findViewById<View>(R.id.rank_text3) as TextView).text = sosoCount.toString() + ""

        val idx = mListItem!![position].bucket!!.idx
        (view.findViewById<View>(R.id.rank_btn1) as Button).tag = idx
        (view.findViewById<View>(R.id.rank_btn2) as Button).tag = idx
        (view.findViewById<View>(R.id.rank_btn3) as Button).tag = idx

        return view
    }

    /**
     * 데이타 리스트 변경 메소드
     */
    fun setDataList(list: ArrayList<BucketRank>) {
        mListItem = list
        notifyDataSetChanged()
    }
}
