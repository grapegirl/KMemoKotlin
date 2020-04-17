package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.refactoring.util.MediaUtils

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : PushPopupActivity
 * @Description : 푸쉬 팝업 액티비티
 * @since 2016-01-23
 */
class PushPopupActivity : Activity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        MediaUtils.vibrate(this, 2000)

        setUIScale()

        val intent = intent
        val msg = intent.getStringExtra("message")

        (findViewById<View>(R.id.basic_body_textView) as TextView).text = msg

        findViewById<View>(R.id.popup_close_button).setOnClickListener(this)
        findViewById<View>(R.id.popup_ok_button).setOnClickListener(this)
    }

    private fun setUIScale() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.push_activity, null)

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        addContentView(view, LinearLayout.LayoutParams(metrics.widthPixels, metrics.heightPixels))
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.popup_ok_button, R.id.popup_close_button -> finish()
        }
    }
}
