package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.RankListActivityBinding
import momo.kikiplus.deprecated.http.HttpUrlTaskManager
import momo.kikiplus.deprecated.http.IHttpReceive
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.NetworkUtils
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.common.util.StringUtils
import momo.kikiplus.refactoring.common.view.KProgressDialog
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.NetworkConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.data.vo.Bucket
import momo.kikiplus.refactoring.kbucket.data.vo.BucketRank
import momo.kikiplus.refactoring.kbucket.data.vo.Category
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity
import momo.kikiplus.refactoring.kbucket.ui.view.adapter.RankListAdpater
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class RankFragment : Fragment(), Handler.Callback , IBackReceive, IHttpReceive, View.OnClickListener {

    companion object {
        fun newInstance() = RankFragment()
    }

    private lateinit var binding : RankListActivityBinding

    private var mHandler: android.os.Handler? = null
    private var mBucketDataList: ArrayList<BucketRank>? = null
    private var mListAdapter: RankListAdpater? = null
    private var mListView: ListView? = null
    private var mBucketRankComment: Int = 0
    private var mBucketRankIdx = -1

    private val TOAST_MASSEGE = 10
    private val SERVER_LOADING_FAIL = 20
    private val LOAD_BUCKET_RANK = 30
    private val SET_LIST = 40
    private val SEND_BUCKET_RANK = 50
    private val CHECK_NETWORK = 70


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.rank_list_activity, container, false)
        binding = RankListActivityBinding.bind(view)
        setBackgroundColor()
        return view
    }

    private fun setBackgroundColor() {
        KLog.d("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            binding.shareBackColor.setBackgroundColor(color)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mHandler = Handler(this)
        mBucketDataList = ArrayList()
        mHandler!!.sendEmptyMessage(CHECK_NETWORK)
    }


    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d("@@ onHttpReceive actionId: $actionId")
        KLog.d( "@@ onHttpReceive  type: $type")
        KLog.d( "@@ onHttpReceive  obj: $obj")
        val mData = obj as String
        var isValid = false
        if (mData.isNotEmpty()) {
            try {
                val json = JSONObject(mData)
                isValid = json.getBoolean("isValid")
            } catch (e: JSONException) {
                KLog.log("@@ jsonException message : " + e.message)
            }

        }
        if (actionId == IHttpReceive.Companion.RANK_LIST) {
            KProgressDialog.setDataLoadingDialog(requireContext(), false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                try {
                    val json = JSONObject(mData)
                    val jsonArray = json.getJSONArray("bucketList")
                    KLog.d("@@ jsonArray :   $jsonArray")
                    val size = jsonArray.length()
                    mBucketDataList!!.clear()
                    for (i in 0 until size) {
                        val jsonObject = jsonArray.get(i) as JSONObject
                        val rank = BucketRank()

                        rank.bucket = Bucket()
                        rank.bucket!!.category = Category()
                        rank.bucket!!.category!!.categoryCode = jsonObject.getInt("categoryCode")
                        rank.bucket!!.content = jsonObject.getString("content")
                        rank.bucket!!.idx = jsonObject.getInt("idx")
                        rank.bestCnt = jsonObject.getInt("bestCnt")
                        rank.goodCnt = jsonObject.getInt("goodCnt")
                        rank.soSoCnt = jsonObject.getInt("ssoCnt")
                        rank.userComment = jsonObject.getInt("comment")
                        mBucketDataList!!.add(rank)
                    }
                    mHandler!!.sendEmptyMessage(SET_LIST)
                } catch (e: JSONException) {
                    KLog.log("@@ jsonException message : " + e.message)
                    mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
                }

            } else {
                mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
            }
        } else if (actionId == IHttpReceive.Companion.RANK_UPDATE_COMMENT) {
            KProgressDialog.setDataLoadingDialog(requireContext(), false, null, false)
            if (type == IHttpReceive.HTTP_OK && isValid == true) {
                mBucketDataList!!.clear()
                mHandler!!.sendEmptyMessage(LOAD_BUCKET_RANK)
            } else {
                mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
            }

        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            TOAST_MASSEGE -> Toast.makeText(requireContext(), msg.obj as String, Toast.LENGTH_LONG).show()
            SERVER_LOADING_FAIL -> {
                KLog.log( "@@ SERVER_LOADING_FAIL")
                val message = getString(R.string.server_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                onBackKey()
            }
            LOAD_BUCKET_RANK -> {
                KProgressDialog.setDataLoadingDialog(requireContext(), true, this.getString(R.string.loading_string), true)
                var userNickName: String = (SharedPreferenceUtils.read(requireContext(), PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?)!!
                var httpUrlTaskManager = HttpUrlTaskManager(NetworkConst.KBUCKET_RANK_LIST_URL, true, this, IHttpReceive.RANK_LIST)
                var map = HashMap<String, Any>()
                map["pageNm"] = "1"
                map["nickname"] = userNickName
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            SET_LIST -> {
                mListView = binding.rankListListview  as ListView
                mListAdapter = RankListAdpater(
                    requireContext(),
                    R.layout.rank_list_line,
                    mBucketDataList!!,
                    this
                )
                mListView!!.adapter = mListAdapter
            }
            SEND_BUCKET_RANK -> {
                KProgressDialog.setDataLoadingDialog(requireContext(), true, this.getString(R.string.loading_string), true)
                val userNickName = SharedPreferenceUtils.read(requireContext(), PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
                val httpUrlTaskManager = HttpUrlTaskManager(NetworkConst.KBUCKET_RANK_COMMENT, true, this, IHttpReceive.RANK_UPDATE_COMMENT)
                val map = HashMap<String, Any>()
                map.put("idx", mBucketRankIdx)
                map.put("comment", mBucketRankComment)
                map.put("nickname", userNickName!!)
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(map))
            }
            CHECK_NETWORK -> {
                val isConnect = NetworkUtils.isConnectivityStatus(requireContext())
                if (!isConnect) {
                    val connectMsg = getString(R.string.check_network)
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, connectMsg))
                } else {
                    mHandler!!.sendEmptyMessage(LOAD_BUCKET_RANK)
                }
            }
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
            //최고에요
            R.id.rank_btn1 -> mBucketRankComment = 3
            //좋아요
            R.id.rank_btn2 -> mBucketRankComment = 2
            //괜찮네요
            R.id.rank_btn3 -> mBucketRankComment = 1
        }

        val nIndex = v.tag as Int
        val isSendServer = getCommentCount(nIndex)
        // 의견이 없으면 서버에 반영
        if (!isSendServer) {
            mHandler!!.sendEmptyMessage(SEND_BUCKET_RANK)
            mBucketRankIdx = nIndex
        } else {
            mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, "이미 의견을 반영했습니다! "))
        }
    }

    private fun getCommentCount(index: Int): Boolean {
        if (mBucketDataList != null) {
            for (i in mBucketDataList!!.indices) {
                if (mBucketDataList!![i].bucket!!.idx == index) {
                    val comment = mBucketDataList!![i].userComment
                    if (comment != 0) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onBackKey() {
        KLog.log("@@ RankFragment onBackKey")
        (activity as MainFragmentActivity).setBackReceive(null)
        if(requireArguments().getString("BACK") == DataConst.VIEW_MAIN){
            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                // .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_main, MainFragment.newInstance())
                .commit()
        }
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  RankFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }
}