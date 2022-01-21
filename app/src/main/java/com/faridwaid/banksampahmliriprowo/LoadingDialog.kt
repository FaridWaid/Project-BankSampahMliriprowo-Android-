package com.faridwaid.banksampahmliriprowo

import android.app.Activity
import android.app.AlertDialog

class LoadingDialog(val mActivity:Activity) {

    // Mendefinisikan variabel global dari view
    private lateinit var isDialog: AlertDialog

    // Membuat fungsi startDialog(), diguakan untuk mendefiniskan alertdialog yang nantinya akan diisi loading dialog
    fun startDialog(){
        //Set View
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_bar, null)
        //set Dialog
        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        isDialog = builder.create()
        isDialog.show()
    }

    // Membuat fungsi isDissmis(), diguakan untuk dismiss dari alert dialog
    fun isDissmis(){
        isDialog.dismiss()
    }

}