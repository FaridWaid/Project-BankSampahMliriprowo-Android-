package com.faridwaid.banksampahmliriprowo.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DaftarPenjualanSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var rvDaftarPenjualan: RecyclerView
    private lateinit var daftarPenjualanList: ArrayList<PenjualanSampah>
    private lateinit var adapter: PenjualanSampahAdapter
    private lateinit var etSearch: EditText
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_penjualan_sampah_admin)

        // Mendefinisikan variabel "rvDaftarPenjualan" yang berupa recyclerview
        rvDaftarPenjualan = findViewById(R.id.rvDaftarPenjualanSampah)
        rvDaftarPenjualan.setHasFixedSize(true)
        rvDaftarPenjualan.layoutManager = LinearLayoutManager(this)

        // Memasukkan data PenjualanSampah ke dalam "daftarPenjualanList" sebagai array list
        daftarPenjualanList = arrayListOf<PenjualanSampah>()
        // Memanggil fungsi "showListPenjualan" yang digunakan untuk menampilkan recyclerview dari data yang sudah ada,
        // pada list
        showListPenjualan()

        // Mendefinisikan variabel "etSearch", ketika memasukkan query ke etSearch maka akan memanggil fungsi filter
        // terdapat closeSearch digunakan untuk menghapus query/inputan
        // overridePendingTransition digunakan untuk animasi dari intent
        etSearch = findViewById(R.id.et_search)
        etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                //after the change calling the method and passing the search input
                val closeSearch: ImageView = findViewById(R.id.closeSearch)
                closeSearch.visibility = View.VISIBLE
                filter(editable.toString())
                closeSearch.setOnClickListener {
                    editable?.clear()
                    closeSearch.visibility = View.INVISIBLE
                }
            }

        })

        // Ketika "plusButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val plusButton: FloatingActionButton = findViewById(R.id.buttonPlusPenjualan)
        plusButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke TambahDaftarPenjualanSampahAdminActivity
            startActivity(Intent(this, TambahDaftarPenjualanSampahAdminActivity::class.java))
            overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
        }

        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke activity sebelumnya
            onBackPressed()
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }

    }

    // Membuat fungsi "showListPenjualan" yang digunakan untuk menampilkan data dari database ke dalam,
    // recyclerview
    private fun showListPenjualan() {
        reference = FirebaseDatabase.getInstance().getReference("daftarpenjualan")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    daftarPenjualanList.clear()
                    for (i in snapshot.children){
                        val penjualan = i.getValue(PenjualanSampah::class.java)
                        if (penjualan != null){
                            daftarPenjualanList.add(penjualan)
                        }
                    }

                    adapter = PenjualanSampahAdapter(daftarPenjualanList, this@DaftarPenjualanSampahAdminActivity)
                    rvDaftarPenjualan.adapter = adapter

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    // Membuat fungsi "filter" yang digunakan untuk memfilter recyclerView,
    private fun filter(text: String) {
        // mendefiniskan variabel "filteredNames" yang berisi arraylist dari data PenjualanSampah
        val filteredNames = ArrayList<PenjualanSampah>()
        // setiap data yang ada pada daftarPenjualanList disamakan dengan filteredNames
        daftarPenjualanList.filterTo(filteredNames) {
            // jika idSampah sama dengan text input yang dimasukkan oleh user
            it.idSampah.toLowerCase().contains(text.toLowerCase())
        }
        // maka akan memenaggil fungsi filterlist dari adapter dan hanyak menampilkan data yang cocok
        adapter!!.filterList(filteredNames)
    }

    // Membuat fungsi "animationToTop" yang berisi animasi ketika pinday activity
    // fungsi ini digunakan pada adapter
    fun animationToTop() {
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

}