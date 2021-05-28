package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.MainFragmentBinding
import momo.kikiplus.refactoring.common.util.AppUtils
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.common.view.KProgressDialog
import momo.kikiplus.refactoring.common.view.popup.BasicPopup
import momo.kikiplus.refactoring.common.view.popup.IPopupReceive
import momo.kikiplus.refactoring.kbucket.action.net.AIRespond
import momo.kikiplus.refactoring.kbucket.action.net.NetRetrofit
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.PopupConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity
import momo.kikiplus.refactoring.kbucket.ui.view.fragment.viewmodel.MainViewModel
import momo.kikiplus.refactoring.kbucket.ui.view.popup.AIPopup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainFragment : Fragment(), View.OnClickListener, Handler.Callback, IPopupReceive, IBackReceive {

    private lateinit var mBinding : MainFragmentBinding

    companion object {
        fun newInstance() =
            MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private var mBasicPopup: BasicPopup? = null
    private var mAIPopup: AIPopup? = null

    private var mHandler: Handler = Handler(this)
    private val TOAST_MASSEGE   : Int = 0
    private val REQUEST_AI      : Int = 10
    private val FAIL_AI         : Int = 20
    private val RESPOND_AI      : Int = 30

    private var mActivity : Activity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        KLog.log("@@  MainFragment onCreateView")

        val view = inflater.inflate(R.layout.main_fragment, container, false)
        mBinding = MainFragmentBinding.bind(view)

        mBinding.mainWriteBtn.setOnClickListener(this)
        mBinding.mainUpdateBtn.setOnClickListener(this)
        mBinding.mainAiBtn.setOnClickListener(this)
        mBinding.mainListBtn.setOnClickListener(this)
        mBinding.mainBucketlistBtn.setOnClickListener(this)
        mBinding.mainConfBtn.setOnClickListener(this)
        mBinding.mainBucketRankBtn.setOnClickListener(this)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        KLog.log("@@ MainFragment onActivityCreated")
        setBackgroundColor()
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mActivity = activity

        MobileAds.initialize(mActivity){}

        var adviewRequset = AdRequest.Builder().build()
        //mBinding.mainAdLayout.adUnitId = DataConst.KBUCKET_AD_UNIT_ID
        //mBinding.mainAdLayout.adUnitId = "ca-app-pub-3940256099942544/6300978111" //TESTING
        mBinding.mainAdLayout.loadAd(adviewRequset)
    }

    private fun setBackgroundColor() {
        KLog.log("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            mBinding.mainBackColor.setBackgroundColor(color)
        }
    }

    override fun onClick(view: View) {
        KLog.log("@@ onClick ")
        val bundle = Bundle()
        when (view.id) {
            R.id.main_writeBtn -> {
                val fragment = WriteFragment()
                fragment.arguments =bundle
                bundle.putString("BACK", DataConst.VIEW_MAIN)

                (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.fragment_main, fragment)
                    .commit()

                (activity as MainFragmentActivity).sendUserEvent("가지작성화면")
            }
            R.id.main_listBtn -> {
                val fragment = DoneFragment()
                fragment.arguments =bundle
                bundle.putString("BACK", DataConst.VIEW_MAIN)

                (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.fragment_main, fragment)
                    .commit()

                (activity as MainFragmentActivity).sendUserEvent("완료가지화면")
            }
            R.id.main_bucketlistBtn ->{
                val fragment = ShareFragment()
                fragment.arguments =bundle
                bundle.putString("BACK", DataConst.VIEW_MAIN)

                (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.fragment_main, fragment)
                    .commit()

                (activity as MainFragmentActivity).sendUserEvent("모두가지화면")
            }
            R.id.main_conf_btn -> {
                (activity as MainFragmentActivity).sendConfEvent()
            }
            R.id.main_update_btn -> {
                NavHostFragment
                    .findNavController(this)
                    .navigate(R.id.action_MainFragment_to_NoticeFragment)

                AppUtils.sendTrackerScreen(mActivity!!, "공지화면")

            }

            R.id.main_ai_btn -> {
                KProgressDialog.setDataLoadingDialog(mActivity, true, this.getString(R.string.loading_string), true)
                mHandler.sendEmptyMessage(REQUEST_AI)
            }
            R.id.main_bucketRankBtn -> {

                val fragment = RankFragment()
                fragment.arguments =bundle
                bundle.putString("BACK", DataConst.VIEW_MAIN)

                (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .add(R.id.fragment_main, fragment)
                    .commit()

                (activity as MainFragmentActivity).sendUserEvent("버킷랭킹")
            }
        }
    }

    override fun handleMessage(message: Message): Boolean {
        when (message.what) {
            TOAST_MASSEGE//메시지 출력
            -> Toast.makeText(context, message.obj as String, Toast.LENGTH_LONG).show()
            REQUEST_AI -> {
                val userNickName = SharedPreferenceUtils.read(mActivity!!, PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
                val res = NetRetrofit.instance.service.getAIRespond(userNickName)
                res.enqueue(object : Callback<AIRespond>{
                    override fun onResponse(call: Call<AIRespond>, response: Response<AIRespond>) {
                        KLog.log("@@ onRecv ok : " + response)
                        KLog.log("@@ onRecv response body: " + response.body()!!.toString())
                        if(response.body()!!.bIsValid){
                            val message = response.body()!!.replay
                            mHandler.sendMessage(mHandler.obtainMessage(RESPOND_AI, message))
                        }else{
                            mHandler.sendEmptyMessage(FAIL_AI)
                        }
                    }
                    override fun onFailure(call: Call<AIRespond>, t: Throwable) {
                        mHandler.sendEmptyMessage(FAIL_AI)
                    }
                })
            }
            FAIL_AI -> {
                KProgressDialog.setDataLoadingDialog(context, false, null, false)
                val title = getString(R.string.popup_title)
                val content = getString(R.string.popup_prepare_string)
                mBasicPopup =
                    BasicPopup(
                        mActivity!!,
                        title,
                        content,
                        R.layout.popup_basic,
                        this,
                        PopupConst.POPUP_BASIC
                    )
                mBasicPopup!!.showDialog()
            }
            RESPOND_AI// AI 대답
            -> {
                KProgressDialog.setDataLoadingDialog(context, false, null, false)
                val content : String = message.obj as String
                KLog.log("@@ Respond AI msg : " + content)
                mAIPopup = AIPopup(
                    mActivity!!,
                    content,
                    R.layout.popup_ai,
                    this,
                    PopupConst.POPUP_AI
                )
                mAIPopup!!.showDialog()
            }
        }
        return false
    }

    override fun onPopupAction(popId: Int, what: Int, obj: Any?) {
        if (popId == PopupConst.POPUP_BASIC) {
            if (what == IPopupReceive.POPUP_BTN_OK || what == IPopupReceive.POPUP_BTN_CLOSEE || what == IPopupReceive.POPUP_DISPOSE) {
                mBasicPopup!!.closeDialog()
            }
        }
    }

    override fun onBackKey() {
        KLog.log("@@ MainFragement onBackKey")
        (activity as MainFragmentActivity).setBackReceive(null)
    }

    override fun onAttach(context: Context) {
        KLog.log("@@ MainFragement onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }

}
