package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.NoticeExtendedBinding
import momo.kikiplus.deprecated.adapter.BaseExpandableAdapter
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.common.view.KProgressDialog
import momo.kikiplus.refactoring.kbucket.action.net.NetRetrofit
import momo.kikiplus.refactoring.kbucket.action.net.NoticeList
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class NoticeFragment : Fragment(), Handler.Callback , IBackReceive {

    companion object {
        fun newInstance() = NoticeFragment()
    }

    private var mHandler: Handler? = null
    private var mList: ArrayList<NoticeList.Notice>? = null
    private var mExtendableListView: ExpandableListView? = null

    private val TOAST_MASSEGE = 10
    private val LOAD_NOTICE_LIST = 20
    private val SET_NOTICE_LIST = 30
    private val SERVER_LOADING_FAIL = 40

    private var mGroupList: ArrayList<String>? = null
    private var mChildList: ArrayList<ArrayList<String>>? = null
    private var mChildListContent: ArrayList<String>? = null

    private lateinit var binding : NoticeExtendedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        KLog.d("@@ onCreateView")

        val view = inflater.inflate(R.layout.notice_extended, container, false)
        binding = NoticeExtendedBinding.bind(view)
        setBackgroundColor()
        return view
    }

    private fun setBackgroundColor() {
        KLog.d("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            binding.noticeBackColor.setBackgroundColor(color)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mHandler = Handler(this)
        mList = ArrayList()
        mGroupList = ArrayList()
        mChildList = ArrayList()
        KProgressDialog.setDataLoadingDialog(context, true, this.getString(R.string.loading_string), true)

        mHandler!!.sendEmptyMessage(LOAD_NOTICE_LIST)
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            LOAD_NOTICE_LIST -> {
                KLog.d("@@ LOAD_NOTICE_LIST")
                val res = NetRetrofit.instance.service.updateList
                res.enqueue(object : Callback<NoticeList> {
                    override fun onResponse(call: Call<NoticeList>, response: Response<NoticeList>) {
                        if (response.body()!!.bIsValid) {
                            val size = response.body()!!.noticeList.size
                            mList!!.clear()
                            mGroupList!!.clear()
                            mChildList!!.clear()
                            mList!!.addAll(response.body()!!.noticeList)

                            for (i in 0 until size) {
                                val mTitle = mList!![i].mContents!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                                mGroupList!!.add(mTitle)
                                mChildListContent = ArrayList()
                                mChildListContent!!.add(mList!![i].mContents!!)
                                mChildList!!.add(mChildListContent!!)
                            }
                            mHandler!!.sendEmptyMessage(SET_NOTICE_LIST)
                        } else {
                            mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
                        }
                    }

                    override fun onFailure(call: Call<NoticeList>, t: Throwable) {
                        KLog.log("@@ NoticeList onFailure call : " + call.request())
                        KLog.log("@@ NoticeList onFailure message : " + t.message)
                        mHandler!!.sendEmptyMessage(SERVER_LOADING_FAIL)
                    }
                })
            }
            SET_NOTICE_LIST -> {
                KProgressDialog.setDataLoadingDialog(context, false, null, false)
                mExtendableListView = binding.noticeListviewExtended as ExpandableListView?
                //KLog.d("@@ LOAD_NOTICE_LIST group : " + mGroupList + ", clild : " + mChildList)
                mExtendableListView!!.setAdapter(
                    BaseExpandableAdapter(
                        requireContext(),
                        mGroupList!!,
                        mChildList!!
                    )
                )
            }
            TOAST_MASSEGE -> Toast.makeText(context, msg.obj as String, Toast.LENGTH_LONG).show()
            SERVER_LOADING_FAIL -> {
                KProgressDialog.setDataLoadingDialog(context, false, null, false)
                KLog.log("@@ SERVER_LOADING_FAIL")
                val message = getString(R.string.server_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                //finish()
            }
        }
        return false
    }

    override fun onBackKey() {
        KLog.log("@@ NoticeFragment onBackKey")
        (activity as MainFragmentActivity).setBackReceive(null)
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_NoticeFragment_to_MainFragement)
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  NoticeFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }
}