package com.faridwaid.banksampahmliriprowo.admin

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.google.firebase.database.*

class DaftarAnggotaAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var rvDaftarAnggota: RecyclerView
    private lateinit var daftarAnggotaList: ArrayList<Users>
    private lateinit var adapter: DaftarAnggotaAdapter
    private lateinit var etSearch: EditText
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_anggota_admin)

        // Mendefinisikan variabel "rvDaftarAnggota" yang berupa recyclerview
        rvDaftarAnggota = findViewById(R.id.rvDaftarAnggota)
        rvDaftarAnggota.setHasFixedSize(true)
        rvDaftarAnggota.layoutManager = LinearLayoutManager(this)

        // Memasukkan data DaftarSampah ke dalam "daftarSampahList" sebagai array list
        daftarAnggotaList = arrayListOf<Users>()
        // Memanggil fungsi "showListSampah" yang digunakan untuk menampilkan recyclerview dari data yang sudah ada,
        // pada list
        showListAnggota()

        // Mendefinisikan variabel "etSearch", ketika memasukkan query ke etSearch maka akan memanggil fungsi filter
        // terdapat closeSearch digunakan untuk menghapus query/inputan
        // overridePendingTransition digunakan untuk animasi dari intent
        etSearch = findViewById(R.id.et_search)
        etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                //after the change calling the method and passing the search input
                val closeSearch: ImageView = findViewById(R.id.closeSearch)
                closeSearch.visibility = View.VISIBLE
                filter(editable.toString())
                closeSearch.setOnClickListener {
                    editable?.clear()
                    closeSearch.visibility = View.INVISIBLE
                }
            }

        })

        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke LoginActivity
            onBackPressed()
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }

    }

    // Membuat fungsi "showListAnggota" yang digunakan untuk menampilkan data dari database ke dalam,
    // recyclerview
    private fun showListAnggota() {
        reference = FirebaseDatabase.getInstance().getReference("users")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    daftarAnggotaList.clear()
                    for (i in snapshot.children){
                        val users = i.getValue(Users::class.java)
                        if (users != null){
                            daftarAnggotaList.add(users)
                        }
                    }

                    adapter = DaftarAnggotaAdapter(daftarAnggotaList, this@DaftarAnggotaAdminActivity)

                    rvDaftarAnggota.adapter = adapter

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    // Membuat fungsi "filter" yang digunakan untuk memfilter recyclerView,
    private fun filter(text: String) {
        // mendefiniskan variabel "filteredNames" yang berisi arraylist dari data users
        val filteredNames = ArrayList<Users>()
        // setiap data yang ada pada daftarAnggotaList disamakan dengan filteredNames
        daftarAnggotaList.filterTo(filteredNames) {
            // jika username sama dengan text input yang dimasukkan oleh user
            it.username.toLowerCase().contains(text.toLowerCase())
        }
        // maka akan memenaggil fungsi filterlist dari adapter dan hanyak menampilkan data yang cocok
        adapter!!.filterList(filteredNames)
    }

    // Membuat fungsi "alertDialog" dengan parameter title, message, dan backActivity
    // Fungsi ini digunakan untuk menampilkan alert dialog
    fun alertDialog(title: String, message: String, backActivity: Boolean){
        // Membuat variabel yang berisikan AlertDialog
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.apply {
            // Menambahkan title dan pesan ke dalam alert dialog
            setTitle(title)
            setMessage(message)
            window.setBackgroundDrawableResource(android.R.color.background_light)
            setPositiveButton(
                "OK",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    if (backActivity){
                        onBackPressed()
                    }
                })
        }
        alertDialog.show()
    }

    // Membuat fungsi "animationToTop" yang berisi animasi ketika pinday activity
    // fungsi ini digunakan pada adapter
    fun animationToTop() {
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.slide_to_top)
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

}