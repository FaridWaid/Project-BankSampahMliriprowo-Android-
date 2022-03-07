package com.faridwaid.banksampahmliriprowo.user

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import com.faridwaid.banksampahmliriprowo.ForgotPasswordActivity
import com.faridwaid.banksampahmliriprowo.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class UpdatePasswordActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    // Mendefinisikan variabel global dari view
    private lateinit var etOldPassword: TextInputEditText
    private lateinit var oldPasswordContainer: TextInputLayout
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var newPasswordContainer: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var confirmPasswordContainer: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_password)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()
        val userIdentity = auth.currentUser

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        etOldPassword = findViewById(R.id.etOldPassword)
        oldPasswordContainer = findViewById(R.id.oldPasswordContainer)
        etNewPassword = findViewById(R.id.etNewPassword)
        newPasswordContainer = findViewById(R.id.newPasswordContainer)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        confirmPasswordContainer = findViewById(R.id.confirmPasswordContainer)

        // Memanggil fungsi "oldPasswordFocusListener", "newPasswordFocusListener", "confirmPasswordFocusListener"
        oldPasswordFocusListener()
        newPasswordFocusListener()
        confirmPasswordFocusListener()

        // Mendefinisikan variabel "buttonSubmit"
        // Ketika "buttonSubmit" di klik maka akan mencoba menjalankan kondisi yang ada di dalam
        val buttonSubmit: Button = findViewById(R.id.btnSubmit)
        buttonSubmit.setOnClickListener {

            // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
            if (!isConnected(this)){
                showInternetDialog()
                return@setOnClickListener
            }

            // Membuat variabel baru yang berisi inputan user
            val oldPassword = etOldPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            // Memastikan lagi apakah format yang diinputkan oleh user sudah benar
            oldPasswordContainer.helperText = oldValidPassword()
            newPasswordContainer.helperText = newValidPassword()
            confirmPasswordContainer.helperText = confirmValidPassword()

            // Jika sudah benar, maka helper pada edittext diisikan dengan null
            val validOldPassword = oldPasswordContainer.helperText == null
            val validNewPassword = newPasswordContainer.helperText == null
            val validConfirm = confirmPasswordContainer.helperText == null

            // Jika semua sudah diisi maka akan masuk ke dalam kondisi untuk autentikasi email dari user,
            // kemudian mengupdate password baru yang telah dibuat oleh user,
            // jika berhasil maka akan mnemapilkan alert dialog berhasil,
            // dan jika gagal maka akan mnemapilkan alert dialog gagal
            if (validOldPassword && validNewPassword && validConfirm) {
                userIdentity?.let {
                    val userCredential = EmailAuthProvider.getCredential(it.email!!, oldPassword)
                    it.reauthenticate(userCredential).addOnCompleteListener {
                        if (it.isSuccessful){
                            userIdentity.updatePassword(newPassword).addOnCompleteListener {
                                if (it.isSuccessful){
                                    alertPassword("Konfirmasi!","Password anda berhasil diubah!", true)
                                } else{
                                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else if (it.exception is FirebaseAuthInvalidCredentialsException){
                            alertPassword("Gagal Mengubah Password!","Password lama yang anda masukkan salah!", false)
                        } else{
                            alertPassword("Gagal Mengubah Password!","${it.exception?.message}", false)
                        }
                    }
                }
            }else {
                // Jika gagal maka akan memunculkan toast gagal
                alertPassword("Gagal Mengubah Password!","Pastikan password yang anda inputkan sudah sesuai format!", false)
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
                    if (!isConnected(this@UpdatePasswordActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: UpdatePasswordActivity): Boolean {
        val connectivityManager = contextActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConn != null && wifiConn.isConnected || mobileConn != null && mobileConn.isConnected
    }

    // Membuat fungsi "alertPassword" dengan parameter title, message, dan backActivity
    // Fungsi ini digunakan untuk menampilkan alert dialog
    private fun alertPassword(title: String, message: String, backActivity: Boolean){
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


    // Membuat fungsi "passwordFocusListener"
    private fun oldPasswordFocusListener() {
        // Memastikan apakah etPassword sudah sesuai dengan format pengisian
        etOldPassword.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                oldPasswordContainer.helperText = oldValidPassword()
            }
        }
    }

    // Membuat fungsi "validPassword"
    private fun oldValidPassword(): String? {
        val oldPassword = etOldPassword.text.toString()

        // Jika password kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if (oldPassword.isEmpty()){
            return "Password Harus Diisi!"
        }
        // Jika panjang password kurang dari 6 maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if(oldPassword.length < 6) {
            return "Password Harus Lebih Dari 6 Karakter!"
        }
        return null
    }

    // Membuat fungsi "passwordFocusListener"
    private fun newPasswordFocusListener() {
        // Memastikan apakah etPassword sudah sesuai dengan format pengisian
        etNewPassword.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                newPasswordContainer.helperText = newValidPassword()
            }
        }
    }

    // Membuat fungsi "validPassword"
    private fun newValidPassword(): String? {
        val newPassword = etNewPassword.text.toString()

        // Jika password kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if (newPassword.isEmpty()){
            return "Password Harus Diisi!"
        }
        // Jika panjang password kurang dari 6 maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if(newPassword.length < 6) {
            return "Password Harus Lebih Dari 6 Karakter!"
        }
        return null
    }

    // Membuat fungsi "passwordFocusListener"
    private fun confirmPasswordFocusListener() {
        // Memastikan apakah etPassword sudah sesuai dengan format pengisian
        etConfirmPassword.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                confirmPasswordContainer.helperText = confirmValidPassword()
            }
        }
    }

    // Membuat fungsi "validPassword"
    private fun confirmValidPassword(): String? {
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        // Jika password kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if (confirmPassword.isEmpty()){
            return "Password Harus Diisi!"
        }
        // Jika panjang password kurang dari 6 maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if(confirmPassword.length < 6) {
            return "Password Harus Lebih Dari 6 Karakter!"
        }
        // Jika panjang password kurang dari 6 maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if(confirmPassword != newPassword) {
            return "Konfirmasi Password Salah!"
        }
        return null
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
    }

}