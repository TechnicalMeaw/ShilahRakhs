package com.coprotect.myapplication

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getUserReference
import com.coprotect.myapplication.constants.IntentStrings
import com.coprotect.myapplication.constants.StorageLocations.Companion.getUserDpReference
import com.coprotect.myapplication.databinding.ActivityRegisterUserBinding
import com.coprotect.myapplication.firebaseClasses.UserItem
import com.coprotect.myapplication.imageOperations.ImageResizer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.util.*

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterUserBinding

    private lateinit var firstName : String
    private lateinit var lastName : String
    private lateinit var phoneNumber : String
    private lateinit var countryName : String
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phoneNumber = intent.getStringExtra(IntentStrings.phoneNumber).toString()
        countryName = intent.getStringExtra(IntentStrings.countryName).toString()

        binding.photoSelector.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }

        binding.createAccountBtn.setOnClickListener {
            firstName = binding.loginFirstNameEditText.text.toString()
            lastName = binding.loginLastNameEditText.text.toString()

            if ( firstName!= "" &&  lastName!= ""){

                if (imageBitmap == null) {
                    registerUserToDatabase(firstName, lastName, phoneNumber, countryName, "", "")
                }else{
                    uploadImageToFirebaseAndRegisterUser(imageBitmap)
                }
            }else{
                Toast.makeText(this, "Empty Fields", Toast.LENGTH_SHORT).show()
            }

        }
    }

    var imageUri: Uri? = null
    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data

            imageUri = data?.data
            binding.circleImageView.setImageURI(imageUri)
            binding.circleImageView.invalidate()
            val dr = binding.circleImageView.drawable
            imageBitmap = dr.toBitmap()
            Glide.with(this).load(imageUri).into(binding.circleImageView)
        }
    }

    private fun uploadImageToFirebaseAndRegisterUser(bitmap: Bitmap?){
        val ref = getUserDpReference(FirebaseAuth.getInstance().uid.toString())
        ref.putBytes(bitmapToByteArray(bitmap!!)).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                val dpUrl = it.toString()
                registerUserToDatabase(firstName, lastName, phoneNumber, countryName, dpUrl, "")
            }
        }.removeOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val newBitmap = ImageResizer.generateThumb(bitmap, 250000)
        val stream = ByteArrayOutputStream()
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)

        return stream.toByteArray()
    }


    private fun registerUserToDatabase(firstName: String, lastName: String, phoneNumber: String, countryName: String, dpUrl: String, notificationToken: String){
        val user = UserItem(firstName, lastName, FirebaseAuth.getInstance().uid.toString(), phoneNumber, countryName, dpUrl, notificationToken, "", System.currentTimeMillis(), 0, 0)
        val ref = getUserReference(FirebaseAuth.getInstance().uid.toString())
        ref.setValue(user).addOnSuccessListener {
            redirectToMainActivity()
            Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun redirectToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}