package momo.kikiplus.refactoring.kbucket.data.vo

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * 버킷 클래스
 */

class Bucket
/**
 * 생성자
 */() : Parcelable {

    var category : Category = Category()

    var content: String = ""

    var date: String = ""

    var idx: Int = 0

    var nickName : String = ""

    var imageUrl: String = ""

    var completeYN: String = ""

    var deadLine: String = ""


    fun toHasnMap(): HashMap<String, Any> {
        val map: HashMap<String, Any> = HashMap<String, Any>()
        map["CATEGORY_CODE"] = category.categoryCode
        map["NICKNAME"] = nickName
        map["PHONE"] = "-"
        map["CONTENT"] = content
        if (imageUrl != "") {
            map["IMAGE_URL"] = "Y"
        } else {
            map["IMAGE_URL"] = "N"
        }
        map["CREATE_DT"] = date
        return map
    }


    constructor(contents: String) : this() {
        date = ""
        content = contents
        idx = 0
    }

    constructor(contents: String, strDate: String) : this() {
        date = strDate
        content = contents
        idx = 0
    }

    /**
     * 생성자
     */
    constructor(contents: String, strDate: String, no: Int) : this() {
        date = strDate
        content = contents
        idx = no
    }

    constructor(contents: String, strDate: String, no: Int,
    nickname : String, image : String, complete: String, deadline : String) : this() {
        date = strDate
        content = contents
        idx = no
        nickName = nickname
        imageUrl = image
        completeYN = complete
        deadLine = deadline
    }

    override fun toString(): String {
        return "contents =$content,date =  $date, complete_yn = $completeYN,image_path = $imageUrl,completeYN = $completeYN,deadLine = $deadLine"
    }

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(content)
        parcel.writeString(date)
        parcel.writeInt(idx)
        parcel.writeString(nickName)
        parcel.writeString(imageUrl)
        parcel.writeString(completeYN)
        parcel.writeString(deadLine)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Bucket> {
        override fun createFromParcel(parcel: Parcel): Bucket {
            return Bucket(parcel)
        }

        override fun newArray(size: Int): Array<Bucket?> {
            return arrayOfNulls(size)
        }
    }
}
