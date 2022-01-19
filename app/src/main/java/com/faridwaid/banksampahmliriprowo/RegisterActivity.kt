package com.faridwaid.banksampahmliriprowo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.faridwaid.banksampahmliriprowo.user.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var etUsername: TextInputEditText
    private lateinit var usernameContainer: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var emailContainer: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var passwordContainer: TextInputLayout

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

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        etUsername = findViewById(R.id.etUsername)
        usernameContainer = findViewById(R.id.usernameContainer)
        etEmail = findViewById(R.id.etEmail)
        emailContainer = findViewById(R.id.emailContainer)
        etPassword = findViewById(R.id.etPassword)
        passwordContainer = findViewById(R.id.passwordContainer)

        // Memanggil fungsi "usernameFocusListener", "emailFocusListener", "passwordFocusListener"
        usernameFocusListener()
        emailFocusListener()
        passwordFocusListener()

        // Mendefinisikan variabel registrasi button
        val btnRegistrasi: Button = findViewById(R.id.btnRegister)
        // Ketika "btnRegistrasi" di klik maka akan mencoba mendaftarkan akun baru
        btnRegistrasi.setOnClickListener {
            // Membuat variabel baru yang berisi inputan user
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Memastikan lagi apakah format yang diinputkan oleh user sudah benar
            emailContainer.helperText = validEmail()
            passwordContainer.helperText = validPassword()
            usernameContainer.helperText = validUsername()

            // Jika sudah benar, maka helper pada edittext diisikan dengan null
            val validEmail = emailContainer.helperText == null
            val validPassword = passwordContainer.helperText == null
            val validUsername = usernameContainer.helperText == null

            // Jika semua sudah diisi maka akan melakukan "createNewUser"
            if (validEmail && validPassword && validUsername) {
                // Memanggil fungsi "createNewUser" dengan membawa variabel ("username","email","password"),
                // Fungsi ini digunakan untuk membuat user baru
                createNewUser(username, email, password)
            }else {
                // Jika gagal maka akan memunculkan toast gagal
                Toast.makeText(this, "Gagal membuat akun!", Toast.LENGTH_SHORT).show()
            }
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

    // Membuat fungsi "usernameFocusListener"
    private fun usernameFocusListener() {
        // Memastikan apakah etUsername sudah sesuai dengan format pengisian
        etUsername.setOnFocusChangeListener { _, focused ->
            if(!focused) {
                usernameContainer.helperText = validUsername()
            }
        }
    }

    // Membuat fungsi "validUsername"
    private fun validUsername(): String? {
        val username = etUsername.text.toString()
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
            Intent(this@RegisterActivity, HomeActivity::class.java).also { intent ->
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

}