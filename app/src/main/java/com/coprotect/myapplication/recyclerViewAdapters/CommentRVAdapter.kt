package com.coprotect.myapplication.recyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coprotect.myapplication.R
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getUserReference
import com.coprotect.myapplication.conversion.TimeConversion.getDate
import com.coprotect.myapplication.firebaseClasses.CommentItem
import com.coprotect.myapplication.firebaseClasses.UserItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

class CommentRVAdapter(private val context: Context) :
    RecyclerView.Adapter<CommentRVAdapter.CommentViewHolder>() {

    private val allComments = ArrayList<CommentItem>()

    inner class CommentViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val commenterDp : CircleImageView = itemView.findViewById(R.id.commentUserCircleImageView)
        val commenterName : TextView = itemView.findViewById(R.id.commentUserName)
        val commentText : TextView = itemView.findViewById(R.id.commentText)
        val commentTimeStamp : TextView = itemView.findViewById(R.id.commentTimeStamp)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentRVAdapter.CommentViewHolder {
        val viewHolder = CommentViewHolder(LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false))
        return viewHolder
    }

    override fun onBindViewHolder(holder: CommentRVAdapter.CommentViewHolder, position: Int) {
        val currentComment = allComments[position]
        // fetching user dp and name
        val options: RequestOptions = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)

        /**
         * Fetch Commenter's
         * Dp & Name
         */
        getUserReference(currentComment.userId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val user = snapshot.getValue(UserItem::class.java)
                        if (user != null){
                            Glide.with(context).load(user.profilePictureUrl).apply(options).into(holder.commenterDp)
                            holder.commenterName.text = "${user.firstName} ${user.lastName}"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        /*---------------*/

        holder.commentText.text = currentComment.commentText
        holder.commentTimeStamp.text = getDate(currentComment.timeStamp)
    }

    override fun getItemCount(): Int {
        return allComments.size
    }

    fun updateComments(commentList: List<CommentItem>){
        allComments.clear()
        allComments.addAll(commentList.sortedBy { it.timeStamp })
        notifyDataSetChanged()
    }
}