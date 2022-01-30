package com.faridwaid.banksampahmliriprowo.admin

// Untuk menampung identitas dari data Daftar Sampah
data class DaftarSampah (
    val nameSampah : String,
    val priceSampah : String,
    val descriptionSampah : String,
    val photoSampah: String
){
    constructor(): this("","", "", ""){

    }
}