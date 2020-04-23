package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.QuestionActivityBinding
import momo.kikiplus.modify.ContextUtils
import momo.kikiplus.refactoring.common.util.AppUtils
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : 문의하기 액티비티
 * @Description : 문의하기
 */
class QuestionActivity : Activity(), View.OnClickListener {

    private var mTitleIndex = 1

    private lateinit var mBinding : QuestionActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinding = QuestionActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setBackgroundColor()
        setTitleIndex(1)

        mBinding.questionLayoutTitleView1.setOnClickListener(this)
        mBinding.questionLayoutTitleView2.setOnClickListener(this)
        mBinding.questionLayoutTitleView3.setOnClickListener(this)
        mBinding.questionLayoutButton.setOnClickListener(this)

        AppUtils.sendTrackerScreen(this, "문의화면")
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.question_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onClick(v: View) {
        when (v.id) {
            // 보내기 버튼
            R.id.question_layout_button -> {
                val title = getTitleIndex(mTitleIndex)
                val content = (findViewById<View>(R.id.question_layout_contentView) as EditText).text.toString()
                sendEmail(title, content)
                finish()
            }
            R.id.question_layout_titleView1 -> setTitleIndex(1)
            R.id.question_layout_titleView2 -> setTitleIndex(2)
            R.id.question_layout_titleView3 -> setTitleIndex(3)
        }
    }

    /***
     * 메일 보내기
     *
     * @param name    제목
     * @param content 내용
     */
    private fun sendEmail(name: String, content: String) {
        val it = Intent(Intent.ACTION_SEND)
        it.type = "plain/text"
        val tos = arrayOf("kikiplus2030@naver.com")
        it.putExtra(Intent.EXTRA_EMAIL, tos)
        it.putExtra(Intent.EXTRA_SUBJECT, name)
        it.putExtra(Intent.EXTRA_TEXT, content)
        startActivity(it)
    }

    private fun setTitleIndex(index: Int) {
        mTitleIndex = index
        when (mTitleIndex) {
            1 -> {
                mBinding.questionLayoutTitleView1.setBackgroundColor(Color.WHITE)
                mBinding.questionLayoutTitleView1.setTextColor(Color.parseColor("#FF99CC00"))
                mBinding.questionLayoutTitleView2.setBackgroundColor(Color.parseColor("#FF99CC00"))
                mBinding.questionLayoutTitleView2.setTextColor(Color.WHITE)
                mBinding.questionLayoutTitleView3.setBackgroundColor(Color.parseColor("#FF99CC00"))
                mBinding.questionLayoutTitleView3.setTextColor(Color.WHITE)
            }
            2 -> {
                mBinding.questionLayoutTitleView2.setBackgroundColor(Color.WHITE)
                mBinding.questionLayoutTitleView2.setTextColor(Color.parseColor("#FF99CC00"))
                mBinding.questionLayoutTitleView1.setBackgroundColor(Color.parseColor("#FF99CC00"))
                mBinding.questionLayoutTitleView1.setTextColor(Color.WHITE)
                mBinding.questionLayoutTitleView3.setBackgroundColor(Color.parseColor("#FF99CC00"))
                mBinding.questionLayoutTitleView3.setTextColor(Color.WHITE)
            }
            3 -> {
                mBinding.questionLayoutTitleView3.setBackgroundColor(Color.WHITE)
                mBinding.questionLayoutTitleView3.setTextColor(Color.parseColor("#FF99CC00"))
                mBinding.questionLayoutTitleView1.setBackgroundColor(Color.parseColor("#FF99CC00"))
                mBinding.questionLayoutTitleView1.setTextColor(Color.WHITE)
                mBinding.questionLayoutTitleView2.setBackgroundColor(Color.parseColor("#FF99CC00"))
                mBinding.questionLayoutTitleView2.setTextColor(Color.WHITE)
            }
        }
    }

    private fun getTitleIndex(index: Int): String {
        when (index) {
            1 -> return "오류"
            2 -> return "개선"
            3 -> return "문의"
            else -> return "기타"
        }
    }
}