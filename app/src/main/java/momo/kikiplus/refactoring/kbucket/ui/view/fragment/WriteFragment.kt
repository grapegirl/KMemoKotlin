package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders.of
import androidx.recyclerview.widget.LinearLayoutManager
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.WriteFragmentBinding
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity
import momo.kikiplus.refactoring.kbucket.ui.view.adapter.RecyclerViewAdapter
import momo.kikiplus.refactoring.kbucket.ui.view.fragment.viewmodel.WriteViewModel
import java.util.*


class WriteFragment : Fragment(), View.OnClickListener, View.OnKeyListener, IBackReceive {

    companion object {
        fun newInstance() = WriteFragment()
    }

    private lateinit var viewModel: WriteViewModel
    private lateinit var mBinding : WriteFragmentBinding

    private var mAdapter: RecyclerViewAdapter<Any?> =
        RecyclerViewAdapter(this)
    private var mDataList: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.write_fragment, container, false)
        mBinding = WriteFragmentBinding.bind(view)
        setBackgroundColor()
        return view
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(/* savedInstanceState = */ savedInstanceState)
        viewModel = of(this)[WriteViewModel::class.java]

        mBinding.writeLayoutAddBtn.setOnClickListener(this)
        mBinding.sortDate.setOnClickListener(this)
        mBinding.sortDeadline.setOnClickListener(this)
        mBinding.sortMemo.setOnClickListener(this)
        mBinding.writeLayoutTitleView.setOnKeyListener(this)

        val layoutMgr = LinearLayoutManager(activity)
        mBinding.writeListListview.layoutManager = layoutMgr
        mBinding.writeListListview.adapter = mAdapter

        setListData()
    }


    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            mBinding.writeBackColor.setBackgroundColor(color)
        }
    }

    override fun onClick(v: View) {
        KLog.d( "@@ onClick id: $v.id")
        when (v.id) {
            R.id.write_layout_addBtn -> {
                val editText = mBinding.writeLayoutTitleView.text.toString()
                if (viewModel.checkduplicateData(editText)) {
                    val message = getString(R.string.check_input_bucket_string)
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.addData(editText, requireActivity())
                    setListData()
                }
                mBinding.writeLayoutTitleView.setText("")
            }
            // 삭제 버튼
            R.id.bucket_list_deleteBtn -> {
                val index = Integer.valueOf(v.tag as String)
                viewModel.removeData(mDataList[index], requireActivity())
                setListData()
            }
            // 수정 버튼
            R.id.bucket_list_modifyBtn -> {
                val index = Integer.valueOf(v.tag as String)

                val fragment = DetailFragment()
                val bundle = Bundle()
                bundle.putString("CONTENTS", mDataList[index])
                bundle.putString("BACK", DataConst.VIEW_WRITE)
                fragment.arguments =bundle

                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                    .addToBackStack("WriteFragment")
                    .add(R.id.fragment_main, fragment)
                    .commit()

                (activity as MainFragmentActivity).sendUserEvent("가지상세화면")
            }
            //메모 정렬순
            R.id.sort_memo -> {
                SharedPreferenceUtils.write(requireContext(), DataConst.KBUCKET_SORT_KEY, DataConst.SORT_MEMO)
                setListData()
            }
            //날짜 정렬순
            R.id.sort_date -> {
                SharedPreferenceUtils.write(requireContext(), DataConst.KBUCKET_SORT_KEY, DataConst.SORT_DATE)
                setListData()
            }
            //기한 정렬순
            R.id.sort_deadline -> {
                SharedPreferenceUtils.write(requireContext(), DataConst.KBUCKET_SORT_KEY, DataConst.SORT_DEADLINE)
                setListData()
            }
        }
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //엔터 입력시 할일 처리
                val editText = mBinding.writeLayoutTitleView.text.toString()
                if (viewModel.checkduplicateData(editText)) {
                    val message = getString(R.string.check_input_bucket_string)
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                } else {
                    viewModel.addData(editText, requireActivity())
                    setListData()
                }
                mBinding.writeLayoutTitleView.setText("")
                mBinding.writeLayoutTitleView.nextFocusDownId = R.id.write_layout_titleView
            }

        }
        return false
    }


    private fun setListData() {
        viewModel.initLocalData(requireActivity())
        mDataList.clear()
        mDataList = viewModel.getListDoing()
        KLog.d("@@ setListData mDataList size : ${mDataList.size}")
        val sortPrf = SharedPreferenceUtils.read(requireContext(), DataConst.KBUCKET_SORT_KEY, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        KLog.d("@@ sort sort: $sortPrf")
        if (sortPrf != null) {
            viewModel.sort(sortPrf)
        }
        mAdapter.updateItems(mDataList)
    }

    override fun onStart() {
        KLog.log("@@ WriteFragment onStart")
        super.onStart()
        setListData()
    }
    override fun onStop() {
        KLog.log("@@ WriteFragment onStop")
        super.onStop()
        mDataList.clear()
    }

    override fun onBackKey() {
        KLog.log("@@ WriteFragment onBackKey")
        KLog.d("@@ WriteFragment back : "+ requireArguments().getString("BACK"))
        (activity as MainFragmentActivity).setBackReceive(null)
        if(requireArguments().getString("BACK") == DataConst.VIEW_MAIN){
            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
           //     .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_main, MainFragment.newInstance())
                .commit()
        }
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  WriteFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }
}
