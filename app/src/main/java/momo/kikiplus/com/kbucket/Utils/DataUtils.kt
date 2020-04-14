package momo.kikiplus.com.kbucket.Utils

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
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
            val path = File(Environment.getExternalStorageDirectory().absolutePath + "/" + ContextUtils.KEY_FILE_FOLDER)
            if (!path.exists()) {
                path.mkdirs()
            }
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
            val dateTime = sdf.format(calendar.time)
            return path.path + "/" + dateTime + ".jpg"
        }

    /**
     * 프로젝트 관련 파일 생성
     *
     * @return 파일 생성 완료 여부
     */
    fun createFile(): Boolean {
        val path = File(Environment.getExternalStorageDirectory().absolutePath + "/" + ContextUtils.KEY_FILE_FOLDER)
        val noMediaFile = File(Environment.getExternalStorageDirectory().absolutePath + "/" + ContextUtils.KEY_FILE_FOLDER + "/.nomedia")
        if (!noMediaFile.exists()) {
            noMediaFile.mkdir()
        }

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
     * @param string
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copyFile(selectedImagePath: String, string: String) {
        val inn = FileInputStream(selectedImagePath)
        val out = FileOutputStream(string)

        // Transfer bytes from in to out
        val buf = ByteArray(1024)
        var len: Int

        do{
            len = inn.read(buf)

            if(len <= 0){
                break
            }
            out.write(buf, 0, len)
        }while (true)


        inn.close()
        out.close()
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
    fun importDB(backupDBPath: String): Boolean {
        var backupPath = backupDBPath
        try {
            val sd = Environment.getExternalStorageDirectory()
            val data = Environment.getDataDirectory()
            if (sd.canWrite()) {
                val currentDBPath = ("//data//" + ContextUtils.PACKAGE_NAME
                        + "//databases//" + ContextUtils.KBUCKET_DB_NAME)
                val backupDB = File(data, currentDBPath)

                if (backupDBPath.contains("/KMemo/")) {
                    val nStartIndex = backupDBPath.indexOf("/KMemo/")
                    backupPath = backupDBPath.substring(nStartIndex, backupDBPath.length)
                }
                val currentDB = File(sd, backupPath)
                val src = FileInputStream(currentDB).channel
                val dst = FileOutputStream(backupDB).channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()
                KLog.d("DataUtils", "@@ DB 파일 복원 완료 ")
            }
        } catch (e: Exception) {
            KLog.d("DataUtils", "@@ DB 파일 복원 에러 : " + e.toString())
            return false
        }

        return true
    }


    /**
     * SQlite DB 파일 백업 기능
     *
     * @return 백업 여부(성공 true, 실패 false 반환)
     */
    fun exportDB(NewdbName: String): Boolean {
        try {
            val sd = Environment.getExternalStorageDirectory()
            val data = Environment.getDataDirectory()

            if (sd.canWrite()) {
                val currentDBPath = ("//data//" + ContextUtils.PACKAGE_NAME
                        + "/databases/" + ContextUtils.KBUCKET_DB_NAME)

                val backupDBPath = ContextUtils.KEY_FILE_FOLDER + "/" + NewdbName + ".db"
                val currentDB = File(data, currentDBPath)

                val kmemoFile = File(sd, ContextUtils.KEY_FILE_FOLDER)
                if (!kmemoFile.exists()) {
                    kmemoFile.mkdirs()
                }
                val backupDB = File(sd, backupDBPath)
                if (!backupDB.exists()) {
                    backupDB.createNewFile()
                }
                val src = FileInputStream(currentDB).channel
                val dst = FileOutputStream(backupDB).channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()
                KLog.d("DataUtils", "@@ DB 파일 백업 완료 ")
            }
        } catch (e: Exception) {
            KLog.e("DataUtils", "@@ DB 파일 백업 에러 : " + e.toString())
            return false
        }

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
     * @return 한나폰트
     */
    fun getHannaFont(context: Context): Typeface {
        return Typeface.createFromAsset(context.assets, "hanna.ttf")
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
