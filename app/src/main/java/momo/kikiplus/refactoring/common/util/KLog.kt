package momo.kikiplus.refactoring.common.util

import android.util.Log

/***
 * @author grapegirl
 * @version 1.1
 * @Class Name : KLog.java
 * @Description : Log 클래스
 * @since 2017.02.11
 */
class KLog {


    fun setLogging(isDebugging: Boolean) {
        VIEW_LOG = isDebugging
    }

    companion object {

        var VIEW_LOG = true
        val TAG : String = "KMemo"

        fun log(msg: String){
            if(!VIEW_LOG)
                return

            Log.d(TAG, buildLogMsg(msg))
        }

        fun d(msg: String) {
            if (!VIEW_LOG)
                return

            Log.d(TAG,
                buildLogMsg(msg)
            )
        }

        fun e(msg: String) {
            if (!VIEW_LOG)
                return

            Log.e(TAG,
                buildLogMsg(msg)
            )
        }

        fun w(msg: String) {
            if (!VIEW_LOG)
                return

            Log.w(TAG,
                buildLogMsg(msg)
            )
        }

        fun i(msg: String) {
            if (!VIEW_LOG)
                return

            Log.i(TAG,
                buildLogMsg(msg)
            )
        }

        fun v(msg: String) {
            if (!VIEW_LOG)
                return

            Log.v(TAG,
                buildLogMsg(msg)
            )
        }

        fun buildLogMsg(message: String): String {
            val ste = Thread.currentThread().stackTrace[4]
            val sb = StringBuilder()
            sb.append("[")
            sb.append(ste.fileName.replace(".java", ""))
            sb.append("::")
            sb.append(ste.methodName)
            sb.append("]")
            sb.append(message)
            return sb.toString()
        }
    }
}
