/**
 *
 */
package momo.kikiplus.refactoring.common.util

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Vibrator

import java.io.File

/**
 * @version 1.0
 * @Class Name : 사운드 및 진동 관련 유틸
 * @Description :
 * @since 2016. 1. 23.
 */
object MediaUtils {

    /****
     * @param context 컨텍스트
     * @param time    초
     * @Description : 진동 울리기
     */
    fun vibrate(context: Context, time: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(time)
    }

    /**
     * PDF 파일 열기
     *
     * @param context    컨텍스트
     * @param strPdfFile pdf 파일 경로
     */
    fun showPdfFile(context: Context, strPdfFile: String) {
        try {
            val apkUri = Uri.fromFile(File(strPdfFile))
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.setDataAndType(apkUri, "application/pdf")
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 오디오 자동 재생 메소드
     *
     * @param context 컨텍스트
     * @param nAudio  오티오
     */
    fun onAudioPlay(context: Context, nAudio: Int) {
        try {
            val rm = RingtoneManager(context)
            rm.setType(RingtoneManager.TYPE_NOTIFICATION)
            rm.cursor

            val notification = rm.getRingtoneUri(nAudio - 1)

            val m_pMediaPlayer = MediaPlayer.create(context, notification)
            m_pMediaPlayer.isLooping = false
            m_pMediaPlayer.setOnSeekCompleteListener { mp -> mp.stop() }
            m_pMediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}
