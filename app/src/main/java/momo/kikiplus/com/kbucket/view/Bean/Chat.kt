package momo.kikiplus.com.kbucket.view.Bean


/**  채팅 클래스  */
/** 생성자  */
class Chat {

    /** 내용  */
    /** 내용 반환 메소드  */
    /** 내용 설정 메소드  */
    var content: String? = null
    /** 날짜  */
    /** 날짜 반환 메소드  */
    /** 날짜 설정 메소드  */
    var date: String? = null
    /** 방번호  */
    /** 번호 반환 메소드  */
    /** 번호 설정 메소드  */
    var idx: String? = null
    /** 닉네임  */
    /** 닉네임 반환 메소드  */
    /** 닉네임 설정 메소드  */
    var nickName = ""
    /** 사용자 전화번호  */
    /** 전화번호 반환 메소드  */
    /** 전화번호 설정 메소드  */
    var phone = ""
    /** 이미지 저장경로  */
    /** 이미지 경로 반환 메소드  */
    /** 이미지 경로 설정 메소드  */
    var imageUrl: String? = null
    /** 숨긴 여부  */
    /** 숨긴여부 반환 메소드  */
    /** 숨긴여부 설정 메소드  */
    var isHidden: String? = null
        get() = if (field == null)
            "-"
        else
            field
    /** seq  */
    /** seq 반환 메소드  */
    /** seq 설정 메소드  */
    var seq: Int = 0


}
