package com.example.ajatusfit.dataClass

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

class MessageRoomModel(
    @SerializedName("name")
    var name:String?="",
    @SerializedName("token")
    var token:String?="",
    @SerializedName("message")
    var message: String?="",
    @SerializedName("timestamp")
    var timestamp: String?=""
) {
}