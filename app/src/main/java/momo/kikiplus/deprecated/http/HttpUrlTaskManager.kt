/**
 *
 */
package momo.kikiplus.deprecated.http

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL


/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : HTTPManager.java
 * @Description : HTTP 통신 매니저 클래스
 * @since 2014.08.01
 */
class HttpUrlTaskManager
/**
 * 생성자
 */
(url: String, post: Boolean, receive: IHttpReceive,
 /**
  * action Id
  */
 private val mId: Int) : AsyncTask<String, Void, Void>() {

    /**
     * 접속할 URL
     */
    private var mURl: String? = null

    /**
     * post방식 true, get방식-false
     */
    private var isPost = false

    /**
     * HTTP 리시브 콜백 메소드 객체
     */
    private var mIHttpReceive: IHttpReceive? = null

    init {
        mURl = url
        isPost = post
        mIHttpReceive = receive
    }


    override fun doInBackground(vararg params: String): Void? {
        var data = ""
        try {
            val url = URL(mURl)
            val urlConnection = url.openConnection()
            val httpURLConnection = urlConnection as HttpURLConnection
            httpURLConnection.connectTimeout = 5000
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            if (isPost) {
                try {
                    httpURLConnection.requestMethod = "POST"
                } catch (e: ProtocolException) {
                    e.printStackTrace()
                }

                httpURLConnection.doOutput = true
            } else {
                httpURLConnection.requestMethod = "GET"
            }
            httpURLConnection.doInput = true
            httpURLConnection.useCaches = false
            httpURLConnection.defaultUseCaches = false

            if (isPost) {//Post 방식으로 데이타 전달시
                val outputStream = httpURLConnection.outputStream
                if (params.size > 0) {
                    System.out.println("@@ sendData : " + params[0])
                    outputStream.write(params[0].toByteArray(charset("UTF-8")))
                    outputStream.flush()
                    outputStream.close()
                }
            }
            if (httpURLConnection.responseCode == HttpURLConnection.HTTP_OK) {
                var buffer: String?
                val bufferedReader: BufferedReader
                if (isPost) {
                    bufferedReader = BufferedReader(InputStreamReader(httpURLConnection.inputStream, "UTF-8"))
                } else {
                    bufferedReader = BufferedReader(InputStreamReader(httpURLConnection.inputStream, "UTF-8"))
                }

                do{
                    buffer = bufferedReader.readLine()
                    if(buffer == null){
                        break
                    }
                    data += buffer
                }while(true)


                bufferedReader.close()
                httpURLConnection.disconnect()
                mIHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_OK, mId, data)
            } else {
                mIHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_FAIL, mId, httpURLConnection.responseMessage)
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            Log.d(this.javaClass.simpleName, " @@ MalformedURLException")
            mIHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_FAIL, mId, null)

        } catch (e: ProtocolException) {
            e.printStackTrace()
            Log.d(this.javaClass.simpleName, " @@ ProtocolException")
            mIHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_FAIL, mId, null)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            Log.d(this.javaClass.simpleName, " @@ UnsupportedEncodingException")
            mIHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_FAIL, mId, null)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(this.javaClass.simpleName, " @@ IOException")
            mIHttpReceive!!.onHttpReceive(IHttpReceive.HTTP_FAIL, mId, null)
        }

        return null
    }

}
