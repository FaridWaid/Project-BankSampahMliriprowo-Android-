package com.faridwaid.banksampahmliriprowo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.faridwaid.banksampahmliriprowo.admin.HomeAdminActivity
import com.faridwaid.banksampahmliriprowo.user.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    // Mendefinisikan variabel global dari view
    private lateinit var etEmail: TextInputEditText
    private lateinit var emailContainer: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var passwordContainer: TextInputLayout


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

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        etEmail = findViewById(R.id.etEmail)
        emailContainer = findViewById(R.id.emailContainer)
        etPassword = findViewById(R.id.etPassword)
        passwordContainer = findViewById(R.id.passwordContainer)

        // Memanggil fungsi "usernameFocusListener", "emailFocusListener"
        emailFocusListener()
        passwordFocusListener()

        // Mendefinisikan variabel login button
        val btnLogin: Button = findViewById(R.id.btnLogin)
        // Ketika "btnLogin" di klik maka akan mencoba masuk ke halaman user
        btnLogin.setOnClickListener {
            // Membuat variabel baru yang berisi inputan user
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Memastikan lagi apakah format yang diinputkan oleh user sudah benar
            emailContainer.helperText = validEmail()
            passwordContainer.helperText = validPassword()

            // Jika sudah benar, maka helper pada edittext diisikan dengan null
            val validEmail = emailContainer.helperText == null
            val validPassword = passwordContainer.helperText == null

            // Jika semua sudah diisi maka akan melakukan "loginUser"
            if (validEmail && validPassword) {
                if (email == "oneoclock05@gmail.com" && password == "oneokrock"){
                    // Jika berhasil maka akan pindah activity ke activity HomeAdminActivity
                    Intent(this@LoginActivity, HomeAdminActivity::class.java).also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } else{
                    // Memanggil fungsi "loginUser" dengan membawa variabel ("username","email","password"),
                    // Fungsi ini digunakan untuk masuk ke halaman user
                    loginUser(email, password)
                }
            }else{
                // Jika gagal maka akan memunculkan toast gagal
                Toast.makeText(this, "Gagal login ke akun!", Toast.LENGTH_SHORT).show()
            }
        }

        // Mendefinisikan variabel lupa password
        val forgotPassword: TextView = findViewById(R.id.forgotPassword)
        forgotPassword.setOnClickListener {
            // Jika berhasil maka akan pindah ke ForgotPasswordActivity
            Intent(this@LoginActivity, ForgotPasswordActivity::class.java).also { intent ->
                startActivity(intent)
            }
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
                    Toast.makeText(this, "Email dan Password Tidak Cocok!", Toast.LENGTH_SHORT).show()
                }
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

    // Membuat fungsi "passwordFocusListener"
    private fun passwordFocusListener() {
        // Memastikan apakah etPassword sudah sesuai dengan format pengisian
        etPassword.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                passwordContainer.helperText = validPassword()
            }
        }
    }

    // Membuat fungsi "validPassword"
    private fun validPassword(): String? {
        val password = etPassword.text.toString()
        // Jika password kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if (password.isEmpty()){
            return "Password Harus Diisi!"
        }
        // Jika panjang password kurang dari 6 maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
        if(password.length < 6) {
            return "Password Harus Lebih Dari 6 Karakter!"
        }
//        if(!password.matches(".*[A-Z].*".toRegex())) {
//            return "Password Setidaknya Harus Memiliki 1 Huruf Besar"
//        }
//        if(!password.matches(".*[a-z].*".toRegex()))
//        {
//            return "Must Contain 1 Lower-case Character"
//        }
//        if(!password.matches(".*[@#\$%^&+=].*".toRegex()))
//        {
//            return "Must Contain 1 Special Character (@#\$%^&+=)"
//        }
        return null
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