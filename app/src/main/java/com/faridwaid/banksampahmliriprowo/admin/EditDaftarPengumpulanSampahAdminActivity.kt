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
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class EditDaftarPengumpulanSampahAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var referencePengumpulan: DatabaseReference
    private lateinit var referenceAnggota: DatabaseReference
    private lateinit var referenceSampah: DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var autoComplete: AutoCompleteTextView
    private lateinit var autoCompleteSampah: AutoCompleteTextView
    private lateinit var etWeight: EditText
    private lateinit var textDate: TextView
    private lateinit var updateButton: Button
    private lateinit var updateDate: String
    private var tempWeight by Delegates.notNull<Int>()
    private var currentWeight by Delegates.notNull<Int>()
    private var currentTotal by Delegates.notNull<Long>()
    private var totalPrice by Delegates.notNull<Long>()
    private var deleteTotal by Delegates.notNull<Long>()
    private lateinit var deleteIdAnggota: String
    private lateinit var deleteIdSampah: String
    private var changeDate by Delegates.notNull<Boolean>()

    // Mendefinisikan companion object yang akan digunakan untuk menerima data
    companion object{
        const val EXTRA_ID = "extra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_daftar_pengumpulan_sampah_admin)

        // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
        if (!isConnected(this)){
            showInternetDialog()
        }

        //mendapatkan id pengumpulan untuk set data pengumpulan
        val idPengumpulan = intent.getStringExtra(EXTRA_ID)!!

        // Mendefinisikan variabel yang nantinya akan berisi inputan user
        autoComplete = findViewById(R.id.autoCompleteTextView)
        autoCompleteSampah = findViewById(R.id.autoCompleteTextViewSampah)
        etWeight = findViewById(R.id.etWeight)
        textDate = findViewById(R.id.date)
        updateButton = findViewById(R.id.btnUpdate)
        tempWeight = 0
        totalPrice = 0
        deleteTotal = 0
        currentTotal = 0
        changeDate = false
        deleteIdAnggota = ""
        deleteIdSampah = ""

        // Membuat reference yang nantinya akan digunakan untuk melakukan aksi ke database
        referencePengumpulan = FirebaseDatabase.getInstance().getReference("daftarpengumpulan").child("$idPengumpulan")
        referenceAnggota = FirebaseDatabase.getInstance().getReference("users")
        referenceSampah = FirebaseDatabase.getInstance().getReference("daftarsampah")

        // Mengambil data pengumpulan dengan referencePengumpulan dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val pengumpulan = dataSnapshot.getValue(PengumpulanAnggota::class.java)
                etWeight.setText(pengumpulan?.weightSampah.toString())
                tempWeight = pengumpulan?.weightSampah!!
                deleteIdSampah = pengumpulan?.idSampah
                autoCompleteSampah.setText(pengumpulan?.idSampah)
                textDate.setText(pengumpulan?.datePengumpulan)
                val referenceAnggota = FirebaseDatabase.getInstance().getReference("users").child(pengumpulan?.idAnggota!!)
                referenceAnggota.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = snapshot.getValue(Users::class.java)!!
                        autoComplete.setText(users.username)
                        deleteIdAnggota = users.id
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // handle error
            }
        }
        referencePengumpulan.addListenerForSingleValueEvent(menuListener)

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

        // Ketika "updateButton" di klik maka akan melakukan aksi
        updateButton.setOnClickListener {

            // Jika tidak ada koneksi internet maka akan memanggil fungsi "showInternetDialog"
            if (!isConnected(this)){
                showInternetDialog()
                return@setOnClickListener
            }

            // Membuat variabel baru yang berisi inputan user
            val weightInput = etWeight.text.toString().trim()

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
            if (weightInput != null) {
                // Mengambil data dari database daftarpengumpulan dengan child idPengumpulan,
                // Kemudian memasukkan inputan user ke dalam database tersebut.
                // Jika berhasil maka akan mengupdate saldo user
                val referencePengumpulan = FirebaseDatabase.getInstance().getReference("daftarpengumpulan")
                    .child(idPengumpulan)
                referencePengumpulan.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val pengumpulan = snapshot.getValue(PengumpulanAnggota::class.java)!!
                        var tempTotal = pengumpulan.total.toLong()
                        totalPrice = pengumpulan.priceSampah.toLong() * weightInput.toLong()
                        if (changeDate == false){
                            updateDate = pengumpulan.datePengumpulan
                        }
                        val pengumpulanUpdate = PengumpulanAnggota(idPengumpulan, updateDate, pengumpulan.idAnggota, pengumpulan.idSampah, weightInput.toInt(), pengumpulan.priceSampah, totalPrice.toString() )
                        referencePengumpulan.setValue(pengumpulanUpdate).addOnCompleteListener {
                            if (it.isSuccessful){
                                    referenceAnggota.child(pengumpulan.idAnggota).addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val users = snapshot.getValue(Users::class.java)!!
                                            if (tempTotal < totalPrice){
                                                currentTotal = totalPrice - tempTotal
                                                val userUpdate = Users(users.id, users.username, users.email, users.photoProfil, users.jumlahSetoran, users.jumlahPenarikan, users.saldo + currentTotal, users.token )
                                                referenceAnggota.child(users.id).setValue(userUpdate)
                                            } else {
                                                currentTotal = tempTotal - totalPrice
                                                val userUpdate = Users(users.id, users.username, users.email, users.photoProfil, users.jumlahSetoran, users.jumlahPenarikan, users.saldo - currentTotal, users.token )
                                                referenceAnggota.child(users.id).setValue(userUpdate)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }


                                    })
                                    referenceSampah.child(pengumpulan.idSampah).addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val sampah = snapshot.getValue(DaftarSampah::class.java)!!
                                            if (tempWeight > weightInput.toInt()){
                                                currentWeight = tempWeight - weightInput.toInt()
                                                val stockUpdate = DaftarSampah(pengumpulan.idSampah, sampah.priceSampah, sampah.descriptionSampah, sampah.stockSampah - currentWeight, sampah.photoSampah )
                                                referenceSampah.child(pengumpulan.idSampah).setValue(stockUpdate)
                                            } else {
                                                currentWeight = weightInput.toInt() - tempWeight
                                                val stockUpdate = DaftarSampah(pengumpulan.idSampah, sampah.priceSampah, sampah.descriptionSampah, sampah.stockSampah + currentWeight, sampah.photoSampah )
                                                referenceSampah.child(pengumpulan.idSampah).setValue(stockUpdate)
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                alertDialog("Konfirmasi!", "Perubahan daftar pengumpulan sampah anggota berhasil!", true)
                            }
                            else{
                                alertDialog("Konfirmasi!", "Perubahan daftar pengumpulan sampah anggota gagal!", true)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        alertDialog("Konfirmasi!", "Perubahan daftar pengumpulan sampah anggota gagal!", true)
                    }

                })
            } else {
                alertDialog("Konfirmasi!", "Perubahan daftar pengumpulan sampah anggota gagal!", true)
            }
        }

        // Ketika "deleteButton" di klik maka akan mengurangi saldo user sesuai dengan saldo dari data/child yang dihapus
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
                setMessage("Yakin hapus pengumpulan ${idPengumpulan}?")
                setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    referencePengumpulan.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val pengumpulan = snapshot.getValue(PengumpulanAnggota::class.java)!!
                            deleteTotal = pengumpulan.total.toLong()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                    referencePengumpulan.removeValue().addOnCompleteListener {
                        referenceAnggota.child(deleteIdAnggota).addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val users = snapshot.getValue(Users::class.java)!!
                                val tempTotal = users.saldo - deleteTotal
                                val userUpdate = Users(users.id, users.username, users.email, users.photoProfil, users.jumlahSetoran - 1, users.jumlahPenarikan, tempTotal, users.token )
                                referenceAnggota.child(deleteIdAnggota).setValue(userUpdate).addOnCompleteListener {
                                    referenceSampah.child(deleteIdSampah).addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val sampah = snapshot.getValue(DaftarSampah::class.java)!!
                                            val stockUpdate = DaftarSampah(deleteIdSampah, sampah.priceSampah, sampah.descriptionSampah, sampah.stockSampah - tempWeight, sampah.photoSampah )
                                            referenceSampah.child(deleteIdSampah).setValue(stockUpdate).addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    alertDialog("Konfirmasi!", "Pengumpulan dengan ID: ${idPengumpulan} berhasil dihapus!", true)
                                                } else {
                                                    alertDialog("Gagal!", "Gagal mengahapus Pengumpulan dengan ID: ${idPengumpulan}!", false)
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
                    if (!isConnected(this@EditDaftarPengumpulanSampahAdminActivity)){
                        showInternetDialog()
                    }
                })
        }
        alertDialog.show()
    }

    // Fungsi untuk melakukan pengecekan apakah ada internet atau tidak
    private fun isConnected(contextActivity: EditDaftarPengumpulanSampahAdminActivity): Boolean {
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