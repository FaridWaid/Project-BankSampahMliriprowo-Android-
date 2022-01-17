package com.faridwaid.banksampahmliriprowo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2

class BeginningAppActivity : AppCompatActivity() {

    // Mendeklarasikan data yang digunakan untuk slider view pager 2
    private val introSlideAdapter = IntroSlideAdapter(
        listOf(
            IntroSlide(
                "BANK SAMPAH MLIRIPROWO",
                "Bank sampah mliriprowo adalah organisasi peduli lingkungan yang berlokasi di Dusun Pilang, Desa Mliriprowo, Kecamatan Tarik, Kabupaten Sidoarjo.",
                R.drawable.bank_sampah
            ),
            IntroSlide(
                "Tahap 1: Pendaftaran",
                "Masyarakat yang ingin menyetorkan sampah harus mendaftar terlebih dahulu",
                R.drawable.pendaftaran
            ),
            IntroSlide(
                "Tahap 2: Pemilahan",
                "Sampah harus dipilah berdasar jenis - jenisnya",
                R.drawable.pemilahan
            ),
            IntroSlide(
                "Tahap 3: Penimbangan",
                "Sampah ditimbang untuk mengetahui berat sampah yang akan disetorkan",
                R.drawable.penimbangan
            ),
            IntroSlide(
                "Tahap 4: Mendapatkan Keuntungan",
                "Masyarakat dapat memperoleh sejumlah uang jika sampah berhasil dijual",
                R.drawable.get_profit
            )
        )
    )

    // Mendefinisikan variabel yang nantinya akan digunakan
    private lateinit var indicatorContainer: LinearLayout
    private lateinit var buttonNext: Button
    private lateinit var textSkipIntro: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beginning_app)

        // Mengambil Id dari view pager 2 dan dijadikan variabel
        val introSlideViewPager: ViewPager2 = findViewById(R.id.introSliderViewPager)
        // Mengset adapter view pager ke "introSlideAdapter"
        introSlideViewPager.adapter = introSlideAdapter

        // Mengambil Id dari view dan dijadikan variabel
        indicatorContainer = findViewById(R.id.indicatorsContainer)
        buttonNext = findViewById(R.id.buttonNext)
        textSkipIntro = findViewById(R.id.textSkipIntro)

        // Memanggil fungsi "setupIndicators" dan "setCurrentIndicators"
        // 0 digunakan untuk indicator pertama terletak pada posisi 0
        setupIndicators()
        setCurrentIndicators(0)

        // Menggunakan library dati view pager
        introSlideViewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicators(position)
            }
        })

        // Ketika "buttonNext" diklik akan pindah ke data selanjutnya,
        // dan jika sudah berada pada data terakhir maka akan pindah activity ke LoginActivity
        buttonNext.setOnClickListener {
            if (introSlideViewPager.currentItem + 1 < introSlideAdapter.itemCount){
                introSlideViewPager.currentItem += 1
            } else{
                // Pindah ke LoginActivity
                Intent(applicationContext, LoginActivity::class.java).also {
                    startActivity(it)
                    finish()
                }
            }
        }

        // Ketika "textSkipIntro" diklik akan pindah activity ke LoginActivity
        textSkipIntro.setOnClickListener {
            Intent(applicationContext, LoginActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

    }

    // Mendefinisakan fungsi "setupIndicators"
    private fun setupIndicators(){
        // Mendefinisikan varibale "indicators" yang bersisi jumlah dari data image yang ada ada introSlideAdapter
        val indicators = arrayOfNulls<ImageView>(introSlideAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices){
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            indicatorContainer.addView(indicators[i])
        }
    }

    // Mendefinisakan fungsi "setCurrentIndicators"
    private fun setCurrentIndicators(index: Int){
        val childCount = indicatorContainer.childCount
        for (i in 0 until childCount){
            val imageView = indicatorContainer[i] as ImageView
            if (i == index){
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

}