package momo.kikiplus.com.kbucket.view.Object

import android.app.AlertDialog
import android.content.Context
import android.widget.ProgressBar


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
        if (mProgressBar != null) {
            mProgressBar!!.progress = percentage
        }
    }

    companion object {

        /**
         * 다이얼로그
         */
        private var mDialog: AlertDialog? = null


        private var mProgressBar : ProgressBar? = null
        /**
         * 로딩설정 여부 메소드
         *
         * @param flag 로딩 설정 여부
         * @param msg  메시지
         */
        fun setDataLoadingDialog(context: Context?, flag: Boolean?, msg: String?, cancle: Boolean?) {
            if (flag!!) {
                val builder =
                    AlertDialog.Builder(context)
                mProgressBar = ProgressBar(context)
                mProgressBar!!.isIndeterminate = true
                //mProgressBar!!.indeterminateDrawable = ContextCompat.getDrawable(context!!, circle_progress);
                builder.setView(mProgressBar)
                mDialog = builder.create()
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

                val builder =
                    AlertDialog.Builder(context)
                mProgressBar = ProgressBar(context)
                //mProgressBar!!.indeterminateDrawable = ContextCompat.getDrawable(context!!, R.drawable.circle_progress);
                builder.setView(mProgressBar)
                mDialog = builder.create()
                mDialog!!.setMessage(msg)
                if (cancle) {
                    mDialog!!.setCancelable(true)
                } else {
                    mDialog!!.setCancelable(false)
                }
                mProgressBar!!.max = 100
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
