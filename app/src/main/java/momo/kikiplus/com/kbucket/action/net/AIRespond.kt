package momo.kikiplus.com.kbucket.action.net

import com.google.gson.annotations.SerializedName

class AIRespond {

    @SerializedName("isValid")
    var bIsValid: Boolean = false

    @SerializedName("replay")
    var replay : String = ""
}