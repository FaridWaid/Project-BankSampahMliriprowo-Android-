package com.faridwaid.banksampahmliriprowo.admin

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class TambahDaftarPengumpulanSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var daftarAnggotaList: ArrayList<String>
    private lateinit var daftarSampahList: ArrayList<String>
    private lateinit var checkList: ArrayList<String>
    private lateinit var autoComplete: AutoCompleteTextView
    private lateinit var autoCompleteSampah: AutoCompleteTextView
    private lateinit var etWeight: EditText
    private var changeDate by Delegates.notNull<Boolean>()
    private lateinit var tambahButton: Button
    private lateinit var idPengumpulan: String
    private lateinit var updateDate: String
    private var priceSampah by Delegates.notNull<Long>()
    private var totalPrice by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_daftar_pengumpulan_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel yang nantinya akan berisi inputan user
        etWeight = findViewById(R.id.etWeight)
        tambahButton = findViewById(R.id.btnTambah)
        changeDate = false
        idPengumpulan = ""
        priceSampah = 0
        totalPrice = 0

        // Memasukkan data ke dalam array list
        daftarAnggotaList = arrayListOf<String>()
        daftarSampahList = arrayListOf<String>()
        checkList = arrayListOf<String>()

        // Mendefinisika reference sebagai FirebaseDatabase
        reference = FirebaseDatabase.getInstance().reference

        // Mendifinisika "arrayAdapterAnggota" sebagai Array Adapter
        val arrayAdapterAnggota = android.widget.ArrayAdapter(this, R.layout.dropdown_item, daftarAnggotaList)
        // Memanggil fungsi "getDataList" yang datanya akan dimasukkan ke dalam daftarAnggotaList
        getDataList("users", "username", daftarAnggotaList)
        // Mendifinisikan autoComplete dan menggunakan "arrayAdapterAnggota" sebagai adapter
        autoComplete = findViewById(R.id.autoCompleteTextView)
        autoComplete.setAdapter(arrayAdapterAnggota)

        // Mendifinisika "arrayAdapterSampah" sebagai Array Adapter
        val arrayAdapterSampah = android.widget.ArrayAdapter(this, R.layout.dropdown_item, daftarSampahList)
        // Memanggil fungsi "getDataList" yang datanya akan dimasukkan ke dalam daftarSampahList
        getDataList("daftarsampah", "nameSampah", daftarSampahList)
        // Mendifinisikan autoCompleteSampah dan menggunakan "arrayAdapterSampah" sebagai adapter
        autoCompleteSampah = findViewById(R.id.autoCompleteTextViewSampah)
        autoCompleteSampah.setAdapter(arrayAdapterSampah)

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

        // Memanggil value/child terakhir dari database daftarpengumulan untuk mendifinisikan idPengumpulan yang terbaru
        val refPengumpulan = FirebaseDatabase.getInstance().getReference("daftarpengumpulan")
        refPengumpulan.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val countChild = snapshot.childrenCount.toInt()
                if (countChild == 0){
                    idPengumpulan = "PNG00001"
                } else {
                    val lastChild = refPengumpulan.limitToLast(1)
                    lastChild.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var lastIdPengumpulan = snapshot.getValue().toString()
                            var newIdPengumpulan = lastIdPengumpulan.substring(4, 9).toLong()
                            newIdPengumpulan += 1
                            if (newIdPengumpulan < 10){
                                idPengumpulan = "PNG0000${newIdPengumpulan}"
                            } else if (newIdPengumpulan < 100){
                                idPengumpulan = "PNG000${newIdPengumpulan}"
                            } else if (newIdPengumpulan < 1000){
                                idPengumpulan = "PNG00${newIdPengumpulan}"
                            } else if (newIdPengumpulan < 10000){
                                idPengumpulan = "PNG0${newIdPengumpulan}"
                            } else if (newIdPengumpulan < 100000){
                                idPengumpulan = "PNG${newIdPengumpulan}"
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
            val weightInput = etWeight.text.toString().trim()
            val dropDownAnggotaInput = autoComplete.text.toString().trim().toLowerCase()
            val dropDownSampahInput = autoCompleteSampah.text.toString().trim().toLowerCase()

            // Jika dropDownAnggotaInput kosong maka akan muncul error harus isi terlebih dahulu
            if (dropDownAnggotaInput == "pilih anggota"){
                autoComplete.error = "Silakan pilih anggota terlebih dahulu"
                autoComplete.requestFocus()
                return@setOnClickListener
            }

            // Jika dropDownSampahInput kosong maka akan muncul error harus isi terlebih dahulu
            if (dropDownSampahInput == "pilih sampah"){
                autoCompleteSampah.error = "Silakan pilih sampah terlebih dahulu"
                autoCompleteSampah.requestFocus()
                return@setOnClickListener
            }

            // Jika changeDate masih false, maka akan memunculkan alert dialog
            if (changeDate == false){
                alertDialog("Gagal!", "Silakan memilih tanggal terlebih dahulu sebelum melakukan penambahan daftar pengumpulan sampah anggota!", false)
                return@setOnClickListener
            }

            // Jika weightInput kosong maka akan muncul error harus isi terlebih dahulu
            if (weightInput.isEmpty()){
                etWeight.error = "Masukkan berat pengumpulan terlebih dahulu!"
                etWeight.requestFocus()
                return@setOnClickListener
            }
            // Jika weightInput memiliki inputan angka maka akan muncul error harus isi terlebih dahulu
            if(weightInput.matches(".*[a-z].*".toRegex())) {
                etWeight.error = "Tidak boleh ada huruf pada berat pengumpulan!"
                etWeight.requestFocus()
                return@setOnClickListener
            }
            // Jika weightInput memiliki inputan symbol maka akan muncul error harus isi terlebih dahulu
            if(weightInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etWeight.error = "Tidak boleh ada simbol pada berat pengumpulan!"
                etWeight.requestFocus()
                return@setOnClickListener
            }

            // jika "weightInput" tidak kosong, maka akan melakukan aksi
            if (weightInput != null){
                // Memanggil value/data dari database daftarsampah dengan child "dropDownSampahInput"
                val referenceSampah = FirebaseDatabase.getInstance().getReference("daftarsampah").child(dropDownSampahInput)
                referenceSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val sampah = snapshot.getValue(DaftarSampah::class.java)!!
                        priceSampah = sampah.priceSampah.toLong()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

                // Memanggil value/data dari database users dengan child "dropDownAnggotaInput"
                val referenceAnggota = FirebaseDatabase.getInstance().getReference("users")
                referenceAnggota.orderByChild("username").equalTo(dropDownAnggotaInput).addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (i in snapshot.children){
                            val users = i.getValue(Users::class.java)!!
                            val tempTotal: Long = priceSampah * weightInput.toLong()
                            totalPrice = tempTotal
                            val pengumpulanUpdate = PengumpulanAnggota(idPengumpulan, updateDate, users?.id, dropDownSampahInput, weightInput.toInt(), priceSampah.toString(), totalPrice.toString() )
                            refPengumpulan.child("$idPengumpulan").setValue(pengumpulanUpdate).addOnCompleteListener {
                                val userUpdate = Users(users.id, users.username, users.email, users.photoProfil, users.jumlahSetoran + 1, users.jumlahPenarikan, users.saldo + totalPrice, users.token )
                                referenceAnggota.child(users.id).setValue(userUpdate).addOnCompleteListener {
                                    val refStockSampah = FirebaseDatabase.getInstance().getReference("daftarsampah").child(dropDownSampahInput)
                                    refStockSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val sampah = snapshot.getValue(DaftarSampah::class.java)!!
                                            val stockUpdate = DaftarSampah(dropDownSampahInput, sampah.priceSampah, sampah.descriptionSampah, sampah.stockSampah + weightInput.toInt(), sampah.photoSampah )
                                            refStockSampah.setValue(stockUpdate).addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    alertDialog("Konfirmasi!", "Penambahan daftar pengumpulan sampah anggota berhasil!", true)
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
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            } else{
                alertDialog("Gagal!", "Gagal melakukan penambahan daftar pengumpulan sampah anggota!", false)
            }
        }

        //ketika button di klik calender dialog akan muncul
        btnChangeDate.setOnClickListener {
            DatePickerDialog(this, datePicker, myCalender.get(Calendar.YEAR), myCalender.get(Calendar.MONTH),
                myCalender.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Ketika "backButton" di klik
        // overridePendingTransition digunakan untuk animasi dari intent
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            // Jika berhasil maka akan pindah ke LoginActivity
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
                    if (!isConnected(this@TambahDaftarPengumpulanSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: TambahDaftarPengumpulanSampahAdminActivity): Boolean {
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