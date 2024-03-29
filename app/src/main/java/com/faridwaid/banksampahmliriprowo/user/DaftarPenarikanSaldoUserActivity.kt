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
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.DaftarPenarikanSaldoAdminAdapter
import com.faridwaid.banksampahmliriprowo.admin.PenarikanSaldo
import com.faridwaid.banksampahmliriprowo.admin.TambahDaftarPenarikanSaldoAdminActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.DecimalFormat
import java.text.NumberFormat

class DaftarPenarikanSaldoUserActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var rvDaftarPenarikan: RecyclerView
    private lateinit var daftarPenarikanList: ArrayList<PenarikanSaldo>
    private lateinit var adapter: DaftarPenarikanSaldoUserAdapter
    private lateinit var etSearch: EditText
    private lateinit var textCountPenarikan: TextView
    private lateinit var textCountTotal: TextView
    private lateinit var date: String
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_penarikan_saldo_user)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel yang berisi view
        textCountPenarikan = findViewById(R.id.countPenarikan)
        textCountTotal = findViewById(R.id.countTotal)

        // Mendeklarasikan variabel date
        date = ""

        // Mendefinisikan variabel "rvDaftarPenarikan" yang berupa recyclerview
        rvDaftarPenarikan = findViewById(R.id.rvDaftarPenarikanSaldo)
        rvDaftarPenarikan.setHasFixedSize(true)
        rvDaftarPenarikan.layoutManager = LinearLayoutManager(this)

        // Memasukkan data PenarikanSaldo ke dalam "daftarPenarikanList" sebagai array list
        daftarPenarikanList = arrayListOf<PenarikanSaldo>()
        // Memanggil fungsi "showListPenarikan" yang digunakan untuk menampilkan recyclerview dari data yang sudah ada,
        // pada list
        showListPenarikan()

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
                val search: ImageView = findViewById(R.id.search)
                search.setOnClickListener {
                    val tempDate = editable.toString()
                    filter(tempDate)
                }
                closeSearch.setOnClickListener {
                    editable?.clear()
                    closeSearch.visibility = View.INVISIBLE
                    showListPenarikan()
                    date = ""
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
                    if (!isConnected(this@DaftarPenarikanSaldoUserActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: DaftarPenarikanSaldoUserActivity): Boolean {
        val connectivityManager = contextActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConn != null && wifiConn.isConnected || mobileConn != null && mobileConn.isConnected
    }

    // Membuat fungsi "showListPenarikan" yang digunakan untuk menampilkan data dari database ke dalam,
    // recyclerview
    private fun showListPenarikan() {
        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        val auth = FirebaseAuth.getInstance()
        // Membuat userIdentity daru auth untuk mendapatkan userid/currrent user
        val userIdentity = auth.currentUser
        reference = FirebaseDatabase.getInstance().getReference("daftarpenarikan")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var countTotalPenarikan = 0
                    var countTotal: Long = 0
                    daftarPenarikanList.clear()
                    for (i in snapshot.children){
                        val penarikan = i.getValue(PenarikanSaldo::class.java)
                        if (penarikan != null && penarikan.idAnggota == "${userIdentity?.uid}"){
                            daftarPenarikanList.add(penarikan)
                            countTotalPenarikan += 1
                            countTotal += penarikan.totalPenarikan.toLong()
                        }
                        val formatter: NumberFormat = DecimalFormat("#,###")
                        val price = countTotal
                        val formattedNumber: String = formatter.format(price)
                        textCountPenarikan.setText("Total penarikan yang dilakukan: $countTotalPenarikan kali")
                        textCountTotal.setText("Total uang yang ditarik: Rp. $formattedNumber")
                    }

                    adapter = DaftarPenarikanSaldoUserAdapter(daftarPenarikanList)
                    rvDaftarPenarikan.adapter = adapter

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    // Membuat fungsi "filter" yang digunakan untuk memfilter recyclerView,
    private fun filter(tempDate: String) {

        val filteredNames = ArrayList<PenarikanSaldo>()
        // setiap data yang ada pada daftarPenarikanList disamakan dengan filteredNames
        daftarPenarikanList.filterTo(filteredNames) {
            // jika datePenarikan sama dengan text input yang dimasukkan oleh user
            it.datePenarikan.contains(tempDate)
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