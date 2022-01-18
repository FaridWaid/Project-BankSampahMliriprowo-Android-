package com.faridwaid.banksampahmliriprowo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class LoginActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Mendefinisikan variabel yang digunukan untuk beralih ke activity RegisterActivity
        val toRegister: TextView = findViewById(R.id.toRegister)
        // Ketika variabel "toRegister" diklik maka akan beralih ke RegisterActivity
        toRegister.setOnClickListener {
            // Menggunakan fungsi intent dan mendifinisakan tujuan activity selanjutnya
            Intent(this@LoginActivity, RegisterActivity::class.java).also {
                startActivity(it)
                // Untuk mengakhiri activity, agar ketika diklik back, tidak kembali ke LoginActivity
                finish()
            }
        }

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Mendefinisikan variabel login button
        val btnLogin: Button = findViewById(R.id.btnLogin)
        // Ketika "btnLogin" di klik maka akan mencoba masuk ke halaman user
        btnLogin.setOnClickListener {
            // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
            val etEmail: EditText = findViewById(R.id.etEmail)
            val etPassword: EditText = findViewById(R.id.etPassword)

            // Membuat variabel baru yang berisi inputan user
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Jika email kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
            if (email.isEmpty()){
                etEmail.error = "Email Harus Diisi!"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // Jika format email tidak valid maka akan gagal membuat user baru dan muncul error email tidak valid
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                etEmail.error = "Email Tidak Valid!"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // Jika password kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
            if (password.isEmpty() || password.length < 6){
                etPassword.error = "Password Harus Lebih Dari 6 Karakter!"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Memanggil fungsi "loginUser" dengan membawa variabel ("username","email","password"),
            // Fungsi ini digunakan untuk masuk ke halaman user
            loginUser(email, password)

        }

    }

    // Membuat fungsi "createNewUser"
    private fun loginUser(email: String, password: String) {
        // Masuk ke halaman user dengan email dan password sebagai autentikasi dan langsung tersambung ke Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    // Jika berhasil maka akan pindah activity ke activity HomeActivity
                    Intent(this@LoginActivity, HomeActivity::class.java).also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } else{
                    // Jika gagal membuat akun baru, maka akan memunculkan toast error
                    Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Membuat fungsi "onStart"
    override fun onStart() {
        super.onStart()
        // Jika user sudah ada user yang login maka akan langsung diarahkan ke HomeActivity
        if (auth.currentUser != null){
            Intent(this@LoginActivity, HomeActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

}