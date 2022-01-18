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
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Mendefinisikan variabel yang digunukan untuk beralih ke activity RegisterActivity
        val toLogin: TextView = findViewById(R.id.toLogin)
        // Ketika variabel "toRegister" diklik maka akan beralih ke RegisterActivity
        toLogin.setOnClickListener {
            // Menggunakan fungsi intent dan mendifinisakan tujuan activity selanjutnya
            Intent(this@RegisterActivity, LoginActivity::class.java).also {
                startActivity(it)
                // Untuk mengakhiri activity, agar ketika diklik back, tidak kembali ke LoginActivity
                finish()
            }
        }

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // Membuat database baru dengan reference users dan dimasukkan ke dalam variabel ref
        ref = FirebaseDatabase.getInstance().getReference("users")

        // Mendefinisikan variabel registrasi button
        val btnRegistrasi: Button = findViewById(R.id.btnRegister)
        // Ketika "btnRegistrasi" di klik maka akan mencoba mendaftarkan akun baru
        btnRegistrasi.setOnClickListener {
            // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
            val etUsername: EditText = findViewById(R.id.etUsername)
            val etEmail: EditText = findViewById(R.id.etEmail)
            val etPassword: EditText = findViewById(R.id.etPassword)

            // Membuat variabel baru yang berisi inputan user
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Jika username kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
            if (username.isEmpty()){
                etUsername.error = "Username Harus Diisi!"
                etUsername.requestFocus()
                return@setOnClickListener
            }

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

            // Memanggil fungsi "createNewUser" dengan membawa variabel ("username","email","password"),
            // Fungsi ini digunakan untuk membuat user baru
            createNewUser(username, email, password)

        }


    }

    // Membuat fungsi "createNewUser"
    private fun createNewUser(username: String, email: String, password: String) {
        // Membuat user baru dengan email dan password dan langsung tersambung ke Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    // Membuat variabel "idUser" yang berisikan id dari user baru yang telah berhasil dibuat
                    val idUser = auth.currentUser?.uid
                    // Membuat variabel "newUser" yang berisikan beberapa data dan data tersebut diinputkan ke dalam Users
                    val newUser = Users(idUser!!, username, email, "", 0, 0, 0, "")

                    // Jika idUser tidak null/kosong
                    if (idUser != null){
                        // Membuat suatu child realtime database baru dengan child = "idUser",
                        // dan valuenya berisi data yang ada di dalam "newUser"
                        ref.child(idUser).setValue(newUser).addOnCompleteListener {
                            // Jika berhasil menambahkan child baru ke realtime database, maka akan memunculkan toast,
                            // Kemudian pindah activity ke activity LoginActivity
                            Toast.makeText(this, "Akun anda berhasil dibuat!", Toast.LENGTH_SHORT).show()
                            Intent(this@RegisterActivity, LoginActivity::class.java).also { intent ->
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        }
                    } else {
                        // Jika gagal menambahkan child baru ke realtime database, maka akan memunculkan toast gagal
                        Toast.makeText(this, "Gagal membuat akun!", Toast.LENGTH_SHORT).show()
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
            Intent(this@RegisterActivity, HomeActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

}