package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.common.util.KLog
import momo.kikiplus.refactoring.common.util.SharedPreferenceUtils
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity

class NameFragment : Fragment() , IBackReceive  {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
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
        KLog.log("@@ NameFragment onBackKey")
        (activity as MainFragmentActivity).setBackReceive(null)
        NavHostFragment
            .findNavController(this)
            .navigate(R.id.action_RankFragment_to_MainFragement)
    }

    override fun onAttach(context: Context) {
        KLog.log("@@  NameFragment onAttach")
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }
}