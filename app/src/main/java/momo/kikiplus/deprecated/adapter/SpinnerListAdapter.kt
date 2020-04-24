/**
 *
 */
package momo.kikiplus.deprecated.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.kbucket.data.vo.Category
import java.util.*

/**
 * @version 1.0
 * @Class Name : SpinnerListAdapter.java
 * @Description : 팝업 목록 어뎁터 클래스 (ListView)
 * @since 2014.08.01
 */
class SpinnerListAdapter(context: Context, data: ArrayList<Category>, res: Int) : BaseAdapter(), OnClickListener {

    /**
     * Context
     */
    private var m_Context: Context? = null

    /**
     * 레이아웃 리소스 ID
     */
    private var m_Res = 0

    /**
     * 팝업  목록 데이타
     */
    private var m_sDataList: ArrayList<Category>? = null

    /**
     * 팝업 아이템 선택 index
     */
    /**
     * 선택한 아이템의 index 반환하는 메소드
     *
     * @return 선택한 인덱스
     */
    var selectedIndex = -1
        private set

    /**
     * 팝업 아이템 선택 수
     */
    private val m_selectCnt = 0

    /**
     * 팝업 아이템 최대 선택 수
     */
    private val MAX_SELECTED_CNT = 1

    /**
     * 라디오 버튼 상태값
     */
    private var m_radioBtn: BooleanArray? = null

    private val isMultiSelect = false

    /**
     * 선택한 아이템 반환하는 메소드
     *
     * @return 선택한 내용
     */
    val selectedItem: String?
        get() = m_sDataList!![selectedIndex].categoryName

    /**
     * 선택한 item 반환 메소드
     *
     * @return 선택한 아이템 반환
     */
    val selectedItemList: String
        get() {
            var data = ""
            for (i in m_radioBtn!!.indices) {
                if (m_radioBtn!![i] == true)
                    data += m_sDataList!![i].categoryName
            }
            return data
        }

    val selectedCodeList: String
        get() {
            var data = ""
            for (i in m_radioBtn!!.indices) {
                if (m_radioBtn!![i] == true)
                    data += m_sDataList!![i].categoryCode
            }
            return data
        }

    init {
        m_Context = context
        m_Res = res
        m_sDataList = data
        m_radioBtn = BooleanArray(m_sDataList!!.size)
    }

    override fun getCount(): Int {
        return m_sDataList!!.size
    }

    override fun getItem(position: Int): Any {
        return m_sDataList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = convertView
        if (view == null) {
            val inflater = m_Context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(m_Res, null)
        }
        (view!!.findViewById<View>(R.id.spiner_item_textview) as TextView).text = m_sDataList!![position].categoryName
        val radio = view.findViewById<View>(R.id.spiner_item_checkbox) as CheckBox
        radio.isChecked = m_radioBtn!![position]
        radio.tag = position.toString()
        radio.setOnClickListener(this as OnClickListener)
        view.tag = position.toString()
        view.setOnClickListener(this as OnClickListener)

        return view
    }

    /***
     * default로 선택되어야 할 인덱스 설정 메소드
     *
     * @param index 선택한 인덱스
     */
    fun setDefaultChecked(index: Int) {
        if (selectedIndex > 0) {
            selectedIndex = index
            m_radioBtn!![selectedIndex] = true
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.spiner_item_checkbox//라디오 버튼 선택
                , R.id.spiner_list_panel//글자 선택
            -> {
                selectedIndex = Integer.parseInt(v.tag as String)
                for (i in m_radioBtn!!.indices) {
                    m_radioBtn!![i] = i == selectedIndex
                }
                notifyDataSetChanged()
            }
        }
    }

    private fun checkStatusCnt(): Int {
        var count = 0
        for (i in m_radioBtn!!.indices) {
            if (m_radioBtn!![i] == true) {
                count++
            }
        }
        return count
    }

}
