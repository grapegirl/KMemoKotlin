package momo.kikiplus.com.kbucket.view.popup

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.util.ScreenUtils


/***
 * @author mh kim
 * @version 1.0
 * @Class Name : Dialog
 * @Description : 이미지 팝업
 * @since 2016. 11. 6.
 */
class ImagePopup
/**
 * 생성자 - 다이얼로그 생성
 *
 * @param context
 * @param contentView
 */
(context: Context, contentView: Int, filePath: String, window: Window) : View.OnClickListener {

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
    protected var mPopupEventListener: OnPopupEventListener? = null
    /**
     * 다이얼로그 구분 값
     */
    protected var mPopId: Int = 0

    /***
     * 팝업창 취소 여부
     */
    protected var mCancleable = false

    private var mWindow: Window? = null

    val isShow: Boolean
        get() = dialog!!.isShowing

    init {
        mContext = context
        dialog = Dialog(mContext!!)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(true)
        //뷰 생성
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        mDialogView = inflater.inflate(contentView, null)
        dialog!!.setContentView(mDialogView!!)
        val bitmap = BitmapFactory.decodeFile(filePath)
        (mDialogView!!.findViewById<View>(R.id.basic_body_imageiew) as ImageView).setImageBitmap(bitmap)
        mDialogView!!.findViewById<View>(R.id.popup_ok_button).setOnClickListener(this)
        mWindow = window
    }

    /**
     * 다이얼로그 열기
     */
    fun showDialog() {
        if (dialog != null)
            ScreenUtils.setSecure(mWindow!!, true)
        dialog!!.show()
    }

    /**
     * 다이얼로그 닫기
     */
    fun closeDialog() {
        if (dialog != null) {
            dialog!!.dismiss()
            ScreenUtils.setSecure(mWindow!!, false)
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

    override fun onClick(v: View) {
        closeDialog()
    }

    companion object {

        /**
         * 다이얼로그
         */
        var dialog: Dialog? = null
            protected set
    }
}
