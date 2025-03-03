package momo.kikiplus.com.kbucket.ui.view.adapter.holder

import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import momo.kikiplus.com.kbucket.R

class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    var mEditBox : EditText
    var mModButton : Button
    var mDelButton : Button

    init {
        mEditBox = itemView.findViewById(R.id.bucket_list_text)
        mModButton = itemView.findViewById(R.id.bucket_list_modifyBtn)
        mDelButton = itemView.findViewById(R.id.bucket_list_deleteBtn)
    }
}