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
import com.google.firebase.database.*

class EditJadwalPengumpulanSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var etDay: EditText
    private lateinit var etType: EditText
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    // Mendefinisikan companion object yang akan digunakan untuk menerima data
    companion object{
        const val EXTRA_DAY = "extra_day"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_jadwal_pengumpulan_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        //mendapatkan id untuk set data
        val idDay = intent.getStringExtra(EXTRA_DAY)

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        etDay = findViewById(R.id.etDay)
        etType = findViewById(R.id.etType)
        updateButton = findViewById(R.id.btnUpdate)
        deleteButton = findViewById(R.id.btnDelete)

        // Membuat reference memiliki child idDay, yang nantinya akan digunakan untuk melakukan aksi ke database
        reference = FirebaseDatabase.getInstance().getReference("jadwalpengumpulan").child("$idDay")

        // Mengambil data sampah dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val jadwal = dataSnapshot.getValue(JadwalPengumpulanSampah::class.java)
                etDay.setText(jadwal?.hari)
                etType.setText(jadwal?.type)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        reference.addListenerForSingleValueEvent(menuListener)

        // Ketika "updateButton" di klik maka akan melakukan aksi
        updateButton.setOnClickListener {

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
                reference.setValue(jadwalUpdate)
                alertDialog("Konfirmasi!", "Jadwal pengumpulan sampah berhasil diperbarui!", true)
            } else{
                alertDialog("Gagal!", "Gagal memperbarui jadwal pengumpulan sampah!", false)
            }
        }

        // Ketika "deleteButton" di klik maka akan melakukan aksi, dan menampilkan alert dialog
        deleteButton.setOnClickListener {

            // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
            if (!isConnected(this)){
                showInternetDialog()
                return@setOnClickListener
            }

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.apply {
                setTitle("Konfirmasi")
                setMessage("Yakin hapus jadwal pengumpulan sampah ini?")
                setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    reference.removeValue().addOnCompleteListener {
                        if (it.isSuccessful){
                            alertDialog("Konfirmasi!", "Jadwal pengumpulan sampah berhasil dihapus!", true)
                        } else {
                            alertDialog("Gagal!", "Gagal mengahapus jadwal pengumpulan sampah!", false)
                        }
                    }
                })
            }
            alertDialog.show()
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
                    if (!isConnected(this@EditJadwalPengumpulanSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: EditJadwalPengumpulanSampahAdminActivity): Boolean {
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