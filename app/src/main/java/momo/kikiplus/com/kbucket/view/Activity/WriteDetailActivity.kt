package momo.kikiplus.com.kbucket.view.Activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.Utils.*
import momo.kikiplus.com.kbucket.databinding.WriteDetailActivityBinding
import momo.kikiplus.com.kbucket.http.HttpUrlFileUploadManager
import momo.kikiplus.com.kbucket.http.HttpUrlTaskManager
import momo.kikiplus.com.kbucket.http.IHttpReceive
import momo.kikiplus.com.kbucket.sqlite.SQLQuery
import momo.kikiplus.com.kbucket.view.Bean.Bucket
import momo.kikiplus.com.kbucket.view.Bean.Category
import momo.kikiplus.com.kbucket.view.popup.ConfirmPopup
import momo.kikiplus.com.kbucket.view.popup.OnPopupEventListener
import momo.kikiplus.com.kbucket.view.popup.SpinnerListPopup
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author grapegirl
 * @version 1.0
 * @Class Name : WriteDetailActivity
 * @Description : 버킷 작성 클래스
 */
class WriteDetailActivity : Activity(), View.OnClickListener, OnPopupEventListener, IHttpReceive, android.os.Handler.Callback {

    private var mSqlQuery: SQLQuery? = null
    private var mContents: String? = null
    private var mDate: String? = null
    private var mDeadLineDate: String? = null

    private val REQ_CODE_PICKCUTRE = 1000
    private val REQ_CODE_GALLERY = 1001
    private var BACK: String? = null
    private var mPhotoPath: String? = null

    private var mConfirmPopup: ConfirmPopup? = null
    private var mCategoryPopup: SpinnerListPopup? = null

    private var mImageIdx = -1
    private var mCategory = 1

    private var mHandler: android.os.Handler? = null
    private val TOAST_MASSEGE = 10
    private val UPLOAD_IMAGE = 20
    private val UPLOAD_BUCKET = 30
    private val SELECT_BUCKET_CATEGORY = 40

    internal var mCheckbox: CheckBox? = null
    internal var mImageView: ImageView? = null

    private lateinit var mBinding : WriteDetailActivityBinding

