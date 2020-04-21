package momo.kikiplus.refactoring.model

import java.io.Serializable
import java.util.*

/**
 * 버킷 클래스
 */

class Bucket : Serializable {

    var category : Category? = null

    var content: String? = null

    var date: String? = null

    var idx: Int = 0

    var nickName : String? = null

    var imageUrl: String? = null

    var completeYN: String? = null

    var deadLine: String? = null

    fun toHasnMap(): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        if(category != null){
            map["CATEGORY_CODE"] = category!!.categoryCode
        }else{
            map["CATEGORY_CODE"] = 0
        }
        if(nickName != null){
            map["NICKNAME"] = nickName!!
        }else{
            map["NICKNAME"] = "-"
        }
        map["PHONE"] = "-"
        map["CONTENT"] = content!!
        if (imageUrl != null && imageUrl != "") {
            map["IMAGE_URL"] = "Y"
        } else {
            map["IMAGE_URL"] = "N"
        }
        map["CREATE_DT"] = date!!
        return map
    }


    /**
     * 생성자
     */

    constructor()
    constructor(contents: String) {
        date = null
        content = contents
        idx = 0
    }

    constructor(contents: String, strDate: String) {
        date = strDate
        content = contents
        idx = 0
    }

    /**
     * 생성자
     */
    constructor(title: String, contents: String, strDate: String, no: Int) {
        date = strDate
        content = contents
        idx = no
    }

    override fun toString(): String {
        return "contents =$content,date =  $date, complete_yn = $completeYN,image_path = $imageUrl,completeYN = $completeYN,deadLine = $deadLine"
    }
}
