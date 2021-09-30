package com.coprotect.myapplication

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.coprotect.myapplication.constants.CheckingPermission
import com.coprotect.myapplication.constants.DatabaseLocations
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream


class AddPostActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var userDbRef: DatabaseReference
    //lateinit var storageReference: StorageReference

    var storagePath = "Posts/"

    lateinit var mPTitle : EditText
    lateinit var mPDescriptionEt: EditText
    lateinit var mPImageIv: ImageView
    lateinit var mPUploadBtn: Button
    lateinit var choseBtn : ImageButton

    lateinit var image_uri: Uri
    lateinit var cp : CheckingPermission
    lateinit var progressDialog: ProgressDialog

    lateinit var name: String
    lateinit var phone : String
    lateinit var dp: String
    lateinit var uid: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_addpost)

        cp = CheckingPermission(this)
        progressDialog = ProgressDialog(this)

        mPTitle = findViewById(R.id.et_postTitle)
        mPDescriptionEt = findViewById(R.id.et_postDescription)
        mPImageIv = findViewById(R.id.iv_post)
        mPUploadBtn = findViewById(R.id.btn_upload)
        choseBtn = findViewById(R.id.choose)

        choseBtn.setOnClickListener {
            showImagePickDialog()
        }

        mAuth = FirebaseAuth.getInstance()
        uid = mAuth.currentUser!!.uid
        checkUserStatus()

        userDbRef = FirebaseDatabase.getInstance().getReference("user")
        val query = userDbRef.orderByChild("userId").equalTo(uid)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    name = ds.child("firstName").value.toString()
                    phone = ds.child("phoneNumber").value.toString()
                    dp = ds.child("profilePictureUrl").value.toString()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        //storageReference = FirebaseStorage.getInstance().reference

        mPUploadBtn.setOnClickListener { v ->
            val description = mPDescriptionEt.text.toString().trim { it <= ' ' }
            val title = mPTitle.text.toString().trim{it <= ' '}
            uploadData(title,description,image_uri)
        }
    }

    private fun uploadData(title: String, description: String, image_uri: Uri) {
        progressDialog.setMessage("Posting...")
        progressDialog.show()
        val timeStamp = System.currentTimeMillis().toString()
        val filePathAndName = "Posts/post_$timeStamp"

        val bitmap = (mPImageIv.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)
        ref.putFile(Uri.parse(image_uri.toString()))
            .addOnSuccessListener { taskSnapshot ->

                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val downloadUri = uriTask.result.toString()
                if (uriTask.isSuccessful) {

                    //url is received now upload it to firebase database
                    val hashMap: HashMap<String, Any?> = HashMap()
                    //put post info now
                    hashMap["userId"] = uid
                    hashMap["uName"] = name
                    hashMap["uPhone"] = phone
                    hashMap["uDp"] = dp
                    hashMap["pId"] = timeStamp
                    hashMap["pTitle"] = title
                    hashMap["pDescription"] = description
                    hashMap["pImage"] = downloadUri
                    hashMap["pTime"] = timeStamp
                    hashMap["pLikes"] = "0"
                    hashMap["pComments"] = "0"

                    //path to store data
                    val dbRef = DatabaseLocations.getAllPostReference()
                    //put data in this ref
                    dbRef.child(timeStamp).setValue(hashMap).addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Done..", Toast.LENGTH_SHORT).show()
                        mPTitle.setText("")
                        mPDescriptionEt.setText("")
                        mPImageIv.setImageURI(null)
                        image_uri == null

                }.addOnFailureListener { e->
                    progressDialog.dismiss()
                    Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showImagePickDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Image From")
        builder.setItems(options) { dialog, which ->
            if (which === 0) {
                if (!cp.checkCameraPermission()) cp.requestCameraPermission() else pickFromCamera()
            } else if (which === 1) {
                if (!cp.checkStoragePermission()) cp.requestStoragePermission() else pickFromGallery()
            }
        }
        builder.show()
    }

    private fun pickFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE)
    }

    private fun pickFromCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
        image_uri =
            this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    val camera = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val writeStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (camera && writeStorage) {
                        pickFromCamera()
                    } else {
                        Toast.makeText(this, "Please allow access to Camera", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    val writeStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (writeStorage) {
                        pickFromGallery()
                    } else {
                        Toast.makeText(this, "Please allow access to Gallery", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data!!.data!!
                mPImageIv.setImageURI(image_uri)
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                mPImageIv.setImageURI(image_uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        checkUserStatus()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        checkUserStatus()
    }

    private fun checkUserStatus() {
        val user = mAuth.currentUser
        if (user != null) {
            uid = user.uid
            phone = user.phoneNumber.toString()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    companion object {
        const val CAMERA_REQUEST_CODE = 100
        const val STORAGE_REQUEST_CODE = 200
        const val IMAGE_PICK_GALLERY_CODE = 300
        const val IMAGE_PICK_CAMERA_CODE = 400
    }
}










































/*package com.coprotect.myapplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewPostFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewPostFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    lateinit var postImageView : ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_new_post, container, false)
        val postBtn = rootView.findViewById<FloatingActionButton>(R.id.sendPostBtn)
        postImageView = rootView.findViewById(R.id.newPostImageView)

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)

        return rootView
    }

    var imageUri: Uri? = null
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data

            if (data != null){
                imageUri = data.data
                postImageView.setImageURI(imageUri)
                postImageView.invalidate()
                val dr =postImageView.drawable
                val imageBitmap = dr.toBitmap()
                Glide.with(this).load(imageUri).into(postImageView)
            }else{
                parentFragmentManager.popBackStackImmediate()
            }
        }else{
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewPostFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewPostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}*/
