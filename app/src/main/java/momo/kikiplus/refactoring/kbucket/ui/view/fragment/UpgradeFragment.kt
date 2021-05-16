package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.QuestionActivityBinding
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity

class UpgradeFragment : Fragment(), IBackReceive , View.OnClickListener{

    companion object {
        fun newInstance() = UpgradeFragment()
    }

    private var mTitleIndex = 1
    private lateinit var binding : QuestionActivityBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.question_activity, container, false)
        binding = QuestionActivityBinding.bind(view)
        setBackgroundColor()
        return view
    }

    private fun setBackgroundColor() {
        KLog.d("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            binding.questionBackColor.setBackgroundColor(color)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setTitleIndex(1)

        binding.questionLayoutTitleView1.setOnClickListener(this)
        binding.questionLayoutTitleView2.setOnClickListener(this)
        binding.questionLayoutTitleView3.setOnClickListener(this)
        binding.questionLayoutButton.setOnClickListener(this)
    }

    override fun onBackKey() {
        KLog.log("@@ UpgradeFragment onBackKey back : " + requireArguments().getString("BACK") )
        (activity as MainFragmentActivity).setBackReceive(null)
        if(requireArguments().getString("BACK") == DataConst.VIEW_MAIN){
            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_main, MainFragment.newInstance())
                .commit()
        }
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  UpgradeFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            // 보내기 버튼
            R.id.question_layout_button -> {
                val title = getTitleIndex(mTitleIndex)
                val content = (binding.questionLayoutContentView as EditText).text.toString()
                sendEmail(title, content)
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
                binding.questionLayoutTitleView1.setBackgroundColor(Color.WHITE)
                binding.questionLayoutTitleView1.setTextColor(Color.parseColor("#FF99CC00"))
                binding.questionLayoutTitleView2.setBackgroundColor(Color.parseColor("#FF99CC00"))
                binding.questionLayoutTitleView2.setTextColor(Color.WHITE)
                binding.questionLayoutTitleView3.setBackgroundColor(Color.parseColor("#FF99CC00"))
                binding.questionLayoutTitleView3.setTextColor(Color.WHITE)
            }
            2 -> {
                binding.questionLayoutTitleView2.setBackgroundColor(Color.WHITE)
                binding.questionLayoutTitleView2.setTextColor(Color.parseColor("#FF99CC00"))
                binding.questionLayoutTitleView1.setBackgroundColor(Color.parseColor("#FF99CC00"))
                binding.questionLayoutTitleView1.setTextColor(Color.WHITE)
                binding.questionLayoutTitleView3.setBackgroundColor(Color.parseColor("#FF99CC00"))
                binding.questionLayoutTitleView3.setTextColor(Color.WHITE)
            }
            3 -> {
                binding.questionLayoutTitleView3.setBackgroundColor(Color.WHITE)
                binding.questionLayoutTitleView3.setTextColor(Color.parseColor("#FF99CC00"))
                binding.questionLayoutTitleView1.setBackgroundColor(Color.parseColor("#FF99CC00"))
                binding.questionLayoutTitleView1.setTextColor(Color.WHITE)
                binding.questionLayoutTitleView2.setBackgroundColor(Color.parseColor("#FF99CC00"))
                binding.questionLayoutTitleView2.setTextColor(Color.WHITE)
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