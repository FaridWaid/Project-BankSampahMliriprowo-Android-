package com.faridwaid.banksampahmliriprowo.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.DaftarAnggotaAdapter
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso

class DaftarAnggotaUserAdapter(private var list: ArrayList<Users>): RecyclerView.Adapter<DaftarAnggotaUserAdapter.DaftarAnggotaViewHolder>() {

    // Membuat class DaftarViewHolder yang digunakan untuk set view yang akan ditampilkan,
    // Menggunakan picasso untuk loading image
    inner class DaftarAnggotaViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val typeUsername: TextView = itemView.findViewById(R.id.typeUsername)
        val countPengumpulan: TextView = itemView.findViewById(R.id.countPengumpulan)
        val imageUser: RoundedImageView = itemView.findViewById(R.id.imageUser)
        fun bind(users: Users){
            with(itemView){
                typeUsername.text = users.username.toUpperCase()
                countPengumpulan.text = "Jumlah Pengumpulan: ${users.jumlahSetoran}"
                Picasso.get().load(users.photoProfil).into(imageUser)
            }
        }
    }

    // Menentukan layout yang akan ditampilkan dalam recyclerview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaftarAnggotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daftar_anggota, parent, false)
        return DaftarAnggotaViewHolder(view)
    }

    // Memasukkan data ke dalam list recyclerview seasui dengan posisi/position
    override fun onBindViewHolder(holder: DaftarAnggotaViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Mendapatkan jumlah data dari list
    override fun getItemCount(): Int = list.size

    //Fungsi ini digunakan untuk memasukkan data ke dalam list
    fun filterList(filteredNames: ArrayList<Users>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }

}