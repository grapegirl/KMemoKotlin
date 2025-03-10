package momo.kikiplus.com.common.view.popup

import android.content.Context
import android.view.View
import android.widget.ListView
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.kbucket.data.vo.Category
import momo.kikiplus.com.kbucket.ui.view.adapter.SpinnerListAdapter
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
        private val mRes: Int, popupReceive: IPopupReceive, popId: Int) : CustomPopup(mCotext, mTitle, mRes, popupReceive, true, popId) {

    /**
     * 선택박스 리스트뷰
     */
    private var mListView: ListView? = null

    /**
     * 선택박스 리스트 목록 어뎁터
     */
    private var mAdapter: SpinnerListAdapter? = null

    /**
     * 선택박스 리스트 목록 데이타
     */
    var selectedItem: ArrayList<Category>? = null
        private set

    /**
     * 선택박스 리스트 기본 선택 index
     */
    private var mDefaultIndex = 0

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
        mAdapter = SpinnerListAdapter(
            mCotext,
            selectedItem,
            R.layout.popupview_spinner_list_line
        )
        mAdapter!!.setDefaultChecked(0)
        mListView = mDialogView!!.findViewById<View>(R.id.popup_spinner_list) as ListView
        mListView!!.adapter = mAdapter
        mListView!!.divider = null
        mListView!!.dividerHeight = 2
        (mDialogView!!.findViewById<View>(R.id.basic_title_textview) as TextView).text = mTitle
    }

    override fun destroyDialog() {
        KLog.e("@@ destory")
        dialog = null
    }

    override fun onClick(v: View) {
        when (v.id) {
            //닫기
            R.id.popup_close_button, R.id.popup_cancle_button -> closeDialog()
            R.id.popup_ok_button -> {
                val jsonObject = JSONObject()
                try {
                    jsonObject.put("selectItem", mAdapter!!.selectedItemList)
                    jsonObject.put("styleCode", mAdapter!!.selectedCodeList)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    KLog.d("@@ JSONException ")
                }

                mPopupReceive!!.onPopupAction(mPopId,
                    IPopupReceive.POPUP_BTN_OK, jsonObject)
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
        mDefaultIndex = index
    }

}
