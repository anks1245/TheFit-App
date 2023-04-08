package com.example.ajatusfit.dataClass

class UserActivityDataModel(
    var position: Int?,
//    @SerializedName("name")
    var name: String?= "",
//    @SerializedName("steps")
    var steps: Int?= 0,
//    @SerializedName("timestamp")
    var timestamp: String?="",

    var from:String?="",

    var pos:Int?) {
}