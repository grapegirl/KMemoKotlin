package momo.kikiplus.modify.sqlite

import android.content.Context
import momo.kikiplus.refactoring.util.KLog
import java.util.*

class SQLQuery {
    private val TAG = this.javaClass.simpleName

    internal var mDBAdapter: SQLiteAdapter? = null

    private var sql = ""

    private val TABLE_MEMO = "KMEMO"
    private val TABLE_USER = "KUSER"
    private val TABLE_CHAT = "KCHAT"
    private val TABLE_IMG = "KIMG"

    fun SQLQuery() {
        KLog.d(TAG, "@@ create SQLQuery 생성자")
    }

    /**
     * 테이블생성
     */
    fun createTable(context: Context) {
        KLog.d(TAG, "@@ create Table 생성")
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "CREATE TABLE IF NOT EXISTS " + TABLE_MEMO + "(" +
                    "CONTENTS TEXT, " +
                    "DATE TEXT, " +
                    "COMPLETE_YN TEXT, " +
                    "COMPLETE_DATE TEXT," +
                    "COLUMN TEXT," +
                    "DEADLINE TEXT, " +
                    "IMAGE_PATH TEXT)"

            mDBAdapter!!.update(sql)

            sql = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "(" +
                    "NICKNAME TEXT, " +
                    "PHONE TEXT, " +
                    "AGE TEXT, " +
                    "GENDER TEXT, " +
                    "JOB TEXT, " +
                    "COUNTRY TEXT)"

            mDBAdapter!!.update(sql)
            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ createTable 테이블 생성 실패" + e.toString())
        }

    }

    fun selectKbucket(context: Context): LinkedList<LinkedHashMap<String, String>>? {
        var userInfoRow: LinkedList<LinkedHashMap<String, String>>? = null
        try {
            mDBAdapter = SQLiteAdapter(context)
            KLog.d(this.javaClass.simpleName, "@@ selectKbucket context: $context")

            mDBAdapter!!.open()
            KLog.d(this.javaClass.simpleName, "@@ selectKbucket mDBAdapter: $mDBAdapter")
            sql = "SELECT CONTENTS, DATE,COMPLETE_YN,COMPLETE_DATE,IMAGE_PATH,DEADLINE FROM $TABLE_MEMO"
            KLog.d(this.javaClass.simpleName, "@@ selectKbucket sql: $sql")
            userInfoRow = mDBAdapter!!.query(sql, null)
            KLog.d(this.javaClass.simpleName, "@@ selectKbucket 1")
            mDBAdapter!!.close()
            KLog.d(this.javaClass.simpleName, "@@ selectKbucket 2")
            mDBAdapter = null
            KLog.d(this.javaClass.simpleName, "@@ selectKbucket 3")
        } catch (e: Exception) {
            KLog.d(TAG, "@@ 메모 정보 가져오기 복수건 오류 : " + e.message)
        }

        return userInfoRow
    }

    fun selectKbucket(context: Context, memoContents: String): LinkedHashMap<String, String>? {
        var userInfoRow: LinkedHashMap<String, String>? = null
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "SELECT CONTENTS, DATE,COMPLETE_YN,COMPLETE_DATE,IMAGE_PATH,DEADLINE FROM $TABLE_MEMO WHERE CONTENTS = ? "

            val bindArgs = arrayOf(memoContents)

            userInfoRow = mDBAdapter!!.queryRow(sql, bindArgs)

            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 메모 정보 가져오기 단건 오류")
        }

        return userInfoRow
    }

    fun containsKbucket(context: Context, memoContents: String): Boolean {
        var userInfoRow: LinkedHashMap<String, String>? = null
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "SELECT CONTENTS, DATE,COMPLETE_YN,COMPLETE_DATE,IMAGE_PATH,DEADLINE FROM $TABLE_MEMO WHERE CONTENTS = ? "

            val bindArgs = arrayOf(memoContents)

            userInfoRow = mDBAdapter!!.queryRow(sql, bindArgs)

            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 메모 정보 가져오기 단건 오류")
        }

        return userInfoRow != null && userInfoRow.size > 0
    }

    /**
     * 사용자 정보 insert 메소드
     *
     * @param context       컨텍스트
     * @param contents      내용
     * @param date          날짜
     * @param completeYN    완료여부
     * @param completedDate 완료된 날짜
     */
    fun insertUserSetting(context: Context, contents: String, date: String, completeYN: String, completedDate: String): Boolean {
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "INSERT INTO  " + TABLE_MEMO +
                    "			(CONTENTS, DATE, COMPLETE_DATE, COMPLETE_YN) " +
                    "	 VALUES (?,?,?,?); "

            KLog.d(TAG, "@@ insertUserSetting sql : $sql")
            val bindArgs = arrayOf<String>(contents, date, completedDate, completeYN)

            mDBAdapter!!.update(sql, bindArgs)

            //입력후 결과 보기
            sql = "SELECT CONTENTS FROM $TABLE_MEMO;"
            val userInfoRowsetCheck = mDBAdapter!!.query(sql, null)
            KLog.d(TAG, "@@ result-> $userInfoRowsetCheck")

            mDBAdapter!!.close()

            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ insertUserInfo 유저정보 입력 실패" + e.toString())
            return false
        }

        return true
    }

    /**
     * 메모 내용 업데이트 (수정)
     *
     * @param context     컨텍스트
     * @param contents    내용
     * @param newContents 새로운 내용
     */
    fun updateMemoContent(context: Context, contents: String, newContents: String) {
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "UPDATE  " + TABLE_MEMO +
                    " SET CONTENTS = ? WHERE CONTENTS = ?"

            val bindArgs = arrayOf<String>(newContents, contents)

            mDBAdapter!!.update(sql, bindArgs)
            mDBAdapter!!.close()

            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 사용자 메모 정보 내용으로 업데이트 오류" + e.toString())
        }

    }

    /**
     * 메모 내용 업데이트 (수정)
     *
     * @param context     컨텍스트
     * @param contents    내용
     * @param newContents 새로운 내용
     * @param completeYn  완료여부(Y/N)
     * @param date        완료날짜
     */
    fun updateMemoContent(context: Context, contents: String, newContents: String, completeYn: String, date: String, imagePath: String) {
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "UPDATE  " + TABLE_MEMO +
                    " SET CONTENTS = ? , COMPLETE_YN = ?, DATE = ? , IMAGE_PATH = ? WHERE CONTENTS = ?"

            val bindArgs = arrayOf<String>(newContents, completeYn, date, imagePath, contents)

            mDBAdapter!!.update(sql, bindArgs)
            KLog.d(this.javaClass.simpleName, "@@사용자 메모 정보 내용으로 업데이트 sql : $sql")
            mDBAdapter!!.close()

            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 사용자 메모 정보 내용으로 업데이트 오류" + e.toString())
        }

    }

    /**
     * 메모 내용 업데이트 (수정)
     */
    fun updateMemoContent(context: Context, contents: String, newContents: String, completeYn: String, date: String, imagePath: String, deadline: String) {
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "UPDATE  " + TABLE_MEMO +
                    " SET CONTENTS = ? , COMPLETE_YN = ?, DATE = ? , IMAGE_PATH = ?, DEADLINE = ? WHERE CONTENTS = ?"

            val bindArgs = arrayOf<String>(newContents, completeYn, date, imagePath, deadline, contents)

            mDBAdapter!!.update(sql, bindArgs)
            KLog.d(this.javaClass.simpleName, "@@사용자 메모 정보 내용으로 업데이트 sql : $sql")
            mDBAdapter!!.close()

            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 사용자 메모 정보 내용으로 업데이트 오류" + e.toString())
        }

    }


    fun containsUserTable(context: Context): Boolean {
        var userInfoRow: LinkedHashMap<String, String>? = null
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "SELECT NICKNAME FROM $TABLE_USER; "

            userInfoRow = mDBAdapter!!.queryRow(sql, null)

            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 메모 정보 가져오기 단건 오류")
        }

        return userInfoRow != null && userInfoRow.size > 0
    }

    /**
     * 사용자 닉네임 추가 메소드
     *
     * @param context  컨텍스트
     * @param nickanme 업데이트 할 닉네임
     */
    fun insertUserNickName(context: Context, nickanme: String) {
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "INSERT INTO  " + TABLE_USER +
                    " (NICKNAME) VALUES (?)"

            val bindArgs = arrayOf<String>(nickanme)

            mDBAdapter!!.update(sql, bindArgs)
            mDBAdapter!!.close()

            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 사용자 닉네임 설정 정보 업데이트 오류" + e.toString())
        }

    }

    /**
     * 사용자 닉네임 업데이트 메소드
     *
     * @param context  컨텍스트
     * @param nickanme 업데이트 할 닉네임
     */
    fun updateUserNickName(context: Context, nickanme: String) {
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "UPDATE  " + TABLE_USER +
                    " SET NICKNAME = ?"

            val bindArgs = arrayOf<String>(nickanme)

            mDBAdapter!!.update(sql, bindArgs)
            mDBAdapter!!.close()

            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 사용자 닉네임 설정 정보 업데이트 오류" + e.toString())
        }

    }

    /**
     * 사용자 정보 테이블 내용 검색하기
     *
     * @param context 컨텍스트
     * @return 사용자 정보 반환
     */
    fun selectUserTable(context: Context): LinkedHashMap<String, String>? {
        var userInfoRow: LinkedHashMap<String, String>? = null
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "SELECT NICKNAME, PHONE, AGE, GENDER, JOB, COUNTRY FROM $TABLE_USER;"

            userInfoRow = mDBAdapter!!.queryRow(sql, null)

            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 메모 정보 가져오기 단건 오류" + e.toString())
        }

        return userInfoRow
    }

    /**
     * 사용자 정보 설정 삭제
     *
     * @param context 컨텍스트
     */
    fun deleteUserBucket(context: Context, contents: String) {
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "DELETE FROM " + TABLE_MEMO +
                    " WHERE CONTENTS = '" + contents + "';"

            mDBAdapter!!.queryRow(sql, null)
            mDBAdapter!!.close()

            mDBAdapter = null
            //KLog.d(TAG, "@@ 유저 설정 정보 삭제 sql : " + sql);
        } catch (e: Exception) {
            KLog.d(TAG, "@@ 유저 설정 정보 삭제 오류" + e.toString())
        }

    }

    fun createChatTable(context: Context) {
        KLog.d(TAG, "@@ create Chat Table 생성")
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = ("CREATE TABLE IF NOT EXISTS " + TABLE_CHAT + "(" + "CONTENTS TEXT, " + "DATE TEXT, "
                    + "NICKNAME TEXT, " + "IMAGE_PATH TEXT, SEQ TEXT, CHAT_IDX TEXT)")

            mDBAdapter!!.update(sql)
            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 테이블 생성 실패" + e.toString())
        }

    }

    /**
     * 채팅 정보 insert 메소드
     *
     * @param context  컨텍스트
     * @param contents 내용
     * @param date     날짜
     */
    fun insertChatting(context: Context, contents: String, date: String, nickname: String, imagePath: String, seq: String, chatId: String) {
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = ("INSERT INTO  " + TABLE_CHAT
                    + "(CONTENTS, DATE, NICKNAME, IMAGE_PATH, SEQ, CHAT_IDX) "
                    + "VALUES (?,?,?,?,?,?); ")

            val bindArgs = arrayOf<String>(contents, date, nickname, imagePath, seq, chatId)

            mDBAdapter!!.update(sql, bindArgs)

            // 입력후 결과 보기
            sql = "SELECT * FROM $TABLE_CHAT"
            val userInfoRowsetCheck = mDBAdapter!!.query(sql, null)
            KLog.d(TAG, "@@ userChatInfoRowsetCheck userInfoRowsetCheck : $userInfoRowsetCheck")
            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 채팅정보 입력 실패" + e.toString())
        }

    }

    /**
     * 채팅 테이블 내용 검색하기
     *
     * @param context 컨텍스트
     * @param chatIdx 채팅방 번호
     * @return 채팅 내용
     */
    fun selectChatTable(context: Context, chatIdx: String): LinkedList<LinkedHashMap<String, String>>? {
        var userInfoRow: LinkedList<LinkedHashMap<String, String>>? = null
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "SELECT NICKNAME, CONTENTS, DATE, IMAGE_PATH, SEQ FROM $TABLE_CHAT WHERE CHAT_IDX = ? ;"

            val bindArgs = arrayOf(chatIdx)

            userInfoRow = mDBAdapter!!.query(sql, bindArgs)

            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 채팅 정보 가져오기 단건 오류" + e.toString())
        }

        return userInfoRow
    }

    /**
     * 테이블 내용 삭제
     */
    fun DeleteBucketContents(context: Context) {
        KLog.d(TAG, "@@ delelte Bucket ")
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "DELETE FROM $TABLE_MEMO"
            mDBAdapter!!.update(sql)

            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ DeleteBucketContents Exception : " + e.toString())
        }

    }

    fun createImageTable(context: Context) {
        KLog.d(TAG, "@@ create Image Table 생성")
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = ("CREATE TABLE IF NOT EXISTS " + TABLE_IMG + "(" + "CONTENTS TEXT, DATE TEXT,"
                    + "IMAGE BLOB)")

            mDBAdapter!!.update(sql)
            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ Image 테이블 생성 실패" + e.toString())
        }

    }


    /**
     * 메모 이미지 업데이트 (수정)
     *
     * @param context     컨텍스트
     * @param contents    내용
     * @param date 날짜
     * @param  bitmaps 비트맵
     */
    fun updateMemoImage(context: Context, contents: String, date: String, bitmaps: ByteArray) {

        KLog.d(this.javaClass.simpleName, "@@ updateMemoImage 1 " )
        deleteMemoImage(context, contents, date)
        KLog.d(this.javaClass.simpleName, "@@ updateMemoImage 2 " )
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "INSERT INTO " + TABLE_IMG +
                    " (CONTENTS, DATE, IMAGE ) VALUES  ( ? , ? , ? )"

            val bindArgs = arrayOf<Any>(contents, date, bitmaps)

            mDBAdapter!!.update(sql, bindArgs)
            mDBAdapter!!.close()

            mDBAdapter = null
            KLog.d(this.javaClass.simpleName, "@@ updateMemoImage 3 " )
            KLog.d(TAG, "@@ 사용자 이미지 업데이트 완료 contents : $contents, date : $date")

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 사용자 메모 이미지 업데이트 오류" + e.toString())
        }
        KLog.d(this.javaClass.simpleName, "@@ updateMemoImage 4 " )

    }

    fun selectImage(context: Context, contents: String, date: String): ByteArray? {
        var bytes: ByteArray? = null
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "SELECT IMAGE FROM $TABLE_IMG WHERE CONTENTS = ? AND DATE = ? ;"

            val bindArgs = arrayOf(contents, date)

            bytes = mDBAdapter!!.queryImageRow(sql, bindArgs)

            mDBAdapter!!.close()
            mDBAdapter = null

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 채팅 정보 가져오기 단건 오류" + e.toString())
        }

        return bytes
    }

    /**
     * 메모 이미지 삭제
     *
     * @param context     컨텍스트
     * @param contents    내용
     * @param date 날짜
     */
    fun deleteMemoImage(context: Context, contents: String, date: String) {
        try {
            mDBAdapter = SQLiteAdapter(context)
            mDBAdapter!!.open()

            sql = "SELECT * FROM $TABLE_IMG WHERE CONTENTS = ? AND DATE = ? "
            val bindArgs = arrayOf(contents, date)
            val nCount = mDBAdapter!!.getRowCount(sql, bindArgs)
            if (nCount > 0) {
                KLog.d(TAG, "@@ DB KIMG row Count : $nCount")
                sql = "DELETE FROM $TABLE_IMG WHERE CONTENTS = ? AND DATE = ?"

                mDBAdapter!!.update(sql, bindArgs)
            }

            mDBAdapter!!.close()

            mDBAdapter = null
            KLog.d(TAG, "@@ 사용자 이미지 삭제 완료 contents : $contents, date : $date")

        } catch (e: Exception) {
            KLog.d(TAG, "@@ 사용자 이미지 삭제 업데이트 오류" + e.toString())
        }

    }
}