/**
 *
 */
package momo.kikiplus.com.kbucket.view.Object

import android.app.ProgressDialog
import android.content.Context


/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : KProgressDialog
 * @Description : 진행율 프로그래스바
 * @since 2015. 6. 30.
 */
class KProgressDialog {

    val dialogStatus: Boolean
        get() = if (mDialog != null) {
            mDialog!!.isShowing
        } else {
            false
        }

    fun setDialogStatus(percentage: Int) {
        if (mDialog != null) {
            mDialog!!.progress = percentage
        }
    }

    companion object {

        /**
         * 다이얼로그
         */
        private var mDialog: ProgressDialog? = null


        /**
         * 로딩설정 여부 메소드
         *
         * @param flag 로딩 설정 여부
         * @param msg  메시지
         */
        fun setDataLoadingDialog(context: Context?, flag: Boolean?, msg: String?, cancle: Boolean?) {
            if (flag!!) {
                mDialog = ProgressDialog(context)
                mDialog!!.setMessage(msg)
                if (cancle!!) {
                    mDialog!!.setCancelable(true)
                } else {
                    mDialog!!.setCancelable(false)
                }
                mDialog!!.show()
            } else {
                if (mDialog != null) {
                    mDialog!!.dismiss()
                    mDialog = null
                }
            }
        }

        /**
         * 프로그래스바 표시 할때 사용 하는 메소드
         *
         * @param flag 로딩 설정 여부
         * @param msg  메시지
         */
        fun setDataProgressLoadingDialog(context: Context, flag: Boolean, msg: String, cancle: Boolean) {
            if (flag) {
                mDialog = ProgressDialog(context)
                mDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                mDialog!!.setMessage(msg)
                if (cancle) {
                    mDialog!!.setCancelable(true)
                } else {
                    mDialog!!.setCancelable(false)
                }
                mDialog!!.max = 100
                mDialog!!.show()
            } else {
                if (mDialog != null) {
                    mDialog!!.dismiss()
                    mDialog = null
                }
            }
        }
    }
}
