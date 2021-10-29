package com.coprotect.myapplication.recyclerViewAdapters

import android.animation.Animator
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coprotect.myapplication.R
import com.coprotect.myapplication.conversion.TimeConversion.getDate
import com.coprotect.myapplication.constants.DatabaseLocations
import com.coprotect.myapplication.constants.DatabaseLocations.Companion.getPostLikeReference
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.coprotect.myapplication.firebaseClasses.UserItem
import com.coprotect.myapplication.listeners.DoubleClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.logging.Handler

class PostRVAdapter(private val context: Context, private val listener: PostListener): RecyclerView.Adapter<PostRVAdapter.PostViewHolder>() {

    private val allPosts = ArrayList<PostItem>()

    inner class PostViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val dp: CircleImageView = itemView.findViewById(R.id.postDpCircleImageView)
        val name : TextView = itemView.findViewById(R.id.postNameTextView)
        val timeStamp : TextView = itemView.findViewById(R.id.postTimeStamp)
        val postImage : ImageView = itemView.findViewById(R.id.postImageView)
        val captionTextView : TextView = itemView.findViewById(R.id.postCaptionTextView)
        val likeTextView: TextView = itemView.findViewById(R.id.postLikeTextView)
        val commentTextView : TextView = itemView.findViewById(R.id.postCommentTextView)
        val likeBtn : ImageButton = itemView.findViewById(R.id.postLikeBtn)
        val commentBtn : ImageButton = itemView.findViewById(R.id.postCommentBtn)
        val shareBtn : ImageButton = itemView.findViewById(R.id.postShareBtn)
        val loveAnimation: LottieAnimationView = itemView.findViewById(R.id.loveAnimation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val viewHolder = PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_allpost, parent, false))
        /**
         * Handle Onclick
         */
        viewHolder.likeBtn.setOnClickListener { listener.onLikeButtonClicked(allPosts[viewHolder.bindingAdapterPosition]) }

        viewHolder.commentBtn.setOnClickListener { listener.onCommentButtonClicked(allPosts[viewHolder.bindingAdapterPosition]) }

        viewHolder.dp.setOnClickListener { listener.onClickedProfile(allPosts[viewHolder.bindingAdapterPosition]) }

        viewHolder.name.setOnClickListener { listener.onClickedProfile(allPosts[viewHolder.bindingAdapterPosition]) }

        viewHolder.shareBtn.setOnClickListener { listener.onShareButtonClicked(allPosts[viewHolder.bindingAdapterPosition]) }

        viewHolder.postImage.setOnClickListener(object : DoubleClickListener(){
            override fun onDoubleClick(v: View) {
                // Play animation
                viewHolder.loveAnimation.playAnimation()
                listener.onPostDoubleClicked(allPosts[viewHolder.bindingAdapterPosition])
            }
        })

        viewHolder.loveAnimation.addAnimatorListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator?) {
                viewHolder.loveAnimation.visibility = View.VISIBLE
                viewHolder.loveAnimation.elevation = 10F
            }

            override fun onAnimationEnd(p0: Animator?) {
                viewHolder.loveAnimation.elevation = -1F
            }

            override fun onAnimationCancel(p0: Animator?) {
                Log.e("Animation:","cancel")
            }

            override fun onAnimationRepeat(p0: Animator?) {
                Log.e("Animation:","cancel")
            }

        })

        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = allPosts[position]
        val options: RequestOptions = RequestOptions()
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)


        try {
            // Load Post Image
//            Glide.with(context).load(currentPost.postMediaUrl).into(holder.postImage)
            /**
             * Used Picasso instead of Glide
             * as having issues with
             * imageMaxHeight
             */
            Picasso.get().load(currentPost.postMediaUrl).into(holder.postImage)

            /**
             * Fetch User DP
             * And Profile Name
             */
            DatabaseLocations.getUserReference(currentPost.userId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val user = snapshot.getValue(UserItem::class.java)
                            if (user != null){
                                Glide.with(context).load(user.profilePictureUrl).apply(options).into(holder.dp)
                                holder.name.text = "${user.firstName} ${user.lastName}"
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            /*<=============>*/

            /**
             * Handle Caption
             */
            if (currentPost.postCaption != ""){
                holder.captionTextView.visibility = View.VISIBLE
                holder.captionTextView.text = currentPost.postCaption
            }else{
                holder.captionTextView.visibility = View.GONE
            }

            holder.timeStamp.text = getDate(currentPost.postTimeInMillis)
            holder.likeTextView.text = currentPost.likeCount.toString() + " Likes"
            holder.commentTextView.text = currentPost.commentCount.toString() + " Comments"

            /**
             * Check Liked or Not
             */
            getPostLikeReference(currentPost.postId).addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChild(FirebaseAuth.getInstance().uid.toString())){
                        try {
                            holder.likeBtn.setBackgroundResource(R.drawable.love_filled)

                        }catch (e: Exception){
                            e.stackTrace
                        }
                    }else{
                        holder.likeBtn.setBackgroundResource(R.drawable.love_hollow)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            /*<============>*/
        }catch (e: Exception){
            e.stackTrace
        }

    }

    override fun getItemCount(): Int {
        return allPosts.size
    }

    fun addPost(postItem: PostItem){
        allPosts.add(postItem)
        notifyDataSetChanged()
    }

    fun updatePosts(postList: List<PostItem>){
        allPosts.clear()
        allPosts.addAll(postList)
        notifyDataSetChanged()
    }
}

interface PostListener{
    fun onLikeButtonClicked(currentPost: PostItem)
    fun onCommentButtonClicked(currentPost: PostItem)
    fun onClickedProfile(currentPost: PostItem)
    fun onShareButtonClicked(currentPost: PostItem)
    fun onPostDoubleClicked(currentPost: PostItem)
}