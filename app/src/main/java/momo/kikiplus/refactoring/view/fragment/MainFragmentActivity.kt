package momo.kikiplus.refactoring.view.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.util.KLog
import momo.kikiplus.refactoring.view.fragment.ui.main.MainFragment

class MainFragmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KLog.log("@@ MainFragmentActivity onCreate")
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        setContentView(R.layout.main_fragment_activity)
        supportActionBar!!.hide()
    }

}
