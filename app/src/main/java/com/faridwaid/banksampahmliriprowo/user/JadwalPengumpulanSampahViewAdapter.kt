package com.faridwaid.banksampahmliriprowo.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.admin.JadwalPengumpulanSampah
import com.makeramen.roundedimageview.RoundedImageView

class JadwalPengumpulanSampahViewAdapter(val list: ArrayList<JadwalPengumpulanSampah>): RecyclerView.Adapter<JadwalPengumpulanSampahViewAdapter.JadwalViewHolder>() {

    // Membuat class JadwalViewHolder yang digunakan untuk set view yang akan ditampilkan,
    // Kemudian membuat kondisi dengan "jadwal.hari" untuk menentukan icon yang akan ditampilkan
    inner class JadwalViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val scheduleDay: TextView = itemView.findViewById(R.id.scheduleDay)
        val scheduleType: TextView = itemView.findViewById(R.id.scheduleType)
        val imageDate: RoundedImageView = itemView.findViewById(R.id.imageJadwal)
        fun bind(jadwal: JadwalPengumpulanSampah){
            with(itemView){
                scheduleDay.text = jadwal.hari.toUpperCase()
                scheduleType.text = jadwal.type
                if (jadwal.hari == "senin"){
                    imageDate.setImageResource(R.drawable.monday)
                } else if (jadwal.hari == "selasa"){
                    imageDate.setImageResource(R.drawable.tuesday)
                } else if (jadwal.hari == "rabu"){
                    imageDate.setImageResource(R.drawable.wednesday)
                } else if (jadwal.hari == "kamis"){
                    imageDate.setImageResource(R.drawable.thursday)
                } else if (jadwal.hari == "jumat"){
                    imageDate.setImageResource(R.drawable.friday)
                } else if (jadwal.hari == "sabtu"){
                    imageDate.setImageResource(R.drawable.saturday)
                } else if (jadwal.hari == "minggu"){
                    imageDate.setImageResource(R.drawable.sunday)
                } else {
                    imageDate.setImageResource(R.drawable.schedule)
                }
            }
        }
    }

    // Menentukan layout yang akan ditampilkan dalam recyclerview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JadwalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_jadwal_pengumpulan_sampah, parent, false)
        return JadwalViewHolder(view)
    }

    // Memasukkan data ke dalam list recyclerview seasui dengan posisi/position
    override fun onBindViewHolder(holder: JadwalViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Mendapatkan jumlah data dari list
    override fun getItemCount(): Int = list.size

}
