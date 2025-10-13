package com.example.newapp

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
class CategoryAdapter(val activity: Activity, val categories: ArrayList<Category>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder( view : View) : RecyclerView.ViewHolder(view){
        val parent: CardView= view.findViewById(R.id.category_card)
        val tv: TextView = view.findViewById(R.id.categoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = activity.layoutInflater.inflate(R.layout.category_list_item,parent,false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.tv.text = categories[position].title
    }

    override fun getItemCount() = categories.size
}
