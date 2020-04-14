package momo.kikiplus.com.kbucket.Managers.asynctask

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask

import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author grape gril
 * @version 1.0
 * @Class Name : ImageLoadTask
 * @Description : 이미지 다운로드 Task
 * @since 2016-02-25
 */
class ImageLoadTask : AsyncTask<String, Void, Bitmap>() {

    private var m_imageListener: imageReceiveListener? = null

    fun setImageReceiveListener(imageListener: imageReceiveListener) {
        m_imageListener = imageListener
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String): Bitmap? {
        var imgBitmap: Bitmap? = null
        var conn: HttpURLConnection? = null
        var bis: BufferedInputStream? = null

        try {
            val url = URL(params[0])
            conn = url.openConnection() as HttpURLConnection
            conn.connect()
            val nSize = conn.contentLength
            bis = BufferedInputStream(conn.inputStream, nSize)
            imgBitmap = BitmapFactory.decodeStream(bis)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (bis != null) {
                try {
                    bis.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            conn?.disconnect()
        }

        return imgBitmap
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        super.onPostExecute(bitmap)

        if (bitmap != null) {
            m_imageListener!!.onImageReceiveCompleted(bitmap)
        } else {
            m_imageListener!!.onImageReceiveCompleted(null)
        }

    }

    /**
     * 이미지 리시브 리스너
     */
    interface imageReceiveListener {
        fun onImageReceiveCompleted(bitmap: Bitmap?)
    }
}
