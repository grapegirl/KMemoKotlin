package momo.kikiplus.refactoring.common.view.popup

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.Window


/***
 * @author mh kim
 * @version 1.0
 * @Class Name : Dialog
 * @Description : 다이얼로그
 * @since 2015. 1. 5.
 */
abstract class CustomPopup
/**
 * 생성자 - 다이얼로그 생성
 *
 * @param context
 * @param title
 * @param contentView
 */
(context: Context, title: String, contentView: Int, popupReceive: IPopupReceive, isCancle: Boolean,
 /**
  * 다이얼로그 구분 값
  */
 protected var mPopId: Int) : View.OnClickListener, DialogInterface.OnKeyListener {

    /**
     * 컨텍스트
     */
    protected var mContext: Context? = null


    /**
     * 다이얼로그 뷰
     */
    protected var mDialogView: View? = null

    /**
     * 다이얼로그 버튼리스너
     */
    protected var mPopupReceive: IPopupReceive? = null

    /***
     * 팝업창 취소 여부
     */
    protected var mCancleable = false

    val isShow: Boolean
        get() = dialog!!.isShowing


    init {
        mContext = context
        mPopupReceive = popupReceive
        mCancleable = isCancle

        dialog = Dialog(mContext!!)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(mCancleable)
        dialog!!.setCanceledOnTouchOutside(false)
        dialog!!.setOnKeyListener(this)
        //뷰 생성
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        mDialogView = inflater.inflate(contentView, null)
        dialog!!.setContentView(mDialogView!!)
        initDialog()
    }

    /**
     * 다이얼로그 초기화 메소드
     */
    protected abstract fun initDialog()

    /**
     * 다이얼로그 해제 메소드
     */
    protected abstract fun destroyDialog()

    /**
     * 다이얼로그 열기
     */
    fun showDialog() {
        if (dialog != null)
            dialog!!.show()
    }

    /**
     * 다이얼로그 닫기
     */
    fun closeDialog() {
        if (dialog != null) {
            dialog!!.dismiss()
            destroyDialog()
        }
    }

    /**
     * 주변 터치 영역 선택시 닫기 설정 메소드
     *
     * @param flag 선택 허용-true, 선택 불가-false
     */
    fun setCanceledOnTouchOutside(flag: Boolean) {
        dialog!!.setCanceledOnTouchOutside(flag)
    }

    /**
     * 백 키 선택 메소드
     */
    fun onPopupBackPressed() {

        if (mCancleable) {
            mPopupReceive!!.onPopupAction(mPopId,
                IPopupReceive.POPUP_BACK, null)
        } else {
            mPopupReceive!!.onPopupAction(mPopId,
                IPopupReceive.POPUP_BTN_OK, null)
        }
    }

    override fun onKey(dialog: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == event.keyCode) {
            onPopupBackPressed()
        }
        return false
    }

    companion object {

        /**
         * 다이얼로그
         */
        var dialog: Dialog? = null

    }

}
