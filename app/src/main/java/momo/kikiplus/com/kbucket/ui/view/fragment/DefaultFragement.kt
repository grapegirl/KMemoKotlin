package momo.kikiplus.com.kbucket.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.common.util.KLog
import momo.kikiplus.com.common.util.SharedPreferenceUtils
import momo.kikiplus.com.kbucket.data.finally.PreferConst
import momo.kikiplus.com.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.com.kbucket.ui.view.activity.MainFragmentActivity

class DefaultFragement : Fragment(), IBackReceive {

    companion object {
        fun newInstance() = DefaultFragement()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.notice_extended, container, false)
//        binding = NoticeExtendedBinding.bind(view)
        setBackgroundColor()
        return view
    }

    private fun setBackgroundColor() {
        KLog.d("@@ setBackgroundColor")
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            //binding.shareBackColor.setBackgroundColor(color)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onBackKey() {
        KLog.log("@@ DefaultFragement onBackKey")
        (activity as MainFragmentActivity).setBackReceive(null)
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_RankFragment_to_MainFragement)
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  DefaultFragement onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }
}