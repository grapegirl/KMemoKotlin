package momo.kikiplus.refactoring.utils

import android.content.Context
import android.os.Debug
import android.util.Log

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name :MemoryUtils
 * @Description : 메모리 관련 유틸
 * @since 2015-06-30.
 */
object MemoryUtils {

    /***
     * 힙 메모리 출력 메소드
     * @param context 컨텍스트
     */
    fun printMemory(context: Context) {
        Log.d(context.javaClass.simpleName, "@@ ==============================================")
        Log.d(context.javaClass.simpleName, "@@ 힙사이즈 : " + Debug.getNativeHeapSize())
        Log.d(context.javaClass.simpleName, "@@ 힙 Free 사이즈: " + Debug.getNativeHeapFreeSize())
        Log.d(context.javaClass.simpleName, "@@ 힙에 할당된 사이즈 : " + Debug.getNativeHeapAllocatedSize())
        Log.d(context.javaClass.simpleName, "@@ ==============================================")
    }
}
