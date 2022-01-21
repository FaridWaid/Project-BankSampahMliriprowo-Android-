package com.faridwaid.banksampahmliriprowo.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.PopupMenu
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.faridwaid.banksampahmliriprowo.LoginActivity
import com.faridwaid.banksampahmliriprowo.R
import com.google.firebase.auth.FirebaseAuth
import me.ibrahimsn.lib.SmoothBottomBar

class HomeActivity : AppCompatActivity() {

    // Mendefinisikan variabel global untuk connect ke Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Mendefinisikan NavController yang nantinya akan digunakan untuk control fragment
        val navController: NavController = findNavController(R.id.nav_host_fragment)

        // setup smooth bar menu
        val popUpMenu =  PopupMenu(this, null)
        popUpMenu.inflate(R.menu.bottom_nav_menu)
        val menu = popUpMenu.menu
        val navBottom: SmoothBottomBar = findViewById(R.id.nav_bottom)
        navBottom.setupWithNavController(menu, navController)


    }

}