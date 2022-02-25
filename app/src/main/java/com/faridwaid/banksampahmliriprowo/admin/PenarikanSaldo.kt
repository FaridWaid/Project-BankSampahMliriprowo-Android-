package com.faridwaid.banksampahmliriprowo.admin

// Untuk menampung identitas dari data Daftar PenarikanSaldo
data class PenarikanSaldo (
    val idPenarikan : String,
    val datePenarikan : String,
    val idAnggota : String,
    val totalPenarikan: String
){
    constructor(): this("","", "", ""){
    }
}