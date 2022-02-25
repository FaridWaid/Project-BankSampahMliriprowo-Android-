package com.faridwaid.banksampahmliriprowo.admin

import android.app.DatePickerDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class EditDaftarPenarikanSaldoAdminActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var referencePenarikan: DatabaseReference
    // Mendefinisikan variabel global dari view
    private lateinit var autoComplete: AutoCompleteTextView
    private lateinit var etTotal: EditText
    private lateinit var textDate: TextView
    private lateinit var updateButton: Button
    private lateinit var updateDate: String
    private var tempTotal by Delegates.notNull<Long>()
    private var currentWeight by Delegates.notNull<Int>()
    private var currentTotal by Delegates.notNull<Long>()
    private var totalPrice by Delegates.notNull<Long>()
    private var deleteTotal by Delegates.notNull<Long>()
    private var checkSaldo by Delegates.notNull<Boolean>()
    private lateinit var IdAnggota: String
    private lateinit var deleteIdSampah: String

    // Mendefinisikan companion object yang akan digunakan untuk menerima data
    companion object{
        const val EXTRA_ID = "extra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_daftar_penarikan_saldo_admin)

        //mendapatkan id penarikan untuk set data penarikan
        val idPenarikan = intent.getStringExtra(EXTRA_ID)!!

        // Mendefinisikan variabel yang nantinya akan berisi inputan user
        autoComplete = findViewById(R.id.autoCompleteTextView)
        etTotal = findViewById(R.id.etTotal)
        textDate = findViewById(R.id.date)
        updateButton = findViewById(R.id.btnUpdate)
        tempTotal = 0
        totalPrice = 0
        deleteTotal = 0
        currentTotal = 0
        checkSaldo = false
        IdAnggota = ""
        deleteIdSampah = ""

        // Membuat reference yang nantinya akan digunakan untuk melakukan aksi ke database
        referencePenarikan = FirebaseDatabase.getInstance().getReference("daftarpenarikan").child("$idPenarikan")

        // Mengambil data penarikan dengan referencePenarikan dan dimasukkan kedalam view (text,etc)
        val menuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val penarikan = dataSnapshot.getValue(PenarikanSaldo::class.java)
                etTotal.setText(penarikan?.totalPenarikan)
                tempTotal = penarikan?.totalPenarikan!!.toLong()
                textDate.setText(penarikan?.datePenarikan)
                updateDate = penarikan?.datePenarikan
                val referenceAnggota = FirebaseDatabase.getInstance().getReference("users").child(penarikan?.idAnggota!!)
                referenceAnggota.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val users = snapshot.getValue(Users::class.java)!!
                        autoComplete.setText(users.username)
                        IdAnggota = users.id
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
        referencePenarikan.addListenerForSingleValueEvent(menuListener)

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
        }

        //ketika button di klik calender dialog akan muncul
        btnChangeDate.setOnClickListener {
            DatePickerDialog(this, datePicker, myCalender.get(Calendar.YEAR), myCalender.get(
                Calendar.MONTH),
                myCalender.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Ketika "updateButton" di klik maka akan melakukan aksi
        updateButton.setOnClickListener {
            // Membuat variabel baru yang berisi inputan user
            val totalInput = etTotal.text.toString().trim()

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

            // Mengecek data stock sampah, jika tersedia maka nilai checkStock menjadi true,
            // Jika tidak tersedia maka akan mucul alert dialog gagal, dan gagal melakukan transaksi
            val referenceAnggota = FirebaseDatabase.getInstance().getReference("users").child(IdAnggota)
            referenceAnggota.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = snapshot.getValue(Users::class.java)!!
                    val saldoUser = users.saldo + tempTotal
                    if (saldoUser >= totalInput.toLong()){
                        currentTotal = totalInput.toLong() - tempTotal
                        checkSaldo = true
                    } else{
                        alertDialog("Gagal!", "Gagal memperbarui data penarikan, jumlah saldo anggota kurang!", false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

            // Jika checkSaldo bernilai true, maka akan menjalankan aksi
            if (checkSaldo == true){
                // Melakukan pengecekan terhadap totalInput apakah saldo mencukupi,
                // Jika tidak mencukupi maka akan menampilkan alert dialog
                // Jika mencukupi akan melakukan update saldo pada database bank sampah
                val refereneBankSampah = FirebaseDatabase.getInstance().getReference("banksampah").child("saldobank")
                refereneBankSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val bank = snapshot.getValue(BankSampah::class.java)!!
                        val saldoBank = bank.totalSaldo + tempTotal
                        if (saldoBank < totalInput.toLong()){
                            alertDialog("Gagal!", "Saldo bank sampah tidak cukup untuk melakukan penarikan saldo dengan jumlah $totalInput, mohon melakukan penarikan ketika bank sampah sudah memiliki cukup saldo!", false)
                        } else {
                            val newSaldo = saldoBank - totalInput.toLong()
                            val updateSaldo = BankSampah(newSaldo)
                            refereneBankSampah.setValue(updateSaldo).addOnCompleteListener {
                                // Melakukan update data pada data penarikan
                                referencePenarikan.addListenerForSingleValueEvent(object : ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val penarikan = snapshot.getValue(PenarikanSaldo::class.java)!!
                                        val updatePenarikan = PenarikanSaldo(idPenarikan, updateDate, penarikan.idAnggota, totalInput)
                                        referencePenarikan.setValue(updatePenarikan).addOnCompleteListener {
                                            // Melakukan update data pada data user, kemudian saldo berasal dari penguangan,
                                            // saldo yang ada pada database dengan currentTotal.
                                            // Jika berhasil maka akan menampilkan alert dialog
                                            val referenceAnggota = FirebaseDatabase.getInstance().getReference("users").child(IdAnggota)
                                            referenceAnggota.addListenerForSingleValueEvent(object : ValueEventListener{
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val users = snapshot.getValue(Users::class.java)!!
                                                    val updateSaldo = users.saldo - currentTotal
                                                    val userUpdate = Users(users.id, users.username, users.email, users.photoProfil, users.jumlahSetoran, users.jumlahPenarikan, updateSaldo, users.token )
                                                    referenceAnggota.setValue(userUpdate).addOnCompleteListener {
                                                        if (it.isSuccessful){
                                                            alertDialog("Konfirmasi!", "Berhasil memperbarui daftar penarikan saldo anggota !", true)
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
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
        }

        // Ketika "deleteButton" di klik maka akan menghapus data referencePenarikan,
        // dan menambahkan saldo kembali ke user
        // Kemudian menambah saldo pada bank sampah
        val deleteButton: Button = findViewById(R.id.btnDelete)
        deleteButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.apply {
                setTitle("Konfirmasi")
                setMessage("Yakin hapus penarikan ${idPenarikan}?")
                setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                })
                setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    referencePenarikan.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val penarikan = snapshot.getValue(PenarikanSaldo::class.java)!!
                            deleteTotal = penarikan.totalPenarikan.toLong()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
                    referencePenarikan.removeValue().addOnCompleteListener {
                        val referenceAnggota = FirebaseDatabase.getInstance().getReference("users").child(IdAnggota)
                        referenceAnggota.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val users = snapshot.getValue(Users::class.java)!!
                                val updateSaldo = users.saldo + deleteTotal
                                val userUpdate = Users(users.id, users.username, users.email, users.photoProfil, users.jumlahSetoran, users.jumlahPenarikan - 1, updateSaldo, users.token )
                                referenceAnggota.setValue(userUpdate).addOnCompleteListener {
                                    val refereneBankSampah = FirebaseDatabase.getInstance().getReference("banksampah").child("saldobank")
                                    refereneBankSampah.addListenerForSingleValueEvent(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val bank = snapshot.getValue(BankSampah::class.java)!!
                                            val updateSaldoBank = bank.totalSaldo + deleteTotal
                                            val newSaldoBank = BankSampah(updateSaldoBank)
                                            refereneBankSampah.setValue(newSaldoBank).addOnCompleteListener {
                                                if (it.isSuccessful){
                                                    alertDialog("Konfirmasi!", "Penarikan dengan ID: ${idPenarikan} berhasil dihapus!", true)
                                                } else {
                                                    alertDialog("Gagal!", "Gagal mengahapus penarikan dengan ID: ${idPenarikan}!", false)
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