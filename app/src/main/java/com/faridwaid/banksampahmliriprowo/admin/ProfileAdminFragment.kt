package com.faridwaid.banksampahmliriprowo.admin

import android.content.DialogInterface
import android.content.Intent
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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.faridwaid.banksampahmliriprowo.LoadingDialog
import com.faridwaid.banksampahmliriprowo.LoginActivity
import com.faridwaid.banksampahmliriprowo.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlin.properties.Delegates

class ProfileAdminFragment : Fragment() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var referen : DatabaseReference
    private lateinit var imageUri: Uri
    // Mendefinisikan variabel global dari view
    private lateinit var photoProfil: ImageView
    private lateinit var textImage: TextView
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private var changeImage by Delegates.notNull<Boolean>()

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
        textImage = view.findViewById(R.id.textImage)
        etUsername = view.findViewById(R.id.etUsername)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        changeImage = false

        // Membuat referen memiliki child userId, yang nantinya akan diisi oleh data user
        referen = FirebaseDatabase.getInstance().getReference("admins").child("admin")

        // Memanggil fungsi loadingBar dan mengeset time = 4000
        loadingBar(2000)

        // Mengambil data user dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val admin = dataSnapshot.getValue(Admin::class.java)
                etUsername.setText("${admin?.username}")
                etEmail.setText("${admin?.email}")
                etPassword.setText("${admin?.password}")
                if (admin?.photoProfil != ""){
                    Picasso.get().load(admin?.photoProfil).into(photoProfil)
                    textImage.visibility = View.INVISIBLE
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        referen.addListenerForSingleValueEvent(menuListener)

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

            // Membuat variabel refImage yang dihungkan dengan firebase storage
            // variabel refImage ini digunakan untuk menyimpan foto profil yang sudah dipilih dan dimasukkan,
            // ke dalam firebase storage, jika tidak memilih foto maka foto akan diset dari android resource untuk dimasukkan
            // ke dalam firebase storage
            val refImage = FirebaseStorage.getInstance().reference.child("img/admin")
            if (photoProfil.drawable == null){
                imageUri = Uri.parse("android.resource://com.faridwaid.banksampahmliriprowo/drawable/ic_profile")
//                val stream = contentResolver.openInputStream(imageUri)
                refImage.putFile(imageUri).addOnSuccessListener {
                    var downloadUrl: Uri? = null
                    refImage.downloadUrl.addOnSuccessListener { it1 ->
                        downloadUrl = it1
                        // Mengupdate child yang ada pada reference dengan inputan baru,
                        val menuListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val adminUpdate = Admin(username, email, password, downloadUrl.toString())
                                referen.setValue(adminUpdate).addOnCompleteListener {
                                    // Memanggil fungsi loadingBar dan mengeset time = 4000
                                    loadingBar(1000)
                                    if (it.isSuccessful){
                                        alertDialog("Konfirmasi!", "Data pribadi anda berhasil diubah!")
                                    } else {
                                        alertDialog("Gagal!", "Gagal Melakukan Perubahan Data Pribadi!")
                                    }
                                }
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
                        val admin = dataSnapshot.getValue(Admin::class.java)
                        val adminUpdate = Admin(username, email, password, admin?.photoProfil!!)
                        referen.setValue(adminUpdate).addOnCompleteListener {
                            loadingBar(3000)
                            if (it.isSuccessful){
                                alertDialog("Konfirmasi!", "Data pribadi anda berhasil diubah!")
                            } else {
                                alertDialog("Gagal!", "Gagal Melakukan Perubahan Data Pribadi!")
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // handle error
                    }
                }

                referen.addListenerForSingleValueEvent(menuListener)
            } else {
                refImage.putFile(imageUri).addOnSuccessListener {
                    var downloadUrl: Uri? = null
                    refImage.downloadUrl.addOnSuccessListener { it1 ->
                        downloadUrl = it1
                        // Mengupdate child yang ada pada reference dengan inputan baru,
                        val menuListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val adminUpdate = Admin(username, email, password, downloadUrl.toString())
                                referen.setValue(adminUpdate).addOnCompleteListener {
                                    loadingBar(3000)
                                    if (it.isSuccessful){
                                        alertDialog("Konfirmasi!", "Data pribadi anda berhasil diubah!")
                                    } else {
                                        alertDialog("Gagal!", "Gagal Melakukan Perubahan Data Pribadi!")
                                    }
                                }
                            }
                            override fun onCancelled(databaseError: DatabaseError) {
                                // handle error
                            }
                        }

                        referen.addListenerForSingleValueEvent(menuListener)
                    }
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
            textImage.visibility = View.INVISIBLE
            changeImage = true
        }
    }

}