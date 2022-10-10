package com.example.ajatusfit.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ajatusfit.R
import com.example.ajatusfit.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root

        val week = Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.DAY_OF_WEEK)
        val date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.DAY_OF_MONTH)
        val day = getDays(week)

        binding.date.text = "$day, $date"

        binding.profile.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }
        binding.viewAll.setOnClickListener{
            startActivity(Intent(this,AllRanksActivity::class.java))
        }

        setContentView(root)
    }

    private fun getDays(day_of_week: Int): String {
        return when(day_of_week){
            1->{
                "Sun"
            }
            2->{
                "Mon"
            }
            3->{
                "Tue"
            }
            4->{
                "Wed"
            }
            5-> {
                "Thu"
            }
            6->{
                "Fri"
            }
            7->{
                "Sat"
            }
            else->{
                "..."
            }
        }
    }

}