    private val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
        mDate = msg
        (findViewById<View>(R.id.write_layout_titleView) as TextView).text = mDate
    }

    private val dateSetListener2 = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
        mDeadLineDate = msg
        (findViewById<View>(R.id.write_layout_deadline) as TextView).text = mDeadLineDate
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        mBinding = WriteDetailActivityBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initialize()

        mHandler = Handler(this)
        mSqlQuery = SQLQuery()
        mContents = intent.getStringExtra("CONTENTS")
        BACK = intent.getStringExtra("BACK")
        setData()
        AppUtils.sendTrackerScreen(this, "가지상세화면")

        mBinding.writeSaveButton.setOnClickListener(this)
        mBinding.writeDeleteButton.setOnClickListener(this)
        mBinding.writeShareButton.setOnClickListener(this)
        mBinding.writeImageCamera.setOnClickListener(this)
        mBinding.writeImageGallery.setOnClickListener(this)
        mBinding.writeImageRemove.setOnClickListener(this)
    }

    private fun initialize() {
        setBackgroundColor()

        mCheckbox = findViewById<CheckBox>(R.id.write_layout_checkbox)
        mImageView = findViewById<ImageView>(R.id.write_layout_imageview)

    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(applicationContext, ContextUtils.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            findViewById<View>(R.id.writedetail_back_color).setBackgroundColor(color)
        }
    }

    override fun finish() {
        super.finish()
        this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onBackPressed() {
        if (mPhotoPath != null) {
            DataUtils.deleteFile(mPhotoPath!!)
        }
        KLog.d(this.javaClass.simpleName, "@@ BACK" + BACK)

        if (BACK == ContextUtils.VIEW_COMPLETE_LIST) {
            val intent = Intent(this, BucketListActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, WriteActivity::class.java)
            startActivity(intent)
            finish()
        }

    }


    override fun onClick(v: View) {
        when (v.id) {
            // 저장 버튼
            R.id.write_saveButton -> {
                updateDBDate()
                onBackPressed()
            }
            // 삭제 버튼
            R.id.write_deleteButton -> {
                var title = getString(R.string.delete_popup_title)
                var content = getString(R.string.delete_popup_content)
                mConfirmPopup = ConfirmPopup(this, title, ": $mContents\n\n $content", R.layout.popup_confirm, this, OnPopupEventListener.POPUP_BUCKET_DELETE)
                mConfirmPopup!!.showDialog()
            }
            // 공유 버튼
            R.id.write_shareButton -> {
                var title = getString(R.string.share_popup_title)
                var content = getString(R.string.share_popup_content)
                mConfirmPopup = ConfirmPopup(this, title, ": $mContents\n\n $content", R.layout.popup_confirm, this, OnPopupEventListener.POPUP_BUCKET_SHARE)
                mConfirmPopup!!.showDialog()
            }
            //이미지 첨부(카메라로 가져오기)
            R.id.write_image_camera -> {
                mPhotoPath = DataUtils.newFileName
                var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(File(mPhotoPath)))
                startActivityForResult(intent, REQ_CODE_PICKCUTRE)
            }
            //갤러리로 부터 이미지 가져오기
            R.id.write_image_gallery -> {
                intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                startActivityForResult(intent, REQ_CODE_GALLERY)
            }
            //첨부 이미지 삭제하기
            R.id.write_image_remove -> {
                mPhotoPath = null
                hideImageAttachButton(false)
                mImageView!!.visibility = View.INVISIBLE
                (findViewById<View>(R.id.write_image_remove) as Button).visibility = View.INVISIBLE
            }

            //날짜 선택하기
            R.id.write_layout_titleView -> {
                var gregorianCalendar = GregorianCalendar()
                var year = gregorianCalendar.get(Calendar.YEAR)
                var month = gregorianCalendar.get(Calendar.MONTH)
                var day = gregorianCalendar.get(Calendar.DAY_OF_MONTH)
                var datePickerDialog = DatePickerDialog(this@WriteDetailActivity, dateSetListener, year, month, day)
                datePickerDialog.show()
            }

            R.id.write_layout_deadline -> {
                var gregorianCalendar = GregorianCalendar()
                var year = gregorianCalendar.get(Calendar.YEAR)
                var month = gregorianCalendar.get(Calendar.MONTH)
                var day = gregorianCalendar.get(Calendar.DAY_OF_MONTH)
                var datePickerDialog = DatePickerDialog(this@WriteDetailActivity, dateSetListener2, year, month, day)
                datePickerDialog.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_PICKCUTRE) {
            if (resultCode == Activity.RESULT_OK) {
                val bm = ByteUtils.getFileBitmap(mPhotoPath!!)
                if (bm != null) {
                    hideImageAttachButton(true)
                    mImageView!!.visibility = View.VISIBLE
                    mImageView!!.scaleType = ImageView.ScaleType.FIT_XY
                    mImageView!!.setImageBitmap(bm)
                    (findViewById<View>(R.id.write_image_remove) as Button).visibility = View.VISIBLE
                }
            }
        } else if (requestCode == REQ_CODE_GALLERY) {
            if (data != null) {
                val imgUri = data.data
                if (imgUri != null) {
                    KLog.d(ContextUtils.TAG, "@@ photo data: " + imgUri.path!!)
                    mPhotoPath = DataUtils.newFileName
                    try {
                        val imagePath = DataUtils.getMediaScanPath(this, imgUri)
                        KLog.d(ContextUtils.TAG, "@@ photo imagePath :$imagePath")
                        if (imagePath.isEmpty()) {
                            val message = getString(R.string.write_bucekt_image_attch)
                            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                        }
                        DataUtils.copyFile(imagePath, mPhotoPath!!)
                        ByteUtils.setFileResize(mPhotoPath!!, 400, 800, false)
                        val photo = ByteUtils.getFileBitmap(mPhotoPath!!)
                        if (photo != null) {
                            hideImageAttachButton(true)
                            mImageView!!.visibility = View.VISIBLE
                            mImageView!!.scaleType = ImageView.ScaleType.FIT_XY
                            mImageView!!.setImageBitmap(photo)
                            (findViewById<View>(R.id.write_image_remove) as Button).visibility = View.VISIBLE
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

    }

    /**
     * DB 데이타 불러와서 데이타 표시하기
     */

    private fun setData() {
        val memoMap = mSqlQuery!!.selectKbucket(applicationContext, mContents!!)
        if (memoMap.isNullOrEmpty()) {
            return
        }
        KLog.d(this.javaClass.simpleName, "@@ memoMap" + memoMap.toString())
        mDate = memoMap["date"]
        val yn = memoMap["complete_yn"]
        mCheckbox!!.isChecked = yn != null && yn == "Y"
        mPhotoPath = memoMap["image_path"]

        val bytes = mSqlQuery!!.selectImage(applicationContext, mContents!!, mDate!!)
        if (bytes != null) {
            KLog.d(ContextUtils.TAG, "@@ bytes  : " + bytes)
            Glide.with(this)
                    .load(bytes)
                    .into(mImageView!!)
            hideImageAttachButton(true)
            mImageView!!.scaleType = ImageView.ScaleType.FIT_XY
            mImageView!!.visibility = View.VISIBLE
            (findViewById<View>(R.id.write_image_remove) as Button).visibility = View.VISIBLE
        } else {
            hideImageAttachButton(false)
            mImageView!!.visibility = View.INVISIBLE
        }

        // 로컬 파일
        //        File file = new File(mPhotoPath);
        //        KLog.d(ContextUtils.TAG, "@@ local file path : " + mPhotoPath);
        //        if(file.exists()){
        //            Glide.with(this)
        //                    .load(file)
        //                    .into(mImageView);
        //            hideImageAttachButton(true);
        //            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        //            mImageView.setVisibility(View.VISIBLE);
        //            ((Button) findViewById(R.id.write_image_remove)).setVisibility(View.VISIBLE);
        //        }else{
        //            hideImageAttachButton(false);
        //            mImageView.setVisibility(View.INVISIBLE);
        //        }

        mDeadLineDate = memoMap["deadline"]

        (findViewById<View>(R.id.write_layout_titleView) as TextView).text = mDate
        (findViewById<View>(R.id.write_layout_titleView) as TextView).setOnClickListener(this)
        (findViewById<View>(R.id.write_layout_contentView) as TextView).text = mContents
        (findViewById<View>(R.id.write_layout_deadline) as TextView).text = mDeadLineDate
        (findViewById<View>(R.id.write_layout_deadline) as TextView).setOnClickListener(this)
    }

    /**
     * DB 데이타 동기화하기
     */
    private fun updateDBDate() {
        val NewContents = (findViewById<View>(R.id.write_layout_contentView) as TextView).text.toString()
        val completeYN = if (mCheckbox!!.isChecked) "Y" else "N"
        val imagePath = if (mPhotoPath != null) mPhotoPath else ""

        KLog.d(this.javaClass.simpleName, "@@ NewContents : " + NewContents)
        KLog.d(this.javaClass.simpleName, "@@ completeYN : " + completeYN)
        KLog.d(this.javaClass.simpleName, "@@ imagePath : " + imagePath)
        KLog.d(this.javaClass.simpleName, "@@ mPhotoPath : " + mPhotoPath)

        mSqlQuery!!.updateMemoContent(applicationContext, mContents!!, NewContents, completeYN, mDate!!, imagePath!!, mDeadLineDate!!)
        //수정한 상태지만 이미지는 이미 없는 상태
        if (mPhotoPath == null) {
            mSqlQuery!!.deleteMemoImage(applicationContext, mContents!!, mDate!!)
        } else {
            //신규로 추가한 경우
            val bitmaps = ByteUtils.getByteArrayFromFile(mPhotoPath!!)
            KLog.d(this.javaClass.simpleName, "@@ bitmaps : " + bitmaps)
            if(bitmaps != null){
                mSqlQuery!!.updateMemoImage(applicationContext, mContents!!, mDate!!, bitmaps)
            }
        }
    }

    /**
     * DB 데이타 동기화하기(삭제)
     */
    private fun removeDBData(Content: String?) {
        KLog.d(this.javaClass.simpleName, "@@ remove Data Contents : " + Content!!)
        mSqlQuery!!.deleteUserBucket(applicationContext, Content)
        mSqlQuery!!.deleteMemoImage(applicationContext, mContents!!, mDate!!)
    }

    /**
     * 이미지 첨부 버튼 보이게 하기/숨기기 메소드
     *
     * @param ishide 숨기기 여부 (true - 숨기기)
     */
    private fun hideImageAttachButton(ishide: Boolean) {
        if (ishide) {
            (findViewById<View>(R.id.write_image_camera) as Button).visibility = View.GONE
            (findViewById<View>(R.id.write_image_gallery) as Button).visibility = View.GONE
        } else {
            (findViewById<View>(R.id.write_image_camera) as Button).visibility = View.VISIBLE
            (findViewById<View>(R.id.write_image_gallery) as Button).visibility = View.VISIBLE
        }
    }

    override fun onPopupAction(popId: Int, what: Int, obj: Any?) {
        if (popId == OnPopupEventListener.POPUP_BUCKET_SHARE) {
            if (what == OnPopupEventListener.POPUP_BTN_OK) {
                mHandler!!.sendEmptyMessage(SELECT_BUCKET_CATEGORY)
            }
            mConfirmPopup!!.closeDialog()
        } else if (popId == OnPopupEventListener.POPUP_BUCKET_DELETE) {
            if (what == OnPopupEventListener.POPUP_BTN_OK) {
                removeDBData(mContents)
                onBackPressed()
            }
            mConfirmPopup!!.closeDialog()
        } else if (popId == OnPopupEventListener.POPUP_BUCKET_CATEGORY) {
            if (what == OnPopupEventListener.POPUP_BTN_OK) {
                val json = obj as JSONObject
                try {
                    mCategory = Integer.valueOf(json.getString("styleCode"))
                } catch (e: JSONException) {
                    mCategory = 1
                }

                mHandler!!.sendEmptyMessage(UPLOAD_BUCKET)
            }
            mCategoryPopup!!.closeDialog()
        }
    }

    /**
     * 서버로 전송할 데이타 만들기
     *
     * @return 전송 데이타
     */
    private fun shareBucketImage(): HashMap<String, Any> {
        val bucket = Bucket()
        val userNickName = SharedPreferenceUtils.read(this, ContextUtils.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        bucket.nickName = userNickName!!
        bucket.content = mContents
        bucket.imageUrl = ""
        bucket.date = mDate
        bucket.categoryCode = mCategory
        return bucket.toHasnMap()
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive : $obj")
        KLog.d(this.javaClass.simpleName, "@@ onHttpReceive type : $type")
        // 버킷 공유 결과
        val mData = obj as String
        var isValid : Boolean = false
        if (actionId == IHttpReceive.INSERT_BUCKET) {
            if (type == IHttpReceive.HTTP_FAIL) {
                val message = getString(R.string.write_bucekt_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
            } else {

                try {
                    val json = JSONObject(mData)
                    isValid = json.getBoolean("isValid")
                    mImageIdx = json.getInt("idx")
                } catch (e: JSONException) {
                    KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
                }

                if (isValid == true) {
                    // 이미지가 있는 경우 전송함
                    if (mPhotoPath != null && mPhotoPath != "") {
                        mHandler!!.sendEmptyMessage(UPLOAD_IMAGE)
                    } else {
                        val message = getString(R.string.write_bucekt_success_string)
                        mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                    }
                }
            }
        }// 이미지 업로드 결과
        else if (actionId == IHttpReceive.INSERT_IMAGE) {
            if (type == IHttpReceive.HTTP_FAIL) {
                val message = getString(R.string.upload_image_fail_string)
                mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
            } else {
                try {
                    val json = JSONObject(mData)
                    isValid = json.getBoolean("isValid")
                } catch (e: JSONException) {
                    KLog.e(ContextUtils.TAG, "@@ jsonException message : " + e.message)
                }

                if (isValid == true) {
                    val message = getString(R.string.write_bucekt_success_string)
                    mHandler!!.sendMessage(mHandler!!.obtainMessage(TOAST_MASSEGE, message))
                }
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            TOAST_MASSEGE -> Toast.makeText(applicationContext, msg.obj as String, Toast.LENGTH_LONG).show()
            UPLOAD_IMAGE -> {
                val photoPath = mPhotoPath
                val bytes = mSqlQuery!!.selectImage(applicationContext, mContents!!, mDate!!)
                if (bytes != null) {
                    val calendar = Calendar.getInstance()
                    val sdf = SimpleDateFormat("yyyyMMdd_hhmmss")
                    val fileName = sdf.format(calendar.time)

                    val httpUrlFileUploadManager = HttpUrlFileUploadManager(ContextUtils.KBUCKET_UPLOAD_IMAGE_URL, this, IHttpReceive.INSERT_IMAGE, bytes)
                    httpUrlFileUploadManager.execute(photoPath, "idx", mImageIdx.toString() + "", "$fileName.jpg")
                } else {
                    KLog.d(ContextUtils.TAG, "@@ UPLOAD IMAGE NO !")
                }
            }
            UPLOAD_BUCKET -> {
                val httpUrlTaskManager = HttpUrlTaskManager(ContextUtils.KBUCKET_INSERT_BUCKET_URL, true, this, IHttpReceive.INSERT_BUCKET)
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(shareBucketImage()))
            }
            SELECT_BUCKET_CATEGORY -> {
                val title = getString(R.string.category_popup_title)
                val content = getString(R.string.category_popup_content)
                val list = ArrayList<Category>()
                list.add(Category("LIEF", 1))
                list.add(Category("LOVE", 2))
                list.add(Category("WORK", 3))
                list.add(Category("EDUCATION", 4))
                list.add(Category("FAMILY", 5))
                list.add(Category("FINANCE", 6))
                list.add(Category("DEVELOP", 7))
                list.add(Category("HEALTH", 8))
                list.add(Category("ETC", 9))
                mCategoryPopup = SpinnerListPopup(this, title, content, list, R.layout.popupview_spinner_list, this, OnPopupEventListener.POPUP_BUCKET_CATEGORY)
                mCategoryPopup!!.showDialog()
            }
        }
        return false
    }
}