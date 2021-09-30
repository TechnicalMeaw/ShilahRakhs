package com.coprotect.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.annotation.Nullable
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import com.coprotect.myapplication.R
import com.coprotect.myapplication.adapter.AdapterPost
import com.coprotect.myapplication.constants.DatabaseLocations
import com.coprotect.myapplication.firebaseClasses.PostItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class HomeFragment : Fragment() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var recyclerView: RecyclerView
    lateinit var postList: MutableList<PostItem>
    lateinit var adapterPost: AdapterPost

    fun HomeFragment() {
        // Required empty public constructor
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        recyclerView = view.findViewById(R.id.postRecyclerView)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()

        loadPost()

        return view
    }

    private fun loadPost() {
        //val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        val ref : DatabaseReference = DatabaseLocations.getAllPostReference()

        ref.addValueEventListener(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (ds in snapshot.children) {
                    val post: PostItem? = ds.getValue(PostItem::class.java)
                    postList.add(post!!)

                    adapterPost = AdapterPost(activity!!,postList)
                    recyclerView.adapter = adapterPost
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun checkUserStatus() {
        val user = firebaseAuth.currentUser
        if (user != null) {

//            mProfile.setText(user.getEmail());
        } else {
            startActivity(Intent(activity, MainActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
}
