package com.coprotect.myapplication.recyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coprotect.myapplication.R
import com.coprotect.myapplication.firebaseClasses.PostItem

class ActivityPostRVAdapter( private val context: Context, val listener: ActivityPostListener) :
    RecyclerView.Adapter<ActivityPostRVAdapter.ActivityViewHolder>() {

    private val allActivityPost = ArrayList<PostItem>()

    inner class ActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val activityImageView: ImageView = itemView.findViewById(R.id.activityImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val viewHolder = ActivityViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.activity_item, parent, false))
        viewHolder.activityImageView.setOnClickListener { listener.onClickedActivityPost(allActivityPost[viewHolder.bindingAdapterPosition], viewHolder.bindingAdapterPosition) }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val currentActivityPost = allActivityPost[position]
        try {
            Glide.with(context).load(currentActivityPost.postMediaUrl).into(holder.activityImageView)
        }catch (e: Exception){
            e.stackTrace
        }
    }

    override fun getItemCount(): Int {
        return allActivityPost.size
    }

    fun updateActivityPosts(postList: List<PostItem>){
        allActivityPost.clear()
        allActivityPost.addAll(postList)
        notifyDataSetChanged()
    }

}

interface ActivityPostListener{
    fun onClickedActivityPost(activityPost: PostItem, position: Int)
}