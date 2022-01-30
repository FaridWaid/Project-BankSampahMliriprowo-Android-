package com.faridwaid.banksampahmliriprowo.user

import android.content.Intent
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
import com.faridwaid.banksampahmliriprowo.admin.JadwalPengumpulanSampahAdminActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class HomeFragment : Fragment() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var referen : DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var textName: TextView
    private lateinit var photoProfil: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // Membuat userIdentity daru auth untuk mendapatkan userid/currrent user
        val userIdentity = auth.currentUser
        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        textName = view.findViewById(R.id.helloUser)
        photoProfil = view.findViewById(R.id.profilePicture)

        // Membuat referen memiliki child userId, yang nantinya akan diisi oleh data user
        referen = FirebaseDatabase.getInstance().getReference("users").child("${userIdentity?.uid}")

        // Memanggil fungsi loadingBar dan mengeset time = 4000
        loadingBar(2000)

        // Mengambil data user dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(Users::class.java)
                textName.setText("Hello ${user?.username}")
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        referen.addListenerForSingleValueEvent(menuListener)

        // Membuat variabel storage untuk inisialisasi FirebaseStorage,
        // gsReference memiliki child dari userId,
        // ketika dalam img terdapat id dari user, maka photo tersebut digunakan untuk photo profile
        // jika dalam img tidak terdapat id user, maka photo profil akan diset dari drawable profile
        val storage = FirebaseStorage.getInstance()
        val gsReference = storage.reference.child("img/${userIdentity?.uid}")
        val localFile = File.createTempFile("tempImage", "jpg")
        gsReference.getFile(localFile).addOnCompleteListener{
            if (it.isSuccessful){
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                photoProfil.setImageBitmap(bitmap)
            } else {
                photoProfil.setImageResource(R.drawable.ic_profile)
            }
        }

        // Mendefinisikan variabel item fitur 4
        // overridePendingTransition digunakan untuk animasi dari intent
        val jadwalPengumpulanSampah: MaterialCardView = view.findViewById(R.id.itemFitur4)
        jadwalPengumpulanSampah.setOnClickListener {
            // Jika berhasil maka akan pindah ke JadwalPengumpulanSampahActivity
            requireActivity().run{
                startActivity(Intent(this, JadwalPengumpulanSampahActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

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

