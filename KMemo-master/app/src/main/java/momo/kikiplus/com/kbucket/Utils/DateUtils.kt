package momo.kikiplus.com.kbucket.Utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/***
 * @version 1.0
 * @Class Name : DateUtils
 * @Description : 날짜 관련 클래스
 * @since 2015. 1. 9.
 */
object DateUtils {

    val KBUCKET_MEMO_DATE_PATTER = "yyyy-MM-dd HH:mm:ss"
    val KBUCKET_DB_DATE_PATTER = "yyyyMMddHHmmss"
    val DATE_YYMMDD_PATTER = "yyyy-MM-dd"
    val DATE_YYMMDD_PATTER2 = "yyyyMMdd"


    /**
     * 현재시간 출력 메소드
     *
     * @return 시분초ms 까지 출력
     */
    val currentTimeHHMMSSMS: String
        get() {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val misecond = calendar.get(Calendar.MILLISECOND)

            return hour.toString() + ":" + minute + ":" + second + ":" + misecond
        }

    /**
     * 현재시간 출력 메소드
     *
     * @return 시분초 까지 출력
     */
    val currentTimeHHMMSS: String
        get() {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)

            val strHH = String.format("%02d", hour)
            val strMM = String.format("%02d", minute)
            val strSS = String.format("%02d", second)

            return strHH + "" + strMM + "" + strSS + ""
        }

    /**
     * 날짜 출력 포맷에 맞게 반환 메소드
     *
     * @param pattern 출력 패턴
     * @param date    날짜
     * @return 날자 출력 포맷에 맞는 문자열 반환
     */
    fun getStringDateFormat(pattern: String, date: Date): String {
        val format = SimpleDateFormat(pattern)
        return format.format(date)
    }

    /**
     * 날짜 형식에 맞게 날짜 변환후 비교
     *
     * @param pattern 날짜 형식
     * @param date1
     * @param date2
     * @return 0 같음, 1 date1이 date2보다 최신일, -1은date1이 date2 보다 과거
     */
    fun getCompareDate(pattern: String, date1: String, date2: String): Int {
        val format = SimpleDateFormat(pattern)
        var compareDay1: Date? = null
        var compareDay2: Date? = null
        try {
            compareDay1 = format.parse(date1)
            compareDay2 = format.parse(date2)
        } catch (e: ParseException) {
            return -2
        }

        return compareDay1!!.compareTo(compareDay2)
    }

    /**
     * 패턴 형식으로 날짜 계산 후 형식에 맞는 날짜 반환하기
     *
     * @param pattern yyyyMMdd
     * @param day
     * @return
     */
    fun getDateFormat(pattern: String, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, day)
        val sdf = SimpleDateFormat(pattern)
        return sdf.format(calendar.time)
    }
}
