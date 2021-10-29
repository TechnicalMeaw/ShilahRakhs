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
import com.coprotect.myapplication.firebaseClasses.FollowingItem
import com.coprotect.myapplication.listeners.FollowItemListener
import de.hdodenhof.circleimageview.CircleImageView

class FollowRVAdapter (private val context: Context, private val listener: FollowItemListener): RecyclerView.Adapter<FollowRVAdapter.FollowItemViewHolder>() {

    private val allFollowUsers = ArrayList<FollowingItem>()

    inner class FollowItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val dp : CircleImageView = itemView.findViewById(R.id.followUserCircleImageView)
        val name: TextView = itemView.findViewById(R.id.followUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowItemViewHolder {
        val viewHolder = FollowItemViewHolder(LayoutInflater.from(context).inflate(R.layout.row_follow_item, parent, false))

        viewHolder.dp.setOnClickListener { listener.onFollowUserClicked(allFollowUsers[viewHolder.bindingAdapterPosition]) }

        viewHolder.name.setOnClickListener { listener.onFollowUserClicked(allFollowUsers[viewHolder.bindingAdapterPosition]) }

        return viewHolder
    }

    override fun onBindViewHolder(holder: FollowItemViewHolder, position: Int) {
        val currentUser = allFollowUsers[position]

        val options: RequestOptions = RequestOptions()
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
        Glide.with(context).load(currentUser.profilePictureUrl).apply(options).into(holder.dp)
        holder.name.text = currentUser.firstName + " " + currentUser.lastName
    }

    override fun getItemCount(): Int {
        return allFollowUsers.size
    }

    fun updateFollowUsers(userList: List<FollowingItem>){
        allFollowUsers.clear()
        allFollowUsers.addAll(userList)
        notifyDataSetChanged()
    }
}