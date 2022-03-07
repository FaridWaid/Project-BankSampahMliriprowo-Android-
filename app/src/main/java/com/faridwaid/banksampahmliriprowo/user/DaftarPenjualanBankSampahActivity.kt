package com.faridwaid.banksampahmliriprowo.user

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.admin.DaftarSampah
import com.faridwaid.banksampahmliriprowo.admin.PenjualanSampah
import com.faridwaid.banksampahmliriprowo.admin.PenjualanSampahAdapter
import com.faridwaid.banksampahmliriprowo.admin.TambahDaftarPenjualanSampahAdminActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import java.text.DecimalFormat
import java.text.NumberFormat

class DaftarPenjualanBankSampahActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var rvDaftarPenjualan: RecyclerView
    private lateinit var daftarPenjualanList: ArrayList<PenjualanSampah>
    private lateinit var adapter: DaftarPenjualanBankSampahAdapter
    private lateinit var etSearch: EditText
    private lateinit var textCountSampah: TextView
    private lateinit var textCountTotal: TextView
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_penjualan_bank_sampah)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel yang berisi view
        textCountSampah = findViewById(R.id.countSampah)
        textCountTotal = findViewById(R.id.countTotal)

        // Mendifinisikan reference
        reference = FirebaseDatabase.getInstance().getReference("daftarpenjualan")

        // Mengambil data count jumlah sampah dan total saldo dengan reference dan dimasukkan kedalam view (text,etc)
        reference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var countTotalSampah = 0
                var countTotal: Long = 0
                for (i in snapshot.children){
                    val sampah = i.getValue(PenjualanSampah::class.java)
                    countTotalSampah += sampah?.weightSampah!!
                    countTotal += sampah?.total.toLong()!!
                }
                val formatter: NumberFormat = DecimalFormat("#,###")
                val price = countTotal
                val formattedNumber: String = formatter.format(price)
                textCountSampah.setText("Total sampah yang terjual: $countTotalSampah(KG)")
                textCountTotal.setText("Total uang yang diperoleh: Rp. $formattedNumber")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

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
        etSearch.addTextChangedListener(object : TextWatcher {
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

        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke activity sebelumnya
            onBackPressed()
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }

    }

    // Fungsi ini digunakan untuk menampilkan dialog peringatan tidak tersambung ke internet,
    // jika tetep tidak connect ke internet maka tetap looping dialog tersebut
    private fun showInternetDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.apply {
            // Menambahkan title dan pesan ke dalam alert dialog
            setTitle("PERINGATAN!")
            setMessage("Tidak ada koneksi internet, mohon nyalakan mobile data/wifi anda terlebih dahulu")
            setPositiveButton(
                "Coba lagi",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    if (!isConnected(this@DaftarPenjualanBankSampahActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: DaftarPenjualanBankSampahActivity): Boolean {
        val connectivityManager = contextActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConn != null && wifiConn.isConnected || mobileConn != null && mobileConn.isConnected
    }

    // Membuat fungsi "showListPenjualan" yang digunakan untuk menampilkan data dari database ke dalam,
    // recyclerview
    private fun showListPenjualan() {
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

                    adapter = DaftarPenjualanBankSampahAdapter(daftarPenjualanList)
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

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

}