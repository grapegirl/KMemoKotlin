package momo.kikiplus.com.kbucket.data.vo

/**
 * 사용자 클래스
 */
/**
 * 생성자
 */
class MobileUser {

    var os = "ANDROID"

    var phone = ""

    var userNickName: String? = null

    var versionName: String? = null

    var market = "GOOGLE"

    var lanuage: String? = null

    var country: String? = null

    var lastDt: String? = null

    var createDt: String? = null

    var token = "N"

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("Os=")
        sb.append(os)
        sb.append(",")
        sb.append("UserNickName=")
        sb.append(userNickName)
        sb.append(",")
        sb.append("VersionName=")
        sb.append(versionName)
        sb.append(",")
        sb.append("Market=")
        sb.append(market)
        sb.append(",")
        sb.append("Language=")
        sb.append(lanuage)
        sb.append(",")
        sb.append("Country=")
        sb.append(country)
        sb.append(",")
        sb.append("LastDt=")
        sb.append(lastDt)
        sb.append(",")
        sb.append("CreateDt=")
        sb.append(createDt)
        sb.append(",")
        sb.append("GcmToken=")
        sb.append(token)
        return sb.toString()
    }

}
