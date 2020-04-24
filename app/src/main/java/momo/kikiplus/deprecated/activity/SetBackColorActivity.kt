package momo.kikiplus.deprecated.activity

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.common.util.AppUtils
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : SetNickNameActivity
 * @Description : 사용자 테마 설정
 * @since 2016-03-29
 */
class SetBackColorActivity : Activity(), View.OnClickListener {

    private var mImageButton: Array<ImageButton?> = arrayOfNulls(18)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.set_background_activity)
        setButtons()
        AppUtils.sendTrackerScreen(this, "테마변경화면")
    }

    private fun setButtons() {
        mImageButton[0] = findViewById<View>(R.id.set_back_btn1) as ImageButton
        mImageButton[1] = findViewById<View>(R.id.set_back_btn2) as ImageButton
        mImageButton[2] = findViewById<View>(R.id.set_back_btn3) as ImageButton
        mImageButton[3] = findViewById<View>(R.id.set_back_btn4) as ImageButton
        mImageButton[4] = findViewById<View>(R.id.set_back_btn5) as ImageButton
        mImageButton[5] = findViewById<View>(R.id.set_back_btn6) as ImageButton
        mImageButton[6] = findViewById<View>(R.id.set_back_btn7) as ImageButton
        mImageButton[7] = findViewById<View>(R.id.set_back_btn8) as ImageButton
        mImageButton[8] = findViewById<View>(R.id.set_back_btn9) as ImageButton
        mImageButton[9] = findViewById<View>(R.id.set_back_btn10) as ImageButton
        mImageButton[10] = findViewById<View>(R.id.set_back_btn11) as ImageButton
        mImageButton[11] = findViewById<View>(R.id.set_back_btn12) as ImageButton
        mImageButton[12] = findViewById<View>(R.id.set_back_btn13) as ImageButton
        mImageButton[13] = findViewById<View>(R.id.set_back_btn14) as ImageButton
        mImageButton[14] = findViewById<View>(R.id.set_back_btn15) as ImageButton
        mImageButton[15] = findViewById<View>(R.id.set_back_btn16) as ImageButton
        mImageButton[16] = findViewById<View>(R.id.set_back_btn17) as ImageButton
        mImageButton[17] = findViewById<View>(R.id.set_back_btn18) as ImageButton

        for (i in 0..17) {
            mImageButton[i]!!.setOnClickListener(this)
        }

    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.set_back_btn1, R.id.set_back_btn2, R.id.set_back_btn3, R.id.set_back_btn4, R.id.set_back_btn5, R.id.set_back_btn6, R.id.set_back_btn7, R.id.set_back_btn8, R.id.set_back_btn9, R.id.set_back_btn10, R.id.set_back_btn11, R.id.set_back_btn12, R.id.set_back_btn13, R.id.set_back_btn14, R.id.set_back_btn15, R.id.set_back_btn16, R.id.set_back_btn17, R.id.set_back_btn18 -> {
                val colorDrawable = (findViewById<View>(v.id) as ImageButton).background as ColorDrawable
                val backColor = colorDrawable.color
                SharedPreferenceUtils.write(applicationContext, PreferConst.BACK_MEMO, backColor)
                KLog.log( "@@ select Back Color : $backColor")
                checkButton(v.id)
            }
        }

    }

    private fun checkButton(buttonId: Int) {
        for (i in 0..17) {
            val vid = mImageButton[i]!!.id
            if (vid == buttonId) {
                mImageButton[i]!!.setImageResource(R.drawable.mark)
            } else {
                mImageButton[i]!!.setImageDrawable(null)
            }
        }
    }

    override fun onBackPressed() {
        finish()
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}
