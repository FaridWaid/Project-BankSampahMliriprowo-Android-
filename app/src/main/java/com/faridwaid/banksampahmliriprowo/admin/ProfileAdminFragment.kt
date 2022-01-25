package com.faridwaid.banksampahmliriprowo.admin

import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.faridwaid.banksampahmliriprowo.LoadingDialog
import com.faridwaid.banksampahmliriprowo.LoginActivity
import com.faridwaid.banksampahmliriprowo.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ProfileAdminFragment : Fragment() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var referen : DatabaseReference
    private lateinit var imageUri: Uri
    // Mendefinisikan variabel global dari view
    private lateinit var photoProfil: ImageView
    private lateinit var etUsername: TextInputEditText
    private lateinit var usernameContainer: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var emailContainer: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var passwordContainer: TextInputLayout

    // Membuat companion object dari Image
    companion object{
        val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        photoProfil = view.findViewById(R.id.ivProfile)
        etUsername = view.findViewById(R.id.etUsername)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)

        // Membuat referen memiliki child userId, yang nantinya akan diisi oleh data user
        referen = FirebaseDatabase.getInstance().getReference("users").child("admin")

        // Memanggil fungsi loadingBar dan mengeset time = 4000
        loadingBar(2000)

        // Mengambil data user dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val admin = dataSnapshot.getValue(Admin::class.java)
                etUsername.setText("${admin?.username}")
                etEmail.setText("${admin?.email}")
                etPassword.setText("${admin?.password}")
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        referen.addListenerForSingleValueEvent(menuListener)

        // Membuat variabel storage untuk inisialisasi FirebaseStorage,
        // gsReference memiliki child dari userId,
        // ketika dalam img terdapat id dari user, maka photo tersebut digunakan untuk photo profile
        // jika dalam img tidak terdapat id user, maka photo profil akan diset dari drawable camera
        val storage = FirebaseStorage.getInstance()
        val gsReference = storage.reference.child("img/admin")
        val localFile = File.createTempFile("tempImage", "jpg")
        gsReference.getFile(localFile).addOnCompleteListener{
            if (it.isSuccessful){
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                photoProfil.setImageBitmap(bitmap)
            } else {
                photoProfil.setImageResource(R.drawable.ic_profile)
            }
        }

        // Memanggil fungsi "usernameFocusListener", "emailFocusListener", "passwordFocusListener"
//        usernameFocusListener()
//        emailFocusListener()
//        passwordFocusListener()

        // Ketika "photoProfil" di klik, maka akan menjalankan fungsi pickImageGallery()
        photoProfil.setOnClickListener {
            pickImageGallery()
        }

        // Membuat variabel "btnChange" yang berisi view dengan id "btnChange",
        val btnChange: Button = view.findViewById(R.id.btnChange)
        btnChange.setOnClickListener {
            // Membuat variabel baru yang berisi inputan user
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Jika username kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
            if (username.isEmpty()){
                etUsername.error = "Nama Harus Diisi!"
                etUsername.requestFocus()
                return@setOnClickListener
            }
            // Jika username memiliki inputan angka maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
            if(username.matches(".*[0-9].*".toRegex())) {
                etUsername.error = "Tidak Boleh Ada Angka Pada Nama!"
                etUsername.requestFocus()
                return@setOnClickListener
            }
            // Jika email kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
            if (email.isEmpty()){
                etEmail.error = "Email Harus Diisi!"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            // Jika email tidak sesuai format maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Email Tidak Valid"
                etEmail.requestFocus()
                return@setOnClickListener
            }
            // Jika password kosong maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
            if (password.isEmpty()){
                etPassword.error = "Password Harus Diisi!"
                etPassword.requestFocus()
                return@setOnClickListener
            }
            // Jika panjang password kurang dari 6 maka akan gagal membuat user baru dan muncul error harus isi terlebih dahulu
            if(password.length < 6) {
                etPassword.error = "Password Harus Lebih Dari 6 Karakter!"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Jika semua sudah diisi maka akan masuk ke dalam kondisi untuk update email dari user,
            // kemudian mengupdate database referen dengan id dari idUser,
            // jika berhasil maka akan mnemapilkan alert dialog berhasil,
            // dan jika gagal maka akan mnemapilkan alert dialog gagal

                val adminUpdate = Admin(username, email, password)
                referen.setValue(adminUpdate).addOnCompleteListener {
                    if (it.isSuccessful){
                        // Jika gagal maka akan memunculkan dialog berhasil
                        alertDialog("Konfirmasi!", "Data pribadi anda berhasil diubah!")
                    } else{
                        // Jika gagal maka akan memunculkan toast gagal
                        alertDialog("Gagal!", "Gagal Melakukan Perubahan Data Pribadi!")

                    }

            }

        }

        // Membuat variabel "btnLogout" yang berisi view dengan id "btnLogout",
        // jika variabel "btnLogout" di klik makan akan pindah intent ke LoginActivity,
        // kemudian fungsi finish() digunakan untuk mengakhiri activity
        val btnLogout: Button = view.findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            requireActivity().run{
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

    }

    // Membuat fungsi "alertDialog" dengan parameter title, message, dan backActivity
    // Fungsi ini digunakan untuk menampilkan alert dialog
    private fun alertDialog(title: String, message: String){
        // Membuat variabel yang berisikan AlertDialog
        val alertDialog = AlertDialog.Builder(requireActivity())
        alertDialog.apply {
            // Menambahkan title dan pesan ke dalam alert dialog
            setTitle(title)
            setMessage(message)
            setPositiveButton(
                "OK",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
        }
        alertDialog.show()
    }

    // Membuat fungsi "loadingBar" dengan parameter time,
    // Fungsi ini digunakan untuk menampilkan loading dialog
    private fun loadingBar(time: Long) {
        val loading = LoadingDialog(requireActivity())
        loading.startDialog()
        val handler = Handler()
        handler.postDelayed(object: Runnable{
            override fun run() {
                loading.isDissmis()
            }

        }, time)
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
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK){
            photoProfil.setImageURI(data?.data)
            imageUri = Uri.parse("${data?.data}")
            val ref = FirebaseStorage.getInstance().reference.child("img/admin")
            ref.putFile(imageUri)
        }
    }

}