package com.faridwaid.banksampahmliriprowo.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

        // Mendefinisikan variabel yang digunukan untuk toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Mengisi variabel auth dengan fungsi yang ada pada FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Mendefinisikan NavController yang nantinya akan digunakan untuk control fragment
        val navController: NavController = findNavController(R.id.nav_host_fragment)

        // appBarConfiguration digunakan untuk item menu pada toolbar akan berubah jika salah satu menu diklik
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_home, R.id.nav_profile
        ).build()

        // setup nav controller dan app bar
        setupActionBarWithNavController(navController, appBarConfiguration)

        // setup smooth bar menu
        val popUpMenu =  PopupMenu(this, null)
        popUpMenu.inflate(R.menu.bottom_nav_menu)
        val menu = popUpMenu.menu
        val navBottom: SmoothBottomBar = findViewById(R.id.nav_bottom)
        navBottom.setupWithNavController(menu, navController)


    }

    // fungsi untuk menampilkan menu option
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    // fungsi "onOptionsItemSelected" adalah fungsi ketika salah satu option ditekan maka akan do something
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // ketika option dengan id "logout" ditekan maka akan pindah intent/activity
        when(item.itemId){
            R.id.logout ->{
                auth.signOut()
                Intent(this@HomeActivity, LoginActivity::class.java).also { intent ->
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                return true
            }
            else -> return true
        }
    }

}