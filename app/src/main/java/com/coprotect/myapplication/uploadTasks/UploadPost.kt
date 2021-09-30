package com.coprotect.myapplication.uploadTasks

import android.graphics.Bitmap
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getAllPostReference
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getUserPostReference
import com.coprotect.myapplication.constants.StorageLocations.Companion.getPostStorageReference
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.coprotect.myapplication.imageOperations.ImageConversion.Companion.bitmapToByteArray
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class UploadPost {
    companion object{
        fun uploadPostImage(bitmap: Bitmap, caption: String){
            val filename = UUID.randomUUID().toString()
            val ref = getPostStorageReference().child(filename)

            ref.putBytes(bitmapToByteArray(bitmap, 500000)).addOnSuccessListener {
                updatePostToDatabase(FirebaseAuth.getInstance().uid.toString(), filename, ref.downloadUrl.toString(), caption)
            }
        }

        private fun updatePostToDatabase(userId: String, postId: String, postUrl: String, postCaption: String){
            val currentTime = System.currentTimeMillis()
            val post = PostItem(userId, postId, "Image", postUrl, postCaption, 0, 0, currentTime)

            getAllPostReference().child(postId).setValue(post).addOnSuccessListener {
                updatePostToUserProfile(postId, post)
            }
        }

        private fun updatePostToUserProfile(postId: String, postItem: PostItem){
            getUserPostReference(FirebaseAuth.getInstance().uid.toString()).child(postId).setValue(postItem)
        }
    }
}