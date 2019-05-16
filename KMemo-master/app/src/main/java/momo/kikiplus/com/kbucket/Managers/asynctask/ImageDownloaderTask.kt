package momo.kikiplus.com.kbucket.Managers.asynctask

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import momo.kikiplus.com.kbucket.Utils.ContextUtils
import momo.kikiplus.com.kbucket.Utils.DataUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 인스타그램 공유 이미지 다운로드
 */
class ImageDownloaderTask(private val mContext: Context) : AsyncTask<String, Void, Bitmap>() {

    private val TAG = this.javaClass.simpleName
    private val mImageAddress: String? = null
    private var mFileName: String? = null
    var imageBitmap: Bitmap? = null
        private set

    override fun doInBackground(vararg params: String): Bitmap? {
        try {
            val url = ContextUtils.KBUCKET_DOWNLOAD_IAMGE + "?idx" + params[0]
            val `is` = java.net.URL(url).openStream()
            imageBitmap = BitmapFactory.decodeStream(`is`)
        } catch (e: IOException) {
            Log.e(TAG, "Cannot load image from " + mImageAddress!!)
        }

        return imageBitmap
    }

    override fun onPostExecute(bitmap: Bitmap) {
        try {
            mFileName = DataUtils.newFileName

            val file = File(mFileName)
            file.parentFile.mkdirs()
            val out = FileOutputStream(file)
            if (imageBitmap != null) {
                imageBitmap!!.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            out.close()
            Log.d(TAG, "Completed download image from file path : " + file.path)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
