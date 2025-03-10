package momo.kikiplus.com.common.util

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import momo.kikiplus.com.kbucket.data.finally.DataConst
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/***
 * @version 1.0
 * @Class Name : DataUtils
 * @Description : 데이터 관련 클래스
 * @since 2015. 12. 14.
 */
object DataUtils {

    /**
     * 현재시간으로 파일 이름 생성 후 반환 메소드
     *
     * @return 현재시간으로 생성된 파일이름 반환
     */
    val newFileName: String
        get() {
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
            val dateTime = sdf.format(calendar.time)

            return dateTime + ".jpg"
        }

    /**
     * 프로젝트 관련 파일 생성
     *
     * @return 파일 생성 완료 여부
     */
    fun createFolder(context: Context): Boolean {
        val path = File(context.filesDir, DataConst.KEY_FILE_FOLDER)

        if (path.exists()) {
            return true
        }
        if (!path.exists()) {
            path.mkdirs()
            return true
        }
        return false
    }

    fun createFile(context: Context, fileName: String): Boolean {
        val path = File(context.filesDir, fileName)
        if (path.exists()) {
            return true
        }
        if (!path.exists()) {
            path.mkdirs()
            return true
        }
        return false
    }

    /***
     * @param selectedImagePath
     * @param newFile
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copyFile(selectedImagePath: String, newFile: String, context: Context) {
        val inn = FileInputStream(selectedImagePath)

        val saveFile = File(context.filesDir, newFile)
        saveFile.createNewFile()

        val fos = FileOutputStream(saveFile)

        val buf = ByteArray(1024)
        var len: Int = 0

        do{
            len = inn.read(buf)
            if(len <= 0){
                break
            }
            fos.write(buf)
        }while (true)
        inn.close()
    }

    /**
     * 미디어 스캔하여 경로 반환 메소드
     *
     * @param context
     * @param uri
     * @return
     */
    fun getMediaScanPath(context: Context, uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    /**
     * 파일명으로 파일 삭제하기
     *
     * @param fileName
     * @return 파일 삭제 여부
     */
    fun deleteFile(fileName: String): Boolean {
        val file = File(fileName)
        var isResult = false
        if (file.exists()) {
            isResult = file.delete()
        }
        return isResult
    }

    /**
     * SQlite DB 파일 복원  기능
     *
     * @return 복원 여부(성공 true, 실패 false 반환)
     */
    fun importDB(context: Context , contentResolver : ContentResolver, uri : Uri): Boolean {
        val data = Environment.getDataDirectory()
        val currentDBPath = ("/data/" + DataConst.PACKAGE_NAME
                + "/databases/" + DataConst.KBUCKET_DB_NAME)
        val newFile = File(data, currentDBPath)
        val inputstream = contentResolver.openInputStream(uri)
        val outstream = FileOutputStream(newFile)
        KLog.log("@@ importDB newFile: " + newFile.path)
        try{
            if(inputstream != null){
                var buf = ByteArray(1024)
                var length : Int = 0

                while(true){
                    length = inputstream.read(buf)
                    if(length <= 0) break
                    outstream.write(buf, 0 , length)
                }
            }
            outstream.close()

        }catch (e : IOException){
            KLog.d( "@@ import DB 실패" + e.toString())
            return false
        }

        return true
    }


    /**
     * SQlite DB 파일 백업 기능
     *
     * @return 백업 여부(성공 true, 실패 false 반환)
     */
    fun exportDB(context: Context, NewdbName: String): Boolean {
        KLog.log("@@ exportDB start")
        try {
            val currentDBPath = ("/data/data/" +  context.packageName
                    + "/databases/" + DataConst.KBUCKET_DB_NAME)
            KLog.log("@@ exportDB currentDBPath : " + currentDBPath)

            val currentDB = File(currentDBPath)
            KLog.log("@@ exportDB currentDB : " + currentDB)

            val kmemoFile = File(context.filesDir, DataConst.KEY_FILE_FOLDER)
            if (!kmemoFile.exists()) {
                kmemoFile.mkdirs()
                KLog.log("@@ kmemoFile folder create")
            }

            val backupDBPath = DataConst.KEY_FILE_FOLDER + "/" + NewdbName + ".db"
            KLog.log("@@ exportDB backupDBPath : " + backupDBPath)
            val backupDB = File(context.filesDir, backupDBPath)
            if (!backupDB.exists()) {
                backupDB.createNewFile()
                KLog.log("@@ backupDB file  create")
            }

            val src = FileInputStream(currentDB).channel
            val dst = FileOutputStream(backupDB).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()
            KLog.d( "@@ DB 파일 백업 완료 ")

        } catch (e: Exception) {
            KLog.e("@@ DB 파일 백업 에러 : " + e.toString())
            return false
        }
        KLog.log("@@ exportDB End")
        return true
    }

    /**
     * 내부에 저장된 이미지 바로 보기
     *
     * @param context  컨텍스트
     * @param filePath 이미지 저장 경로(외부저장소)
     */
    fun startImageIntent(context: Context, filePath: String) {
        val file = File(filePath)
        val galleryIntent = Intent(Intent.ACTION_VIEW)
        galleryIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        galleryIntent.setDataAndType(Uri.fromFile(file), "image/*")
        context.startActivity(galleryIntent)
    }

    /**
     * 폰트 반환하기
     *
     * @param context 컨텍스트
     * @return 폰트
     */
    fun getFont(context: Context, fontName: String): Typeface {
        return Typeface.createFromAsset(context.assets, fontName)
    }

    /**
     * 파일 존재여부 반환하기
     *
     * @param path 파일 경로
     * @return 존재여부
     */
    fun isFileExists(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }
}
