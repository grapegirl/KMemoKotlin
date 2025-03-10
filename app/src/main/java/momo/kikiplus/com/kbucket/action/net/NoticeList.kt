package momo.kikiplus.com.kbucket.action.net

import com.google.gson.annotations.SerializedName
import java.util.*

class NoticeList {

    @SerializedName("isValid")
    var bIsValid: Boolean = false

    @SerializedName("updateVOList")
    var noticeList: List<Notice> = ArrayList()

    inner class Notice {

        @SerializedName("updateContent")
        var mContents: String? = null

        @SerializedName("versionCode")
        var mVersion: String? = null
    }
}
