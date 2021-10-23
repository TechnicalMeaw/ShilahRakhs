package com.coprotect.myapplication.recyclerViewAdapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.coprotect.myapplication.R
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.squareup.picasso.Picasso

class ExploreRVAdapter(private val context: Context, private val listener: ImageExploreListener): RecyclerView.Adapter<ExploreRVAdapter.PostViewHolder>() {

    private val allPosts = ArrayList<PostItem>()

    inner class PostViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val postImage : ImageView = itemView.findViewById(R.id.postImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val viewHolder = PostViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_items_explore, parent, false))
        //Handle Onclick
        viewHolder.postImage.setOnClickListener { listener. onClickedPost(allPosts[viewHolder.bindingAdapterPosition])}

        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentPost = allPosts[position]

        try {
            Picasso.get().load(currentPost.postMediaUrl).into(holder.postImage)
        }catch (e: Exception){
            e.stackTrace
        }

    }

    override fun getItemCount(): Int {
        return allPosts.size
    }

    fun updatePosts(postList: List<PostItem>){
        allPosts.clear()
        allPosts.addAll(postList)
        notifyDataSetChanged()
    }
}

interface ImageExploreListener{
    fun onClickedPost(currentPost: PostItem)
}