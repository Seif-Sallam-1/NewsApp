package com.example.newapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.newapp.databinding.ActivitySettingsBinding
import com.example.newapp.databinding.AdProductItemBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val PREFS = "user_prefs"
    private val KEY_COUNTRY = "country_code"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //setupProductAds()


        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val savedCountry = prefs.getString(KEY_COUNTRY, "us") ?: "us"

        when (savedCountry) {
            "eg" -> binding.radioEg.isChecked = true
            "gb" -> binding.radioGb.isChecked = true
            "fr" -> binding.radioFr.isChecked = true
            "de" -> binding.radioDe.isChecked = true
            "it" -> binding.radioIt.isChecked = true
            else -> binding.radioUs.isChecked = true
        }

        binding.saveBtn.setOnClickListener {
            val selectedCode = when (binding.radioGroup?.checkedRadioButtonId) {
                binding.radioEg.id -> "eg"
                binding.radioGb.id -> "gb"
                binding.radioFr.id -> "fr"
                binding.radioDe.id -> "de"
                binding.radioIt.id -> "it"
                else -> "us"
            }
            prefs.edit().putString(KEY_COUNTRY, selectedCode).apply()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

//    private fun setupProductAds() {
//        val products = listOf(
//            mapOf("image" to R.drawable.product_1, "price" to "$49.99", "url" to "https://www.amazon.com/s?k=headphones" ),
//            mapOf("image" to R.drawable.product_2, "price" to "$129.50", "url" to "https://www.amazon.com/s?k=smart+watch" ),
//            mapOf("image" to R.drawable.product_3, "price" to "$14.95", "url" to "https://www.amazon.com/s?k=bestseller+books" ),
//            mapOf("image" to R.drawable.product_4, "price" to "$22.00", "url" to "https://www.amazon.com/s?k=coffee+mug" ),
//            mapOf("image" to R.drawable.product_5, "price" to "$35.75", "url" to "https://www.amazon.com/s?k=indoor+plant" )
//        )
//
//        val container = findViewById<LinearLayout>(R.id.product_ads_container)
//        val inflater = LayoutInflater.from(this)
//        container.removeAllViews()
//
//        for (product in products) {
//            val adItemBinding = AdProductItemBinding.inflate(inflater, container, false)
//            adItemBinding.productImage.setImageResource(product["image"] as Int)
//            adItemBinding.productPrice.text = product["price"] as String
//            adItemBinding.root.setOnClickListener {
//                val url = product["url"] as String
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                try {
//                    startActivity(intent)
//                } catch (e: Exception) {
//                    Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
//                }
//            }
//            container.addView(adItemBinding.root)
//        }
//    }

}
