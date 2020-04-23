package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.app.Activity
import android.content.Intent
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
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.MainFragmentBinding
import momo.kikiplus.com.kbucket.view.Activity.BucketListActivity
import momo.kikiplus.com.kbucket.view.Activity.NoticeActivity
import momo.kikiplus.com.kbucket.view.Activity.RankListActivity
import momo.kikiplus.com.kbucket.view.Activity.ShareListActivity
import momo.kikiplus.modify.http.HttpUrlTaskManager
import momo.kikiplus.modify.http.IHttpReceive
import momo.kikiplus.refactoring.common.util.*
import momo.kikiplus.refactoring.common.view.KProgressDialog
import momo.kikiplus.refactoring.common.view.popup.BasicPopup
import momo.kikiplus.refactoring.common.view.popup.IPopupReceive
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.NetworkConst
import momo.kikiplus.refactoring.kbucket.data.finally.PopupConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity
import momo.kikiplus.refactoring.kbucket.ui.view.fragment.viewmodel.MainViewModel
import momo.kikiplus.refactoring.kbucket.ui.view.popup.AIPopup
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MainFragment : Fragment(), View.OnClickListener, Handler.Callback,
    IPopupReceive, IHttpReceive {

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
    private val BUCKET_LIST     : Int = 20
    private val SHARE_THE_WORLD : Int = 30
    private val NOTICE          : Int = 40
    private val REQUEST_AI      : Int = 50
    private val FAIL_AI         : Int = 60
    private val RESPOND_AI      : Int = 70

    private var mActivity : Activity? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        KLog.log("@@ onCreateView")

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
        KLog.log("@@ onActivityCreated")

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mActivity = activity

        fragmentManager!!.beginTransaction().add(newInstance(), "MainFragment")
    }

    override fun onClick(view: View) {
        KLog.d("@@ onClick ")
        when (view.id) {
            R.id.main_writeBtn -> {

                NavHostFragment
                    .findNavController(this)
                    .navigate(R.id.action_MainFragment_to_WriteFragment)


                (mActivity as MainFragmentActivity).sendUserEvent("가지작성화면")

            }
            R.id.main_listBtn -> mHandler.sendEmptyMessage(BUCKET_LIST)
            R.id.main_bucketlistBtn -> mHandler.sendEmptyMessage(SHARE_THE_WORLD)
            R.id.main_conf_btn -> {
                val intent = Intent(mActivity, MainFragmentActivity::class.java)
                intent.putExtra("DATA", DataConst.START_OPEN_DRAWER)
                startActivity(intent)
            }
            R.id.main_update_btn -> mHandler.sendEmptyMessage(NOTICE)
            R.id.main_ai_btn -> {
                KProgressDialog.setDataLoadingDialog(mActivity, true, this.getString(R.string.loading_string), true)
                mHandler.sendEmptyMessage(REQUEST_AI)
            }
            R.id.main_bucketRankBtn -> {
                val intent = Intent(mActivity, RankListActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun handleMessage(message: Message): Boolean {
        when (message.what) {
            TOAST_MASSEGE//메시지 출력
            -> Toast.makeText(context, message.obj as String, Toast.LENGTH_LONG).show()
            BUCKET_LIST//리스트 목록 보여주기
            -> {
                var intent = Intent(mActivity, BucketListActivity::class.java)
                startActivity(intent)
                AppUtils.sendTrackerScreen(mActivity!!, "완료가지화면")
            }
            SHARE_THE_WORLD//공유화면 보여주기
            -> {
                var intent = Intent(mActivity, ShareListActivity::class.java)
                startActivity(intent)
                AppUtils.sendTrackerScreen(mActivity!!, "모두가지화면")
            }
            NOTICE//공지사항 화면 보여주기
            -> {
                var intent = Intent(mActivity, NoticeActivity::class.java)
                startActivity(intent)
                AppUtils.sendTrackerScreen(mActivity!!, "공지화면")
            }
            REQUEST_AI -> {
                val userNickName = SharedPreferenceUtils.read(mActivity!!, PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
                val httpUrlTaskManager = HttpUrlTaskManager(NetworkConst.KBUCKET_AI, true, this, IHttpReceive.REQUEST_AI)
                val map = HashMap<String, Any>()
                map["nickname"] = userNickName!!
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
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
                KLog.d("@@ Respond AI msg : " + content)
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

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d("@@ onHttpReceive : $obj")
        // 버킷 공유 결과
        val mData = obj as String
        var message: String? = null
        if (actionId == IHttpReceive.REQUEST_AI) {
            if (type == IHttpReceive.HTTP_OK) {
                if (mData.isNotEmpty()) {
                    try {
                        val json = JSONObject(mData)
                        message = json.getString("replay")
                    } catch (e: JSONException) {
                        ErrorLogUtils.saveFileEror("@@ AI Respond jsonException message : " + e.message)
                        mHandler.sendEmptyMessage(FAIL_AI)
                    }
                }
                mHandler.sendMessage(mHandler.obtainMessage(RESPOND_AI, message))
            } else {
                mHandler.sendEmptyMessage(FAIL_AI)
            }
        }
    }
}
