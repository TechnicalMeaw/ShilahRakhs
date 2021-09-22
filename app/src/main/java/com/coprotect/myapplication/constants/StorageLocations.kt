package com.coprotect.myapplication.constants

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class StorageLocations {
    companion object{
        private const val profileImageLocation = "/dp"

        fun getUserDpReference(userId: String): StorageReference {
            val filename = UUID.randomUUID().toString()
            return FirebaseStorage.getInstance().getReference(profileImageLocation).child(userId).child(filename)
        }
    }
}