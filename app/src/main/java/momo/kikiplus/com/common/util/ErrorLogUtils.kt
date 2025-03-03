package momo.kikiplus.com.common.util

import android.os.Environment
import android.util.Log
import java.io.*
import java.util.*
import kotlin.system.exitProcess

/***
 * @author grapegirl
 * @version 1.0
 * @Class Name : ErrorLogUtils.java
 * @Description : Error Log 파일 생성 클래스
 * @since 2016.05.02
 */
class ErrorLogUtils {
    private var mLogFile: File? = null

    /**
     * 사용법
     * Thread.setDefaultUncaughtExceptionHandler(new ErrorLogUtils.UncaughtExceptionHandlerApplication());
     */
    class UncaughtExceptionHandlerApplication : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, ex: Throwable) {

            // 예외상황이 발행 되는 경우 작업
            val error = getStackTrace(ex)
            Log.e("Error", error)
            saveFileEror(error)

            // 현재 프로세스 종료
            exitProcess(android.os.Process.myPid())

        }
    }


    /**
     * Logcat capture 기능 추가
     */
    fun startFileLogging() {
        val isDebuggable = true
        if (isDebuggable) {
            try {
                createLogFile()
                Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-f", mLogFile!!.path))
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun createLogFile() {
        if (mLogFile == null) {
            val folder = File(ERROR_FILE)
            if (folder.mkdir() || folder.isDirectory) {
                mLogFile = File(ERROR_FILE, "log_$currentTime.log")
                try {
                    when {
                        mLogFile!!.createNewFile() -> {
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    companion object {
        private val ERROR_FILE = (Environment.getExternalStorageDirectory().absolutePath + "/KMemo/"
                + "ErrorLog.txt")

        /**
         * 로그 파일 생성
         *
         * @param error
         */
        fun saveFileEror(error: String) {
            val file = File(ERROR_FILE)
            val fw: FileWriter
            try {
                // 파일이 존재하지 않으면
                if (!file.exists()) {
                    // 파일 생성
                    file.createNewFile()
                    fw = FileWriter(file.path)
                } else {
                    // 기존 파일에 추가하기
                    fw = FileWriter(file.path, true)
                }

                val bw = BufferedWriter(fw)
                bw.write("Error Invoke Date : $currentTime")
                bw.write("\n")
                bw.write(error)
                bw.write("\n")
                bw.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        /**
         * 로그 파일 생성 시점 반환
         *
         * @return 로그 파일 생성 시간
         */
        private val currentTime: String
            get() {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR)
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)
                val misecond = calendar.get(Calendar.MILLISECOND)

                return hour.toString() + ":" + minute + ":" + second + ":" + misecond
            }

        /**
         * 메시지로 변환
         *
         * @param th
         * @return
         */
        private fun getStackTrace(th: Throwable): String {

            val result = StringWriter()
            val printWriter = PrintWriter(result)

            var cause: Throwable? = th
            while (cause != null) {
                cause.printStackTrace(printWriter)
                cause = cause.cause
            }
            val stacktraceAsString = result.toString()
            printWriter.close()

            return stacktraceAsString
        }
    }

}
