package com.example.doanmess

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager



class AllChatFra : Fragment() {
    // TODO: Rename and change types of parameters
    private var list: List<DataMess> = listOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = listOf(
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn A", "Hello", "10:00", true, false),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn B", "Hi", "10:00", false, true),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn C", "Hello", "10:00", false, true),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn D", "Hi", "10:00", false, false),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn E", "Hello", "10:00", false, true),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn F", "Hi", "10:00", true, false),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn G", "Hello", "10:00", false, false),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn H", "Hi", "10:00", false, true),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn I", "Hello", "10:00", false, true),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn J", "Hi", "10:00", true, false),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn K", "Hello", "10:00", false, true),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn L", "Hi", "10:00", false, true),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn M", "Hello", "10:00", false, false),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn N", "Hi", "10:00", true, true),
            DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn O", "Hello", "10:00", false, false),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_all_chat, container, false)
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.RVChat_AllChat)
        val adapter = Chat_AllChatAdapter(list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        return view
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            AllChatFra().apply {
            }
    }
}