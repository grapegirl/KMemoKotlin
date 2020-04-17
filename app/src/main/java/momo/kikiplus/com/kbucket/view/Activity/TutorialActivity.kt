package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ViewFlipper
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.modify.SharedPreferenceUtils
import momo.kikiplus.refactoring.util.AppUtils


/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : TutorialActivity
 * @Description : 뷰 플리퍼를 이용하여 튜토리얼
 * @since 2015-06-30.
 */
class TutorialActivity : Activity(), View.OnTouchListener {

    /**
     * 화면 전환 객체
     */
    private var mViewFlipper: ViewFlipper? = null

    /**
     * 화면 전환을 위한 터치 X값
     */
    private var mPreTouchPosX = 0

    /**
     * 현재 페이지
     */
    private var mCurrentPage = 0

    /**
     * 최대 페이지
     */
    private var mMacPage = 0
    /**
     * 라디오 아이템
     */
    private val mCheckBox = intArrayOf(R.id.tutorial_main_checkbox1, R.id.tutorial_main_checkbox2, R.id.tutorial_main_checkbox3, R.id.tutorial_main_checkbox4, R.id.tutorial_main_checkbox5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.tutorial_main_layout)
        setBackgroundColor()

        mViewFlipper = findViewById<View>(R.id.tutorial_main_viewFlipper) as ViewFlipper
        mViewFlipper!!.setOnTouchListener(this)
        mCurrentPage = 0
        mMacPage = 5
        setViewImgData()
        AppUtils.sendTrackerScreen(this, "가이드화면")
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.tutorial_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewFlipper!!.removeAllViews()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            mPreTouchPosX = event.x.toInt()
        }

        if (event.action == MotionEvent.ACTION_UP) {
            val nTouchPosX = event.x.toInt()

            if (nTouchPosX < mPreTouchPosX) {
                MoveNextView()
            } else if (nTouchPosX > mPreTouchPosX) {
                MovePreviousView()
            }
            mPreTouchPosX = nTouchPosX
        }
        return true
    }

    /**
     * 다음 화면 호출 메소드
     */
    private fun MoveNextView() {
        if (mCurrentPage + 1 < mMacPage) {
            mCurrentPage++
            //            mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.apper_from_right));
            //            mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.disapper_from_left));
            mViewFlipper!!.showNext()
            setPageCheck()
        }
    }

    /**
     * 이전 화면 호출 메소드
     */
    private fun MovePreviousView() {
        if (mCurrentPage - 1 >= 0) {
            mCurrentPage--
            //            mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.apper_from_left));
            //            mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.disapper_from_right));
            mViewFlipper!!.showPrevious()
            setPageCheck()
        }
    }

    /**
     * 현재 페이지 라디오 버튼 설정 메소드
     */
    private fun setPageCheck() {
        for (i in 0 until mMacPage) {
            val checkBox = findViewById<View>(mCheckBox[i]) as CheckBox
            if (i == mCurrentPage) {
                checkBox.isChecked = true
            } else {
                checkBox.isChecked = false
            }
        }
    }

    /**
     * 화면 설정 메소드
     */
    fun setViewImgData() {
        for (i in 0 until mMacPage) {
            val resource = getImgResource(i)
            val img = ImageView(this)
            try {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.RGB_565
                options.inSampleSize = 1
                val src = BitmapFactory.decodeResource(resources, resource, options)
                val resize = Bitmap.createScaledBitmap(src, options.outWidth, options.outHeight, true)
                img.setImageBitmap(resize)
                img.scaleType = ImageView.ScaleType.FIT_XY
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mViewFlipper!!.addView(img)
        }
        setPageCheck()
    }

    /**
     * 이미지 리소스 반환 메소드
     *
     * @return 이미지 리소스 id
     */
    fun getImgResource(index: Int): Int {
        var resId = -1
        when (index) {
            0 -> resId = R.drawable.tutorial01
            1 -> resId = R.drawable.tutorial02
            2 -> resId = R.drawable.tutorial03
            3 -> resId = R.drawable.tutorial04
            4 -> resId = R.drawable.tutorial05
        }
        return resId
    }
}
