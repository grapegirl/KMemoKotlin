package momo.kikiplus.refactoring.vo

/** 카테고리  클래스  */
class Category {

    /** 카테고리 코드  */
    /** 카테고리 반환 메소드  */
    /** 카테고리 설정 메소드  */
    var categoryCode: Int = 0
    /** 카테고리명  */
    /** 카테고리명 반환 메소드  */
    /** 카테고리명 설정 메소드  */
    var categoryName: String? = null

    /** 생성자  */
    constructor()

    constructor(name: String, code: Int) {
        categoryName = name
        categoryCode = code
    }
}
