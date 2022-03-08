package com.faridwaid.banksampahmliriprowo.admin

import android.content.DialogInterface
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import java.text.DecimalFormat
import java.text.NumberFormat

class HomeAdminFragment : Fragment() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var referen : DatabaseReference
    private lateinit var referenceBankSampah : DatabaseReference
    private lateinit var referenceAnggota : DatabaseReference
    private lateinit var referenceSampah : DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var textName: TextView
    private lateinit var textSaldoBank: TextView
    private lateinit var textCountAnggota: TextView
    private lateinit var textCountSampah: TextView
    private lateinit var photoProfil: ImageView
    private lateinit var refreshData: SwipeRefreshLayout

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
        textSaldoBank = view.findViewById(R.id.saldoBank)
        textCountAnggota = view.findViewById(R.id.countAnggota)
        textCountSampah = view.findViewById(R.id.countSampah)
        refreshData = view.findViewById(R.id.refreshData)

        refreshData.setOnRefreshListener {
            // Loading selama beberapa waktu, ketika sudah selesa nilai refreshFrament menjadi false
            Handler().postDelayed(Runnable {
                refreshData.isRefreshing = false
            }, 2000)

            // Memanggil fungsi keepData
            keepData()
        }

        // Membuat referen memiliki child userId, yang nantinya akan diisi oleh data user
        referen = FirebaseDatabase.getInstance().getReference("admins").child("admin")
        referenceBankSampah = FirebaseDatabase.getInstance().getReference("banksampah").child("saldobank")
        referenceAnggota = FirebaseDatabase.getInstance().getReference("users")
        referenceSampah = FirebaseDatabase.getInstance().getReference("daftarsampah")

        // Memanggil fungsi loadingBar dan mengeset time = 4000
        loadingBar(2000)

        // Memanggil fungsi keepData
        keepData()

        // Mendefinisikan variabel item fitur 1
        // overridePendingTransition digunakan untuk animasi dari intent
        val daftarAnggota: MaterialCardView = view.findViewById(R.id.itemFitur1)
        daftarAnggota.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarAnggotaAdminActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarAnggotaAdminActivity::class.java))
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

        // Mendefinisikan variabel item fitur 3
        // overridePendingTransition digunakan untuk animasi dari intent
        val daftarPengumpulanSampah: MaterialCardView = view.findViewById(R.id.itemFitur3)
        daftarPengumpulanSampah.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarSampahAdminActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarPengumpulanSampahAdminActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item fitur 4
        // overridePendingTransition digunakan untuk animasi dari intent
        val daftarPenarikanSaldo: MaterialCardView = view.findViewById(R.id.itemFitur4)
        daftarPenarikanSaldo.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarPenarikanSaldoAdminActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarPenarikanSaldoAdminActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item fitur 5
        // overridePendingTransition digunakan untuk animasi dari intent
        val daftarPenjualanSampah: MaterialCardView = view.findViewById(R.id.itemFitur5)
        daftarPenjualanSampah.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarPenjualanSampahAdminActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarPenjualanSampahAdminActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item fitur 6
        // overridePendingTransition digunakan untuk animasi dari intent
        val notificationAdmin: MaterialCardView = view.findViewById(R.id.itemFitur6)
        notificationAdmin.setOnClickListener {
            // Jika berhasil maka akan pindah ke NotificationAdminActivity
            requireActivity().run{
                startActivity(Intent(this, NotificationAdminActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

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

    }

    // Fungsi untuk mengambil data dari database
    private fun keepData() {
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

        // Mengambil data saldo bank sampah dengan referenceBankSampah dan dimasukkan kedalam view (text,etc)
        referenceBankSampah.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val bank = snapshot.getValue(BankSampah::class.java)!!
                val formatter: NumberFormat = DecimalFormat("#,###")
                val myNumber = bank?.totalSaldo
                val formattedNumber: String = formatter.format(myNumber)
                textSaldoBank.setText("Rp. ${formattedNumber}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        // Mengambil data count anggota dengan referenceAnggota dan dimasukkan kedalam view (text,etc)
        referenceAnggota.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var countAnggota = 0
                for (i in snapshot.children){
                    countAnggota += 1
                }
                textCountAnggota.setText(countAnggota.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        // Mengambil data count jumlah sampah dengan referenceSampah dan dimasukkan kedalam view (text,etc)
        referenceSampah.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var countSampah = 0
                for (i in snapshot.children){
                    val sampah = i.getValue(DaftarSampah::class.java)
                    countSampah += sampah?.stockSampah!!
                }
                textCountSampah.setText("$countSampah KG")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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