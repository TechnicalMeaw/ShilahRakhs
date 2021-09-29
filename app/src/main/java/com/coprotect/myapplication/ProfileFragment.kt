/*package com.coprotect.myapplication
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coprotect.myapplication.R
import com.coprotect.myapplication.constants.DatabaseLocations
import com.coprotect.myapplication.firebaseClasses.UserItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        val userName = rootView.findViewById<TextView>(R.id.userProfileName)
        val userDp = rootView.findViewById<CircleImageView>(R.id.userDpCircleImageView)
        val rView = rootView.findViewById<RecyclerView>(R.id.activityRecyclerView)
        rView.layoutManager = GridLayoutManager(rootView.context, 3)

        val ref = DatabaseLocations.getUserReference(FirebaseAuth.getInstance().uid.toString())
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value != null){
                    val item = snapshot.getValue(UserItem::class.java)
                    Glide.with(this@ProfileFragment).load(item?.profilePictureUrl).into(userDp)
                    userName.text = "${item?.firstName} ${item?.lastName}"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
        })


        return rootView
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}*/



package com.coprotect.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ProfileFragment : Fragment() {

    lateinit var fab : FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragmnt_profile, container, false)

        fab = view.findViewById(R.id.fab)

        fab.setOnClickListener {
            val i = Intent(context,AddPostActivity::class.java)
            startActivity(i)
        }

        return view
    }
}
