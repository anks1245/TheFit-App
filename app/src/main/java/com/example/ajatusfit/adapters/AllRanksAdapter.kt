package com.example.ajatusfit.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ajatusfit.R
import com.example.ajatusfit.dataClass.UserActivityDataModel

class AllRanksAdapter(var arrayList: ArrayList<UserActivityDataModel>):RecyclerView.Adapter<AllRanksAdapter.AllRankViewHolder>() {
    inner class AllRankViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val positionTextView:TextView = itemView.findViewById(R.id.rank_position)
        val nameTextView:TextView = itemView.findViewById(R.id.name_textView)
        val stepsTextView:TextView = itemView.findViewById(R.id.steps)
        val step_text:TextView = itemView.findViewById(R.id.steps_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllRankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rank_list_item,parent,false)
        return AllRankViewHolder(view)
    }

    override fun onBindViewHolder(holder: AllRankViewHolder, position: Int) {
        holder.positionTextView.text = "#${arrayList[position].position.toString()}"
        holder.nameTextView.text = arrayList[position].name
        holder.stepsTextView.text = arrayList[position].steps.toString()
        if(arrayList[position].from == "main" && arrayList[position].pos != -1){
            holder.positionTextView.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP,16F)
                setTextColor(Color.BLACK)
                setTypeface(null,Typeface.BOLD)
            }
            holder.nameTextView.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP,16F)
                setTextColor(Color.BLACK)
                setTypeface(null,Typeface.BOLD)
            }
            holder.stepsTextView.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP,16F)
                setTextColor(Color.BLACK)
                setTypeface(null,Typeface.BOLD)
            }
            holder.step_text.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP,14F)
                setTextColor(Color.BLACK)
                setTypeface(null,Typeface.BOLD)
            }
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}