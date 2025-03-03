package momo.kikiplus.com.common.util

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
            val c = Canvas(bmp)
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
    fun setFileResize(context: Context, photoPath: String, width: Int, height: Int, filter: Boolean) {
        KLog.log("@@ setFileResize start")
        val file = File(context.filesDir, photoPath)
        val bitmap = BitmapFactory.decodeFile(file.path)
        val newBitmap = Bitmap.createScaledBitmap(bitmap, width, height, filter)
        saveBitmapToFile(context, newBitmap, photoPath)
        KLog.log("@@ setFileResize End")
    }

    /**
     * 비트맵 파일 저장하기
     *
     * @param bitmap      저장할 비트맵 이미지
     * @param strFilePath 저장할 파일 경로
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, strFilePath: String) {
        KLog.log("@@ saveBitmapToFile start")
        val file = File(context.filesDir, strFilePath)
        if(!file.exists()){
            file.createNewFile()
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        val byteArray = stream.toByteArray()
        try {
            context.openFileOutput(strFilePath, Context.MODE_PRIVATE).use {
                it.write(byteArray)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        KLog.log("@@ saveBitmapToFile end")
    }

    /***
     * 파일로부터 이미지 가져오기
     *
     * @param photoPath
     * @return
     */
    fun getFileBitmap(context: Context,photoPath: String): Bitmap {
        KLog.log("@@ getFileBitmap start")
        val file = File(photoPath)
        KLog.log("@@ getFileBitmap End")
        return BitmapFactory.decodeFile(file.absolutePath)

    }

    /**
     * 파일에서 바이트 배열로 변환하는 메소드
     *
     * @param path 경로
     * @return 바이트 배열
     */
    fun getByteArrayFromFile(context: Context, name: String): ByteArray {
        KLog.log("@@ getByteArrayFromFile start")

        val file = File(context.filesDir, name)
        val size = file.length()
        val bytes = ByteArray(size.toInt())
        val stream = ByteArrayInputStream(bytes)
        try {
            context.openFileInput(name).use {
                it.read(bytes, 0, size.toInt())
                it.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bytes
        KLog.log("@@ getByteArrayFromFile End")
    }
}
