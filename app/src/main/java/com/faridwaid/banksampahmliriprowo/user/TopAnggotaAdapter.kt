package com.faridwaid.banksampahmliriprowo.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso

class TopAnggotaAdapter(private var list: ArrayList<Users>): RecyclerView.Adapter<TopAnggotaAdapter.TopAnggotaViewHolder>() {

    // Mendefinisikan variabel yang nantinya akan ditampilkan ke dalam view
    private var count: String = ""
    private var rankSaldo: String = "0"

    // Membuat class TopAnggotaViewHolder yang digunakan untuk set view yang akan ditampilkan,
    // Menggunakan picasso untuk loading image
    inner class TopAnggotaViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val typeUsername: TextView = itemView.findViewById(R.id.typeUser)
        val countPengumpulan: TextView = itemView.findViewById(R.id.countPengumpulan)
        val saldo: TextView = itemView.findViewById(R.id.saldo)
        val imageUser: RoundedImageView = itemView.findViewById(R.id.imageUser)
        fun bind(users: Users){
            with(itemView){
                typeUsername.text = "${users.username.toUpperCase()} ($count)"
                countPengumpulan.text = "${users.jumlahSetoran}"
                Picasso.get().load(users.photoProfil).into(imageUser)
                saldo.text = "Rp. $rankSaldo"
            }
        }
    }

    // Menentukan layout yang akan ditampilkan dalam recyclerview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopAnggotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_top_anggota, parent, false)
        return TopAnggotaViewHolder(view)
    }

    // Memasukkan data ke dalam list recyclerview seasui dengan posisi/position,
    // memeberikan value baru pada variabel dengan pengkodisian
    override fun onBindViewHolder(holder: TopAnggotaViewHolder, position: Int) {
        if (position == 0){
            count = "1st"
            rankSaldo = "5,000"
        } else if (position == 1){
            count = "2nd"
            rankSaldo = "4,000"
        } else{
            count = "3rd"
            rankSaldo = "3,000"
        }
        holder.bind(list[position])
    }

    // Mendapatkan jumlah data dari list
    override fun getItemCount(): Int = list.size

}