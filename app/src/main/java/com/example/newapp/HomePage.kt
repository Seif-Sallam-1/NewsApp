package com.example.newapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.newapp.databinding.ActivityHomePageBinding

class HomePage : AppCompatActivity() {
    lateinit var binding: ActivityHomePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val categories = arrayListOf<Category>(
            Category("Business","business"),
            Category("Entertainment","entertainment"),
            Category("General","general"),
            Category("Health","health"),
            Category("Science","science"),
            Category("Technology","technology"),
            Category("Sports","sports"),
        )
        val adapter = CategoryAdapter(this,categories)
        val rv: RecyclerView = findViewById(R.id.categoryRecyclerView)
    }
}