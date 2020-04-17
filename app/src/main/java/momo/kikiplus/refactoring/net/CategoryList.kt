package momo.kikiplus.refactoring.net

import com.google.gson.annotations.SerializedName
import java.util.*

class CategoryList {

    @SerializedName("isValid")
    var bIsValid: Boolean = false

    @SerializedName("categoryVOList")
    var categoryList: List<Category> = ArrayList()

    inner class Category {

        @SerializedName("categoryCode")
        var mCategoryCode: Int = 0

        @SerializedName("categoryName")
        var mCategoryName: String? = null

    }
}
