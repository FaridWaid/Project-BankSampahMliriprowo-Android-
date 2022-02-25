package com.faridwaid.banksampahmliriprowo.admin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.EditDaftarPenarikanSaldoAdminActivity.Companion.EXTRA_ID
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.NumberFormat

class DaftarPenarikanSaldoAdminAdapter(private var list: ArrayList<PenarikanSaldo>, val method: DaftarPenarikanSaldoAdminActivity): RecyclerView.Adapter<DaftarPenarikanSaldoAdminAdapter.DaftarPenarikanViewHolder>() {

    // Membuat class DaftarPenarikanViewHolder yang digunakan untuk set view yang akan ditampilkan,
    // Jika salah satu item dari recyclerview di klik, maka akan pindah ke EditDaftarPenarikanSaldoAdminActivity
    // kemudian variabel method digunakan untuk memanggil method dari class lain
    inner class DaftarPenarikanViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val typeUsername: TextView = itemView.findViewById(R.id.userPenarik)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPenarikan)
        val datePengumpulan: TextView = itemView.findViewById(R.id.datePenarikan)
        fun bind(penarikan: PenarikanSaldo){
            with(itemView){
                val referenceAnggota = FirebaseDatabase.getInstance().getReference("users").child(penarikan.idAnggota)
                referenceAnggota.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(Users::class.java)!!
                        typeUsername.text = user.username.toUpperCase()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                val formatter: NumberFormat = DecimalFormat("#,###")
                val myNumber = penarikan.totalPenarikan.toLong()
                val formattedNumber: String = formatter.format(myNumber)
                totalPrice.text = "Total penarikan saldo: Rp. ${formattedNumber}"
                datePengumpulan.text = "Tanggal penarikan: ${penarikan.datePenarikan}"
                itemView.setOnClickListener {
                    val moveIntent = Intent(itemView.context, EditDaftarPenarikanSaldoAdminActivity::class.java)
                    moveIntent.putExtra(EXTRA_ID, penarikan.idPenarikan)
                    itemView.context.startActivity(moveIntent)
                    method.animationToTop()
                }
            }
        }
    }

    // Menentukan layout yang akan ditampilkan dalam recyclerview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaftarPenarikanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daftar_penarikan, parent, false)
        return DaftarPenarikanViewHolder(view)
    }

    // Memasukkan data ke dalam list recyclerview seasui dengan posisi/position
    override fun onBindViewHolder(holder: DaftarPenarikanViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Mendapatkan jumlah data dari list
    override fun getItemCount(): Int = list.size

    //Fungsi ini digunakan untuk memasukkan data ke dalam list
    fun filterList(filteredNames: ArrayList<PenarikanSaldo>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }


}