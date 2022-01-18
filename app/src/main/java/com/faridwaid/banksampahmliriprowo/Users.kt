package com.faridwaid.banksampahmliriprowo

// Untuk menampung identitas dari data users
data class Users(
    val id : String,
    val username : String,
    val email : String,
    val telepon : String,
    val jumlahSetoran : Int,
    val jumlahPenarikan : Int,
    val saldo : Int,
    val token : String
){
    constructor(): this("","", "", "", 0, 0, 0, ""){

    }
}