package com.faridwaid.banksampahmliriprowo.admin

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import kotlin.properties.Delegates

class EditDaftarAnggotaAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etJumlahSetoran: EditText
    private lateinit var etJumlahPenarikan: EditText
    private lateinit var etJumlahSaldo: EditText
    private lateinit var imageUser: RoundedImageView
    private lateinit var updateButton: Button
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    // Mendefinisikan companion object yang akan digunakan untuk menerima data
    companion object{
        const val EXTRA_ID = "extra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_daftar_anggota_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        //mendapatkan id user untuk set data user
        val idUser = intent.getStringExtra(EXTRA_ID)

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etJumlahSetoran = findViewById(R.id.etJumlahSetoran)
        etJumlahPenarikan = findViewById(R.id.etJumlahPenarikan)
        etJumlahSaldo = findViewById(R.id.etJumlahSaldo)
        imageUser = findViewById(R.id.imageUser)
        updateButton = findViewById(R.id.btnUpdate)

        // Membuat reference memiliki child idUser, yang nantinya akan digunakan untuk melakukan aksi ke database
        reference = FirebaseDatabase.getInstance().getReference("users").child("$idUser")

        // Mengambil data sampah dengan reference dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val users = dataSnapshot.getValue(Users::class.java)
                etUsername.setText(users?.username)
                etEmail.setText(users?.email)
                etJumlahSetoran.setText(users?.jumlahSetoran.toString())
                etJumlahPenarikan.setText(users?.jumlahPenarikan.toString())
                etJumlahSaldo.setText(users?.saldo.toString())
                Picasso.get().load(users?.photoProfil).into(imageUser)
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
            val usernameInput = etUsername.text.toString().trim().toLowerCase()
            val emailInput = etEmail.text.toString().trim()
            val jumlahSetoranInput = etJumlahSetoran.text.toString().trim()
            val jumlahPenarikanInput = etJumlahPenarikan.text.toString().trim()
            val jumlahSaldoInput = etJumlahSaldo.text.toString().trim()

            // Jika jumlahSetoranInput kosong maka akan muncul error harus isi terlebih dahulu
            if (jumlahSetoranInput.isEmpty()){
                etJumlahSetoran.error = "Masukkan jumlah setoran terlebih dahulu!"
                etJumlahSetoran.requestFocus()
                return@setOnClickListener
            }
            // Jika jumlahSetoranInput memiliki inputan symbol maka akan muncul error
            if(jumlahSetoranInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etJumlahSetoran.error = "Tidak boleh ada simbol pada jumlah setoran!"
                etJumlahSetoran.requestFocus()
                return@setOnClickListener
            }
            // Jika jumlahSetoranInput memiliki inputan huruf maka akan muncul error
            if(jumlahSetoranInput.matches(".*[a-z].*".toRegex())) {
                etJumlahSetoran.error = "Tidak boleh ada huruf pada jumlah setoran!"
                etJumlahSetoran.requestFocus()
                return@setOnClickListener
            }
            // Jika jumlahPenarikanInput kosong maka akan muncul error harus isi terlebih dahulu
            if (jumlahPenarikanInput.isEmpty()){
                etJumlahPenarikan.error = "Masukkan jumlah penarikan terlebih dahulu!"
                etJumlahPenarikan.requestFocus()
                return@setOnClickListener
            }
            // Jika jumlahPenarikanInput memiliki inputan symbol maka akan muncul error
            if(jumlahPenarikanInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etJumlahPenarikan.error = "Tidak boleh ada simbol pada jumlah penarikan!"
                etJumlahPenarikan.requestFocus()
                return@setOnClickListener
            }
            // Jika jumlahPenarikanInput memiliki inputan huruf maka akan muncul error
            if(jumlahPenarikanInput.matches(".*[a-z].*".toRegex())) {
                etJumlahPenarikan.error = "Tidak boleh ada huruf pada jumlah penarikan!"
                etJumlahPenarikan.requestFocus()
                return@setOnClickListener
            }
            // Jika jumlahSaldoInput kosong maka akan muncul error harus isi terlebih dahulu
            if (jumlahSaldoInput.isEmpty()){
                etJumlahSaldo.error = "Masukkan jumlah saldo terlebih dahulu!"
                etJumlahSaldo.requestFocus()
                return@setOnClickListener
            }
            // Jika jumlahSaldoInput memiliki inputan symbol maka akan muncul error
            if(jumlahSaldoInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etJumlahSaldo.error = "Tidak boleh ada simbol pada jumlah saldo!"
                etJumlahSaldo.requestFocus()
                return@setOnClickListener
            }
            // Jika jumlahSaldoInput memiliki inputan huruf maka akan muncul error
            if(jumlahSaldoInput.matches(".*[a-z].*".toRegex())) {
                etJumlahSaldo.error = "Tidak boleh ada huruf pada jumlah saldo!"
                etJumlahSaldo.requestFocus()
                return@setOnClickListener
            }

            // jika semua inutan user tidak kosong, maka akan melakukan aksi
            if (usernameInput != null && emailInput!= null && jumlahSetoranInput!= null && jumlahPenarikanInput!= null && jumlahSaldoInput!= null){
                // Mengambil data sampah dengan referen dan dimasukkan kedalam view (text,etc)
                val menuListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val users = dataSnapshot.getValue(Users::class.java)
                        // Mengupdate child yang ada pada reference dengan inputan baru,
                        val usersUpdate = Users(idUser!!, usernameInput, emailInput, users?.photoProfil!!, jumlahSetoranInput.toInt(), jumlahPenarikanInput.toInt(), jumlahSaldoInput.toLong(), users?.token!!)
                        reference.setValue(usersUpdate)
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // handle error
                    }
                }
                reference.addListenerForSingleValueEvent(menuListener)
                alertDialog("Konfirmasi!", "Data anggota berhasil diperbarui!", true)
            } else{
                alertDialog("Gagal!", "Gagal memperbarui sampah!", false)
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
                    if (!isConnected(this@EditDaftarAnggotaAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: EditDaftarAnggotaAdminActivity): Boolean {
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