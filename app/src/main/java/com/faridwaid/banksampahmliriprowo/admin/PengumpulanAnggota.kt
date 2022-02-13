package com.faridwaid.banksampahmliriprowo.admin

// Untuk menampung identitas dari data Daftar PengumpulanAnggota
data class PengumpulanAnggota (
    val idPengumpulan : String,
    val datePengumpulan : String,
    val idAnggota : String,
    val idSampah : String,
    val weightSampah : Int,
    val priceSampah : String,
    val total: String
){
    constructor(): this("","", "", "", 0, "", ""){
    }
}