package com.faridwaid.banksampahmliriprowo.admin

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class TambahDaftarPenarikanSaldoAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var daftarAnggotaList: ArrayList<String>
    private lateinit var autoComplete: AutoCompleteTextView
    private lateinit var etTotal: EditText
    private var changeDate by Delegates.notNull<Boolean>()
    private lateinit var tambahButton: Button
    private lateinit var idPenarikan: String
    private lateinit var updateDate: String
    private var priceSampah by Delegates.notNull<Long>()
    private var totalPrice by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_daftar_penarikan_saldo_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel yang nantinya akan berisi inputan user
        etTotal = findViewById(R.id.etTotal)
        tambahButton = findViewById(R.id.btnTambah)
        changeDate = false
        idPenarikan = ""
        priceSampah = 0
        totalPrice = 0

        // Memasukkan data ke dalam array list
        daftarAnggotaList = arrayListOf<String>()

        // Mendefinisika reference sebagai FirebaseDatabase
        reference = FirebaseDatabase.getInstance().reference

        // Mendifinisika "arrayAdapterAnggota" sebagai Array Adapter
        val arrayAdapterAnggota = android.widget.ArrayAdapter(this, R.layout.dropdown_item, daftarAnggotaList)
        // Memanggil fungsi "getDataList" yang datanya akan dimasukkan ke dalam daftarAnggotaList
        getDataList("users", "username", daftarAnggotaList)
        // Mendifinisikan autoComplete dan menggunakan "arrayAdapterAnggota" sebagai adapter
        autoComplete = findViewById(R.id.autoCompleteTextView)
        autoComplete.setAdapter(arrayAdapterAnggota)

        // Memanggil Class Calender untuk mendapatkan value date
        var btnChangeDate: TextView = findViewById(R.id.btnDate)
        val myCalender = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalender.set(Calendar.YEAR, year)
            myCalender.set(Calendar.MONTH, month)
            myCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            var tvDate: TextView = findViewById(R.id.date)
            val format = "dd-MM-yyyy"
            val sdf = SimpleDateFormat(format, Locale.US)
            updateDate = sdf.format(myCalender.time)
            tvDate.setText(updateDate)
            changeDate = true
        }

        //ketika button di klik calender dialog akan muncul
        btnChangeDate.setOnClickListener {
            DatePickerDialog(this, datePicker, myCalender.get(Calendar.YEAR), myCalender.get(Calendar.MONTH),
                myCalender.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Memanggil value/child terakhir dari database daftarpenarikan untuk mendifinisikan idPenarikan yang terbaru
        val refPenarikan = FirebaseDatabase.getInstance().getReference("daftarpenarikan")
        refPenarikan.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val countChild = snapshot.childrenCount.toInt()
                if (countChild == 0){
                    idPenarikan = "TRK00001"
                } else {
                    val lastChild = refPenarikan.limitToLast(1)
                    lastChild.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var lastIdPenarikan = snapshot.getValue().toString()
                            var newIdPenarikan = lastIdPenarikan.substring(4, 9).toLong()
                            newIdPenarikan += 1
                            if (newIdPenarikan < 10){
                                idPenarikan = "TRK0000${newIdPenarikan}"
                            } else if (newIdPenarikan < 100){
                                idPenarikan = "TRK000${newIdPenarikan}"
                            } else if (newIdPenarikan < 1000){
                                idPenarikan = "TRK00${newIdPenarikan}"
                            } else if (newIdPenarikan < 10000){
                                idPenarikan = "TRK0${newIdPenarikan}"
                            } else if (newIdPenarikan < 100000){
                                idPenarikan = "TRK${newIdPenarikan}"
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        // Ketika "tambahButton" di klik maka akan melakukan aksi
        tambahButton.setOnClickListener {

            // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
            if (!isConnected(this)){
                showInternetDialog()
                return@setOnClickListener
            }

            // Membuat variabel baru yang berisi inputan user
            val totalInput = etTotal.text.toString().trim()
            val dropDownAnggotaInput = autoComplete.text.toString().trim().toLowerCase()

            // Jika dropDownAnggotaInput kosong maka akan muncul error harus isi terlebih dahulu
            if (dropDownAnggotaInput == "pilih anggota") {
                autoComplete.error = "Silakan pilih anggota terlebih dahulu"
                autoComplete.requestFocus()
                return@setOnClickListener
            }

            // Jika changeDate masih false, maka akan memunculkan alert dialog
            if (changeDate == false) {
                alertDialog("Gagal!", "Silakan memilih tanggal terlebih dahulu sebelum melakukan penambahan daftar penarikan saldo anggota!", false)
                return@setOnClickListener
            }

            // Jika totalInput kosong maka akan muncul error harus isi terlebih dahulu
            if (totalInput.isEmpty()){
                etTotal.error = "Masukkan total penarikan saldo terlebih dahulu!"
                etTotal.requestFocus()
                return@setOnClickListener
            }
            // Jika totalInput memiliki inputan huruf maka akan muncul error harus isi terlebih dahulu
            if(totalInput.matches(".*[a-z].*".toRegex())) {
                etTotal.error = "Tidak boleh ada huruf pada total penarikan saldo!"
                etTotal.requestFocus()
                return@setOnClickListener
            }
            // Jika totalInput memiliki inputan symbol maka akan muncul error harus isi terlebih dahulu
            if(totalInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etTotal.error = "Tidak boleh ada simbol pada total penarikan saldo!"
                etTotal.requestFocus()
                return@setOnClickListener
            }

            // Jika idPenarikan tidak null, maka akan mencjalankan aksi
            if (idPenarikan != null){
                // Melakukan pengecekan pada saldo user, apakah mencukupi untuk melakukan penarikan,
                // Jika tidak mencukupi maka akan muncul alert dialog, dan jika mencukupi maka akan melakukan pengecekan
                // pada saldo bank, jika saldo bank tidak mencukupi untuk melakukan penarikan maka akan muncul alert dialog
                // jika mencukupi akan melakukan penambahan data penarikan, dan mengurangi saldo bank sampah
                // Jika berhasil maka akan memunculkan alert dialog berhasil!
                val referenceAnggota = FirebaseDatabase.getInstance().getReference("users")
                referenceAnggota.orderByChild("username").equalTo(dropDownAnggotaInput).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (i in snapshot.children){
                            val users = i.getValue(Users::class.java)!!
                            val saldoUser = users.saldo
                            if (saldoUser < totalInput.toLong()){
                                alertDialog("Gagal!", "Saldo anggota tidak cukup untuk melakukan penarikan saldo dengan jumlah $totalInput!", false)
                            } else{
                                val refereneBankSampah = FirebaseDatabase.getInstance().getReference("banksampah").child("saldobank")
                                refereneBankSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val bank = snapshot.getValue(BankSampah::class.java)!!
                                        val saldoBank = bank.totalSaldo
                                        if (saldoBank < totalInput.toLong()){
                                            alertDialog("Gagal!", "Saldo bank sampah tidak cukup untuk melakukan penarikan saldo dengan jumlah $totalInput, mohon melakukan penarikan ketika bank sampah sudah memiliki cukup saldo!", false)
                                        } else{
                                            val addPenarikan = PenarikanSaldo(idPenarikan, updateDate, users?.id, totalInput)
                                            refPenarikan.child(idPenarikan).setValue(addPenarikan).addOnCompleteListener {
                                                val userUpdate = Users(users.id, users.username, users.email, users.photoProfil, users.jumlahSetoran, users.jumlahPenarikan + 1, users.saldo - totalInput.toLong(), users.token )
                                                referenceAnggota.child(users.id).setValue(userUpdate).addOnCompleteListener {
                                                    val updateSaldo = BankSampah(saldoBank - totalInput.toLong())
                                                    refereneBankSampah.setValue(updateSaldo).addOnCompleteListener {
                                                        if (it.isSuccessful){
                                                            alertDialog("Konfirmasi!", "Penambahan daftar penarikan saldo anggota berhasil!", true)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }

                                })
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            } else{
                alertDialog("Gagal!", "Gagal melakukan penambahan daftar penarikan saldo anggota!", false)
            }

        }

        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke activity sebelumnya
            onBackPressed()
            overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
        }

    }

    // Fungsi ini digunakan untuk menampilkan dialog peringatan tidak tersambung ke internet,
    // jika tetep tidak connect ke internet maka tetap looping dialog tersebut
    private fun showInternetDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.apply {
            // Menambahkan title dan pesan ke dalam alert dialog
            setTitle("PERINGATAN!")
            setMessage("Tidak ada koneksi internet, mohon nyalakan mobile data/wifi anda terlebih dahulu")
            setPositiveButton(
                "Coba lagi",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    if (!isConnected(this@TambahDaftarPenarikanSaldoAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: TambahDaftarPenarikanSaldoAdminActivity): Boolean {
        val connectivityManager = contextActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobileConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

        return wifiConn != null && wifiConn.isConnected || mobileConn != null && mobileConn.isConnected
    }

    // Membuat fungsi "alertDialog" dengan parameter title, message, dan backActivity
    // Fungsi ini digunakan untuk menampilkan alert dialog
    private fun alertDialog(title: String, message: String, backActivity: Boolean){
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

    // Fungsi "getDataList" untuk mendapat value/data
    private fun getDataList(child: String, path: String, list: ArrayList<String>) {
        reference.child(child).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    list.clear()
                    for (i in snapshot.children){
                        val suggestion: String = i.child(path).getValue(String::class.java)!!.toUpperCase()
                        //Add the retrieved string to the list
                        list.add(suggestion)
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
    }

}