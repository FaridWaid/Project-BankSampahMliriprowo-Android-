package com.faridwaid.banksampahmliriprowo.user


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.faridwaid.banksampahmliriprowo.LoadingDialog
import com.faridwaid.banksampahmliriprowo.LoginActivity
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class ProfileFragment : Fragment() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var referen : DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var textName: TextView
    private lateinit var textSaldo: TextView
    private lateinit var textEmail: TextView
    private lateinit var icVerified: ImageView
    private lateinit var icUnverified: ImageView
    private lateinit var photoProfil: CircleImageView
    private lateinit var refreshData: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()
        // Membuat userIdentity daru auth untuk mendapatkan userid/currrent user
        val userIdentity = auth.currentUser
        // Mendefinisikan variabel edit text yang nantinya akan berisi inputan user
        textName = view.findViewById(R.id.yourName)
        textSaldo = view.findViewById(R.id.yourSaldo)
        textEmail = view.findViewById(R.id.yourEmail)
        icVerified = view.findViewById(R.id.icVerified)
        icUnverified = view.findViewById(R.id.icUnverified)
        photoProfil = view.findViewById(R.id.ivProfile)
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

        // Jika userIdentity tidak null dan email dari user sudah terverifikasi, maka icVerified akan ditampilkan,
        // jika tidak maka sebaliknya
        if (userIdentity != null){
            if (userIdentity.isEmailVerified){
                icVerified.visibility = View.VISIBLE
            } else{
                icUnverified.visibility = View.VISIBLE
            }

        }

        // Jika icUnverified diklik, maka akan mengirimkan verifikasi ke email,
        // jika berhasil mengirim verifikasi email akan menampilkan alert dialog,
        // dan jika gagal akan menampilkan toast dengan error
        icUnverified.setOnClickListener {
            userIdentity?.sendEmailVerification()?.addOnCompleteListener {
                if (it.isSuccessful){
                    // Membuat variabel yang berisikan AlertDialog
                    val alertDialog = AlertDialog.Builder(requireContext())
                    alertDialog.apply {
                        // Menambahkan title dan pesan ke dalam alert dialog
                        setTitle("Konfirmasi")
                        setMessage("Silakan buka email anda untuk verifikasi email")
                        setPositiveButton(
                            "OK",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()
                            })
                    }
                    alertDialog.show()
                } else{
                    Toast.makeText(activity, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Membuat variabel "ubahPassword" yang berisi view dengan id "changePassword",
        // jika variabel "ubahPassword" di klik makan akan pindah intent ke UpdatePasswordActivity,
        // overridePendingTransition digunakan untuk animasi dari intent
        val ubahPassword: TextView = view.findViewById(R.id.changePassword)
        ubahPassword.setOnClickListener {
            requireActivity().run{
                startActivity(Intent(this, UpdatePasswordActivity::class.java))
                overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
            }
        }

        // Membuat variabel "btnChange" yang berisi view dengan id "btnChange",
        // jika variabel "btnChange" di klik makan akan pindah intent ke UpdateDataPofileActivity,
        // overridePendingTransition digunakan untuk animasi dari intent
        val btnChange: Button = view.findViewById(R.id.btnChange)
        btnChange.setOnClickListener {
            requireActivity().run{
                startActivity(Intent(this, UpdateDataPofileActivity::class.java))
                overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
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

    private fun keepData() {
        // Mengambil data user dengan referen dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(Users::class.java)
                textName.text = user?.username
                textSaldo.text = user?.saldo.toString()
                textEmail.text = user?.email
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