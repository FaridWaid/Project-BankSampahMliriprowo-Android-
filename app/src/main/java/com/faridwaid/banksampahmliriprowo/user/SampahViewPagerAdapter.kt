package com.faridwaid.banksampahmliriprowo.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.admin.DaftarSampah
import com.faridwaid.banksampahmliriprowo.admin.DaftarSampahAdminActivity
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso

class SampahViewPagerAdapter(val list: ArrayList<DaftarSampah>): RecyclerView.Adapter<SampahViewPagerAdapter.DaftarViewHolder>() {

    // Membuat class DaftarViewHolder yang digunakan untuk set view yang akan ditampilkan,
    // Menggunakan picasso untuk loading image
    inner class DaftarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val typeSampah: TextView = itemView.findViewById(R.id.typeSampah)
        val priceSampah: TextView = itemView.findViewById(R.id.priceSampah)
        val descriptionSampah: TextView = itemView.findViewById(R.id.descriptionSampah)
        val imageSampah: RoundedImageView = itemView.findViewById(R.id.imageSampah)
        fun bind(sampah: DaftarSampah){
            with(itemView){
                typeSampah.text = sampah.nameSampah.toUpperCase()
                priceSampah.text = "Rp. ${sampah.priceSampah} / KG"
                descriptionSampah.text = "${sampah.descriptionSampah}"
                Picasso.get().load(sampah.photoSampah).into(imageSampah)
            }
        }
    }

    // Menentukan layout yang akan ditampilkan dalam recyclerview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampahViewPagerAdapter.DaftarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_viewpager_sampah, parent, false)
        return DaftarViewHolder(view)
    }

    // Memasukkan data ke dalam list recyclerview seasui dengan posisi/position
    override fun onBindViewHolder(holder: SampahViewPagerAdapter.DaftarViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Mendapatkan jumlah data dari list
    override fun getItemCount(): Int = list.size


}