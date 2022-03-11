package com.faridwaid.banksampahmliriprowo.admin

// Untuk menampung identitas dari data admin
data class Admin(
    val id : String,
    val username : String,
    val email : String,
    val password : String,
    val photoProfil : String
){
    constructor(): this("", "","", "", ""){

    }
}