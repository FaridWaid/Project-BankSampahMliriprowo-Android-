package com.faridwaid.banksampahmliriprowo.admin

// Untuk menampung identitas dari data Saldo BankSampah
data class BankSampah (
    val totalSaldo: Long
){
    constructor(): this(0){
    }
}