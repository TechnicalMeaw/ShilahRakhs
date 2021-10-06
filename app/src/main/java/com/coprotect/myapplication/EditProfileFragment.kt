package com.coprotect.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getUserReference
import com.coprotect.myapplication.databinding.FragmentEditProfileBinding
import com.coprotect.myapplication.firebaseClasses.UserItem
import com.coprotect.myapplication.uploadTasks.UpdateProfile.Companion.updateUserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : Fragment() {

    private lateinit var binding : FragmentEditProfileBinding
    private var user: UserItem? = null
    private var imageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        /**
         * Load User Details
         */
        loadDetails()

        binding.closeBtn.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.changePhotoText.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }

        binding.saveButton.setOnClickListener {
            if (user!= null){
                val firstName: String = binding.editProfileFirstNameEditText.text.toString().trim()
                val lastName: String = binding.editProfileLastNameEditText.text.toString().trim()
                val bio: String = binding.editProfileBioEditText.text.toString().trim()
                val email: String = binding.editProfileEmailEditText.text.toString().trim()
                val website: String = binding.editProfileWebsiteEditText.text.toString().trim()

                if (firstName != "" && lastName != ""){
                    Thread{
                        updateUserInfo(requireContext(), imageBitmap, user!!, firstName, lastName, bio, email, website)
                        parentFragmentManager.popBackStack()
                    }.start()
                    Toast.makeText(requireContext(), "Updating...", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }

            }
        }

        return binding.root
    }


    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data

            if (data != null) {
                val imageUri = data.data
                binding.profileImage.setImageURI(imageUri)
                binding.profileImage.invalidate()
                val dr = binding.profileImage.drawable
                imageBitmap = dr.toBitmap()
                Glide.with(this).load(imageUri).into(binding.profileImage)
            }
        }
    }

    private fun loadDetails() {
        getUserReference(FirebaseAuth.getInstance().uid.toString()).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    user = snapshot.getValue(UserItem::class.java)!!
                    if (user != null){
                        binding.editProfileFirstNameEditText.setText(user!!.firstName)
                        binding.editProfileLastNameEditText.setText(user!!.lastName)
                        binding.editProfileBioEditText.setText(user!!.bio)
                        binding.editProfileWebsiteEditText.setText(user!!.website)

                        binding.editProfilePhoneEditText.setText(user!!.phoneNumber)
                        binding.editProfileCountryEditText.setText(user!!.country)
                        binding.editProfileEmailEditText.setText(user!!.email)

                        try {
                            val options: RequestOptions = RequestOptions()
                                .placeholder(R.drawable.user_icon_default)
                                .error(R.drawable.user_icon_default)
                            Glide.with(requireContext()).load(user!!.profilePictureUrl).apply(options).into(binding.profileImage)
                        }catch (e: Exception){
                            e.stackTrace
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}