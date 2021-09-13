package com.example.firebasechatapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserClass (val username : String, val email : String, val uid : String) : Parcelable{

    constructor(): this("","","")

}