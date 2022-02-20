package com.faridwaid.banksampahmliriprowo.admin

// Untuk menampung identitas dari data Daftar PenjualanSampah
data class PenjualanSampah (
    val idPenjualan : String,
    val nameBuyer: String,
    val telpBuyer: String,
    val datePenjualan: String,
    val idSampah : String,
    val weightSampah : Int,
    val priceSampah : String,
    val total: String
){
    constructor(): this("","", "", "", "", 0, "", ""){
    }
}