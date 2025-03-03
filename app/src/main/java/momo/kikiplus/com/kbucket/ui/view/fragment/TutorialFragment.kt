package momo.kikiplus.com.kbucket.ui.view.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.TutorialFragmentBinding
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.common.util.SharedPreferenceUtils
import momo.kikiplus.com.kbucket.data.finally.DataConst
import momo.kikiplus.com.kbucket.data.finally.PreferConst
import momo.kikiplus.com.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.com.kbucket.ui.view.activity.MainFragmentActivity
import momo.kikiplus.com.kbucket.ui.view.fragment.viewmodel.TutorialViewModel

class TutorialFragment : Fragment() , View.OnTouchListener, IBackReceive {

    companion object {
        fun newInstance() = TutorialFragment()
    }

    private lateinit var viewModel: TutorialViewModel

    private val mCheckBox = intArrayOf(R.id.tutorial_main_checkbox1, R.id.tutorial_main_checkbox2, R.id.tutorial_main_checkbox3, R.id.tutorial_main_checkbox4, R.id.tutorial_main_checkbox5)

    private lateinit var mBinding : TutorialFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.tutorial_fragment, container, false)
        mBinding = TutorialFragmentBinding.bind(view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TutorialViewModel::class.java)
        mBinding.tutorialMainViewFlipper!!.setOnTouchListener(this)
        viewModel.mCurrentPage = 0
        viewModel.mMacPage = 5
        setBackgroundColor()
        setViewImgData()
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(requireActivity().applicationContext, PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            mBinding.tutorialBackColor.setBackgroundColor(color)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mBinding.tutorialMainViewFlipper!!.removeAllViews()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            viewModel.mPreTouchPosX = event.x.toInt()
        }

        if (event.action == MotionEvent.ACTION_UP) {
            val nTouchPosX = event.x.toInt()

            if (nTouchPosX < viewModel.mPreTouchPosX) {
                MoveNextView()
            } else if (nTouchPosX > viewModel.mPreTouchPosX) {
                MovePreviousView()
            }
            viewModel.mPreTouchPosX = nTouchPosX
        }
        return true
    }

    /**
     * 다음 화면 호출 메소드
     */
    private fun MoveNextView() {
        if (viewModel.mCurrentPage + 1 < viewModel.mMacPage) {
            viewModel.mCurrentPage++
            //            mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.apper_from_right));
            //            mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.disapper_from_left));
            mBinding.tutorialMainViewFlipper!!.showNext()
            setPageCheck()
        }
    }

    /**
     * 이전 화면 호출 메소드
     */
    private fun MovePreviousView() {
        if (viewModel.mCurrentPage - 1 >= 0) {
            viewModel.mCurrentPage--
            //            mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.apper_from_left));
            //            mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.disapper_from_right));
            mBinding.tutorialMainViewFlipper!!.showPrevious()
            setPageCheck()
        }
    }

    /**
     * 현재 페이지 라디오 버튼 설정 메소드
     */
    private fun setPageCheck() {
        for (i in 0 until viewModel.mMacPage) {
            val checkBox = requireActivity().findViewById<View>(mCheckBox[i]) as CheckBox
            checkBox.isChecked = i == viewModel.mCurrentPage
        }
    }

    /**
     * 화면 설정 메소드
     */
    fun setViewImgData() {
        for (i in 0 until viewModel.mMacPage) {
            val resource = getImgResource(i)
            val img = ImageView(requireActivity().applicationContext)
            try {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.RGB_565
                options.inSampleSize = 1
                val src = BitmapFactory.decodeResource(resources, resource, options)
                val resize = Bitmap.createScaledBitmap(src, options.outWidth, options.outHeight, true)
                img.setImageBitmap(resize)
                img.scaleType = ImageView.ScaleType.FIT_CENTER
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mBinding.tutorialMainViewFlipper!!.addView(img)
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

    override fun onBackKey() {
        KLog.log("@@ TutorialFragment onBackKey back : " + requireArguments().getString("BACK") )
        (activity as MainFragmentActivity).setBackReceive(null)
        if(requireArguments().getString("BACK") == DataConst.VIEW_MAIN){
            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_main, MainFragment.newInstance())
                .commit()
        }
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  TutorialFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }

}
