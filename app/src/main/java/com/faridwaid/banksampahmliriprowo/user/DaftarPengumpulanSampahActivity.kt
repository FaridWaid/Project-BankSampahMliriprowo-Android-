package com.faridwaid.banksampahmliriprowo.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.DaftarPengumpulanSampahAdapter
import com.faridwaid.banksampahmliriprowo.admin.DaftarSampah
import com.faridwaid.banksampahmliriprowo.admin.PengumpulanAnggota
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DaftarPengumpulanSampahActivity : AppCompatActivity() {

    // Mendefinisikan variabel global dari view
    private lateinit var rvDaftarPengumpulan: RecyclerView
    private lateinit var daftarPengumpulanList: ArrayList<PengumpulanAnggota>
    private lateinit var adapter: DaftarPengumpulanSampahUserAdapter
    private lateinit var etSearch: EditText
    private lateinit var nameSampah: String
    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_pengumpulan_sampah)

        // Mendeklarasikan variabel nameSampah
        nameSampah = ""

        // Mendefinisikan variabel "rvDaftarPengumpulan" yang berupa recyclerview
        rvDaftarPengumpulan = findViewById(R.id.rvDaftarPengumpulanSampah)
        rvDaftarPengumpulan.setHasFixedSize(true)
        rvDaftarPengumpulan.layoutManager = LinearLayoutManager(this)

        // Memasukkan data DaftarSampah ke dalam "daftarPengumpulanList" sebagai array list
        daftarPengumpulanList = arrayListOf<PengumpulanAnggota>()
        // Memanggil fungsi "showListPengumpulan" yang digunakan untuk menampilkan recyclerview dari data yang sudah ada,
        // pada list
        showListPengumpulan()

        // Mendefinisikan variabel "etSearch", ketika memasukkan query ke etSearch maka akan memanggil fungsi filter
        // terdapat closeSearch digunakan untuk menghapus query/inputan
        // overridePendingTransition digunakan untuk animasi dari intent
        etSearch = findViewById(R.id.et_search)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                //after the change calling the method and passing the search input
                val closeSearch: ImageView = findViewById(R.id.closeSearch)
                closeSearch.visibility = View.VISIBLE
                val search: ImageView = findViewById(R.id.search)
                search.setOnClickListener {
                    val tempSampah = searchUser(editable.toString())
                    filter(tempSampah)
                }
                closeSearch.setOnClickListener {
                    editable?.clear()
                    closeSearch.visibility = View.INVISIBLE
                    showListPengumpulan()
                    nameSampah = ""
                }
            }

        })

        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke activity sebelumnya
            onBackPressed()
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }

    }

    // Membuat fungsi "showListPengumpulan" yang digunakan untuk menampilkan data dari database ke dalam,
    // recyclerview
    private fun showListPengumpulan() {
        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        val auth = FirebaseAuth.getInstance()
        // Membuat userIdentity daru auth untuk mendapatkan userid/currrent user
        val userIdentity = auth.currentUser
        reference = FirebaseDatabase.getInstance().getReference("daftarpengumpulan")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    daftarPengumpulanList.clear()
                    for (i in snapshot.children){
                        val pengumpulan = i.getValue(PengumpulanAnggota::class.java)
                        if (pengumpulan != null && pengumpulan.idAnggota == "${userIdentity?.uid}"){
                            daftarPengumpulanList.add(pengumpulan)
                        }
                    }

                    adapter = DaftarPengumpulanSampahUserAdapter(daftarPengumpulanList)
                    rvDaftarPengumpulan.adapter = adapter

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    // Membuat fungsi "filter" yang digunakan untuk memfilter recyclerView,
    private fun filter(tempSampah: String) {

        val filteredNames = ArrayList<PengumpulanAnggota>()
        // setiap data yang ada pada daftarAnggotaList disamakan dengan filteredNames
        daftarPengumpulanList.filterTo(filteredNames) {
            // jika idSampah sama dengan text input yang dimasukkan oleh user
            it.idSampah.contains(tempSampah)
        }
        // maka akan memenaggil fungsi filterlist dari adapter dan hanyak menampilkan data yang cocok
        adapter!!.filterList(filteredNames)

    }

    // Fungsi "searchUser" digunakan untuk mengambil data username yang cocok dari berbagai user
    private fun searchUser(text: String): String {
        val referenceSampah = FirebaseDatabase.getInstance().getReference("daftarsampah")
        referenceSampah.orderByChild("nameSampah").equalTo(text.toLowerCase()).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (i in snapshot.children){
                    val sampah = i.getValue(DaftarSampah::class.java)!!
                    nameSampah = sampah.nameSampah
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        return nameSampah
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

}