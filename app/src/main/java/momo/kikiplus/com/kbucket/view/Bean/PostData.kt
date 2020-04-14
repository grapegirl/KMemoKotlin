package momo.kikiplus.com.kbucket.view.Bean


/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : PostData
 * @Description : 게시글 클래스
 * @since 2015. 6. 23.
 */
class PostData {

    /**
     * 타이틀
     */
    /**
     * @return 타이틀
     * @Method : 타이틀 반환 메소드
     */
    /**
     * @param title 타이틀
     * @Description : 타이틀 설정 메소드
     */
    var title: String? = null

    /**
     * 등록 날짜
     */
    /**
     * @return 등록 날짜
     * @Method : 등록 날짜 반환 메소드
     */
    /**
     * @Description : 등록날짜 설정 메소드
     * @Return date 등록날짜
     */
    var date: String? = null

    /**
     * 내용
     */
    /***
     * @Description : 내용 반환 메소드
     * @Return 내용
     */
    /**
     * @Description : 내용 설정 메소드
     * @Return contents 내용
     */
    var contents: String? = null

    /**
     * 순번
     */
    private var mNo = 0

    /**
     * 이미지 이름
     */
    /***
     * @Description : 이미지 경로 반환 메소드
     * @Return 이미지 경로
     */
    /**
     * @Description : 이미지 경로 설정 메소드
     * @Return ImageName 이미지 경로
     */
    var imageName: String? = null

    /**
     * 완료여부
     */
    /***
     * @Description : 완료여부 반환 메소드
     * @Return 완료여부
     */
    /**
     * @Description : 완료여부 설정 메소드
     * @Return complete 완료여부
     */
    var completeYN: String? = null

    /**
     * 기한
     */
    /***
     * @Description : 기한 반환 메소드
     * @Return 기한
     */
    /**
     * @param date 기한
     * @Description : 기한 설정 메소드
     */
    var deadLine: String? = null

    /**
     * 생성자
     */
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
