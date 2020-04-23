package momo.kikiplus.refactoring.view.fragment.ui.viewmodel

import androidx.lifecycle.ViewModel

class TutorialViewModel : ViewModel() {
    /**
     * 화면 전환을 위한 터치 X값
     */
     var mPreTouchPosX = 0

    /**
     * 현재 페이지
     */
     var mCurrentPage = 0

    /**
     * 최대 페이지
     */
     var mMacPage = 0
}
