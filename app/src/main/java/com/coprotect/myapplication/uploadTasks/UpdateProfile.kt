package com.coprotect.myapplication.uploadTasks

import android.content.Context
import android.graphics.Bitmap
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getUserReference
import com.coprotect.myapplication.constants.StorageLocations.Companion.getStorageReferenceByUrl
import com.coprotect.myapplication.constants.StorageLocations.Companion.getUserDpStorageReference
import com.coprotect.myapplication.firebaseClasses.UserItem
import com.coprotect.myapplication.imageOperations.ImageConversion.Companion.bitmapToByteArray


class UpdateProfile {
    companion object{
        fun updateUserInfo(context: Context,
            profileImage: Bitmap?,
            userItem: UserItem,
            firstName: String,
            lastName: String,
            bio: String,
            email: String,
            website: String
        ){
            if (profileImage == null){
                updateDatabase(context, userItem, firstName,
                    lastName,
                    bio,
                    email,
                    website,
                    userItem.profilePictureUrl)
            }else{
                if (userItem.profilePictureUrl != ""){
                    getStorageReferenceByUrl(userItem.profilePictureUrl).delete().addOnSuccessListener {
                        val ref = getUserDpStorageReference(userItem.userId)
                        ref.putBytes(bitmapToByteArray(profileImage, 250000)).addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener {
                                updateDatabase(context, userItem, firstName,
                                    lastName,
                                    bio,
                                    email,
                                    website,
                                    it.toString())
                            }
                        }
                    }
                }else{
                    val ref = getUserDpStorageReference(userItem.userId)
                    ref.putBytes(bitmapToByteArray(profileImage, 250000)).addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener {
                            updateDatabase(context, userItem, firstName,
                                lastName,
                                bio,
                                email,
                                website,
                                it.toString())
                        }
                    }
                }
            }
        }

        private fun updateDatabase(context: Context, userItem: UserItem,
                                   firstName: String,
                                   lastName: String,
                                   bio: String,
                                   email: String,
                                   website: String,
                                   dpUrl: String){

            val updatedUser = UserItem(firstName, lastName, userItem.userId, userItem.phoneNumber, userItem.country, dpUrl,
                userItem.notificationToken, bio, userItem.createdAccountTime, userItem.following, userItem.followers, email, website)
            getUserReference(userItem.userId).setValue(updatedUser).addOnSuccessListener {
                println("Updated User Info")
            }
        }
    }
}