package com.example.ajatusfit.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.ajatusfit.R
import com.example.ajatusfit.dataClass.MessageRoomModel

class MessageAdapter(var chatArrayList: ArrayList<MessageRoomModel>,var token:String): RecyclerView.Adapter<MessageAdapter.MessageViewModel>(){
    inner class MessageViewModel(itemView: View):RecyclerView.ViewHolder(itemView){
        val name1:TextView = itemView.findViewById(R.id.nameOthers)
        val msg1:TextView = itemView.findViewById(R.id.msgOthers)
        val name2:TextView = itemView.findViewById(R.id.nameMe)
        val msg2:TextView = itemView.findViewById(R.id.msgMe)
        val chat1:ConstraintLayout = itemView.findViewById(R.id.chatOthers)
        val chat2:ConstraintLayout = itemView.findViewById(R.id.chatMe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewModel {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_listitem,parent,false)
        return MessageViewModel(view)
    }

    override fun onBindViewHolder(holder: MessageViewModel, position: Int) {
        if(token == chatArrayList[position].token){
            holder.chat2.visibility = View.VISIBLE
            holder.msg2.text = chatArrayList[position].message
            holder.name2.text = chatArrayList[position].name
//            Toast.makeText(holder.itemView.context, "${token}", Toast.LENGTH_SHORT).show()
        }else{
            holder.chat1.visibility = View.VISIBLE
            holder.msg1.text = chatArrayList[position].message
            holder.name1.text = chatArrayList[position].name
        }
    }

    override fun getItemCount(): Int {
        return chatArrayList.size
    }
}