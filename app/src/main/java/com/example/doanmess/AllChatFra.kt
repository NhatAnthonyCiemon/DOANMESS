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
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn A", message = "Hello", time = "10:00", status = true),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn B", message = "Hi", time = "11:00", status = false),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn C", message = "Hey", time = "12:00", status = true),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn D", message = "Haha", time = "13:00", status = false),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn E", message = "Hehe", time = "14:00", status = true),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn F", message = "Hoho", time = "15:00", status = false),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn G", message = "Huhu", time = "16:00", status = true),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn H", message = "Haha", time = "17:00", status = false),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn I", message = "Hehe", time = "18:00", status = true),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn K", message = "Hoho", time = "19:00", status = false),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn L", message = "Huhu", time = "20:00", status = true),
            DataMess(avatar = R.drawable.avatar_placeholder_allchat, name = "Nguyễn Văn M", message = "Haha", time = "21:00", status = false)
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