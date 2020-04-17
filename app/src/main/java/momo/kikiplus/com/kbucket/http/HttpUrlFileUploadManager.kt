package momo.kikiplus.com.kbucket.http

import android.os.AsyncTask
import momo.kikiplus.modify.KLog
import java.io.ByteArrayInputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : HttpUrlFileUploadManager
 * @Description : 파일 업로드
 * @since 2015-07-01.
 */
class HttpUrlFileUploadManager
/**
 * 생성자
 */
(url: String, receive: IHttpReceive,
 /**
  * action Id
  */
 private val mId: Int, private val mByteArray: ByteArray) : AsyncTask<Any, Void, Void>() {

    /**
     * 접속할 URL
     */
    private var mURl: String? = null

    /**
     * HTTP 리시브 콜백 메소드 객체
     */
    private var mIHttpReceive: IHttpReceive? = null

    /**
     * 데이터 경계선 문자열
     */
    private val BOUNDARY_STRING = "== DATA END ==="

    init {
        mURl = url
        mIHttpReceive = receive
    }

    override fun doInBackground(vararg params: Any): Void? {
        //val filePath = params[0] as String
        val setValue = params[1] as String
        val reqValue = params[2] as String
        val fileName = params[3] as String

        val lineEnd = "\r\n"
        val twoHyphens = "--"
        val boundary = "*****"

        try {

            val byteArrayInputStream = ByteArrayInputStream(mByteArray)
            val connectUrl = URL(mURl)

            // open connection
            val conn = connectUrl.openConnection() as HttpURLConnection
            conn.doInput = true
            conn.doOutput = true
            conn.useCaches = false
            conn.requestMethod = "POST"
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
            conn.setRequestProperty(setValue, reqValue)
            conn.setRequestProperty("filename", fileName)

            // write data
            val dos = DataOutputStream(conn.outputStream)
            dos.writeBytes(twoHyphens + boundary + lineEnd)
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadFile\";filename=\"$fileName\"$lineEnd")
            dos.writeBytes(lineEnd)

            var bytesAvailable = byteArrayInputStream.available()
            val maxBufferSize = 1024
            var bufferSize = Math.min(bytesAvailable, maxBufferSize)

            val buffer = ByteArray(bufferSize)
            var bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize)

            // read image
            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize)
                bytesAvailable = byteArrayInputStream.available()
                bufferSize = Math.min(bytesAvailable, maxBufferSize)
                bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize)
            }

            dos.writeBytes(lineEnd)
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)

            // close streams
            byteArrayInputStream.close()
            dos.flush() // finish upload...

            // get response
            var ch: Int
            val `is` = conn.inputStream
            val b = StringBuffer()

            do{
                ch = `is`.read()
                if(ch != -1){
                    b.append(ch.toChar())
                }
            }while (ch == -1)

            val s = b.toString()
            KLog.e("Test", "result = $s")
            dos.close()
            mIHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_OK, mId, s)

        } catch (e: Exception) {
            KLog.d("Test", "exception " + e.message)
            mIHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_FAIL, mId, this.javaClass.toString() + " @@ Exception ")
        }

        return null
    }


    companion object {


        /**
         * Map 형식으로 Key와 Value를 셋팅한다.
         *
         * @param key   : 서버에서 사용할 변수명
         * @param value : 변수명에 해당하는 실제 값
         * @return
         */
        fun setParams(key: String, value: String): String {
            return "Content-Disposition: form-data; name=\"$key\"\r\n\r\n$value"
        }

        /**
         * 업로드할 파일에 대한 메타 데이터를 설정한다.
         *
         * @param key      : 서버에서 사용할 파일 변수명
         * @param fileName : 서버에서 저장될 파일명
         * @return
         */
        fun setFile(key: String, fileName: String): String {
            return ("Content-Disposition: form-data; name=\"" + key
                    + "\";filename=\"" + fileName + "\"\r\n")
        }
    }
}

