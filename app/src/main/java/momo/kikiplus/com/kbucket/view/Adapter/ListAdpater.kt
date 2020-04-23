package momo.kikiplus.com.kbucket.view.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.common.util.ScreenUtils
import java.util.*

/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : ListAdpater
 * @Description : 리스트 목록 어뎁터
 * @since 2015. 1. 6.
 */
class ListAdpater
/**
 * 생성자
 */
(context: Context, res: Int, list: ArrayList<String>,
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
    private var mListItem: ArrayList<String>? = null

    /**
     * 버튼 표시 여부
     */
    private var mbVisible = true
    private var mbSetting = false

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
        (view!!.findViewById<View>(R.id.bucket_list_text) as EditText).setText(mListItem!![position])


        (view.findViewById<View>(R.id.bucket_list_modifyBtn) as Button).setOnClickListener(mClickListener)
        (view.findViewById<View>(R.id.bucket_list_modifyBtn) as Button).tag = position.toString() + ""
        (view.findViewById<View>(R.id.bucket_list_deleteBtn) as Button).setOnClickListener(mClickListener)
        (view.findViewById<View>(R.id.bucket_list_deleteBtn) as Button).tag = position.toString() + ""

        if (mbSetting) {
            if (mbVisible) {
                val width = ScreenUtils.dpToPx(200)
                (view.findViewById<View>(R.id.bucket_list_text) as EditText).layoutParams = LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT)
                (view.findViewById<View>(R.id.bucket_list_modifyBtn) as Button).visibility = View.VISIBLE
                (view.findViewById<View>(R.id.bucket_list_deleteBtn) as Button).visibility = View.VISIBLE
            } else {
                (view.findViewById<View>(R.id.bucket_list_text) as EditText).layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                (view.findViewById<View>(R.id.bucket_list_modifyBtn) as Button).visibility = View.GONE
                (view.findViewById<View>(R.id.bucket_list_deleteBtn) as Button).visibility = View.GONE
            }
        }
        return view
    }

    /**
     * 데이타 리스트 변경 메소드
     */
    fun setDataList(list: ArrayList<String>) {
        mListItem = list
        notifyDataSetChanged()
    }

    /**
     * 버튼들 보이게/ 숨기는 메소드
     *
     * @param isVisible
     */
    fun setDataVisible(isVisible: Boolean) {
        mbVisible = isVisible
        mbSetting = true
        notifyDataSetChanged()
    }
}
