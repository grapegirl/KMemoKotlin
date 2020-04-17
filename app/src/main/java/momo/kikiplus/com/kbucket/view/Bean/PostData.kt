package momo.kikiplus.com.kbucket.view.Bean


/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : PostData
 * @Description : 게시글 클래스
 * @since 2015. 6. 23.
 */
class PostData {


    var title: String? = null


    var date: String? = null


    var contents: String? = null


    private var mNo = 0


    var imageName: String? = null


    var completeYN: String? = null


    var deadLine: String? = null


    constructor() {
        contents = ""
    }

    /**
     * 생성자
     */
    constructor(contents: String) {
        title = null
        date = null
        this.contents = contents
        mNo = 0
    }

    constructor(contents: String, date: String) {
        title = null
        this.date = date
        this.contents = contents
        mNo = 0
    }

    /**
     * 생성자
     */
    constructor(title: String, contents: String, date: String, no: Int) {
        this.title = title
        this.date = date
        this.contents = contents
        mNo = no
    }


    /***
     * @Description : 순번 반환 메소드
     * @Return 순번
     */
    fun getmNo(): Int {
        return mNo
    }

    /**
     * @Description : 순번 설정 메소드
     * @Return mNo 순번
     */
    fun setmNo(mNo: Int) {
        this.mNo = mNo
    }

    override fun toString(): String {
        return "contents =$contents,date =  $date, complete_yn = $completeYN,image_path = $imageName"
    }
}
