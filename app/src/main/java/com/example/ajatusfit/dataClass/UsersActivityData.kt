package com.example.ajatusfit.dataClass


import com.google.gson.annotations.SerializedName

data class UsersTokens(
    @SerializedName("name")
    val name: String?,
    @SerializedName("steps")
    val steps: Int?,
    @SerializedName("timestamp")
    val timestamp: String?
)