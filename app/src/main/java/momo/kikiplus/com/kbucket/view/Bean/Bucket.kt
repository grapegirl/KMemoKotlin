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


    var categoryCode: Int = 0

    var content: String? = null

    var date: String? = null

    var idx: Int = 0

    var nickName = "-"

    var phone = "-"

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
