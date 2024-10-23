package com.example.doanmess
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.createuiproject.MainChat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllChatFra : Fragment() {
    // TODO: Rename and change types of parameters
    private var list: MutableList<DataMess> = mutableListOf()
    lateinit var atvtContext : Context
    private lateinit var auth: FirebaseAuth
    private var User: FirebaseUser? =null
    val dbfirestore = Firebase.firestore
    val adapter: Chat_AllChatAdapter = Chat_AllChatAdapter(list)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        atvtContext = requireActivity()
        auth = Firebase.auth
        User = auth.currentUser


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_all_chat, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.RVChat_AllChat)
        val adapter = Chat_AllChatAdapter(list)
        adapter.setOnItemClickListener(object: Chat_AllChatAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                //code để chuyển đến màn hình chat
                val intent = Intent(atvtContext, MainChat::class.java)
                startActivity(intent)
            }
        })
        recyclerView.adapter = adapter

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)




        Firebase.database.getReference("users").child(User?.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            for(smallchildSnapshot in childSnapshot.children) {
                                val content =
                                    smallchildSnapshot.child("Content").getValue(String::class.java)

                                val recvId =
                                    smallchildSnapshot.child("RecvId").getValue(String::class.java)
                                val sendId =
                                    smallchildSnapshot.child("SendId").getValue(String::class.java)
                                val status =
                                    smallchildSnapshot.child("Status").getValue(Boolean::class.java)

                                val time = convertTimestampToString(
                                    smallchildSnapshot.child("Time").getValue(Long::class.java)
                                        ?.toLong()!!
                                )

                                if(User?.uid.toString() == sendId) {
                                    dbfirestore.collection("users").document(recvId.toString())
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                val name = document.data?.get("Name").toString()
                                                list.add(DataMess(R.drawable.avatar_placeholder_allchat, name, content.toString(), time, status!!, true))
                                                adapter.notifyDataSetChanged()
                                            } else {
                                                Log.d("exist", "No such document")
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.d("exist", "get failed with ", exception)
                                        }
                                }
                                else{
                                    dbfirestore.collection("users").document(sendId.toString())
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document != null) {
                                                val name = document.data?.get("Name").toString()
                                                list.add(DataMess(R.drawable.avatar_placeholder_allchat, name, content.toString(), time, status!!, false))
                                                adapter.notifyDataSetChanged()
                                            } else {
                                                Log.d("exist", "No such document")
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.d("exist", "get failed with ", exception)
                                        }
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(atvtContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })


        return view
    }
    fun convertTimestampToString(timestamp: Long): String {
        val sdf = SimpleDateFormat("E HH:mm", Locale.getDefault())
        val date = Date(timestamp)
        return sdf.format(date)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            AllChatFra().apply {
            }
    }
}