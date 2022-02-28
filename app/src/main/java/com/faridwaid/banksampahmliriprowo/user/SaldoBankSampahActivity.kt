package com.faridwaid.banksampahmliriprowo.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.admin.BankSampah
import com.google.firebase.database.*
import java.text.DecimalFormat
import java.text.NumberFormat

class SaldoBankSampahActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var textSaldo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saldo_bank_sampah)

        // Mendefinisikan variabel yang berisi view
        textSaldo = findViewById(R.id.saldoBank)

        // Mendefinisikan reference
        reference = FirebaseDatabase.getInstance().getReference("banksampah").child("saldobank")

        // Mengambil data saldo bank sampah dengan reference dan dimasukkan kedalam view (text,etc)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bank = snapshot.getValue(BankSampah::class.java)!!
                val formatter: NumberFormat = DecimalFormat("#,###")
                val myNumber = bank?.totalSaldo
                val formattedNumber: String = formatter.format(myNumber)
                textSaldo.setText("Rp. ${formattedNumber}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke LoginActivity
            onBackPressed()
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }

    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

}