package com.faridwaid.banksampahmliriprowo.admin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DaftarPengumpulanSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var rvDaftarPengumpulan: RecyclerView
    private lateinit var daftarPengumpulanList: ArrayList<PengumpulanAnggota>
    private lateinit var adapter: DaftarPengumpulanSampahAdapter
    private lateinit var etSearch: EditText
    private lateinit var username: String
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_pengumpulan_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendeklarasikan variabel username
        username = ""

        // Mendefinisikan variabel "rvDaftarPengumpulan" yang berupa recyclerview
        rvDaftarPengumpulan = findViewById(R.id.rvDaftarPengumpulanSampah)
        rvDaftarPengumpulan.setHasFixedSize(true)
        rvDaftarPengumpulan.layoutManager = LinearLayoutManager(this)

        // Memasukkan data DaftarSampah ke dalam "daftarPengumpulanList" sebagai array list
        daftarPengumpulanList = arrayListOf<PengumpulanAnggota>()
        // Memanggil fungsi "showListPengumpulan" yang digunakan untuk menampilkan recyclerview dari data yang sudah ada,
        // pada list
        showListPengumpulan()

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
                    val tempUser = searchUser(editable.toString())
                    filter(tempUser)
                }
                closeSearch.setOnClickListener {
                    editable?.clear()
                    closeSearch.visibility = View.INVISIBLE
                    showListPengumpulan()
                    username = ""
                }
            }

        })

        // Ketika "plusButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val plusButton: FloatingActionButton = findViewById(R.id.buttonPlusPengumpulan)
        plusButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke TambahDaftarPengumpulanSampahAdminActivity
            startActivity(Intent(this, TambahDaftarPengumpulanSampahAdminActivity::class.java))
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
                    if (!isConnected(this@DaftarPengumpulanSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: DaftarPengumpulanSampahAdminActivity): Boolean {
        val connectivityManager = contextActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConn != null && wifiConn.isConnected || mobileConn != null && mobileConn.isConnected
    }

    // Membuat fungsi "showListPengumpulan" yang digunakan untuk menampilkan data dari database ke dalam,
    // recyclerview
    private fun showListPengumpulan() {
        reference = FirebaseDatabase.getInstance().getReference("daftarpengumpulan")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    daftarPengumpulanList.clear()
                    for (i in snapshot.children){
                        val pengumpulan = i.getValue(PengumpulanAnggota::class.java)
                        if (pengumpulan != null){
                            daftarPengumpulanList.add(pengumpulan)
                        }
                    }

                    adapter = DaftarPengumpulanSampahAdapter(daftarPengumpulanList, this@DaftarPengumpulanSampahAdminActivity)
                    rvDaftarPengumpulan.adapter = adapter

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    // Membuat fungsi "filter" yang digunakan untuk memfilter recyclerView,
    private fun filter(tempUser: String) {

        val filteredNames = ArrayList<PengumpulanAnggota>()
        // setiap data yang ada pada daftarAnggotaList disamakan dengan filteredNames
        daftarPengumpulanList.filterTo(filteredNames) {
            // jika username sama dengan text input yang dimasukkan oleh user
            it.idAnggota.contains(tempUser)
        }
        // maka akan memenaggil fungsi filterlist dari adapter dan hanyak menampilkan data yang cocok
        adapter!!.filterList(filteredNames)

    }

    // Fungsi "searchUser" digunakan untuk mengambil data username yang cocok dari berbagai user
    private fun searchUser(text: String): String {
        val referenceAnggota = FirebaseDatabase.getInstance().getReference("users")
        referenceAnggota.orderByChild("username").equalTo(text.toLowerCase()).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val users = i.getValue(Users::class.java)!!
                    username = users.id
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        return username
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