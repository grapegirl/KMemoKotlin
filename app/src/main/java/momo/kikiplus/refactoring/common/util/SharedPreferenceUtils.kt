/**
 *
 */
package momo.kikiplus.refactoring.common.util

import android.content.Context

/**
 * @Class Name : SharedPreferenceUtils.java
 * @Description :
 *
 * @since 2014.08.01
 * @version 1.0
 */
object SharedPreferenceUtils {

    /**프레퍼런스 integer 타입 */
    val SHARED_PREF_VALUE_INTEGER = 0

    /**프레퍼런스 float 타입 */
    val SHARED_PREF_VALUE_FLOAT = 1

    /**프레퍼런스 long 타입 */
    val SHARED_PREF_VALUE_LONG = 2

    /**프레퍼런스 string 타입 */
    val SHARED_PREF_VALUE_STRING = 3

    /**프레퍼런스 boolean 타입 */
    val SHARED_PREF_VALUE_BOOLEAN = 4

    /**
     * 프리퍼런스 이름
     */
    val KEY_PREFER_NAME = "APP_PREFER_NAME"


    /***
     * 프레퍼런스 쓰기 메소드
     *
     * @param context 컨텍스트
     * @param key     저장할 키값
     * @param value   저장할 값
     */
    fun write(context :  Context, key: String, value: Any?) {
        val sharedPref = context.getSharedPreferences(KEY_PREFER_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        if (value is Int) {
            editor.putInt(key, (value as Int?)!!)
        } else if (value is Float) {
            editor.putFloat(key, (value as Float?)!!)
        } else if (value is Long) {
            editor.putLong(key, (value as Long?)!!)
        } else if (value is String) {
            editor.putString(key, value as String?)
        } else if (value == null) {
            editor.putString(key, null)
        } else {
            editor.putBoolean(key, (value as Boolean?)!!)
        }
        editor.commit()
    }

    /**
     * 프레퍼런스 읽기 메소드
     *
     * @param context 컨텍스트
     * @param key     읽어올 키값
     * @param type    읽어올 값의 타입
     * @return 프레퍼런스 저장한 값
     */
    fun read(context :  Context, key: String, type: Int): Any? {
        val sharedPref = context.getSharedPreferences(KEY_PREFER_NAME, Context.MODE_PRIVATE)
        when (type) {
            SHARED_PREF_VALUE_INTEGER -> return sharedPref.getInt(key, -1)
            SHARED_PREF_VALUE_FLOAT -> return sharedPref.getFloat(key, -1f)
            SHARED_PREF_VALUE_LONG -> return sharedPref.getLong(key, -1)
            SHARED_PREF_VALUE_STRING -> return sharedPref.getString(key, null)
            SHARED_PREF_VALUE_BOOLEAN -> return sharedPref.getBoolean(key, false)
        }
        return null
    }
}
