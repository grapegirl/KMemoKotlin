package momo.kikiplus.refactoring.kbucket.data.vo

import java.io.Serializable
import java.util.*

/**
 * 버킷 클래스
 */

class Bucket : Serializable {

    var category : Category = Category()

    var content: String = ""

    var date: String = ""

    var idx: Int = 0

    var nickName : String = ""

    var imageUrl: String = ""

    var completeYN: String = ""

    var deadLine: String = ""

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

    constructor(){}

    constructor(contents: String) {
        date = ""
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
