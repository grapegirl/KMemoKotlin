package momo.kikiplus.com.kbucket.view.Bean

/**
 * 사용자 클래스
 */
/**
 * 생성자
 */
class MobileUser {

    /**
     * 모바일 OS
     */
    /**
     * 모바일 OS 반환 메소드
     */
    /**
     * 모바일 OS 설정 메소드
     */
    var os = "ANDROID"
    /**
     * 사용자 전화번호
     */
    /**
     * 사용자 전화번호 반환 메소드
     */
    /**
     * 사용자 전화번호 설정 메소드
     */
    var phone = ""
    /**
     * 사용자 닉네임
     */
    /**
     * 사용자 닉네임 반환 메소드
     */
    /**
     * 사용자 닉네임 설정 메소드
     */
    var userNickName: String? = null
    /**
     * 사용자 버전명
     */
    /**
     * 사용자 버전명 반환 메소드
     */
    /**
     * 사용자 버전명 설정 메소드
     */
    var versionName: String? = null
    /**
     * 사용자 마켓구분
     */
    /**
     * 사용자 마켓 반환 메소드
     */
    /**
     * 사용자 마켓 설정 메소드
     */
    var market = "GOOGLE"
    /**
     * 사용자 언어
     */
    /**
     * 사용자 언어 반환 메소드
     */
    /**
     * 사용자 언어 설정 메소드
     */
    var lanuage: String? = null
    /**
     * 사용자 나라
     */
    /**
     * 사용자 나라 반환 메소드
     */
    /**
     * 사용자 나라 설정 메소드
     */
    var country: String? = null
    /**
     * 사용자 최근 방문 날짜
     */
    /**
     * 사용자 최근 방문 날짜 반환 메소드
     */
    /**
     * 사용자 최근 방문 날짜 설정 메소드
     */
    var lastDt: String? = null
    /**
     * 사용자 생성 날짜
     */
    /**
     * 사용자 최초 방문 날짜 반환 메소드
     */
    /**
     * 사용자 최초 방문 날짜 설정 메소드
     */
    var createDt: String? = null
    /**
     * 사용자 토큰키
     */
    /**
     * 사용자 토큰 반환 메소드
     */
    /**
     * 사용자 토큰 설정 메소드
     */
    var gcmToken = "N"

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
        sb.append(gcmToken)
        return sb.toString()
    }

}
