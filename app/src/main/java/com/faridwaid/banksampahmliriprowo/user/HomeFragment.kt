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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.faridwaid.banksampahmliriprowo.BeginningAppActivity
import com.faridwaid.banksampahmliriprowo.LoadingDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.JadwalPengumpulanSampahAdminActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.text.DecimalFormat
import java.text.NumberFormat

class HomeFragment : Fragment() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var referen : DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var textName: TextView
    private lateinit var textCountPengumpulan: TextView
    private lateinit var textCountPenarikan: TextView
    private lateinit var textSaldoUser: TextView
    private lateinit var photoProfil: ImageView
    private lateinit var refreshData: SwipeRefreshLayout

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
        textCountPengumpulan = view.findViewById(R.id.countPengumpulan)
        textCountPenarikan = view.findViewById(R.id.countPenarikan)
        textSaldoUser = view.findViewById(R.id.saldoUser)
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
        referen = FirebaseDatabase.getInstance().getReference("users").child("${userIdentity?.uid}")

        // Memanggil fungsi loadingBar dan mengeset time = 4000
        loadingBar(1000)

        // Memanggil fungsi keepData
        keepData()

        // Mendefinisikan variabel item layout pengumpulan
        // overridePendingTransition digunakan untuk animasi dari intent
        val layoutPengumpulanSampah: LinearLayout = view.findViewById(R.id.layoutPengumpulan)
        layoutPengumpulanSampah.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarPengumpulanSampahActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarPengumpulanSampahActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item layout penarikan
        // overridePendingTransition digunakan untuk animasi dari intent
        val layoutPenarikanSaldo: LinearLayout = view.findViewById(R.id.layoutPenarikan)
        layoutPenarikanSaldo.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarPenarikanSaldoUserActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarPenarikanSaldoUserActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item fitur 1
        // overridePendingTransition digunakan untuk animasi dari intent
        val pengumpulanSemuaAnggota: MaterialCardView = view.findViewById(R.id.itemFitur1)
        pengumpulanSemuaAnggota.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarPengumpulanSemuaAnggotaActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarPengumpulanSemuaAnggotaActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item fitur 2
        // overridePendingTransition digunakan untuk animasi dari intent
        val penjualanBankSampah: MaterialCardView = view.findViewById(R.id.itemFitur2)
        penjualanBankSampah.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarPenjualanBankSampahActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarPenjualanBankSampahActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item fitur 3
        // overridePendingTransition digunakan untuk animasi dari intent
        val daftarAnggota: MaterialCardView = view.findViewById(R.id.itemFitur3)
        daftarAnggota.setOnClickListener {
            // Jika berhasil maka akan pindah ke DaftarAnggotaActivity
            requireActivity().run{
                startActivity(Intent(this, DaftarAnggotaActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
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

        // Mendefinisikan variabel item fitur 5
        // overridePendingTransition digunakan untuk animasi dari intent
        val saldoBankSampah: MaterialCardView = view.findViewById(R.id.itemFitur5)
        saldoBankSampah.setOnClickListener {
            // Jika berhasil maka akan pindah ke SaldoBankSampahActivity
            requireActivity().run{
                startActivity(Intent(this, SaldoBankSampahActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item fitur 6
        // overridePendingTransition digunakan untuk animasi dari intent
        val topAnggota: MaterialCardView = view.findViewById(R.id.itemFitur6)
        topAnggota.setOnClickListener {
            // Jika berhasil maka akan pindah ke TopAnggotaActivity
            requireActivity().run{
                startActivity(Intent(this, TopAnggotaActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

        // Mendefinisikan variabel item question
        // overridePendingTransition digunakan untuk animasi dari intent
        val question: AppCompatImageView = view.findViewById(R.id.question)
        question.setOnClickListener {
            // Jika berhasil maka akan pindah ke BeginningAppActivity
            requireActivity().run{
                startActivity(Intent(this, BeginningAppActivity::class.java))
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }

    }

    private fun keepData() {
        // Mengambil data user dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(Users::class.java)
                textName.setText("Hello ${user?.username}")
                textCountPengumpulan.setText(user?.jumlahSetoran.toString())
                textCountPenarikan.setText(user?.jumlahPenarikan.toString())
                val formatter: NumberFormat = DecimalFormat("#,###")
                val myNumber = user?.saldo
                val formattedNumber: String = formatter.format(myNumber)
                textSaldoUser.setText("Rp. $formattedNumber")
                if (user?.photoProfil == ""){
                    photoProfil.setImageResource(R.drawable.ic_profile)
                } else {
                    Picasso.get().load(user?.photoProfil).into(photoProfil)
                }
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

