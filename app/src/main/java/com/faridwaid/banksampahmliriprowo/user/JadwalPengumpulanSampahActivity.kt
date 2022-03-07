package com.faridwaid.banksampahmliriprowo.user

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.admin.*
import com.google.firebase.database.*

class JadwalPengumpulanSampahActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var rvJadwalPengumpulanSampah: RecyclerView
    private lateinit var scheduleList: ArrayList<JadwalPengumpulanSampah>
    private lateinit var adapterSchedule: JadwalPengumpulanSampahViewAdapter
    private lateinit var daftarSampahList: ArrayList<DaftarSampah>
    private lateinit var adapterSampah: SampahViewPagerAdapter
    private lateinit var sampahViewPager: ViewPager2
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jadwal_pengumpulan_sampah)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel "rvJadwalPengumpulanSampah" yang berupa recyclerview
        rvJadwalPengumpulanSampah = findViewById(R.id.rvJadwalPengumpulanSampah)
        rvJadwalPengumpulanSampah.setHasFixedSize(true)
        rvJadwalPengumpulanSampah.layoutManager = LinearLayoutManager(this)

        // Mendefinisikan variabel "sampahViewPager" yang berupa viewpager
        sampahViewPager = findViewById(R.id.sampahViewPager)
        sampahViewPager.clipToPadding = false
        sampahViewPager.clipChildren = false
        sampahViewPager.offscreenPageLimit = 3
        sampahViewPager.getChildAt(0)
        sampahViewPager.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        // Memasukkan data DaftarSampah ke dalam "daftarSampahList" sebagai array list
        daftarSampahList = arrayListOf<DaftarSampah>()
        // Memanggil fungsi "showListSampah" yang digunakan untuk menampilkan recyclerview dari data yang sudah ada,
        // pada list
        showListSampah()

        // Memasukkan data JadwalPengumpulanSampah ke dalam "scheduleList" sebagai array list
        scheduleList = arrayListOf<JadwalPengumpulanSampah>()
        // Memanggil fungsi "showListSchedule" yang digunakan untuk menampilkan recyclerview dari data yang sudah ada,
        // pada list
        showListSchedule()

        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke LoginActivity
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
                    if (!isConnected(this@JadwalPengumpulanSampahActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: JadwalPengumpulanSampahActivity): Boolean {
        val connectivityManager = contextActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConn != null && wifiConn.isConnected || mobileConn != null && mobileConn.isConnected
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

                    adapterSampah = SampahViewPagerAdapter(daftarSampahList)
                    sampahViewPager.adapter = adapterSampah

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    // Membuat fungsi "showListSchedule" yang digunakan untuk menampilkan data dari database ke dalam,
    // recyclerview
    private fun showListSchedule() {
        reference = FirebaseDatabase.getInstance().getReference("jadwalpengumpulan")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    scheduleList.clear()
                    for (i in snapshot.children){
                        val schedule = i.getValue(JadwalPengumpulanSampah::class.java)
                        if (schedule != null){
                            scheduleList.add(schedule)
                        }
                    }

                    adapterSchedule = JadwalPengumpulanSampahViewAdapter(scheduleList)
                    rvJadwalPengumpulanSampah.adapter = adapterSchedule

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

}