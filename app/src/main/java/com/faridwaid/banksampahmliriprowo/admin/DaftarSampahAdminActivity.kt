package com.faridwaid.banksampahmliriprowo.admin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DaftarSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var rvDaftarSampah: RecyclerView
    private lateinit var daftarSampahList: ArrayList<DaftarSampah>
    private lateinit var adapter: DaftarSampahAdapter
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel "rvDaftarSampah" yang berupa recyclerview
        rvDaftarSampah = findViewById(R.id.rvDaftarSampah)
        rvDaftarSampah.setHasFixedSize(true)
        rvDaftarSampah.layoutManager = LinearLayoutManager(this)

        // Memasukkan data DaftarSampah ke dalam "daftarSampahList" sebagai array list
        daftarSampahList = arrayListOf<DaftarSampah>()
        // Memanggil fungsi "showListSampah" yang digunakan untuk menampilkan recyclerview dari data yang sudah ada,
        // pada list
        showListSampah()

        // Ketika "plusButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val plusButton: FloatingActionButton = findViewById(R.id.buttonPlusDaftarSampah)
        plusButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke TambahDaftarSampahAdminActivity
            startActivity(Intent(this, TambahDaftarSampahAdminActivity::class.java))
            overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
        }

        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke LoginActivity
            onBackPressed()
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }

    }

    // Membuat fungsi "showListSampah" yang digunakan untuk menampilkan data dari database ke dalam,
    // recyclerview
    private fun showListSampah() {
        reference = FirebaseDatabase.getInstance().getReference("daftarsampah")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    daftarSampahList.clear()
                    for (i in snapshot.children){
                        val sampah = i.getValue(DaftarSampah::class.java)
                        if (sampah != null){
                            daftarSampahList.add(sampah)
                        }
                    }

                    adapter = DaftarSampahAdapter(daftarSampahList, this@DaftarSampahAdminActivity)
                    rvDaftarSampah.adapter = adapter

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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
                    if (!isConnected(this@DaftarSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: DaftarSampahAdminActivity): Boolean {
        val connectivityManager = contextActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConn != null && wifiConn.isConnected || mobileConn != null && mobileConn.isConnected
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    // Membuat fungsi "animationToTop" yang berisi animasi ketika pinday activity
    // fungsi ini digunakan pada adapter
    fun animationToTop() {
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
    }

}