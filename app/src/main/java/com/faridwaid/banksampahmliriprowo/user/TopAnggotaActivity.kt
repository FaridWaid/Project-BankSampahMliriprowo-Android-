package com.faridwaid.banksampahmliriprowo.user

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.PengumpulanAnggota
import com.google.firebase.database.*
import java.text.DecimalFormat
import java.text.NumberFormat

class TopAnggotaActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var rvDaftarTopAnggota: RecyclerView
    private lateinit var adapter: TopAnggotaAdapter
    private lateinit var daftarAnggota: ArrayList<Users>
    private lateinit var topAnggota: ArrayList<Users>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_anggota)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel "rvDaftarTopAnggota" yang berupa recyclerview
        rvDaftarTopAnggota = findViewById(R.id.rvDaftarAnggota)
        rvDaftarTopAnggota.setHasFixedSize(true)
        rvDaftarTopAnggota.layoutManager = LinearLayoutManager(this)

        // Memasukkan data Users ke dalam "daftarAnggota" sebagai array list
        daftarAnggota = arrayListOf<Users>()
        topAnggota = arrayListOf<Users>()

        // Mendefinisikan variabel reference
        reference = FirebaseDatabase.getInstance().getReference("users")

        // Membuat 3 top teratas dari segi pengunpulan menjadi masuk ke dalam array topAnggota,
        // Menampilkan data tersebut yang awalnya berada dalam database ke dalam recycleview
        reference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    daftarAnggota.clear()
                    for (i in snapshot.children){
                        val users = i.getValue(Users::class.java)
                        if (users != null){
                            daftarAnggota.add(users)
                        }
                    }

                    daftarAnggota.sortByDescending { it.jumlahSetoran }
                    var count = 0
                    for (i in 1..3){
                        topAnggota.add(daftarAnggota[count])
                        count += 1
                    }

                    adapter = TopAnggotaAdapter(topAnggota)
                    rvDaftarTopAnggota.adapter = adapter

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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
                    if (!isConnected(this@TopAnggotaActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: TopAnggotaActivity): Boolean {
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

}