package momo.kikiplus.modify.http

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : IHttpReceive
 * @Description : HTTP 통신 결과
 * @since 2015-06-30.
 */
interface IHttpReceive {

    /***
     * 리비스 콜백 메소드
     */
    fun onHttpReceive(type: Int, actionId: Int, obj: Any?)

    companion object {
        /**
         * 성공
         */
        val HTTP_OK = 0

        /**
         * 실패
         */
        val HTTP_FAIL = -1

        /** 버킷 공유하기   */
        val INSERT_BUCKET = 2000
        /** 버킷 공유하기 (image)  */
        val INSERT_IMAGE = 2001
        /**  카데고리 정보  */
        val CATEGORY_LIST = 2003
        /**  버킷 정보  */
        val BUCKET_LIST = 2004
        /**  댓글 정보  */
        val COMMENT_LIST = 2005
        /**  댓글 추가  */
        val INSERT_COMMENT = 2006
        /**  사용자 정보 업데이트 */
        val UPDATE_USER = 2008
        /**  이미지 다운로드 */
        val DOWNLOAD_IMAGE = 2009
        /**  AI */
        val REQUEST_AI = 2010
        /**  랭킹 리스트 */
        val RANK_LIST = 2011
        /**  랭킹 의견 업데이트 */
        val RANK_UPDATE_COMMENT = 2012
        /**  DB 업로드  */
        val UPLOAD_DB = 2013
    }
}
