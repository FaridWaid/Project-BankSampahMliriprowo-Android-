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

class EditDaftarPenjualanSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var referencePenjualan: DatabaseReference
    private lateinit var refBankSampah: DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var autoCompleteSampah: AutoCompleteTextView
    private lateinit var etWeight: EditText
    private lateinit var textDate: TextView
    private lateinit var etBuyer: EditText
    private lateinit var etNoBuyer: EditText
    private lateinit var etPriceSampah: EditText
    private var changeDate by Delegates.notNull<Boolean>()
    private var checkStock by Delegates.notNull<Boolean>()
    private lateinit var updateButton: Button
    private lateinit var updateDate: String
    private var currentSampah by Delegates.notNull<Int>()
    private var totalPendapatan by Delegates.notNull<Long>()
    private var tempPendapatan by Delegates.notNull<Long>()
    private var tempSampah by Delegates.notNull<Int>()
    private var deleteTotal by Delegates.notNull<Long>()
    private var deleteWeight by Delegates.notNull<Int>()
    private lateinit var deleteIdSampah: String

    // Mendefinisikan companion object yang akan digunakan untuk menerima data
    companion object{
        const val EXTRA_ID = "extra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_daftar_penjualan_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        //mendapatkan id pengumpulan untuk set data penjualan
        val idPenjualan = intent.getStringExtra(EXTRA_ID)!!

        // Mendefinisikan variabel yang nantinya akan berisi inputan user
        autoCompleteSampah = findViewById(R.id.autoCompleteTextViewSampah)
        etWeight = findViewById(R.id.etWeight)
        textDate = findViewById(R.id.date)
        etBuyer = findViewById(R.id.etBuyer)
        etNoBuyer = findViewById(R.id.etNoBuyer)
        etPriceSampah = findViewById(R.id.etPriceSampah)
        updateButton = findViewById(R.id.btnUpdate)
        changeDate = true
        checkStock = false
        totalPendapatan = 0
        tempPendapatan = 0
        tempSampah = 0
        deleteTotal = 0
        deleteWeight = 0

        // Membuat reference yang nantinya akan digunakan untuk melakukan aksi ke database
        referencePenjualan = FirebaseDatabase.getInstance().getReference("daftarpenjualan").child("$idPenjualan")
        refBankSampah = FirebaseDatabase.getInstance().getReference("banksampah").child("saldobank")

        // Mengambil data pengumpulan dengan referencePenjualan dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val penjualan = dataSnapshot.getValue(PenjualanSampah::class.java)
                autoCompleteSampah.setText(penjualan?.idSampah)
                etWeight.setText(penjualan?.weightSampah.toString())
                currentSampah = penjualan?.weightSampah!!
                textDate.setText(penjualan?.datePenjualan)
                etBuyer.setText(penjualan?.nameBuyer)
                etNoBuyer.setText(penjualan?.telpBuyer)
                etPriceSampah.setText(penjualan?.priceSampah)
                tempPendapatan = penjualan?.total.toLong()
                updateDate = penjualan?.datePenjualan
                deleteIdSampah = penjualan?.idSampah
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        referencePenjualan.addListenerForSingleValueEvent(menuListener)

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
            DatePickerDialog(this, datePicker, myCalender.get(Calendar.YEAR), myCalender.get(
                Calendar.MONTH),
                myCalender.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Ketika "updateButton" di klik maka akan melakukan aksi
        updateButton.setOnClickListener {

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

            // Jika weightInput kosong maka akan muncul error harus isi terlebih dahulu
            if (weightInput.isEmpty()){
                etWeight.error = "Masukkan berat sampah terlebih dahulu!"
                etWeight.requestFocus()
                return@setOnClickListener
            }
            // Jika weightInput memiliki inputan huruf maka akan muncul error harus isi terlebih dahulu
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
                    val stockSampah = sampah.stockSampah + currentSampah
                    if (stockSampah >= weightInput.toInt()){
                        tempSampah = weightInput.toInt() - currentSampah
                        checkStock = true
                    } else{
                        alertDialog("Gagal!", "Gagal memperbarui data penjualan, jumlah sampah yang tersedia di bank sampah kurang!", false)
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
                val updatePenjualan = PenjualanSampah(idPenjualan, buyerInput, noBuyerInput, updateDate, dropDownSampahInput, weightInput.toInt(), priceSampahInput, totalPendapatan.toString())
                referencePenjualan.setValue(updatePenjualan).addOnCompleteListener {
                    // Jika berhasil, maka akan mengupdate data stock pada daftarsampah
                    val referenceSampah = FirebaseDatabase.getInstance().getReference("daftarsampah").child(dropDownSampahInput)
                    referenceSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val sampah = snapshot.getValue(DaftarSampah::class.java)!!
                            val newStock = sampah.stockSampah - tempSampah
                            val stockUpdate = DaftarSampah(dropDownSampahInput, sampah.priceSampah, sampah.descriptionSampah, newStock, sampah.photoSampah )
                            referenceSampah.setValue(stockUpdate).addOnCompleteListener {
                                // Jika berhasil, maka akan mengupdate saldo banksampah
                                refBankSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val bank = snapshot.getValue(BankSampah::class.java)!!
                                        if (totalPendapatan > tempPendapatan){
                                            val newPendapatan = totalPendapatan - tempPendapatan
                                            val newSaldo = bank.totalSaldo + newPendapatan
                                            val updateSaldo = BankSampah(newSaldo)
                                            refBankSampah.setValue(updateSaldo).addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    alertDialog("Konfirmasi!", "Berhasil memperbarui daftar penjualan sampah!", true)
                                                }
                                            }
                                        } else if (totalPendapatan < tempPendapatan){
                                            val newPendapatan = tempPendapatan - totalPendapatan
                                            val newSaldo = bank.totalSaldo - newPendapatan
                                            val updateSaldo = BankSampah(newSaldo)
                                            refBankSampah.setValue(updateSaldo).addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    alertDialog("Konfirmasi!", "Berhasil memperbarui daftar penjualan sampah!", true)
                                                }
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

        // Ketika "deleteButton" di klik maka akan menghapus data referencePenjualan, dan menambahkan stock kembali ke daftar sampah
        // Kemudian mengurangi saldo pada bank sampah
        val deleteButton: Button = findViewById(R.id.btnDelete)
        deleteButton.setOnClickListener {

            // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
            if (!isConnected(this)){
                showInternetDialog()
                return@setOnClickListener
            }

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.apply {
                setTitle("Konfirmasi")
                setMessage("Yakin hapus pengumpulan ${idPenjualan}?")
                setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    referencePenjualan.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val penjualan = snapshot.getValue(PenjualanSampah::class.java)!!
                            deleteTotal = penjualan.total.toLong()
                            deleteWeight = penjualan.weightSampah
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                    referencePenjualan.removeValue().addOnCompleteListener {
                        val referenceSampah = FirebaseDatabase.getInstance().getReference("daftarsampah").child(deleteIdSampah)
                        referenceSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val sampah = snapshot.getValue(DaftarSampah::class.java)!!
                                val newStock = sampah.stockSampah + deleteWeight
                                val stockUpdate = DaftarSampah(deleteIdSampah, sampah.priceSampah, sampah.descriptionSampah, newStock, sampah.photoSampah )
                                referenceSampah.setValue(stockUpdate).addOnCompleteListener {
                                    refBankSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val bank = snapshot.getValue(BankSampah::class.java)!!
                                            val newSaldo = bank.totalSaldo - deleteTotal
                                            val updateSaldo = BankSampah(newSaldo)
                                            refBankSampah.setValue(updateSaldo).addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    alertDialog("Konfirmasi!", "Penjualan dengan ID: ${idPenjualan} berhasil dihapus!", true)
                                                } else {
                                                    alertDialog("Gagal!", "Gagal mengahapus Pengumpulan dengan ID: ${idPenjualan}!", false)
                                                }
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
                    }
                })
            }
            alertDialog.show()

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
                    if (!isConnected(this@EditDaftarPenjualanSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: EditDaftarPenjualanSampahAdminActivity): Boolean {
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

    //back button
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_from_top, R.anim.slide_to_bottom)
    }

}