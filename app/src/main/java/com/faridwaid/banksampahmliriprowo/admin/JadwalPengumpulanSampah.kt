package com.faridwaid.banksampahmliriprowo.admin

// Untuk menampung identitas dari data JadwalPengumpulanSampah
data class JadwalPengumpulanSampah (
    val hari : String,
    val type : String,
){
    constructor(): this("",""){

    }
}