package com.faridwaid.banksampahmliriprowo.admin

// Untuk menampung identitas dari data Daftar Sampah
data class DaftarSampah (
    val nameSampah : String,
    val priceSampah : String,
    val descriptionSampah : String,
    val stockSampah : Int,
    val photoSampah: String
){
    constructor(): this("","", "", 0, ""){

    }
}