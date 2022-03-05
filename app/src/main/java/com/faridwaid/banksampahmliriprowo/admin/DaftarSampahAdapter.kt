package com.faridwaid.banksampahmliriprowo.admin

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.admin.EditDaftarSampahAdminActivity.Companion.EXTRA_SAMPAH
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import java.io.File
import java.text.DecimalFormat
import java.text.NumberFormat

class DaftarSampahAdapter(val list: ArrayList<DaftarSampah>, val method: DaftarSampahAdminActivity): RecyclerView.Adapter<DaftarSampahAdapter.DaftarViewHolder>() {

    // Membuat class DaftarViewHolder yang digunakan untuk set view yang akan ditampilkan,
    // Menggunakan picasso untuk loading image
    // Jika salah satu item dari recyclerview di klik, maka akan pindah ke EditDaftarSampahAdminActivity
    // kemudian variabel method digunakan untuk memanggil method dari class lain
    inner class DaftarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val typeSampah: TextView = itemView.findViewById(R.id.typeSampah)
        val priceSampah: TextView = itemView.findViewById(R.id.priceSampah)
        val descriptionSampah: TextView = itemView.findViewById(R.id.descriptionSampah)
        val stockSampah: TextView = itemView.findViewById(R.id.stockSampah)
        val imageSampah: RoundedImageView = itemView.findViewById(R.id.imageSampah)
        fun bind(sampah: DaftarSampah){
            with(itemView){
                typeSampah.text = sampah.nameSampah.toUpperCase()
                val formatter: NumberFormat = DecimalFormat("#,###")
                val myNumber = sampah.priceSampah.toLong()
                val formattedNumber: String = formatter.format(myNumber)
                priceSampah.text = "Rp. $formattedNumber / KG"
                descriptionSampah.text = "${sampah.descriptionSampah}"
                stockSampah.text = "${sampah.stockSampah}"
                Picasso.get().load(sampah.photoSampah).into(imageSampah)
                itemView.setOnClickListener {
                    val moveIntent = Intent(itemView.context, EditDaftarSampahAdminActivity::class.java)
                    moveIntent.putExtra(EXTRA_SAMPAH, sampah.nameSampah)
                    itemView.context.startActivity(moveIntent)
                    method.animationToTop()
                }
            }
        }
    }

    // Menentukan layout yang akan ditampilkan dalam recyclerview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaftarSampahAdapter.DaftarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daftar_sampah, parent, false)
        return DaftarViewHolder(view)
    }

    // Memasukkan data ke dalam list recyclerview seasui dengan posisi/position
    override fun onBindViewHolder(holder: DaftarSampahAdapter.DaftarViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Mendapatkan jumlah data dari list
    override fun getItemCount(): Int = list.size

}