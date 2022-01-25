package com.faridwaid.banksampahmliriprowo.admin

// Untuk menampung identitas dari data admin
data class Admin(
    val username : String,
    val email : String,
    val password : String,
){
    constructor(): this("","", ""){

    }
}