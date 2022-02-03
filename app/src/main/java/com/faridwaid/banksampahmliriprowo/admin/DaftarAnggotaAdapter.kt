package com.faridwaid.banksampahmliriprowo.admin

import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.faridwaid.banksampahmliriprowo.R
import com.faridwaid.banksampahmliriprowo.Users
import com.faridwaid.banksampahmliriprowo.admin.EditDaftarAnggotaAdminActivity.Companion.EXTRA_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso

class DaftarAnggotaAdapter(private var list: ArrayList<Users>, val method: DaftarAnggotaAdminActivity): RecyclerView.Adapter<DaftarAnggotaAdapter.DaftarAnggotaViewHolder>() {

    // Membuat class DaftarViewHolder yang digunakan untuk set view yang akan ditampilkan,
    // Menggunakan picasso untuk loading image
    // Jika salah satu item dari recyclerview di klik, maka akan pindah ke EditDaftarAnggotaAdminActivity
    // kemudian variabel method digunakan untuk memanggil method dari class lain
    // Jika deleteUser diklik, maka akan menjalinkan
    inner class DaftarAnggotaViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val typeUsername: TextView = itemView.findViewById(R.id.typeUsername)
        val saldoUser: TextView = itemView.findViewById(R.id.saldoUser)
        val imageUser: RoundedImageView = itemView.findViewById(R.id.imageUser)
        val deleteUser: RoundedImageView = itemView.findViewById(R.id.deleteUser)
        fun bind(users: Users){
            with(itemView){
                typeUsername.text = users.username.toUpperCase()
                saldoUser.text = "Saldo: Rp. ${users.saldo}"
                Picasso.get().load(users.photoProfil).into(imageUser)
                itemView.setOnClickListener {
                    val moveIntent = Intent(itemView.context, EditDaftarAnggotaAdminActivity::class.java)
                    moveIntent.putExtra(EXTRA_ID, users.id)
                    itemView.context.startActivity(moveIntent)
                    method.animationToTop()
                }
                deleteUser.setOnClickListener {
                    val alertDialog = AlertDialog.Builder(context)
                    alertDialog.apply {
                        setTitle("Konfirmasi")
                        setMessage("Yakin hapus anggota ${users.username}?")
                        setNegativeButton("Batal", DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                        })
                        setPositiveButton("Hapus", DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                            val reference = FirebaseDatabase.getInstance().getReference("users").child("${users.id}")
                            reference.removeValue().addOnCompleteListener {
                                val ref = FirebaseStorage.getInstance().reference.child("img/${users.id}")
                                ref.delete().addOnCompleteListener {
                                    if (it.isSuccessful){
                                        Toast.makeText(context, "Anggota tersebut berhasil dihapus!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Anggota tersebut gagal dihapus!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        })
                    }
                    alertDialog.show()
                }
            }
        }
    }

    // Menentukan layout yang akan ditampilkan dalam recyclerview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaftarAnggotaAdapter.DaftarAnggotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daftar_users, parent, false)
        return DaftarAnggotaViewHolder(view)
    }

    // Memasukkan data ke dalam list recyclerview seasui dengan posisi/position
    override fun onBindViewHolder(holder: DaftarAnggotaAdapter.DaftarAnggotaViewHolder, position: Int) {
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