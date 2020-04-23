package momo.kikiplus.com.kbucket.view.popup

import android.content.Context
import android.view.View
import android.widget.ListView
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.view.Adapter.SpinnerListAdapter
import momo.kikiplus.refactoring.util.KLog
import momo.kikiplus.refactoring.vo.Category
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/***
 * @version 1.0
 * @Class Name : SpinnerListPopup.java
 * @Description : 선택박스 리스트뷰 팝업
 * @since 2014.08.01
 */
class SpinnerListPopup
/***
 * 선택박스 리스트뷰 팝업 생성자
 */
(
    /**
         * 컨텍스트
         */
        private val mCotext: Context,
    /**
         * 타이틀
         */
        private val mTitle: String,
    /**
         * 내용
         */
        private val mContent: String, listData: ArrayList<Category>,
    /**
         * 레이아웃 리소스
         */
        private val m_Res: Int, popupEventListener: OnPopupEventListener, popId: Int) : CustomPopup(mCotext, mTitle, m_Res, popupEventListener, true, popId) {

    /**
     * 선택박스 리스트뷰
     */
    private var m_ListView: ListView? = null

    /**
     * 선택박스 리스트 목록 어뎁터
     */
    private var m_Adapter: SpinnerListAdapter? = null

    /**
     * 선택박스 리스트 목록 데이타
     */
    var selectedItem: ArrayList<Category>? = null
        private set

    /**
     * 선택박스 리스트 기본 선택 index
     */
    private var m_defaultIndex = 0

    init {
        setData(listData)
    }

    override fun initDialog() {
        mDialogView!!.findViewById<View>(R.id.popup_close_button).setOnClickListener(this)
        mDialogView!!.findViewById<View>(R.id.popup_ok_button).setOnClickListener(this)
        mDialogView!!.findViewById<View>(R.id.popup_cancle_button).setOnClickListener(this)
    }

    private fun setData(listData: ArrayList<Category>) {
        val selectedItem = listData
        m_Adapter = SpinnerListAdapter(mCotext, selectedItem, R.layout.popupview_spinner_list_line)
        m_Adapter!!.setDefaultChecked(0)
        m_ListView = mDialogView!!.findViewById<View>(R.id.popup_spinner_list) as ListView
        m_ListView!!.adapter = m_Adapter
        m_ListView!!.divider = null
        m_ListView!!.dividerHeight = 2
        (mDialogView!!.findViewById<View>(R.id.basic_title_textview) as TextView).text = mTitle
    }

    override fun destroyDialog() {
        KLog.e(this.javaClass.simpleName, "@@ destory")
        dialog = null
    }

    override fun onClick(v: View) {
        when (v.id) {
            //닫기
            R.id.popup_close_button, R.id.popup_cancle_button -> closeDialog()
            R.id.popup_ok_button -> {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("selectItem", m_Adapter!!.selectedItemList)
                    jsonObject.put("styleCode", m_Adapter!!.selectedCodeList)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    KLog.d(this.javaClass.simpleName, "@@ JSONException ")
                }

                mPopupEventListener!!.onPopupAction(mPopId, OnPopupEventListener.POPUP_BTN_OK, jsonObject)
                closeDialog()
            }
        }
    }

    /**
     * default 선택할 index 설정하는 메소드
     *
     * @param index 기본 설정 인덱스
     */
    fun setDefaultChecked(index: Int) {
        m_defaultIndex = index
    }

}
