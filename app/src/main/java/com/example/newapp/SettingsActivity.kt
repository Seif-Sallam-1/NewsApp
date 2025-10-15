package com.example.newapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.newapp.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val PREFS = "user_prefs"
    private val KEY_COUNTRY = "country_code"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
            val selectedCode = when (binding.radioGroup.checkedRadioButtonId) {
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
}
