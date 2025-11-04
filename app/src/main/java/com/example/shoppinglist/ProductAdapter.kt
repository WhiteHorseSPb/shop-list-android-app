package com.example.shoppinglist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.example.shoppinglist.R

class ProductAdapter : ListAdapter<Any, RecyclerView.ViewHolder>(ProductDiffCallback()) {
    
    var onProductChanged: ((Product) -> Unit)? = null
    var onProductLongClick: ((Product) -> Unit)? = null

    companion object {
        const val TYPE_GROUP_HEADER = 0
        const val TYPE_PRODUCT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is String -> TYPE_GROUP_HEADER
            is Product -> TYPE_PRODUCT
            else -> TYPE_PRODUCT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_GROUP_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.group_header_item, parent, false)
                GroupHeaderViewHolder(view)
            }
            TYPE_PRODUCT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.product_item, parent, false)
                ProductViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.product_item, parent, false)
                ProductViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is String -> {
                (holder as GroupHeaderViewHolder).bind(item)
            }
            is Product -> {
                (holder as ProductViewHolder).bind(item, onProductChanged, onProductLongClick)
            }
        }
    }

    inner class GroupHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupTitle: TextView = itemView.findViewById(R.id.groupTitle)

        fun bind(title: String) {
            groupTitle.text = title
        }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.productName)
        private val switchBuy: SwitchMaterial = itemView.findViewById(R.id.switchBuy)
        private val urgentStar: ImageView = itemView.findViewById(R.id.urgentStar)

        fun bind(product: Product, onProductChanged: ((Product) -> Unit)?, onProductLongClick: ((Product) -> Unit)?) {
            productName.text = product.name
            
            // Определяем группу товара
            val group = product.getGroup()
            
            // Показываем переключатель для всех товаров
            switchBuy.visibility = View.VISIBLE
            switchBuy.isChecked = product.needsToBuy
            
            // Показываем звездочку для всех товаров (заполненная для срочных, пустая для остальных)
            urgentStar.visibility = View.VISIBLE
            urgentStar.alpha = if (product.isUrgent) 1.0f else 0.4f
            
            // Устанавливаем иконку звездочки в зависимости от статуса
            urgentStar.setImageResource(
                if (product.isUrgent) 
                    android.R.drawable.btn_star_big_on 
                else 
                    android.R.drawable.btn_star_big_off
            )
            
            urgentStar.setColorFilter(itemView.context.getColor(R.color.urgent_color))
            
            // Устанавливаем фон для срочных товаров
            itemView.setBackgroundResource(if (product.isUrgent) R.color.urgent_background else R.drawable.product_item_background)
            
            // Устанавливаем зеленый цвет для активного состояния (только если переключатель виден)
            if (switchBuy.visibility == View.VISIBLE) {
                switchBuy.thumbTintList = if (product.needsToBuy) {
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_active))
                } else {
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_thumb))
                }
                
                switchBuy.trackTintList = if (product.needsToBuy) {
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_active))
                } else {
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_track))
                }
            }

            switchBuy.setOnCheckedChangeListener { _, isChecked ->
                product.needsToBuy = isChecked
                
                // Обновляем цвета при изменении состояния
                switchBuy.thumbTintList = if (isChecked) {
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_active))
                } else {
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_thumb))
                }
                
                switchBuy.trackTintList = if (isChecked) {
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_active))
                } else {
                    android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_track))
                }
                
                // Уведомляем об изменении для сохранения и пересортировки
                onProductChanged?.invoke(product)
            }
            
            // Обработка клика на звездочку для пометки срочности
            urgentStar.setOnClickListener {
                // Вызываем callback для изменения срочности, сохранения и пересортировки
                onProductLongClick?.invoke(product)
            }
            
            // Обработка долгого нажатия для пометки срочности (как альтернатива)
            itemView.setOnLongClickListener {
                onProductLongClick?.invoke(product)
                true
            }
        }
        
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is String && newItem is String -> oldItem == newItem
            oldItem is Product && newItem is Product -> oldItem.name == newItem.name
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is String && newItem is String -> oldItem == newItem
            oldItem is Product && newItem is Product -> {
                // Всегда считаем содержимое разным для продуктов, чтобы гарантировать перерисовку
                // Это решает проблему с визуальным обновлением при изменении состояния
                false
            }
            else -> false
        }
    }
}
