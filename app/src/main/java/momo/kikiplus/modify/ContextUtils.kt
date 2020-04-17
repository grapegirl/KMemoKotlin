package momo.kikiplus.modify

/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : ContextUtils
 * @Description : 앱 관리 정보
 * @since 2015-08-21.
 */
object ContextUtils {

    /**
     * App Name Tag
     */
    val TAG = "KMemo"

    /**************************************************************************
     *
     * Shared Preference
     *
     */

    /**
     * 프리퍼런스 이름
     */
    val KEY_PREFER_NAME = "APP_PRF_NAME"
    /**
     * 프리퍼런스 이름(환경설정_비밀번호)
     */
    val KEY_CONF_PASSWORD = "KEY_CONF_PASSWORD"

    /**
     * 프리퍼런스 이름(환경설정_닉네임)
     */
    val KEY_USER_NICKNAME = "KEY_USER_NICKNAME"

    /**
     * 프리퍼런스 이름(FCM 키)
     */
    val KEY_USER_FCM = "KEY_USER_FCM"

    /**
     * 프리퍼런스 이름(MEMO)
     */
    val KEY_USER_MEMO = "KEY_USER_MEMO"

    /**
     * 프리퍼런스 이름 (MEMO_WIDGET)
     */
    val KEY_USER_MEMO_WIDGET = "KEY_USER_MEMO_WIDGET"

    /**
     * 프리퍼런스 이름(MEMO 위젯 갱신용)
     */
    val KEY_USER_MEMO_PREV = "KEY_USER_MEMO_PREV"

    /**************************************************************************
     *
     * Activity 별 상수 정의
     *
     */

    /**
     * 가지 작성 화면
     */
    val VIEW_WRITE = "WriteActivity"
    /**
     * 완료된 가지 목록 화면
     */
    val VIEW_COMPLETE_LIST = "BucketListActivity"
    /**
     * 버킷 공유(Life)
     */
    val DEFULAT_SHARE_BUCKET_IDX = "1"
    /**
     * 공유 idx
     */
    val NUM_SHARE_BUCKET_IDX = "SHARE_BUCKET_IDX"
    /**
     * 공유 bucket
     */
    val OBJ_SHARE_BUCKET = "OBJ_SHARE_BUCKET"

    //상수들
    val WIDGET_WRITE_BUCKET = "WRITE"
    val WIDGET_BUCKET_LIST = "LIST"
    val WIDGET_SHARE = "SHARE"
    val WIDGET_OURS_BUCKET = "OURS_BUCKET"
    val WIDGET_SEND_DATA = "WIDGET_SEND_DATA"

    val SORT_DATE = "SORT_DATE"
    val SORT_MEMO = "SORT_MEMO"
    val SORT_DEADLINE = "SORT_DEADLINE"
    val BACK_MEMO = "BACK_MEMO"


    /**************************************************************************
     *
     * Application 상수 정의
     *
     */

    /**
     * 패키지명
     */
    val PACKAGE_NAME = "momo.kikiplus.com.kbucket"

    /**
     * 파일 저장 장소(폴더명)
     */
    val KEY_FILE_FOLDER = "KMemo"

    /**
     * 데이터베이스 이름명
     */
    val KBUCKET_DB_NAME = "bucket.db"
    /**
     * 구글 Ads ID
     */
    val KBUCKET_AD_UNIT_ID = "ca-app-pub-9950741145331464/5719370666"


    /**
     * Sort
     */
    val KBUCKET_SORT_KEY = "KBUCKET_SORT"


    /**************************************************************************
     *
     * Application I/O 정의
     *
     */

    /**
     * 서버 IP
     */
    val KBUCKET_SERVER_IP = "http://kikiplus.cafe24.com"
    /**
     * 서버 port
     */
    val KBUCKET_PORT = "9000"

    /**
     * 가지 공유하기 URL
     */
    val KBUCKET_INSERT_BUCKET_URL = "$KBUCKET_SERVER_IP/mobile/insertBucket.jsp"
    /**
     * 가지 파일 업로드 URL
     */
    val KBUCKET_UPLOAD_IMAGE_URL = "$KBUCKET_SERVER_IP/mobile/uploadFile.jsp"
    /**
     * category URL
     */
    val KBUCKET_CATEGORY_URL = "$KBUCKET_SERVER_IP/mobile/category.jsp"
    /**
     * 버킷 리스트 URL
     */
    val KBUCKET_BUCKET_LIST_URL = "$KBUCKET_SERVER_IP/mobile/bucketList.jsp"
    /**
     * 버킷 상세 URL
     */
    val KBUCKET_BUCKET_DETAIL_URL = "$KBUCKET_SERVER_IP/mobile/bucketInfo.jsp"
    /**
     * 버킷 댓글 URL
     */
    val KBUCKET_COMMENT_URL = "$KBUCKET_SERVER_IP/mobile/comment.jsp"
    /**
     * 버킷 댓글 업로드 URL
     */
    val INSERT_COMMENT_URL = "$KBUCKET_SERVER_IP/mobile/insertComment.jsp"

    /**
     * 사용자 정보 업데이트 URL
     */
    val KBUCKET_UPDATE_USER = "$KBUCKET_SERVER_IP/mobile/insertMobileUser.jsp"
    /**
     * 이미지 다운로드 URL
     */
    val KBUCKET_DOWNLOAD_IAMGE = "$KBUCKET_SERVER_IP/mobile/downloadFile.jsp"
    /**
     * 이미지 다운로드 URL
     */
    val KBUCKET_AI = "$KBUCKET_SERVER_IP/mobile/getAIReplay.jsp"

    /**
     * 버킷랭킹 리스트 URL
     */
    val KBUCKET_RANK_LIST_URL = "$KBUCKET_SERVER_IP/mobile/bucketRankList.jsp"

    /**
     * 버킷랭킹 정보 업데이트
     */
    val KBUCKET_RANK_COMMENT = "$KBUCKET_SERVER_IP/mobile/bucketRankComment.jsp"

    /**
     * 가지 파일 업로드 URL
     */
    val KBUCKET_UPLOAD_DB_URL = "$KBUCKET_SERVER_IP/mobile/uploadDBFile.jsp"

    /**
     * 채팅 내용 전송 URL
     */
    val INSERT_CHAT = "$KBUCKET_SERVER_IP/chat/chat.jsp"
    /**
     * 채팅 내용 조회 URL
     */
    val SELECT_CHAT = "$KBUCKET_SERVER_IP/chat/loadChat.jsp"

    /**
     * Anayltics 키값
     */
    val KEY_ANALYTICS = "UA-86096322-1"
}