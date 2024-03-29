package com.faridwaid.banksampahmliriprowo.admin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.user.UpdateDataPofileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import de.hdodenhof.circleimageview.CircleImageView
import java.io.InputStream
import android.system.Os.link

import com.google.firebase.storage.StorageReference
import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener
import java.io.ByteArrayOutputStream


class TambahDaftarSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference
    private lateinit var imageUri: Uri
    // Mendefinisikan variabel global dari view
    private lateinit var photoSampah: RoundedImageView
    private lateinit var textImage: TextView
    private lateinit var etNameSampah: EditText
    private lateinit var etPriceSampah: EditText
    private lateinit var etDescriptionSampah: EditText
    private lateinit var tambahButton: Button

    // Membuat companion object dari Image
    companion object{
        val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_daftar_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        etNameSampah = findViewById(R.id.etNameSampah)
        etPriceSampah = findViewById(R.id.etPriceSampah)
        etDescriptionSampah = findViewById(R.id.etDesctiptionSampah)
        tambahButton = findViewById(R.id.btnTambah)
        photoSampah = findViewById(R.id.imageSampah)
        textImage = findViewById(R.id.textImage)

        // Membuat reference yang nantinya akan digunakan untuk melakukan aksi ke database
        reference = FirebaseDatabase.getInstance().getReference("daftarsampah")

        // Ketika "tambahButton" di klik maka akan melakukan aksi
        tambahButton.setOnClickListener {

            // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
            if (!isConnected(this)){
                showInternetDialog()
                return@setOnClickListener
            }

            // Membuat variabel baru yang berisi inputan user
            val nameSampahInput = etNameSampah.text.toString().trim().toLowerCase()
            val priceSampahInput = etPriceSampah.text.toString().trim()
            val descriptionSampahInput = etDescriptionSampah.text.toString().trim()

            // Jika nameSampahInput kosong maka akan muncul error harus isi terlebih dahulu
            if (nameSampahInput.isEmpty()){
                etNameSampah.error = "Masukkan nama sampah terlebih dahulu!"
                etNameSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika nameSampahInput memiliki inputan symbol maka akan muncul error harus isi terlebih dahulu
            if(nameSampahInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etNameSampah.error = "Tidak boleh ada simbol pada nama sampah!"
                etNameSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika nameSampahInput memiliki inputan angka maka akan muncul error harus isi terlebih dahulu
            if(nameSampahInput.matches(".*[0-9].*".toRegex())) {
                etNameSampah.error = "Tidak boleh ada angka pada nama sampah!"
                etNameSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika priceSampahInput kosong maka akan muncul error harus isi terlebih dahulu
            if (priceSampahInput.isEmpty()){
                etPriceSampah.error = "Masukkan harga sampah terlebih dahulu!"
                etPriceSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika priceSampahInput memiliki inputan symbol maka akan muncul error harus isi terlebih dahulu
            if(priceSampahInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etPriceSampah.error = "Tidak boleh ada simbol pada harga sampah!"
                etPriceSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika priceSampahInput memiliki inputan huruf maka akan muncul error harus isi terlebih dahulu
            if(priceSampahInput.matches(".*[a-z].*".toRegex())) {
                etPriceSampah.error = "Tidak boleh ada huruf pada harga sampah!"
                etPriceSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika descriptionSampahInput kosong maka akan muncul error harus isi terlebih dahulu
            if (descriptionSampahInput.isEmpty()){
                etDescriptionSampah.error = "Masukkan deskripsi sampah terlebih dahulu!"
                etDescriptionSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika descriptionSampahInput memiliki inputan angka maka akan muncul error harus isi terlebih dahulu
            if(descriptionSampahInput.matches(".*[0-9].*".toRegex())) {
                etDescriptionSampah.error = "Tidak boleh ada deskripsi pada nama sampah!"
                etDescriptionSampah.requestFocus()
                return@setOnClickListener
            }

            // jika "nameSampahInput", "priceSampahInput", dan "descriptionSampahInput" tidak kosong, maka akan melakukan aksi
            if (nameSampahInput != null && priceSampahInput!= null && descriptionSampahInput!= null){
                // Membuat variabel ref yang dihungkan dengan firebase storage
                // variabel ref ini digunakan untuk menyimpan foto sampah yang sudah dipilih dan dimasukkan,
                // ke dalam firebase storage, jika tidak memilih foto maka foto akan diset dari android resource untuk dimasukkan
                // ke dalam firebase storage
                val ref = FirebaseStorage.getInstance().reference.child("imgSampah/${nameSampahInput}")
                if (photoSampah.drawable == null){
                    imageUri = Uri.parse("android.resource://com.faridwaid.banksampahmliriprowo/drawable/tempat_sampah")
                    val stream = contentResolver.openInputStream(imageUri)
                    ref.putFile(imageUri).addOnSuccessListener {
                        var downloadUrl: Uri? = null
                        FirebaseStorage.getInstance().reference.child("imgSampah/${nameSampahInput}").downloadUrl.addOnSuccessListener { it1 ->
                                downloadUrl = it1
                                // Mengupdate child yang ada pada reference dengan inputan baru,
                                val sampahUpdate = DaftarSampah(nameSampahInput, priceSampahInput, descriptionSampahInput, 0, downloadUrl.toString())
                                reference.child("$nameSampahInput").setValue(sampahUpdate)
                            }
                    }
                } else {
                    ref.putFile(imageUri).addOnSuccessListener {
                        var downloadUrl: Uri? = null
                        FirebaseStorage.getInstance().reference.child("imgSampah/${nameSampahInput}").downloadUrl.addOnSuccessListener { it1 ->
                            downloadUrl = it1
                            // Mengupdate child yang ada pada reference dengan inputan baru,
                            val sampahUpdate = DaftarSampah(nameSampahInput, priceSampahInput, descriptionSampahInput, 0, downloadUrl.toString())
                            reference.child("$nameSampahInput").setValue(sampahUpdate)
                        }
                    }
                }
                alertDialog("Konfirmasi!", "Penambahan daftar sampah berhasil!", true)
            } else{
                alertDialog("Gagal!", "Gagal melakukan penambahan daftar sampah!", false)
            }
        }

        // Ketika "photoSampah" di klik, maka akan menjalankan fungsi pickImageGallery()
        photoSampah.setOnClickListener {
            pickImageGallery()
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
                    if (!isConnected(this@TambahDaftarSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: TambahDaftarSampahAdminActivity): Boolean {
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

    // Membuat fungsi "pickImageGallery",
    // Fungsi ini digunakan untuk memilik photo dari gallery
    private fun pickImageGallery() {
        val inten = Intent(Intent.ACTION_PICK)
        inten.type = "image/*"
        startActivityForResult(inten, IMAGE_REQUEST_CODE)
    }

    // Memanggi fungsi turunan "onActivityResult", fungsi ini berjalan ketika activity dibuka
    // Fungsi ini digunakan untuk mengambil image yang telah dipilih di gallery dan dipasang ke photoSampah,
    // dan dimasukkan ke dalam imageUri, kemudian membuat textImage menjadi invisible
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK){
            photoSampah.setImageURI(data?.data)
            imageUri = Uri.parse("${data?.data}")
            textImage.visibility = View.INVISIBLE
        }
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
    }

}