package com.faridwaid.banksampahmliriprowo

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    // Mendefinisikan variabel global dari view
    private lateinit var etEmail: TextInputEditText
    private lateinit var emailContainer: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        etEmail = findViewById(R.id.etEmail)
        emailContainer = findViewById(R.id.emailContainer)
        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val backButton: ImageView = findViewById(R.id.backButton)

        // Memanggil fungsi "usernameFocusListener", "emailFocusListener"
        emailFocusListener()

        // Ketika "btnSubmit" di klik
        btnSubmit.setOnClickListener {
            // Membuat variabel baru yang berisi inputan user
            val email = etEmail.text.toString().trim()

            // Memastikan lagi apakah format yang diinputkan oleh user sudah benar
            emailContainer.helperText = validEmail()

            // Jika sudah benar, maka helper pada edittext diisikan dengan null
            val validEmail = emailContainer.helperText == null

            // Jika semua sudah diisi maka akan melakukan autentikasi email
            if (validEmail){
                // Mengirimkan pesan ke email user untuk membuat password baru, dan jika berhasil..
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // Membuat variabel yang berisikan AlertDialog
                        val alertDialog = AlertDialog.Builder(this)
                        alertDialog.apply {
                            // Menambahkan title dan pesan ke dalam alert dialog
                            setTitle("Konfirmasi")
                            setMessage("Silakan buka email anda untuk membuat password baru!")
                            setPositiveButton(
                                "OK",
                                DialogInterface.OnClickListener { dialogInterface, i ->
                                    dialogInterface.dismiss()
                                    // Jika berhasil maka akan pindah ke LoginActivity
                                    Intent(
                                        this@ForgotPasswordActivity,
                                        LoginActivity::class.java
                                    ).also { intent ->
                                        intent.flags =
                                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                })
                        }
                        alertDialog.show()
                    } else {
                        // Jika gagal membuat akun baru, maka akan memunculkan toast error
                        Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                // Jika gagal membuat akun baru, maka akan memunculkan toast error
                Toast.makeText(this, "Gagal Mengirim Email!", Toast.LENGTH_SHORT).show()
            }
        }

        // Ketika "backButton" di klik
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke LoginActivity
            onBackPressed()
        }

    }

    // Membuat fungsi "emailFocusListener"
    private fun emailFocusListener() {
        // Memastikan apakah etEmail sudah sesuai dengan format pengisian
        etEmail.setOnFocusChangeListener { _, focused ->
            if(!focused){
                emailContainer.helperText = validEmail()
            }
        }
    }

    // Membuat fungsi "validEmail"
    private fun validEmail(): String? {
        val email = etEmail.text.toString()
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
}