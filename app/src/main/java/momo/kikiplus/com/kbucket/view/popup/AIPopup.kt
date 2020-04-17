package momo.kikiplus.com.kbucket.view.popup

import android.content.Context
import android.view.View
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.util.KLog

/**
 * Created by mihyeKim on 2015-12-16.
 */
class AIPopup
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
 private val mTitle: String, contentView: Int, popupEventListener: OnPopupEventListener, popId: Int) : CustomPopup(context, mTitle, contentView, popupEventListener, true, popId) {

    init {
        initDialog()
    }

    override fun initDialog() {
        (mDialogView!!.findViewById<View>(R.id.basic_title_textview) as TextView).text = mTitle
    }


    override fun destroyDialog() {
        dialog = null
    }

    override fun onClick(v: View) {
        KLog.d(this.javaClass.simpleName, "@@ BasicDialog onClick!")
        mPopupEventListener!!.onPopupAction(mPopId, OnPopupEventListener.POPUP_BTN_OK, null)
    }
}
