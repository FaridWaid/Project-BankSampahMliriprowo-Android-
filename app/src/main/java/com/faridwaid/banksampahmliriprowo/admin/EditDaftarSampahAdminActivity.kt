package com.faridwaid.banksampahmliriprowo.admin

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.faridwaid.banksampahmliriprowo.R
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import kotlin.properties.Delegates

class EditDaftarSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var etNameSampah: EditText
    private lateinit var etPriceSampah: EditText
    private lateinit var etDescriptionSampah: EditText
    private lateinit var etStockSampah: EditText
    private lateinit var stockSampah: String
    private lateinit var imageSampah: RoundedImageView
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    private var updateImage by Delegates.notNull<Boolean>()
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference
    private lateinit var imageUri: Uri

    // Mendefinisikan companion object yang akan digunakan untuk menerima data
    companion object{
        const val EXTRA_SAMPAH = "extra_sampah"
        val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_daftar_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        //mendapatkan id untuk set data sampah
        val idSampah = intent.getStringExtra(EXTRA_SAMPAH)

        // set value untuk updateImage
        updateImage = false

        // set value untuk stockSampah
        stockSampah = ""

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        etNameSampah = findViewById(R.id.etNameSampah)
        etPriceSampah = findViewById(R.id.etPriceSampah)
        etDescriptionSampah = findViewById(R.id.etDesctiptionSampah)
        etStockSampah = findViewById(R.id.etStockSampah)
        imageSampah = findViewById(R.id.imageSampah)
        updateButton = findViewById(R.id.btnUpdate)
        deleteButton = findViewById(R.id.btnDelete)

        // Membuat reference memiliki child idSampah, yang nantinya akan digunakan untuk melakukan aksi ke database
        reference = FirebaseDatabase.getInstance().getReference("daftarsampah").child("$idSampah")

        // Mengambil data sampah dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val sampah = dataSnapshot.getValue(DaftarSampah::class.java)
                etNameSampah.setText(sampah?.nameSampah)
                etPriceSampah.setText(sampah?.priceSampah)
                etDescriptionSampah.setText(sampah?.descriptionSampah)
                etStockSampah.setText(sampah?.stockSampah.toString())
                stockSampah = sampah?.stockSampah.toString()
                Picasso.get().load(sampah?.photoSampah).into(imageSampah)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        reference.addListenerForSingleValueEvent(menuListener)

        // Ketika "imageSampah" di klik, maka akan menjalankan fungsi pickImageGallery()
        imageSampah.setOnClickListener {
            pickImageGallery()
        }

        // Ketika "updateButton" di klik maka akan melakukan aksi
        updateButton.setOnClickListener {

            // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
            if (!isConnected(this)){
                showInternetDialog()
                return@setOnClickListener
            }

            // Membuat variabel baru yang berisi inputan user
            val nameSampahInput = etNameSampah.text.toString().trim().toLowerCase()
            val priceSampahInput = etPriceSampah.text.toString().trim()
            val descriptionSampahInput = etDescriptionSampah.text.toString().trim()

            // Jika priceSampahInput kosong maka akan muncul error harus isi terlebih dahulu
            if (priceSampahInput.isEmpty()){
                etPriceSampah.error = "Masukkan harga sampah terlebih dahulu!"
                etPriceSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika priceSampahInput memiliki inputan angka maka akan muncul error harus isi terlebih dahulu
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
                // ke dalam firebase storage
                val ref = FirebaseStorage.getInstance().reference.child("imgSampah/${nameSampahInput}")
                if (!updateImage){
                    var downloadUrl: Uri? = null
                    ref.downloadUrl.addOnSuccessListener { it1 ->
                        downloadUrl = it1
                        // Mengupdate child yang ada pada reference dengan inputan baru,
                        val sampahUpdate = DaftarSampah(nameSampahInput, priceSampahInput, descriptionSampahInput, stockSampah.toInt(), downloadUrl.toString())
                        reference.setValue(sampahUpdate)
                    }
                } else {
                    ref.putFile(imageUri).addOnSuccessListener {
                        var downloadUrl: Uri? = null
                        ref.downloadUrl.addOnSuccessListener { it1 ->
                            downloadUrl = it1
                            // Mengupdate child yang ada pada reference dengan inputan baru,
                            val sampahUpdate = DaftarSampah(nameSampahInput, priceSampahInput, descriptionSampahInput, stockSampah.toInt(), downloadUrl.toString())
                            reference.setValue(sampahUpdate)
                        }
                    }
                }
                alertDialog("Konfirmasi!", "Sampah berhasil diperbarui!", true)
            } else{
                alertDialog("Gagal!", "Gagal memperbarui sampah!", false)
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
                setMessage("Yakin hapus sampah ${idSampah}?")
                setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    reference.removeValue().addOnCompleteListener {
                        val ref = FirebaseStorage.getInstance().reference.child("imgSampah/${idSampah}")
                        ref.delete().addOnCompleteListener {
                            if (it.isSuccessful){
                                alertDialog("Konfirmasi!", "Sampah ${idSampah} berhasil dihapus!", true)
                            } else {
                                alertDialog("Gagal!", "Gagal mengahapus sampah ${idSampah}!", false)
                            }
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
                    if (!isConnected(this@EditDaftarSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: EditDaftarSampahAdminActivity): Boolean {
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
    // dan dimasukkan ke dalam imageUri, kemudian set updateImage menjadi true, digunakan untuk kondisi upload image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TambahDaftarSampahAdminActivity.IMAGE_REQUEST_CODE && resultCode == RESULT_OK){
            imageSampah.setImageURI(data?.data)
            imageUri = Uri.parse("${data?.data}")
            updateImage = true
        }
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
    }

}