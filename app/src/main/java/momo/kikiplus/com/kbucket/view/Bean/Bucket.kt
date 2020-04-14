package momo.kikiplus.com.kbucket.view.Bean

import java.io.Serializable
import java.util.*

/**
 * 버킷 클래스
 */
/**
 * 생성자
 */
class Bucket : Serializable {

    /**
     * 카테고리코드
     */
    /**
     * 카테고리코드 반환 메소드
     */
    /**
     * 카테고리코드 설정 메소드
     */
    var categoryCode: Int = 0
    /**
     * 메모내용
     */
    /**
     * 내용 반환 메소드
     */
    /**
     * 내용 설정 메소드
     */
    var content: String? = null
    /**
     * 날짜
     */
    /**
     * 날짜 반환 메소드
     */
    /**
     * 등록일 설정 메소드
     */
    var date: String? = null
    /**
     * 번호
     */
    /**
     * 번호 반환 메소드
     */
    /**
     * 번호 설정 메소드
     */
    var idx: Int = 0
    /**
     * 닉네임
     */
    /**
     * 닉네임 반환 메소드
     */
    /**
     * 닉네임 설정 메소드
     */
    var nickName = "-"
    /**
     * 사용자 전화번호
     */
    /**
     * 전화번호 반환 메소드
     */
    /**
     * 전화번호 설정 메소드
     */
    var phone = "-"
    /**
     * 이미지 저장경로
     */
    /**
     * 이미지 경로 반환 메소드
     */
    /**
     * 이미지 경로 설정 메소드
     */
    var imageUrl: String? = null


    fun toHasnMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map["CATEGORY_CODE"] = categoryCode
        map["NICKNAME"] = nickName
        map["PHONE"] = phone
        map["CONTENT"] = content!!
        if (imageUrl != null && imageUrl != "") {
            map["IMAGE_URL"] = "Y"
        } else {
            map["IMAGE_URL"] = "N"
        }
        map["CREATE_DT"] = date!!
        return map
    }
}
