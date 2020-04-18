package momo.kikiplus.refactoring.util

import android.util.Log
import momo.kikiplus.modify.ContextUtils

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

        private var VIEW_LOG = true

        fun log(msg: String){
            if(!VIEW_LOG)
                return

            Log.d(ContextUtils.TAG, buildLogMsg(msg))
        }

        fun d(tag: String, msg: String) {
            if (!VIEW_LOG)
                return

            Log.d(tag,
                buildLogMsg(msg)
            )
        }

        fun e(tag: String, msg: String) {
            if (!VIEW_LOG)
                return

            Log.e(tag,
                buildLogMsg(msg)
            )
        }

        fun w(tag: String, msg: String) {
            if (!VIEW_LOG)
                return

            Log.w(tag,
                buildLogMsg(msg)
            )
        }

        fun i(tag: String, msg: String) {
            if (!VIEW_LOG)
                return

            Log.i(tag,
                buildLogMsg(msg)
            )
        }

        fun v(tag: String, msg: String) {
            if (!VIEW_LOG)
                return

            Log.v(tag,
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
