package momo.kikiplus.com.kbucket.view.popup

import android.content.Context
import android.view.View
import android.widget.TextView

import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.util.KLog


/***
 * @author grape girl
 * @version 1.0
 * @Class Name : Dialog
 * @Description : 확인/취소 다이얼로그
 * @since 2015. 1. 5.
 */
class ConfirmPopup
/**
 * 생성자 - 다이얼로그 생성
 *
 * @param context            컨텍스트
 * @param title              타이틀
 * @param contentView        레이아웃 리소스 아이디
 * @param popupEventListener 팝업 이벤트 리스너
 * @param popId              팝업 구분 값
 */
(context: Context,
 /**
  * 타이틀
  */
 private val mTitle: String,
 /**
  * 내용
  */
 private val mContent: String, contentView: Int, popupEventListener: OnPopupEventListener, popId: Int) : CustomPopup(context, mTitle, contentView, popupEventListener, true, popId) {

    init {
        setData()
    }

    override fun initDialog() {
        mDialogView!!.findViewById<View>(R.id.popup_close_button).setOnClickListener(this)
        mDialogView!!.findViewById<View>(R.id.popup_ok_button).setOnClickListener(this)
        mDialogView!!.findViewById<View>(R.id.popup_cancle_button).setOnClickListener(this)
    }

    private fun setData() {
        (mDialogView!!.findViewById<View>(R.id.basic_title_textview) as TextView).text = mTitle
        (mDialogView!!.findViewById<View>(R.id.basic_body_textView) as TextView).text = mContent
    }

    override fun destroyDialog() {
        dialog = null
    }

    override fun onClick(v: View) {
        KLog.d(this.javaClass.simpleName, "@@ ConfirmDialog onClick!")
        when (v.id) {
            R.id.popup_close_button, R.id.popup_cancle_button -> mPopupEventListener!!.onPopupAction(mPopId, OnPopupEventListener.POPUP_BTN_CLOSEE, null)
            R.id.popup_ok_button -> mPopupEventListener!!.onPopupAction(mPopId, OnPopupEventListener.POPUP_BTN_OK, null)
        }
    }
}
