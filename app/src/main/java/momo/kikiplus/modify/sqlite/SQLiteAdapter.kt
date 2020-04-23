package momo.kikiplus.modify.sqlite

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import momo.kikiplus.refactoring.util.StringUtils
import java.util.*

class SQLiteAdapter(internal var mCtx: Context) {
    private var mDbHelper: DatabaseHelper? = null
    private var mDb: SQLiteDatabase? = null

    @Throws(SQLException::class)
    fun open(): SQLiteAdapter {
        mDbHelper = DatabaseHelper(mCtx)
        mDb = mDbHelper!!.writableDatabase
        return this
    }

    fun close() {
        mDbHelper!!.close()
    }

    fun update(sql: String, bindArgs: Array<String>?) {
        try {
            if (bindArgs == null) {
                mDb!!.execSQL(sql)
            } else {
                mDb!!.execSQL(sql, bindArgs)
            }

        } catch (e: Exception) {
            Log.w(TAG, sql + "\n" + e.message)
        }

    }


    fun update(sql: String, bindArgs: Array<Any>?) {
        try {
            if (bindArgs == null) {
                mDb!!.execSQL(sql)
            } else {
                mDb!!.execSQL(sql, bindArgs)
            }

        } catch (e: Exception) {
            Log.w(TAG, sql + "\n" + e.message)
        }

    }

    fun update(sql: String) {
        try {
            mDb!!.execSQL(sql)

        } catch (e: Exception) {
            Log.w(TAG, sql + "\n" + e.message)
        }

    }

    fun query(sql: String, selectionArgs: Array<String>?): LinkedList<LinkedHashMap<String, String>> {
        val rowset = LinkedList<LinkedHashMap<String, String>>()

        var result: Cursor? = null

        try {
            result = mDb!!.rawQuery(sql, selectionArgs)
            //			Log.e("SQLiteAdapter",""+result);
            if (result != null && result.moveToFirst()) {
                val columnCount = result.columnCount
                //				Log.e("SQLiteAdapter=columnCount  ",""+columnCount);
                do {
                    val row = LinkedHashMap<String, String>()

                    for (i in 0 until columnCount) {
                        result.getString(i)
                        row[(result.getColumnName(i) as String).toLowerCase()] = StringUtils.checkString(result.getString(i), "")
                        //						Log.e("SQLiteAdapter=row",""+row);
                        //						Log.e("NAME",result.getColumnName(i));
                    }

                    rowset.add(row)
                } while (result.moveToNext())
            }
        } catch (e: Exception) {
            Log.w(TAG, sql + "\n" + e.message)
        } finally {
            try {
                result!!.close()
            } catch (ex: Exception) {
            }

        }

        return rowset
    }

    fun queryRow(sql: String, selectionArgs: Array<String>?): LinkedHashMap<String, String> {
        var result: Cursor? = null
        val row = LinkedHashMap<String, String>()
        try {
            result = mDb!!.rawQuery(sql, selectionArgs)
            if (result != null && result.moveToFirst()) {
                val columnCount = result.columnCount
                for (i in 0 until columnCount) {
                    result.getString(i)
                    row[(result.getColumnName(i) as String).toLowerCase()] = StringUtils.checkString(result.getString(i), "")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, sql + "\n" + e.message)
        } finally {
            try {
                result!!.close()
            } catch (ex: Exception) {
            }

        }
        return row
    }

    fun queryImageRow(sql: String, selectionArgs: Array<String>): ByteArray? {
        var result: Cursor?
        var bytes: ByteArray? = null
        result = mDb!!.rawQuery(sql, selectionArgs)
        if (result != null && result.moveToFirst()) {
            //val columnCount = result.columnCount
            bytes = result.getBlob(0)
        }
        result!!.close()
        return bytes
    }

    /**
     * 해당 테이블의 Row 수 반환 메소드
     *
     * @param sql           실행할 질의문
     * @param selectionArgs 값
     * @return 테이블의 Row 수 반환
     */
    fun getRowCount(sql: String, selectionArgs: Array<String>): Int {

        var count = 0
        var result: Cursor?

        result = mDb!!.rawQuery(sql, selectionArgs)

        if (result != null && result.moveToFirst()) {
            count++
        }
        while (result!!.moveToNext());

        return count
    }

    //헬퍼 이너클래스
    private inner class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            Log.e("@@ onCreate", "시작")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.e("@@ onUpgrade", "DB 업데이트")
        }
    }

    companion object {
        private val TAG = "SQLiteAdapter"
        private val DATABASE_NAME = "bucket.db"
        private val DATABASE_VERSION = 1
    }
}
