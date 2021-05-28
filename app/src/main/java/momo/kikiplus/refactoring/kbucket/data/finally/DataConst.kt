package momo.kikiplus.refactoring.kbucket.data.finally

object DataConst {

    /*
    * Activity 별 상수 정의
    *
    */

    /**
     * 가지 작성 화면
     */
    val VIEW_WRITE = "WriteFragment"

    /**
     * 메인 화면
     */
    val VIEW_MAIN = "MainFragment"


    /**
     * 공 화면
     */
    val VIEW_SHARE = "ShareFragment"

    /**
     * 완료된 가지 목록 화면
     */
    val VIEW_DONE = "DoneFragment"
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
    val WIDGET_WRITE_BUCKET : String = "WRITE"
    val WIDGET_BUCKET_LIST : String = "LIST"
    val WIDGET_SHARE : String = "SHARE"
    val WIDGET_OURS_BUCKET : String = "OURS_BUCKET"
    val WIDGET_SEND_DATA : String = "WIDGET_SEND_DATA"
    val WIDGET_PASS : String = "PASS"
    val START_OPEN_DRAWER : String = "START_OPEN_DRAWER"

    val SORT_DATE : String = "SORT_DATE"
    val SORT_MEMO : String = "SORT_MEMO"
    val SORT_DEADLINE : String = "SORT_DEADLINE"



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
    val KBUCKET_AD_UNIT_ID = "ca-app-pub-9950741145331464/3074095529"


    /**
     * Sort
     */
    val KBUCKET_SORT_KEY = "KBUCKET_SORT"

    val SHORTCUT_MAIN : String = "android.intent.action.VIEW.WRITE"
    val SHORTCUT_LIST : String = "android.intent.action.VIEW.LIST"
    val SHORTCUT_SHARE : String = "android.intent.action.VIEW.SHARE"
    val SHORTCUT_RANK : String = "android.intent.action.VIEW.RANK"

}