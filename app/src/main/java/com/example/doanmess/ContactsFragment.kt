package com.example.doanmess

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ContactsFragment : Fragment() {

    private var list: MutableList<Contact> = mutableListOf()
    lateinit var recyclerView: RecyclerView
    lateinit var searchBtn : ImageButton
    lateinit var searchFilter: EditText
    lateinit var toAddFriendBtn: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var userStatusListener: ValueEventListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
        list = mutableListOf(
            Contact("5sOWGPgonbafPOPh2weIwvcP0wK2",R.drawable.avatar_placeholder_allchat, "Nguyễn Văn C", false),
            Contact("c33ebNdc6rStVchv3ovFalNOxDh2" ,R.drawable.avatar_placeholder_allchat, "conchocuaduynhan", false),
            Contact("vwAUzgbCSNWNq4a48xoM2zZVCcH3",R.drawable.avatar_placeholder_allchat, "conglongcuaduylan", false),

            /*
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn D", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn E", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn F", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn G", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn H", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn I", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn J", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn K", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn L", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn M", true),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn N", false),
            Contact(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn O", true),*/
        )
        // Listen for changes in online status
        list.forEach { contact ->

            val userStatusRef = database.getReference("users/${contact.id}/online")
            userStatusListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isOnline = snapshot.getValue(Boolean::class.java) ?: false
                    contact.online = isOnline
                    recyclerView.adapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                }
            }
            userStatusRef.addValueEventListener(userStatusListener)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view:View = inflater.inflate(R.layout.fragment_contacts, container, false)
        recyclerView=view.findViewById(R.id.recyclerViewContact)
        val adapter = ContactsAdapter(list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        searchBtn = view.findViewById(R.id.search_btn)
        searchFilter = view.findViewById(R.id.filter_search)
        toAddFriendBtn = view.findViewById(R.id.toAddFriendBtn)
        toAddFriendBtn.setOnClickListener {
            val intent = Intent(activity, AddFriend::class.java)
            startActivity(intent)
        }


        searchBtn.setOnClickListener{
            val filter = searchFilter.text.toString()
            if(filter.isEmpty()){
                adapter.changeList(list)

            }
            else{
                val filterLowerCase = filter.toLowerCase()
                val filteredList = list.filter { it.name.toLowerCase().contains(filterLowerCase) }
                adapter.changeList(filteredList)
            }

        }
        return view
    }
    override fun onDestroy() {
        super.onDestroy()
        // Remove the listener to avoid memory leaks
        list.forEach { contact ->
            val userStatusRef = database.getReference("users/${contact.id}/online")
            userStatusRef.removeEventListener(userStatusListener)
        }
    }
    fun focusSearch() {
        if(::searchFilter.isInitialized){
            searchFilter.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchFilter, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            ContactsFragment().apply {
            }
    }
}