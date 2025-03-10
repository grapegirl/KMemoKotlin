/**
 *
 */
package momo.kikiplus.com.common.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @version 1.0
 * @Class Name : 스트링 유틸
 * @Description :
 * @since 2015. 1. 8.
 */
object StringUtils {


    private const val STRING_TIME_PATTERN = "yyyy-MM-dd"
    private const val STRING_DATETIME_PATTERN = "HH"
    private const val STRING_TIME_YYMMDD = "yy.MM.dd"
    private const val STRING_TIME_YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss"

    /****
     * @Description : HTML 태그 변환된거 다시 변환하는 메소드
     * @Return 변환된 스트링 값
     */
    fun convertString(str: String): String {
        var strData = str
        if (strData.contains("&amp;")) {
            strData = strData.replace("&amp;", "&")
        }
        if (strData.contains("&apos;")) {
            strData = strData.replace("&apos;", "'")
        }
        if (strData.contains("&quot;")) {
            strData = strData.replace("&quot;", "\"")
        }
        if (strData.contains("\\")) {
            strData = strData.replace("\\", "\\")
        }
        if (strData.contains("&lt;")) {
            strData = strData.replace("&lt;", "<")
        }
        if (strData.contains("&gt;")) {
            strData = strData.replace("&gt;", ">")
        }
        return strData
    }

    /***
     * 버전정보 비교 메소드
     *
     * @param srcVersion 버전
     * @param newVersion 서버에서 내려오는 버전
     * @return 버전 비교값(0이면 같음, 1이면 서버에서 내려오는 버전이 크다, -1이면 서버에서 이전버전이 내려옴)
     */
    fun compareVersion(srcVersion: String, newVersion: String): Int {
        val arrSrc = getIntegrArrayFromStringArray(srcVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        val arrNew = getIntegrArrayFromStringArray(newVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

        if (arrSrc.size != arrNew.size) {
            return 1
        }

        var i = 0
        val n = arrSrc.size
        while (i < n) {
            if (arrNew[i] > arrSrc[i]) {
                return 1
            } else if (arrNew[i] < arrSrc[i]) {
                return -1
            }
            i++
        }
        return 0

    }

    /***
     * String 배열을 integer 배열로 변환 메소드
     *
     * @param arr 변환할 String 배열
     * @return 변환된 integer 배열
     * @throws NumberFormatException
     */
    @Throws(NumberFormatException::class)
    fun getIntegrArrayFromStringArray(arr: Array<String>): Array<Int> {
        val list = ArrayList<Int>()
        for (str in arr) {
            list.add(Integer.parseInt(str))
        }
        return list.toTypedArray()
    }

    /**
     * 현재시간 패턴으로 가져오기
     *
     * @param pettern 패턴
     * @param time    현지시간 long
     * @return 현재시간
     */
    fun getTime(pettern: String, time: String): String {
        val ltime = java.lang.Long.parseLong(time)
        val format = SimpleDateFormat(pettern, Locale.KOREA)
        return format.format(Date(ltime))
    }

    /**
     * 원하는 패턴으로 시간 변경하기
     *
     * @param time 시간 String(YYYY-MM-DD HH:mm:ss)형식
     * @return 현재시간
     */
    @Throws(Exception::class)
    fun convertTime(time: String): String {
        val sdt = SimpleDateFormat(STRING_TIME_YYYYMMDDHHMMSS, Locale.KOREA)
        try {
            val date = sdt.parse(time) as Date
            val format = SimpleDateFormat(STRING_TIME_YYMMDD, Locale.KOREA)
            return format.format(date)
        } catch (e: ParseException) {
            return time
        }

    }

    /**
     * 포스트 방식으로 데이타 전송시 인자 설정 메소드
     *
     * @param sendData
     * @return 포스트 방식 전송 데이타
     */
    fun getHTTPPostSendData(sendData: HashMap<String, Any>): String {
        val sb = StringBuilder()

        //키값과 값을 추가함.
        val key = sendData.keys
        val iterator = key.iterator()
        while (iterator.hasNext()) {
            val keyName = iterator.next()
            val value = sendData[keyName]

            if (iterator.hasNext())
                sb.append(keyName).append("=").append(value).append("&")
            else
                sb.append(keyName).append("=").append(value)
        }
        //System.out.println("@@ getHTTPPostSendData :  " + sb.toString());
        return sb.toString()
    }

    /**
     * 스트링 트림 처리.
     */
    fun checkString(str: String?, tmp: String): String {
        return if (!(str == null || str.trim { it <= ' ' } == "" || str.trim { it <= ' ' } == "null"))
            str.trim { it <= ' ' }
        else
            tmp
    }
}
