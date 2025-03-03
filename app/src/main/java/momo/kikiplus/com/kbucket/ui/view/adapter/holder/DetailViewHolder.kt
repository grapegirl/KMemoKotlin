package momo.kikiplus.com.kbucket.ui.view.adapter.holder

import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import momo.kikiplus.com.kbucket.R

class DetailViewHolder(itemView: View, res : Int) : RecyclerView.ViewHolder(itemView){

    var mEditBox : EditText
    var mButton : Button
    var mRes : Int = -1

    init {
        if(mRes == R.layout.comment_layout){
            mEditBox = itemView.findViewById(R.id.comment_list_text)
            mButton = itemView.findViewById(R.id.comment_list_nickname)
        }else{
            mEditBox = itemView.findViewById(R.id.share_list_text)
            mButton = itemView.findViewById(R.id.share_list_detailBtn)
        }

    }
}