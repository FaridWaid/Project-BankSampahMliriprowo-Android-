package com.faridwaid.banksampahmliriprowo.admin

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.EditDaftarPenjualanSampahAdminActivity.Companion.EXTRA_ID
import java.text.DecimalFormat
import java.text.NumberFormat

class PenjualanSampahAdapter(private var list: ArrayList<PenjualanSampah>, val method: DaftarPenjualanSampahAdminActivity): RecyclerView.Adapter<PenjualanSampahAdapter.DaftarPenjualanViewHolder>() {

    // Membuat class DaftarPenjualanViewHolder yang digunakan untuk set view yang akan ditampilkan,
    // Jika salah satu item dari recyclerview di klik, maka akan pindah ke EditDaftarPenjualanSampahAdminActivity
    // kemudian variabel method digunakan untuk memanggil method dari class lain
    inner class DaftarPenjualanViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val typeSampah: TextView = itemView.findViewById(R.id.textSampah)
        val buyerName: TextView = itemView.findViewById(R.id.buyerName)
        val buyerTelp: TextView = itemView.findViewById(R.id.buyerTelp)
        val priceSampah: TextView = itemView.findViewById(R.id.priceSampah)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        val datePenjualan: TextView = itemView.findViewById(R.id.datePenjualan)
        fun bind(penjualan: PenjualanSampah){
            with(itemView){
                val capitalize = penjualan.idSampah.capitalize()
                typeSampah.text = "$capitalize (${penjualan.weightSampah}KG)"
                val formatter: NumberFormat = DecimalFormat("#,###")
                val total = penjualan.total.toLong()
                val price = penjualan.priceSampah.toLong()
                val formattedNumberPrice: String = formatter.format(price)
                val formattedNumberTotal: String = formatter.format(total)
                buyerName.text = "Nama pembeli: ${penjualan.nameBuyer}"
                buyerTelp.text = "Nomer pembeli: ${penjualan.telpBuyer}"
                priceSampah.text = "Harga sampah: Rp. ${formattedNumberPrice}/KG"
                totalPrice.text = "Total yang didapat: Rp. ${formattedNumberTotal}"
                datePenjualan.text = "Tanggal pengumpulan: ${penjualan.datePenjualan}"
                itemView.setOnClickListener {
                    val moveIntent = Intent(itemView.context, EditDaftarPenjualanSampahAdminActivity::class.java)
                    moveIntent.putExtra(EXTRA_ID, penjualan.idPenjualan)
                    itemView.context.startActivity(moveIntent)
                    method.animationToTop()
                }
            }
        }
    }

    // Menentukan layout yang akan ditampilkan dalam recyclerview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaftarPenjualanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daftar_penjualan, parent, false)
        return DaftarPenjualanViewHolder(view)
    }

    // Memasukkan data ke dalam list recyclerview seasui dengan posisi/position
    override fun onBindViewHolder(holder: DaftarPenjualanViewHolder, position: Int) {
        holder.bind(list[position])
    }

    // Mendapatkan jumlah data dari list
    override fun getItemCount(): Int = list.size

    //Fungsi ini digunakan untuk memasukkan data ke dalam list
    fun filterList(filteredNames: ArrayList<PenjualanSampah>) {
        this.list = filteredNames
        notifyDataSetChanged()
    }

}