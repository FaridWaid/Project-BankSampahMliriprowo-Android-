package com.faridwaid.banksampahmliriprowo.admin

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.user.DaftarAnggotaActivity
import com.google.firebase.database.*

class TambahJadwalPengumpulanSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var etDay: EditText
    private lateinit var etType: EditText
    private lateinit var tambahButton: Button
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var referen: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_jadwal_pengumpulan_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        etDay = findViewById(R.id.etDay)
        etType = findViewById(R.id.etType)
        tambahButton = findViewById(R.id.btnTambah)

        // Membuat referen yang nantinya akan digunakan untuk melakukan aksi ke database
        referen = FirebaseDatabase.getInstance().getReference("jadwalpengumpulan")

        // Ketika "tambahButton" di klik maka akan melakukan aksi
        tambahButton.setOnClickListener {

            // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
            if (!isConnected(this)){
                showInternetDialog()
                return@setOnClickListener
            }

            // Membuat variabel baru yang berisi inputan user
            val dayInput = etDay.text.toString().trim().toLowerCase()
            val typeInput = etType.text.toString().trim()

            // Jika dayInput kosong maka akan muncul error harus isi terlebih dahulu
            if (dayInput.isEmpty()){
                etDay.error = "Massukkan hari terlebih dahulu!"
                etDay.requestFocus()
                return@setOnClickListener
            }
            // Jika dayInput memiliki inputan angka maka akan muncul error harus isi terlebih dahulu
            if(dayInput.matches(".*[0-9].*".toRegex())) {
                etDay.error = "Tidak boleh ada angka pada hari!"
                etDay.requestFocus()
                return@setOnClickListener
            }
            // Jika typeInput kosong maka akan muncul error harus isi terlebih dahulu
            if (typeInput.isEmpty()){
                etType.error = "Massukkan jenis sampah terlebih dahulu!"
                etType.requestFocus()
                return@setOnClickListener
            }

            // jika "dayInput" dan "typeInput" tidak kosong, maka akan melakukan aksi
            if (dayInput != null && typeInput!= null){
                // Mengupdate child yang ada pada reference dengan inputan baru,
                // Jika berhasil aka memunculukan alert dialog berhasil
                // Jika gagal akan memunculkan alert dialog gagal
                val jadwalUpdate = JadwalPengumpulanSampah(dayInput, typeInput)
                referen.child("$dayInput").setValue(jadwalUpdate)
                alertDialog("Konfirmasi!", "Penambahan jadwal pengumpulan sampah berhasil!", true)
            } else{
                alertDialog("Gagal!", "Gagal melakukan nenambahan jadwal pengumpulan sampah!", false)
            }
        }


        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke LoginActivity
            onBackPressed()
            overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
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
                    if (!isConnected(this@TambahJadwalPengumpulanSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: TambahJadwalPengumpulanSampahAdminActivity): Boolean {
        val connectivityManager = contextActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConn != null && wifiConn.isConnected || mobileConn != null && mobileConn.isConnected
    }

    // Membuat fungsi "alertDialog" dengan parameter title, message, dan backActivity
    // Fungsi ini digunakan untuk menampilkan alert dialog
    private fun alertDialog(title: String, message: String, backActivity: Boolean){
        // Membuat variabel yang berisikan AlertDialog
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.apply {
            // Menambahkan title dan pesan ke dalam alert dialog
            setTitle(title)
            setMessage(message)
            window.setBackgroundDrawableResource(android.R.color.background_light)
            setPositiveButton(
                "OK",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    if (backActivity){
                        onBackPressed()
                    }
                })
        }
        alertDialog.show()
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
    }

}