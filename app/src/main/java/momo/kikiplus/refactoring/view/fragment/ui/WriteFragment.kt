package momo.kikiplus.refactoring.view.fragment.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.view.fragment.ui.viewmodel.WriteViewModel

class WriteFragment : Fragment() {

    companion object {
        fun newInstance() =
            WriteFragment()
    }

    private lateinit var viewModel: WriteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.write_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WriteViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
