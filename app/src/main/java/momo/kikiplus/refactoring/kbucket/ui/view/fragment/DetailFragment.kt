package momo.kikiplus.refactoring.kbucket.ui.view.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import momo.kikiplus.com.kbucket.R
import momo.kikiplus.com.kbucket.databinding.DetailFragmentBinding
import momo.kikiplus.deprecated.http.HttpUrlFileUploadManager
import momo.kikiplus.deprecated.http.HttpUrlTaskManager
import momo.kikiplus.deprecated.http.IHttpReceive
import momo.kikiplus.refactoring.common.util.*
import momo.kikiplus.refactoring.common.view.popup.ConfirmPopup
import momo.kikiplus.refactoring.common.view.popup.IPopupReceive
import momo.kikiplus.refactoring.common.view.popup.SpinnerListPopup
import momo.kikiplus.refactoring.kbucket.data.finally.DataConst
import momo.kikiplus.refactoring.kbucket.data.finally.NetworkConst
import momo.kikiplus.refactoring.kbucket.data.finally.PopupConst
import momo.kikiplus.refactoring.kbucket.data.finally.PreferConst
import momo.kikiplus.refactoring.kbucket.data.vo.Bucket
import momo.kikiplus.refactoring.kbucket.data.vo.Category
import momo.kikiplus.refactoring.kbucket.ui.view.activity.IBackReceive
import momo.kikiplus.refactoring.kbucket.ui.view.activity.MainFragmentActivity
import momo.kikiplus.refactoring.kbucket.ui.view.fragment.viewmodel.DetailViewModel
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DetailFragment : Fragment() , View.OnClickListener,
    IPopupReceive, IHttpReceive, android.os.Handler.Callback, IBackReceive {

    companion object {
        fun newInstance() = DetailFragment()
    }

    private lateinit var viewModel: DetailViewModel
    private lateinit var binding : DetailFragmentBinding

    private var buckets: Bucket? = null
    private var contents : String? = null

    private val REQ_CODE_PICKCUTRE = 1000
    private val REQ_CODE_GALLERY = 1001

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(arguments != null){
            contents = requireArguments().getString("CONTENTS")
            KLog.d("@@ contents : " + contents)
            buckets = Bucket(contents!!)
        }else{
            KLog.d("@@ argument is null")
        }
        val view = inflater.inflate(R.layout.detail_fragment, container, false)
        binding = DetailFragmentBinding.bind(view)
        setBackgroundColor()
        return view
    }

    private fun setBackgroundColor() {
        val color = (SharedPreferenceUtils.read(requireContext(), PreferConst.BACK_MEMO, SharedPreferenceUtils.SHARED_PREF_VALUE_INTEGER) as Int?)!!
        if (color != -1) {
            binding.writedetailBackColor.setBackgroundColor(color)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)
        setData()
    }

    fun setData(){
        val memoMap = buckets!!.content ?.let { viewModel.loadDBData(requireContext(), it) }
        if (memoMap.isNullOrEmpty()) {
            return
        }
        KLog.d("@@ memoMap" + memoMap.toString())
        buckets!!.date = memoMap["date"].toString()
        buckets!!.imageUrl = memoMap["image_path"].toString()
        buckets!!.deadLine = memoMap["deadline"].toString()

        val yn = memoMap["complete_yn"]
        binding.writeCompleteCheckbox.isChecked  = yn != null && yn == "Y"

        val bytes = viewModel.loadDBImage(requireContext(), buckets!!.completeYN.toString(),
            buckets!!.date.toString())
        if (bytes != null) {
            KLog.log( "@@ bytes  : " + bytes)
            Glide.with(this)
                .load(bytes)
                .into(binding.detailImageview)
            hideImageAttachButton(true)
            binding.detailImageview.scaleType = ImageView.ScaleType.FIT_XY
            binding.detailImageview.visibility = View.VISIBLE
            binding.detailRemove.visibility = View.VISIBLE
        } else {
            hideImageAttachButton(false)
            binding.detailImageview.visibility = View.INVISIBLE
        }

        binding.detailDateView.setText(buckets!!.date)
        binding.detailDateView.setOnClickListener(this)
        binding.detailDeadline.setText(buckets!!.deadLine)
        binding.detailDeadline.setOnClickListener(this)
        binding.detailContentView.setText(buckets!!.content)

        binding.writeSaveButton.setOnClickListener(this)
        binding.writeDeleteButton.setOnClickListener(this)
        binding.writeShareButton.setOnClickListener(this)
        binding.writeImageCamera.setOnClickListener(this)
        binding.writeImageGallery.setOnClickListener(this)
        binding.detailRemove.setOnClickListener(this)
    }

    /**
     * 이미지 첨부 버튼 보이게 하기/숨기기 메소드
     *
     * @param ishide 숨기기 여부 (true - 숨기기)
     */
    private fun hideImageAttachButton(ishide: Boolean) {
        if (ishide) {
            binding.writeImageCamera.visibility = View.GONE
            binding.writeImageGallery.visibility = View.GONE
        } else {
            binding.writeImageCamera.visibility = View.VISIBLE
            binding.writeImageGallery.visibility = View.VISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainFragmentActivity).setBackReceive(this)
    }

    override fun onBackKey() {
        KLog.log("@@ DetailFragment onBackKey back : " + requireArguments().getString("BACK") )
        (activity as MainFragmentActivity).setBackReceive(null)

        if (mPhotoPath != null) {
            KLog.log("@@ DetailFragment mphoto path " + mPhotoPath)
            DataUtils.deleteFile(mPhotoPath!!)
        }

        if (requireArguments().getString("BACK") == DataConst.VIEW_DONE) {
            val fragment = DoneFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            bundle.putString("BACK", DataConst.VIEW_MAIN)

            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_main, fragment)
                .commit()

        } else {
            val fragment = WriteFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            bundle.putString("BACK", DataConst.VIEW_MAIN)

            (activity as MainFragmentActivity).supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_main, fragment)
                .commit()

        }
    }


    override fun onClick(v: View) {
        when (v.id) {
            // 저장 버튼
            R.id.write_saveButton -> {

                contents = binding.detailContentView.text.toString()
                KLog.log("@@ 저장하기 내용 : " + contents!!)
                KLog.log("@@ 저장하기 내용2 : " + buckets!!)
                viewModel.updateDBDate(requireContext(), contents!!, buckets!!)
                onBackKey()
            }
            // 삭제 버튼
            R.id.write_deleteButton -> {
                var title = getString(R.string.delete_popup_title)
                var content = getString(R.string.delete_popup_content)
                mConfirmPopup =
                    ConfirmPopup(
                        requireContext(),
                        title,
                        ": ${buckets!!.content!!}\n\n $content",
                        R.layout.popup_confirm,
                        this,
                        PopupConst.POPUP_BUCKET_DELETE
                    )
                mConfirmPopup!!.showDialog()
            }
            // 공유 버튼
            R.id.write_shareButton -> {
                var title = getString(R.string.share_popup_title)
                var content = getString(R.string.share_popup_content)
                mConfirmPopup =
                    ConfirmPopup(
                        requireContext(),
                        title,
                        ": $buckets!!.content!!\n\n $content",
                        R.layout.popup_confirm,
                        this,
                        PopupConst.POPUP_BUCKET_SHARE
                    )
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
                var intent = Intent(Intent.ACTION_PICK)
                intent.type = MediaStore.Images.Media.CONTENT_TYPE
                intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                startActivityForResult(intent, REQ_CODE_GALLERY)
            }
            //첨부 이미지 삭제하기
            R.id.detail_remove -> {
                mPhotoPath = null
                hideImageAttachButton(false)
                binding.detailImageview!!.visibility = View.INVISIBLE
                binding.detailRemove.visibility = View.INVISIBLE
            }

            //날짜 선택하기
            R.id.write_layout_titleView -> {
                var gregorianCalendar = GregorianCalendar()
                var year = gregorianCalendar.get(Calendar.YEAR)
                var month = gregorianCalendar.get(Calendar.MONTH)
                var day = gregorianCalendar.get(Calendar.DAY_OF_MONTH)
                var datePickerDialog = DatePickerDialog(requireContext(), dateSetListener, year, month, day)
                datePickerDialog.show()
            }

            R.id.detail_deadline -> {
                var gregorianCalendar = GregorianCalendar()
                var year = gregorianCalendar.get(Calendar.YEAR)
                var month = gregorianCalendar.get(Calendar.MONTH)
                var day = gregorianCalendar.get(Calendar.DAY_OF_MONTH)
                var datePickerDialog = DatePickerDialog(requireContext(), dateSetListener2, year, month, day)
                datePickerDialog.show()
            }
        }
    }

    override fun onHttpReceive(type: Int, actionId: Int, obj: Any?) {
        KLog.d("@@ onHttpReceive : $obj")
        KLog.d("@@ onHttpReceive type : $type")
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
                    KLog.log( "@@ jsonException message : " + e.message)
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
                    KLog.e("@@ jsonException message : " + e.message)
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
            TOAST_MASSEGE -> Toast.makeText(requireContext(), msg.obj as String, Toast.LENGTH_LONG).show()
            UPLOAD_IMAGE -> {
                val photoPath = mPhotoPath
                val bytes = viewModel.loadDBImage(requireContext(), buckets!!.content!!, buckets!!.date!!)
                if (bytes != null) {
                    val calendar = Calendar.getInstance()
                    val sdf = SimpleDateFormat("yyyyMMdd_hhmmss")
                    val fileName = sdf.format(calendar.time)

                    val httpUrlFileUploadManager = HttpUrlFileUploadManager(NetworkConst.KBUCKET_UPLOAD_IMAGE_URL, this, IHttpReceive.INSERT_IMAGE, bytes)
                    httpUrlFileUploadManager.execute(photoPath, "idx", mImageIdx.toString() + "", "$fileName.jpg")
                } else {
                    KLog.d( "@@ UPLOAD IMAGE NO !")
                }
            }
            UPLOAD_BUCKET -> {
                val httpUrlTaskManager = HttpUrlTaskManager(NetworkConst.KBUCKET_INSERT_BUCKET_URL, true, this, IHttpReceive.INSERT_BUCKET)
                httpUrlTaskManager.execute(StringUtils.getHTTPPostSendData(shareBucketImage()))
            }
            SELECT_BUCKET_CATEGORY -> {
                val title = getString(R.string.category_popup_title)
                val content = getString(R.string.category_popup_content)
                val list = ArrayList<Category>()
                list.add(Category("LIEF", 1))
                list.add(Category("LOVE", 2))
                list.add(Category("WORK", 3))
                list.add(
                    Category(
                        "EDUCATION",
                        4
                    )
                )
                list.add(Category("FAMILY", 5))
                list.add(Category("FINANCE", 6))
                list.add(Category("DEVELOP", 7))
                list.add(Category("HEALTH", 8))
                list.add(Category("ETC", 9))
                mCategoryPopup =
                    SpinnerListPopup(
                        requireContext(),
                        title,
                        content,
                        list,
                        R.layout.popupview_spinner_list,
                        this,
                        PopupConst.POPUP_BUCKET_CATEGORY
                    )
                mCategoryPopup!!.showDialog()
            }
        }
        return false
    }

    override fun onPopupAction(popId: Int, what: Int, obj: Any?) {
        if (popId == PopupConst.POPUP_BUCKET_SHARE) {
            if (what == IPopupReceive.POPUP_BTN_OK) {
                mHandler!!.sendEmptyMessage(SELECT_BUCKET_CATEGORY)
            }
            mConfirmPopup!!.closeDialog()
        } else if (popId == PopupConst.POPUP_BUCKET_DELETE) {
            if (what == IPopupReceive.POPUP_BTN_OK) {
                viewModel.removeDBData(requireContext(), buckets!!.content!!, buckets!!.date!!)

            }
            mConfirmPopup!!.closeDialog()
        } else if (popId == PopupConst.POPUP_BUCKET_CATEGORY) {
            if (what == IPopupReceive.POPUP_BTN_OK) {
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
        val userNickName = SharedPreferenceUtils.read(requireContext(), PreferConst.KEY_USER_NICKNAME, SharedPreferenceUtils.SHARED_PREF_VALUE_STRING) as String?
        bucket.nickName = userNickName!!
        bucket.content = buckets!!.content
        bucket.imageUrl = ""
        bucket.date = buckets!!.date
        bucket.category!!.categoryCode = mCategory
        return bucket.toHasnMap()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_PICKCUTRE) {
            if (resultCode == Activity.RESULT_OK) {
                val bm = ByteUtils.getFileBitmap(mPhotoPath!!)
                if (bm != null) {
                    hideImageAttachButton(true)
                    binding.detailImageview.visibility = View.VISIBLE
                    binding.detailImageview.scaleType = ImageView.ScaleType.FIT_XY
                    binding.detailImageview.setImageBitmap(bm)
                    binding.detailRemove.visibility = View.VISIBLE
                }
            }
        } else if (requestCode == REQ_CODE_GALLERY) {
            if (data != null) {
                val imgUri = data.data
                if (imgUri != null) {
                    KLog.log("@@ photo data: " + imgUri.path!!)
                    mPhotoPath = DataUtils.newFileName
                    try {
                        val imagePath = DataUtils.getMediaScanPath(requireContext(), imgUri)
                        KLog.log("@@ photo imagePath :$imagePath")
                        if (imagePath.isEmpty()) {
                            val message = getString(R.string.write_bucekt_image_attch)
                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                        DataUtils.copyFile(imagePath, mPhotoPath!!)
                        ByteUtils.setFileResize(mPhotoPath!!, 400, 800, false)
                        val photo = ByteUtils.getFileBitmap(mPhotoPath!!)
                        if (photo != null) {
                            hideImageAttachButton(true)
                            binding.detailImageview.visibility = View.VISIBLE
                            binding.detailImageview.scaleType = ImageView.ScaleType.FIT_XY
                            binding.detailImageview.setImageBitmap(photo)
                            binding.detailRemove.visibility = View.VISIBLE
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

    }


    private val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
        binding.detailDateView.setText(msg)
    }

    private val dateSetListener2 = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        val msg = String.format("%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth)
        binding.detailDeadline.setText(msg)
    }
}
