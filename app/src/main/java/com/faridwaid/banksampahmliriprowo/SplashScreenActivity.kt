package com.faridwaid.banksampahmliriprowo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        //Menyembunyikan action bar
        supportActionBar?.hide()

        //Pindah activity setelah beberapa detik
        Handler().postDelayed({
            val intent = Intent(this@SplashScreenActivity, BeginningAppActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)

    }
}