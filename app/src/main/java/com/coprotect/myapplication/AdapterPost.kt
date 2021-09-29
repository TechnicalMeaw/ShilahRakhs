package com.coprotect.myapplication.adapter

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coprotect.myapplication.R
import com.coprotect.myapplication.constants.DatabaseLocations
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.*

class AdapterPost(private val context: Context, postList: MutableList<PostItem>): RecyclerView.Adapter<AdapterPost.myHolder>() {

    private var myUid : String = FirebaseAuth.getInstance().currentUser!!.uid
    private var likeRef : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Likes")
    private var postRef = DatabaseLocations.getAllPostReference()

    //button is not liked at first
    private var mProcessLike : Boolean = false

    private var list: List<PostItem>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.row_allpost,parent,false)
        return myHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: myHolder, position: Int) {

        val uid = list[position].uid
        //val UEmail: String? = postList[position]!!.uEmail
        val uName: String? = list[position].uName
        val pDerscription: String? = list[position].pDescription
        val uDp: String? = list[position].uDp
        val pTitle: String? = list[position].pTitle
        val pId: String? = list[position].pId
        val pImage: String? = list[position].pImage
        val pTimeStamp: String? = list[position].pTime
        val pLikes: String? = list[position].pLikes
        val pComments: String? = list[position].pComments

        val calendar: Calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = pTimeStamp!!.toLong()
        val pTime: String = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString()

        holder.uNameTv.text = uName
        holder.pTimeTv.text = pTime
        holder.pTitletv.text = pTitle
        holder.pDescriptionTv.text = pDerscription
        holder.pLikeTv.text = "$pLikes Likes"
        holder.pCommentsTv.text = "$pComments Comments"
        setLiked(holder, pId.toString())

//        Picasso.get().load(uDp).placeholder(R.drawable.ic_person).into(holder.uPictureIv)
//        Picasso.get().load(pImage).fit().into(holder.pImageIv)

        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_person).into(holder.uPictureIv)
        } catch (e: Exception) {
        }
        if (pImage == "noImage") {
            holder.pImageIv.visibility = View.GONE
        } else {
            holder.pImageIv.visibility = View.VISIBLE
            try {
                Picasso.get().load(pImage).fit().into(holder.pImageIv)
            } catch (e: Exception) {
            }
        }

        /*holder.likeBtn.setOnClickListener {
            val pLikes: Int = list[position].pLikes!!.toInt()
            mProcessLike = true
            val postIde: String? = list[position]!!.pId
            likeRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (mProcessLike) {
                        if (snapshot.child(postIde!!).hasChild(myUid)) {
                            //already liked so for unlike
                            postRef.child(postIde).child("pLikes").setValue("" + (pLikes - 1))
                            likeRef.child(postIde).child(myUid).removeValue()
                            mProcessLike = false
                        } else {
                            //unliked so like it
                            postRef.child(postIde).child("pLikes").setValue("" + (pLikes + 1))
                            likeRef.child(postIde).child(myUid).setValue("Liked")
                            mProcessLike = false
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }*/
    }

    inner class myHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        var uPictureIv: ImageView = itemView.findViewById(R.id.uPictureIv)
        var pImageIv: ImageView = itemView.findViewById(R.id.pImageIv)
        var uNameTv: TextView = itemView.findViewById(R.id.uNameTv)
        var pTitletv: TextView = itemView.findViewById(R.id.pTitleTv)
        var pTimeTv: TextView = itemView.findViewById(R.id.pTimeTv)
        var pDescriptionTv: TextView = itemView.findViewById(R.id.pDescriptionTv)
        var pLikeTv: TextView = itemView.findViewById(R.id.pLikeTv)
        var pCommentsTv: TextView = itemView.findViewById(R.id.pCommentsTv)
        val likeBtn: ImageView = itemView.findViewById(R.id.likeBtn)
        //var mProcessLike: Boolean = false

        init {
            likeBtn.setOnClickListener {
                val position = adapterPosition
                mProcessLike = true
                val myUid: String = FirebaseAuth.getInstance().currentUser!!.uid
                val postIde: String? = list[position].pId
                val pLikes: Int = list[position].pLikes!!.toInt()
                likeRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (mProcessLike) {
                            if (snapshot.child(postIde!!).hasChild(myUid)) {
                                //already liked so for unlike
                                postRef.child(postIde).child("pLikes").setValue("" + (pLikes!! - 1))
                                likeRef.child(postIde).child(myUid).removeValue()
                                mProcessLike = false
                            } else {
                                //unliked so like it
                                postRef.child(postIde).child("pLikes").setValue("" + (pLikes!! + 1))
                                likeRef.child(postIde).child(myUid).setValue("Liked")
                                mProcessLike = false
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })


            }
        }
    }

    private fun setLiked(holder: AdapterPost.myHolder, postKey: String) {
        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(postKey).hasChild(myUid)) {
                    holder.likeBtn.setImageResource(R.drawable.liked)
                    //holder.likeBtn.setBackgroundResource(R.drawable.liked)
                } else {
                    holder.likeBtn.setImageResource(R.drawable.like)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    init {
        this.list = postList
    }
}