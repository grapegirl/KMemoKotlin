package momo.kikiplus.com.kbucket.view.popup

/***
 * @Class Name : PopupListener
 * @Description : 팝업 관련 리스너
 * @since 2015. 5. 18.
 * @version 1.0
 * @author grape girl
 */
interface OnPopupEventListener {

    /**
     * 팝업 관련 동작 메소드
     * @param  popId 팝업 구분값
     * @param what 버튼 상태
     * @param obj 전달할 값
     */
    fun onPopupAction(popId: Int, what: Int, obj: Any?)

    companion object {

        /** 팝업 확인 버튼 선택  */
        const val POPUP_BTN_OK = 0
        /** 팝업 취소 버튼 선택  */
        const val POPUP_BTN_CLOSEE = 1
        /** 팝업 백키  */
        const val POPUP_BACK = 2
        /** 팝업 해제  */
        const val POPUP_DISPOSE = 3

        /** 기본 팝업  */
        val POPUP_BASIC = 1000
        /** 공유하기 팝업  */
        val POPUP_BUCKET_SHARE = 1001
        /** 카테고리 선택 팝업  */
        val POPUP_BUCKET_CATEGORY = 1002
        /** AI 팝업  */
        val POPUP_AI = 1003
        /** 강제 업데이트 팝업  */
        val POPUP_UPDATE_FORCE = 1004
        /** 선택 업데이트 팝업  */
        val POPUP_UPDATE_SELECT = 1005
        /** 삭제 팝업  */
        val POPUP_BUCKET_DELETE = 1006
        /** 관심 버킷 추가 팝업  */
        val POPUP_BUCKET_ADD = 1007
    }
}
