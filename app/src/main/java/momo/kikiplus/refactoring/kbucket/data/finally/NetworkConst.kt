package momo.kikiplus.refactoring.kbucket.data.finally

object NetworkConst {

    /**************************************************************************
     *
     * Application I/O 정의
     *
     */

    /**
     * 서버 IP
     */
    val KBUCKET_SERVER_IP = "https://kikiplus.cafe24.com"

    /**
     * 가지 공유하기 URL
     */
    val KBUCKET_INSERT_BUCKET_URL = "$KBUCKET_SERVER_IP/mobile/insertBucket.jsp"
    /**
     * 가지 파일 업로드 URL
     */
    val KBUCKET_UPLOAD_IMAGE_URL = "$KBUCKET_SERVER_IP/mobile/uploadFile.jsp"

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