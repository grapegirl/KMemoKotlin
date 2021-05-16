package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.SetBackgroundActivityBinding
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity

class ColorFragment: Fragment(), IBackReceive ,  View.OnClickListener {

    companion object {
        fun newInstance() = ColorFragment()
    }

    private lateinit var binding : SetBackgroundActivityBinding
    private var mImageButton: Array<ImageButton?> = arrayOfNulls(18)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.set_background_activity, container, false)
        binding = SetBackgroundActivityBinding.bind(view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setButtons()
    }

    private fun setButtons() {
        mImageButton[0] = binding.setBackBtn1 as ImageButton
        mImageButton[1] = binding.setBackBtn2 as ImageButton
        mImageButton[2] = binding.setBackBtn3 as ImageButton
        mImageButton[3] = binding.setBackBtn4 as ImageButton
        mImageButton[4] = binding.setBackBtn5 as ImageButton
        mImageButton[5] = binding.setBackBtn6 as ImageButton
        mImageButton[6] = binding.setBackBtn7 as ImageButton
        mImageButton[7] = binding.setBackBtn8 as ImageButton
        mImageButton[8] = binding.setBackBtn9 as ImageButton
        mImageButton[9] = binding.setBackBtn10 as ImageButton
        mImageButton[10] = binding.setBackBtn11 as ImageButton
        mImageButton[11] = binding.setBackBtn12 as ImageButton
        mImageButton[12] = binding.setBackBtn13 as ImageButton
        mImageButton[13] = binding.setBackBtn14 as ImageButton
        mImageButton[14] = binding.setBackBtn15 as ImageButton
        mImageButton[15] = binding.setBackBtn16 as ImageButton
        mImageButton[16] = binding.setBackBtn17 as ImageButton
        mImageButton[17] = binding.setBackBtn18 as ImageButton

        for (i in 0..17) {
            mImageButton[i]!!.setOnClickListener(this)
        }

    }


    override fun onBackKey() {
        KLog.log("@@ ColorFragment onBackKey back : " + requireArguments().getString("BACK") )
        (activity as MainFragmentActivity).setBackReceive(null)
        if(requireArguments().getString("BACK") == DataConst.VIEW_MAIN){
            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_main, MainFragment.newInstance())
                .commit()
        }
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  ColorFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.set_back_btn1, R.id.set_back_btn2, R.id.set_back_btn3, R.id.set_back_btn4, R.id.set_back_btn5, R.id.set_back_btn6, R.id.set_back_btn7, R.id.set_back_btn8, R.id.set_back_btn9, R.id.set_back_btn10, R.id.set_back_btn11, R.id.set_back_btn12, R.id.set_back_btn13, R.id.set_back_btn14, R.id.set_back_btn15, R.id.set_back_btn16, R.id.set_back_btn17, R.id.set_back_btn18 -> {
                val colorDrawable = (v as ImageButton).background as ColorDrawable
                val backColor = colorDrawable.color
                SharedPreferenceUtils.write(requireContext(), PreferConst.BACK_MEMO, backColor)
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
}