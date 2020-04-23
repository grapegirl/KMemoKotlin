package momo.kikiplus.refactoring.kbucket.action.net

import com.google.gson.annotations.SerializedName

class Version {

    @SerializedName("isValid")
    var bIsValid: Boolean = false

    @SerializedName("versionCode")
    var versionCode: Int = 0

    @SerializedName("versionName")
    var versionName: String? = null

    @SerializedName("forceYN")
    var forceYN: String? = null
}
