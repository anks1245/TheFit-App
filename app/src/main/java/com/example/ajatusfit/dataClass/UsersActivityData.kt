package com.example.ajatusfit.dataClass


import com.google.gson.annotations.SerializedName

data class UsersActivityData(

    @SerializedName("name")
    var name: String?= "",

    @SerializedName("id")
    var id: String ?= "",

    @SerializedName("steps")
    var steps: Int?= 0,

    @SerializedName("timestamp")
    var timestamp: String?=""
)