package momo.kikiplus.deprecated

import android.os.AsyncTask
import momo.kikiplus.modify.http.IHttpReceive
import momo.kikiplus.refactoring.common.util.KLog
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : HttpFileDownloadManager
 * @Description : 파일 다운로드 매니저
 * @since 2015-07-02.
 */
class HttpUrlFileDownloadManager
/**
 * 생성자
 *
 * @param receive 응답 리시버 객체
 */
(url: String, receive: IHttpReceive,
 /**
  * action Id
  */
 private val mId: Int) : AsyncTask<Any, Void, Void>() {

    /**
     * 응답 리시버 객체
     */
    private var mHttpReceive: IHttpReceive? = null

    /**
     * 접속할 URL
     */
    private var mURl: String? = null

    init {
        mURl = url
        mHttpReceive = receive
    }

    override fun doInBackground(vararg params: Any): Void? {
        var count = 0
        try {
            val url = URL(mURl)
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.connect()

            val input = urlConnection.inputStream

            val saveFile = params[0] as String
            KLog.d(this.javaClass.simpleName, "@@ saveFile : $saveFile")
            val output = FileOutputStream(saveFile)

            val data = ByteArray(1024)
            var total: Long = 0
            if (input != null) {

                while ((input.read(data)) != -1) {
                    total += count.toLong()
                    KLog.d(this.javaClass.simpleName, "@@ data loading = " + total.toInt())
                    output.write(data, 0, count)
                }
                output.flush()
                output.close()
                input.close()
                mHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_OK, mId, "HttpResponse Download Ok")
            } else {
                mHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_FAIL, mId, "HttpResponse InputStream null")
            }
            urlConnection.disconnect()
        } catch (e: IOException) {
            e.printStackTrace()
            KLog.d(this.javaClass.simpleName, this.javaClass.toString() + "IOException ")
        }

        return null
    }

}
