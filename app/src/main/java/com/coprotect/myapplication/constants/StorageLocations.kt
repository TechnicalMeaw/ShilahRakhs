package com.coprotect.myapplication.constants

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class StorageLocations {
    companion object{
        private const val profileImageLocation = "/dp"
        private const val postLocation = "/Posts"

        fun getUserDpStorageReference(userId: String): StorageReference {
            val filename = UUID.randomUUID().toString()
            return FirebaseStorage.getInstance().getReference(profileImageLocation).child(userId).child(filename)
        }

        fun getPostStorageReference(): StorageReference {
            return FirebaseStorage.getInstance().getReference(postLocation)
        }

        fun getStorageReferenceByUrl(url: String): StorageReference {
            return FirebaseStorage.getInstance().getReferenceFromUrl(url)
        }
    }
}