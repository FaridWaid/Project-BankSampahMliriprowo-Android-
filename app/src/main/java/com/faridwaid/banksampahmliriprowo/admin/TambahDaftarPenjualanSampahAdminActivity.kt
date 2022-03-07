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

class TambahDaftarPenjualanSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var reference: DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var daftarSampahList: ArrayList<String>
    private lateinit var autoCompleteSampah: AutoCompleteTextView
    private lateinit var etWeight: EditText
    private lateinit var etBuyer: EditText
    private lateinit var etNoBuyer: EditText
    private lateinit var etPriceSampah: EditText
    private var changeDate by Delegates.notNull<Boolean>()
    private var checkStock by Delegates.notNull<Boolean>()
    private lateinit var tambahButton: Button
    private lateinit var idPenjualan: String
    private lateinit var updateDate: String
    private var totalPendapatan by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_daftar_penjualan_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        // Mendefinisikan variabel yang nantinya akan berisi inputan user
        etWeight = findViewById(R.id.etWeight)
        etBuyer = findViewById(R.id.etBuyer)
        etNoBuyer = findViewById(R.id.etNoBuyer)
        etPriceSampah = findViewById(R.id.etPriceSampah)
        tambahButton = findViewById(R.id.btnTambah)
        changeDate = false
        checkStock = false
        totalPendapatan = 0

        // Memasukkan data ke dalam array list
        daftarSampahList = arrayListOf<String>()

        // Mendefinisika reference sebagai FirebaseDatabase
        reference = FirebaseDatabase.getInstance().reference

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

        //ketika button di klik calender dialog akan muncul
        btnChangeDate.setOnClickListener {
            DatePickerDialog(this, datePicker, myCalender.get(Calendar.YEAR), myCalender.get(Calendar.MONTH),
                myCalender.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Memanggil value/child terakhir dari database daftarpenjualan untuk mendifinisikan idPenjualan yang terbaru
        val refPenjualan = FirebaseDatabase.getInstance().getReference("daftarpenjualan")
        refPenjualan.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val countChild = snapshot.childrenCount.toInt()
                if (countChild == 0){
                    idPenjualan = "JUL00001"
                } else {
                    val lastChild = refPenjualan.limitToLast(1)
                    lastChild.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var lastIdPenjualan = snapshot.getValue().toString()
                            var newIdPenjualan = lastIdPenjualan.substring(4, 9).toLong()
                            newIdPenjualan += 1
                            if (newIdPenjualan < 10){
                                idPenjualan = "JUL0000${newIdPenjualan}"
                            } else if (newIdPenjualan < 100){
                                idPenjualan = "JUL000${newIdPenjualan}"
                            } else if (newIdPenjualan < 1000){
                                idPenjualan = "JUL00${newIdPenjualan}"
                            } else if (newIdPenjualan < 10000){
                                idPenjualan = "JUL0${newIdPenjualan}"
                            } else if (newIdPenjualan < 100000){
                                idPenjualan = "JUL${newIdPenjualan}"
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
            val buyerInput = etBuyer.text.toString().trim()
            val noBuyerInput = etNoBuyer.text.toString().trim()
            val priceSampahInput = etPriceSampah.text.toString().trim()
            val dropDownSampahInput = autoCompleteSampah.text.toString().trim().toLowerCase()

            // Jika dropDownSampahInput kosong maka akan muncul error harus isi terlebih dahulu
            if (dropDownSampahInput == "pilih sampah"){
                autoCompleteSampah.error = "Silakan pilih sampah terlebih dahulu"
                autoCompleteSampah.requestFocus()
                return@setOnClickListener
            }

            // Jika changeDate masih false, maka akan memunculkan alert dialog
            if (changeDate == false){
                alertDialog("Gagal!", "Silakan memilih tanggal terlebih dahulu sebelum melakukan penambahan daftar penjualan sampah!", false)
                return@setOnClickListener
            }

            // Jika weightInput kosong maka akan muncul error harus isi terlebih dahulu
            if (weightInput.isEmpty()){
                etWeight.error = "Masukkan berat sampah terlebih dahulu!"
                etWeight.requestFocus()
                return@setOnClickListener
            }
            // Jika weightInput memiliki inputan angka maka akan muncul error harus isi terlebih dahulu
            if(weightInput.matches(".*[a-z].*".toRegex())) {
                etWeight.error = "Tidak boleh ada huruf pada berat penjualan!"
                etWeight.requestFocus()
                return@setOnClickListener
            }
            // Jika weightInput memiliki inputan symbol maka akan muncul error harus isi terlebih dahulu
            if(weightInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etWeight.error = "Tidak boleh ada simbol pada berat penjualan!"
                etWeight.requestFocus()
                return@setOnClickListener
            }

            // Jika buyerInput kosong maka akan muncul error harus isi terlebih dahulu
            if (buyerInput.isEmpty()){
                etBuyer.error = "Masukkan nama pembeli terlebih dahulu!"
                etBuyer.requestFocus()
                return@setOnClickListener
            }
            // Jika buyerInput memiliki inputan angka maka akan muncul error harus isi terlebih dahulu
            if(buyerInput.matches(".*[0-9].*".toRegex())) {
                etBuyer.error = "Tidak boleh ada angka pada nama pembeli!"
                etBuyer.requestFocus()
                return@setOnClickListener
            }
            // Jika buyerInput memiliki inputan symbol maka akan muncul error harus isi terlebih dahulu
            if(buyerInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etBuyer.error = "Tidak boleh ada simbol pada nama pembeli!"
                etBuyer.requestFocus()
                return@setOnClickListener
            }

            // Jika noBuyerInput kosong maka akan muncul error harus isi terlebih dahulu
            if (noBuyerInput.isEmpty()){
                etNoBuyer.error = "Masukkan nomor pembeli terlebih dahulu!"
                etNoBuyer.requestFocus()
                return@setOnClickListener
            }
            // Jika noBuyerInput memiliki inputan huruf maka akan muncul error harus isi terlebih dahulu
            if(noBuyerInput.matches(".*[a-z].*".toRegex())) {
                etNoBuyer.error = "Tidak boleh ada huruf pada nomer pembeli!"
                etNoBuyer.requestFocus()
                return@setOnClickListener
            }
            // Jika noBuyerInput memiliki inputan symbol maka akan muncul error harus isi terlebih dahulu
            if(noBuyerInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etNoBuyer.error = "Tidak boleh ada simbol pada nomer pembeli!"
                etNoBuyer.requestFocus()
                return@setOnClickListener
            }

            // Jika priceSampahInput kosong maka akan muncul error harus isi terlebih dahulu
            if (priceSampahInput.isEmpty()){
                etPriceSampah.error = "Masukkan harga sampah terlebih dahulu!"
                etPriceSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika priceSampahInput memiliki inputan huruf maka akan muncul error harus isi terlebih dahulu
            if(priceSampahInput.matches(".*[a-z].*".toRegex())) {
                etPriceSampah.error = "Tidak boleh ada huruf pada harga sampah!"
                etPriceSampah.requestFocus()
                return@setOnClickListener
            }
            // Jika priceSampahInput memiliki inputan symbol maka akan muncul error harus isi terlebih dahulu
            if(priceSampahInput.matches(".*[?=.*/><,!@#$%^&()_=+].*".toRegex())) {
                etPriceSampah.error = "Tidak boleh ada simbol pada harga sampah!"
                etPriceSampah.requestFocus()
                return@setOnClickListener
            }

            // Mengecek data stock sampah, jika tersedia maka nilai checkStock menjadi true,
            // Jika tidak tersedia maka akan mucul alert dialog gagal, dan gagal melakukan transaksi
            val referenceSampah = FirebaseDatabase.getInstance().getReference("daftarsampah").child(dropDownSampahInput)
            referenceSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sampah = snapshot.getValue(DaftarSampah::class.java)!!
                    if (sampah.stockSampah >= weightInput.toInt()){
                        checkStock = true
                    } else{
                        val currentStock = sampah.stockSampah.toString()
                        alertDialog("Gagal!", "Gagal melakukan penambahan data penjualan, jumlah sampah yang tersedia di bank sampah hanya $currentStock KG!\nMohon masukkan jumlah yang sesuai.", false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            // jika "checkStock" = true, maka akan melakukan aksi
            if (checkStock == true){
                // Melakukan perhitungan totalPendapatan dan mengaupdate reference penjualan
                totalPendapatan = weightInput.toLong() * priceSampahInput.toLong()
                val addPenjualan = PenjualanSampah(idPenjualan, buyerInput, noBuyerInput, updateDate, dropDownSampahInput, weightInput.toInt(), priceSampahInput, totalPendapatan.toString())
                refPenjualan.child(idPenjualan).setValue(addPenjualan).addOnCompleteListener {
                    // Jika berhasil, maka akan mengupdate data stock pada daftarsampah
                    referenceSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val sampah = snapshot.getValue(DaftarSampah::class.java)!!
                            val updateStock = sampah.stockSampah - weightInput.toInt()
                            val stockUpdate = DaftarSampah(dropDownSampahInput, sampah.priceSampah, sampah.descriptionSampah, updateStock, sampah.photoSampah )
                            referenceSampah.setValue(stockUpdate).addOnCompleteListener {
                                // Jika berhasil, maka akan mengupdate saldo banksampah
                                val refBankSampah = FirebaseDatabase.getInstance().getReference("banksampah").child("saldobank")
                                refBankSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val bank = snapshot.getValue(BankSampah::class.java)!!
                                        val newSaldo = bank.totalSaldo + totalPendapatan
                                        val updateSaldo = BankSampah(newSaldo)
                                        refBankSampah.setValue(updateSaldo).addOnCompleteListener {
                                            if (it.isSuccessful){
                                                alertDialog("Konfirmasi!", "Penambahan daftar penjualan sampah berhasil!", true)
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                }
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
                    if (!isConnected(this@TambahDaftarPenjualanSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: TambahDaftarPenjualanSampahAdminActivity): Boolean {
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