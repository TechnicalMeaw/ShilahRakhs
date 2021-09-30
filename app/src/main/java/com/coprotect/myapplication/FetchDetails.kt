package com.coprotect.myapplication

import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getUserReference
import com.coprotect.myapplication.firebaseClasses.UserItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FetchDetails {
    companion object{
        fun fetchUserDetails(userId: String){

            getUserReference(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val user = snapshot.getValue(UserItem::class.java)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }
}