package com.example.doanmess
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.createuiproject.MainChat

class AllChatFra : Fragment() {
    // TODO: Rename and change types of parameters
    private var list: MutableList<DataMess> = mutableListOf()
    lateinit var atvtContext : Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list = mutableListOf(
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
        list.add(DataMess(R.drawable.avatar_placeholder_allchat, "Nguyễn Văn P", "Hi", "10:00", false, true))
        atvtContext = requireActivity()
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

        view.findViewById<Button>(R.id.button).setOnClickListener({
            val intent = Intent(atvtContext, CreateGroup::class.java)
            startActivity(intent)
        })
        return view
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            AllChatFra().apply {
            }
    }
}