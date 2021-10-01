package com.coprotect.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.coprotect.myapplication.uploadTasks.UploadPost.Companion.uploadPostImage
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
    private var imageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_new_post, container, false)
        val postBtn = rootView.findViewById<FloatingActionButton>(R.id.sendPostBtn)
        val captionEditText = rootView.findViewById<EditText>(R.id.newPostCaptionEditText)

        postImageView = rootView.findViewById(R.id.newPostImageView)

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)


        postBtn.setOnClickListener {
            if (imageBitmap != null){
                try {
                    Thread{
                        uploadPostImage(imageBitmap!!, captionEditText.text.toString())
                    }.start()

                    Toast.makeText(this.requireContext(), "Uploading...", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }catch (e: Exception){
                    Toast.makeText(this.requireContext(), "Failed to upload", Toast.LENGTH_SHORT).show()
                }
            }
        }

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
                imageBitmap = dr.toBitmap()
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
}
