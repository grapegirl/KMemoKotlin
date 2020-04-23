package momo.kikiplus.refactoring.common.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import java.io.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : ByteUtils
 * @Description : 바이트 변환 유틸 클래스
 * @since 2015-07-03.
 */
object ByteUtils {

    /**
     * 비트맵을 바이트 배열로 변환하는 메소드
     *
     * @param map 비트맵
     * @return 바이트 배열
     */
    fun getByteArrayFromBitmap(map: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        map.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

    fun getResBitmap(context: Context, bmpResId: Int): Bitmap {
        val opts = BitmapFactory.Options()
        opts.inDither = false

        val res = context.resources
        var bmp: Bitmap? = BitmapFactory.decodeResource(res, bmpResId, opts)

        if (bmp == null) {

            //val dd = context.getDrawable(bmpResId)
            val d = res.getDrawable(bmpResId)

            val w = d.intrinsicWidth
            val h = d.intrinsicHeight
            bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp!!)
            d.setBounds(0, 0, w - 1, h - 1)
            d.draw(c)
        }

        return bmp
    }

    /***
     * 파일로부터 이미지 리사이즈해서 다시 저장하기
     *
     * @param photoPath 파일경로
     */
    fun setFileResize(photoPath: String, width: Int, height: Int, filter: Boolean) {
        val bitmap = BitmapFactory.decodeFile(photoPath)
        val newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, filter)
        saveBitmapToFile(newBitmap, photoPath)
    }

    /**
     * 비트맵 파일 저장하기
     *
     * @param bitmap      저장할 비트맵 이미지
     * @param strFilePath 저장할 파일 경로
     */
    fun saveBitmapToFile(bitmap: Bitmap, strFilePath: String) {
        val fileCacheItem = File(strFilePath)
        var out: OutputStream? = null
        try {
            fileCacheItem.createNewFile()
            out = FileOutputStream(fileCacheItem)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    /***
     * 파일로부터 이미지 가져오기
     *
     * @param photoPath
     * @return
     */
    fun getFileBitmap(photoPath: String): Bitmap {
        return BitmapFactory.decodeFile(photoPath)
    }

    /**
     * 파일에서 바이트 배열로 변환하는 메소드
     *
     * @param path 경로
     * @return 바이트 배열
     */
    fun getByteArrayFromFile(path: String): ByteArray? {
        val file = File(path)
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        return bytes
    }
}
