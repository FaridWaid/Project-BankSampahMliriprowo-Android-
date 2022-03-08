package com.faridwaid.banksampahmliriprowo.user

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import androidx.navigation.Navigation
import com.faridwaid.banksampahmliriprowo.LoadingDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.DaftarSampah
import com.faridwaid.banksampahmliriprowo.admin.HomeAdminActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigInteger
import kotlin.properties.Delegates

class UpdateDataPofileActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var referen : DatabaseReference
    private lateinit var imageUri: Uri
    // Mendefinisikan variabel global dari view
    private lateinit var textName: TextInputEditText
    private lateinit var usernameContainer: TextInputLayout
    private lateinit var textSaldo: TextView
    private lateinit var textEmail: TextInputEditText
    private lateinit var emailContainer: TextInputLayout
    private lateinit var photoProfil: CircleImageView
    private lateinit var textImage: TextView
    private var changeImage by Delegates.notNull<Boolean>()

    // Membuat companion object dari Image
    companion object{
        val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_data_pofile)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // Membuat userIdentity daru auth untuk mendapatkan userid/currrent user
        val userIdentity = auth.currentUser
        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        textName = findViewById(R.id.yourName)
        usernameContainer = findViewById(R.id.nameContainer)
        textSaldo = findViewById(R.id.yourSaldo)
        textEmail = findViewById(R.id.yourEmail)
        emailContainer = findViewById(R.id.emailContainer)
        photoProfil = findViewById(R.id.ivProfile)
        textImage = findViewById(R.id.textImage)
        changeImage = false

        // Membuat referen memiliki child userId, yang nantinya akan diisi oleh data user
        referen = FirebaseDatabase.getInstance().getReference("users").child("${userIdentity?.uid}")


        // Mengambil data user dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(Users::class.java)
                textName.setText(user?.username)
                textSaldo.text = user?.saldo.toString()
                textEmail.setText(user?.email)
                if (user?.photoProfil != ""){
                    Picasso.get().load(user?.photoProfil).into(photoProfil)
                    textImage.visibility = View.INVISIBLE
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        referen.addListenerForSingleValueEvent(menuListener)


        // Memanggil fungsi "usernameFocusListener", "emailFocusListener"
        usernameFocusListener()
        emailFocusListener()

        // Ketika "photoProfil" di klik, maka akan menjalankan fungsi pickImageGallery()
        photoProfil.setOnClickListener {
            pickImageGallery()
        }

        // Membuat variabel "btnChange" yang berisi view dengan id "btnChange",
        val btnChange: Button = findViewById(R.id.btnChange)
        btnChange.setOnClickListener {

            // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
            if (!isConnected(this)){
                showInternetDialog()
                return@setOnClickListener
            }

            // Membuat variabel baru yang berisi inputan user
            val email = textEmail.text.toString().trim()
            val username = textName.text.toString().trim()

            // Memastikan lagi apakah format yang diinputkan oleh user sudah benar
            emailContainer.helperText = validEmail()
            usernameContainer.helperText = validUsername()

            // Jika sudah benar, maka helper pada edittext diisikan dengan null
            val validEmail = emailContainer.helperText == null
            val validUsername = usernameContainer.helperText == null

            // Jika semua sudah diisi maka akan masuk ke dalam kondisi untuk update email dari user,
            // kemudian mengupdate database referen dengan id dari idUser,
            // jika berhasil maka akan mnemapilkan alert dialog berhasil,
            // dan jika gagal maka akan mnemapilkan alert dialog gagal
            if (validEmail && validUsername) {
                userIdentity?.let {
                    userIdentity.updateEmail(email).addOnCompleteListener {
                        if (it.isSuccessful){
                            // Membuat variabel ref yang dihungkan dengan firebase storage
                            // variabel ref ini digunakan untuk menyimpan foto sampah yang sudah dipilih dan dimasukkan,
                            // ke dalam firebase storage, jika tidak memilih foto maka foto akan diset dari android resource untuk dimasukkan
                            // ke dalam firebase storage
                            val refImage = FirebaseStorage.getInstance().reference.child("img/${userIdentity?.uid}")
                            if (photoProfil.drawable == null){
                                imageUri = Uri.parse("android.resource://com.faridwaid.banksampahmliriprowo/drawable/ic_profile")
                                val stream = contentResolver.openInputStream(imageUri)
                                refImage.putFile(imageUri).addOnSuccessListener {
                                    var downloadUrl: Uri? = null
                                    refImage.downloadUrl.addOnSuccessListener { it1 ->
                                        downloadUrl = it1
                                        // Mengupdate child yang ada pada reference dengan inputan baru,
                                        val menuListener = object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                val user = dataSnapshot.getValue(Users::class.java)
                                                val userUpdate = Users(userIdentity?.uid!!,username, email, downloadUrl.toString(), user?.jumlahSetoran!!, user?.jumlahPenarikan!!, user?.saldo!!, user?.token!!)
                                                referen.setValue(userUpdate)
                                            }
                                            override fun onCancelled(databaseError: DatabaseError) {
                                                // handle error
                                            }
                                        }

                                        referen.addListenerForSingleValueEvent(menuListener)
                                    }
                                }
                            } else if (changeImage == false){
                                // Mengupdate child yang ada pada reference dengan inputan baru,
                                val menuListener = object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val user = dataSnapshot.getValue(Users::class.java)
                                        val userUpdate = Users(userIdentity?.uid!!,username, email, user?.photoProfil!!, user?.jumlahSetoran!!, user?.jumlahPenarikan!!, user?.saldo!!, user?.token!!)
                                        referen.setValue(userUpdate)
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // handle error
                                    }
                                }

                                referen.addListenerForSingleValueEvent(menuListener)
                            }
                            else {
                                refImage.putFile(imageUri).addOnSuccessListener {
                                    var downloadUrl: Uri? = null
                                    refImage.downloadUrl.addOnSuccessListener { it1 ->
                                        downloadUrl = it1
                                        // Mengupdate child yang ada pada reference dengan inputan baru,
                                        val menuListener = object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                val user = dataSnapshot.getValue(Users::class.java)
                                                val userUpdate = Users(userIdentity?.uid!!,username, email, downloadUrl.toString(), user?.jumlahSetoran!!, user?.jumlahPenarikan!!, user?.saldo!!, user?.token!!)
                                                referen.setValue(userUpdate)
                                            }
                                            override fun onCancelled(databaseError: DatabaseError) {
                                                // handle error
                                            }
                                        }

                                        referen.addListenerForSingleValueEvent(menuListener)
                                    }
                                }
                            }

                            alertDialog("Konfirmasi!", "Data pribadi anda berhasil diubah!", true)
                        } else{
                            alertDialog("Gagal Mengubah Data Pribadi!", "${it.exception?.message}", false)
                        }
                    }
                }
            }else{
                // Jika gagal maka akan memunculkan toast gagal
                alertDialog("Gagal!", "Gagal Melakukan Perubahan Data Pribadi!", false)
            }

        }

        // Membuat variabel "backButton" yang berisi view dengan id "backButton",
        // jika variabel "backButton" di klik makan akan pindah intent ke activity sebelumnya
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: AppCompatImageView = findViewById(R.id.backButton)
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
                    if (!isConnected(this@UpdateDataPofileActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: UpdateDataPofileActivity): Boolean {
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
    // Fungsi ini digunakan untuk mengambil image yang telah dipilih di gallery dan dipasang ke photoprofil,
    // dan dimasukkan ke dalam database img, dengan id dari user
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK){
            photoProfil.setImageURI(data?.data)
            imageUri = Uri.parse("${data?.data}")
            textImage.visibility = View.INVISIBLE
            changeImage = true
        }
    }

    // Membuat fungsi "usernameFocusListener"
    private fun usernameFocusListener() {
        // Memastikan apakah etUsername sudah sesuai dengan format pengisian
        textName.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                usernameContainer.helperText = validUsername()
            }
        }
    }

    // Membuat fungsi "validUsername"
    private fun validUsername(): String? {
        val username = textName.text.toString()
        // Jika username kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if (username.isEmpty()){
            return "Nama Harus Diisi!"
        }
        // Jika username memiliki inputan angka maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if(username.matches(".*[0-9].*".toRegex())) {
            return "Tidak Boleh Ada Angka Pada Nama!"
        }
        return null
    }

    // Membuat fungsi "emailFocusListener"
    private fun emailFocusListener() {
        // Memastikan apakah etEmail sudah sesuai dengan format pengisian
        textEmail.setOnFocusChangeListener { _, focused ->
            if(!focused){
                emailContainer.helperText = validEmail()
            }
        }
    }

    // Membuat fungsi "validEmail"
    private fun validEmail(): String? {
        val email = textEmail.text.toString()
        // Jika email kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if (email.isEmpty()){
            return "Email Harus Diisi!"
        }
        // Jika email tidak sesuai format maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Email Tidak Valid"
        }
        return null
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
    }
}