package com.faridwaid.banksampahmliriprowo

// Untuk menampung identitas dari data users
data class Users(
    val id : String,
    val username : String,
    val email : String,
    val photoProfil : String,
    val jumlahSetoran : Int,
    val jumlahPenarikan : Int,
    val saldo : Long,
    val token : String
){
    constructor(): this("","", "", "", 0, 0, 0, ""){

    }
}