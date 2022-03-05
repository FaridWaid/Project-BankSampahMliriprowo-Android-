package com.faridwaid.banksampahmliriprowo.user

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.DaftarPengumpulanSampahAdminActivity
import com.faridwaid.banksampahmliriprowo.admin.EditDaftarPengumpulanSampahAdminActivity
import com.faridwaid.banksampahmliriprowo.admin.PengumpulanAnggota
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.NumberFormat

class DaftarPengumpulanSampahUserAdapter(private var list: ArrayList<PengumpulanAnggota>): RecyclerView.Adapter<DaftarPengumpulanSampahUserAdapter.DaftarPengumpulanViewHolder>() {

    // Membuat class DaftarPengumpulanViewHolder yang digunakan untuk set view yang akan ditampilkan
    inner class DaftarPengumpulanViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val typeUsername: TextView = itemView.findViewById(R.id.userPengumpul)
        val typeSampah: TextView = itemView.findViewById(R.id.typeSampah)
        val priceSampah: TextView = itemView.findViewById(R.id.priceSampah)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        val datePengumpulan: TextView = itemView.findViewById(R.id.datePengumpulan)
        fun bind(pengumpulan: PengumpulanAnggota){
            with(itemView){
                val referenceSampah = FirebaseDatabase.getInstance().getReference("users").child(pengumpulan.idAnggota)
                referenceSampah.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(Users::class.java)!!
                        typeUsername.text = user.username.toUpperCase()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
                val capitalize = pengumpulan.idSampah.capitalize()
                val formatter: NumberFormat = DecimalFormat("#,###")
                val myNumber = pengumpulan.total.toLong()
                val myNumber2 = pengumpulan.priceSampah.toLong()
                val formattedNumber: String = formatter.format(myNumber)
                val formattedNumber2: String = formatter.format(myNumber2)
                typeSampah.text = "Jenis sampah: ${capitalize}(${pengumpulan.weightSampah}KG)"
                priceSampah.text = "Harga sampah: $formattedNumber2/KG"
                totalPrice.text = "Total yang didapat: Rp. ${formattedNumber}"
                datePengumpulan.text = "Tanggal pengumpulan: ${pengumpulan.datePengumpulan}"
            }
        }
    }

    // Menentukan layout yang akan ditampilkan dalam recyclerview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaftarPengumpulanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daftar_pengumpulan, parent, false)
        return DaftarPengumpulanViewHolder(view)
    }

    // Memasukkan data ke dalam list recyclerview seasui dengan posisi/position
    override fun onBindViewHolder(holder: DaftarPengumpulanViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Mendapatkan jumlah data dari list
    override fun getItemCount(): Int = list.size

    //Fungsi ini digunakan untuk memasukkan data ke dalam list
    fun filterList(filteredNames: ArrayList<PengumpulanAnggota>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }

}