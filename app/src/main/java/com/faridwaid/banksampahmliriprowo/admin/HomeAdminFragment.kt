package com.faridwaid.banksampahmliriprowo.admin

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.faridwaid.banksampahmliriprowo.LoadingDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class HomeAdminFragment : Fragment() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var referen : DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var textName: TextView
    private lateinit var photoProfil: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        textName = view.findViewById(R.id.helloUser)
        photoProfil = view.findViewById(R.id.profilePicture)

        // Membuat referen memiliki child userId, yang nantinya akan diisi oleh data user
        referen = FirebaseDatabase.getInstance().getReference("users").child("admin")

        // Memanggil fungsi loadingBar dan mengeset time = 4000
        loadingBar(2000)

        // Membuat variabel storage untuk inisialisasi FirebaseStorage,
        // gsReference memiliki child dari userId,
        // ketika dalam img terdapat id dari user, maka photo tersebut digunakan untuk photo profile
        // jika dalam img tidak terdapat id user, maka photo profil akan diset dari drawable profile
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

        // Mengambil data user dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val admin = dataSnapshot.getValue(Admin::class.java)
                textName.setText("Hello ${admin?.username}")
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        referen.addListenerForSingleValueEvent(menuListener)

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


}