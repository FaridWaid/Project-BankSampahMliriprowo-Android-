package com.faridwaid.banksampahmliriprowo.admin

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
import com.faridwaid.banksampahmliriprowo.ForgotPasswordActivity
import com.faridwaid.banksampahmliriprowo.LoadingDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.user.UpdateDataPofileActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
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

        // Mendefinisikan variabel yang berisi view
        textName = view.findViewById(R.id.helloUser)
        photoProfil = view.findViewById(R.id.profilePicture)

        // Membuat referen memiliki child userId, yang nantinya akan diisi oleh data user
        referen = FirebaseDatabase.getInstance().getReference("users").child("admin")

        // Memanggil fungsi loadingBar dan mengeset time = 4000
        loadingBar(2000)

        // Mengambil data user dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val admin = dataSnapshot.getValue(Admin::class.java)
                textName.setText("Hello ${admin?.username}")
                if (admin?.photoProfil == ""){
                    photoProfil.setImageResource(R.drawable.ic_profile)
                } else {
                    Picasso.get().load(admin?.photoProfil).into(photoProfil)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        referen.addListenerForSingleValueEvent(menuListener)

        // Mendefinisikan variabel item fitur 7
        // overridePendingTransition digunakan untuk animasi dari intent
        val jadwalPengumpulanSampah: MaterialCardView = view.findViewById(R.id.itemFitur7)
        jadwalPengumpulanSampah.setOnClickListener {
            // Jika berhasil maka akan pindah ke JadwalPengumpulanSampahAdminActivity
            requireActivity().run{
                startActivity(Intent(this, JadwalPengumpulanSampahAdminActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item fitur 2
        // overridePendingTransition digunakan untuk animasi dari intent
        val daftarSampah: MaterialCardView = view.findViewById(R.id.itemFitur2)
        daftarSampah.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarSampahAdminActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarSampahAdminActivity::class.java))
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