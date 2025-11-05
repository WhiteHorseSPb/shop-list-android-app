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
    
    // Добавляем для точечных обновлений на MIUI
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        when (val item = getItem(position)) {
            is String -> {
                (holder as GroupHeaderViewHolder).bind(item)
            }
            is Product -> {
                if (payloads.isEmpty()) {
                    // Полная перерисовка только при необходимости
                    (holder as ProductViewHolder).bind(item, onProductChanged, onProductLongClick)
                } else {
                    // Точечное обновление для конкретных полей
                    payloads.forEach { payload ->
                        when (payload) {
                            "urgency_changed" -> {
                                (holder as ProductViewHolder).updateUrgency(item)
                            }
                        }
                    }
                }
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
        
        // Сохраняем текущий продукт и callbacks для доступа в обработчиках
        private var currentProduct: Product? = null
        private var currentOnProductChanged: ((Product) -> Unit)? = null
        private var currentOnProductLongClick: ((Product) -> Unit)? = null

        fun bind(product: Product, onProductChanged: ((Product) -> Unit)?, onProductLongClick: ((Product) -> Unit)?) {
            FileLogger.d("PRODUCT_ADAPTER", "=== BIND: ${product.name} ===")
            FileLogger.d("PRODUCT_ADAPTER", "INPUT DATA: urgent=${product.isUrgent}, buy=${product.needsToBuy}")
            
            // Сохраняем текущий продукт и callbacks
            currentProduct = product
            currentOnProductChanged = onProductChanged
            currentOnProductLongClick = onProductLongClick
            
            productName.text = product.name
            
            // Вызываем принудительное обновление визуальных элементов
            updateVisualElements(product)
            
            FileLogger.d("PRODUCT_ADAPTER", "=== END BIND: ${product.name} ===")
        }
        
        // Вынесен отдельный метод для обновления визуальных элементов
        private fun updateVisualElements(product: Product) {
            val isUrgent = product.isUrgent
            val needsToBuy = product.needsToBuy
            
            FileLogger.d("PRODUCT_ADAPTER", "UPDATE VISUAL: urgent=$isUrgent, buy=$needsToBuy")
            
            // Показываем переключатель для всех товаров
            switchBuy.visibility = View.VISIBLE
            switchBuy.isChecked = needsToBuy
            
            // Принудительно обновляем звездочку
            urgentStar.visibility = View.VISIBLE
            urgentStar.alpha = if (isUrgent) 1.0f else 0.4f
            urgentStar.setImageResource(if (isUrgent) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
            urgentStar.setColorFilter(itemView.context.getColor(R.color.urgent_color))
            
            // Принудительно обновляем фон
            val backgroundRes = if (isUrgent) R.color.urgent_background else R.drawable.product_item_background
            itemView.setBackgroundResource(backgroundRes)
            
            // Принудительно обновляем цвета переключателя
            val thumbColor = if (needsToBuy) {
                android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_active))
            } else {
                android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_thumb))
            }
            
            val trackColor = if (needsToBuy) {
                android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_active))
            } else {
                android.content.res.ColorStateList.valueOf(itemView.context.getColor(R.color.switch_track))
            }
            
            switchBuy.thumbTintList = thumbColor
            switchBuy.trackTintList = trackColor
            
            // Логируем финальное состояние визуальных элементов
            FileLogger.d("PRODUCT_ADAPTER", "VISUAL STATE: star_alpha=${urgentStar.alpha}, star_src=${if (isUrgent) "btn_star_big_on" else "btn_star_big_off"}, bg=$backgroundRes")
            
            // Принудительная инвалидация для MIUI - усиленная версия
            itemView.post {
                // Полная перерисовка для MIUI
                itemView.invalidate()
                urgentStar.invalidate()
                switchBuy.invalidate()
                
                // Дополнительные методы для MIUI
                itemView.requestLayout()
                urgentStar.requestLayout()
                switchBuy.requestLayout()
                
                // Принудительное обновление через короткую задержку
                itemView.postDelayed({
                    itemView.invalidate()
                    urgentStar.invalidate()
                    switchBuy.invalidate()
                }, 50)
            }
        }
        
        init {
            // Устанавливаем обработчики в init блоке с доступом к сохраненным переменным
            switchBuy.setOnCheckedChangeListener { _, isChecked ->
                val product = currentProduct
                val onProductChanged = currentOnProductChanged
                
                if (product != null && onProductChanged != null) {
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
                    onProductChanged.invoke(product)
                }
            }
            
            // Обработка клика на звездочку для пометки срочности
            urgentStar.setOnClickListener {
                val product = currentProduct
                val onProductLongClick = currentOnProductLongClick
                
                if (product != null && onProductLongClick != null) {
                    // Вызываем callback для изменения срочности, сохранения и пересортировки
                    onProductLongClick.invoke(product)
                    
                    // ПРИНУДИТЕЛЬНОЕ ОБНОВЛЕНИЕ: сразу после клика обновляем визуальные элементы
                    // Это гарантирует что визуальные элементы обновятся даже если DiffUtil не сработает
                    updateVisualElements(product)
                }
            }
            
            // Обработка долгого нажатия для пометки срочности (как альтернатива)
            itemView.setOnLongClickListener {
                val product = currentProduct
                val onProductLongClick = currentOnProductLongClick
                
                if (product != null && onProductLongClick != null) {
                    onProductLongClick.invoke(product)
                }
                true
            }
        }
        
        // Точечное обновление для MIUI совместимости
        fun updateUrgency(product: Product) {
            FileLogger.d("PRODUCT_ADAPTER", "=== UPDATE URGENCY: ${product.name} ===")
            FileLogger.d("PRODUCT_ADAPTER", "INPUT: urgent=${product.isUrgent}, buy=${product.needsToBuy}")
            
            // Обновляем сохраненный продукт
            currentProduct = product
            
            // Вызываем полное обновление визуальных элементов
            updateVisualElements(product)
            
            FileLogger.d("PRODUCT_ADAPTER", "=== END UPDATE URGENCY: ${product.name} ===")
        }
        
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        val result = when {
            oldItem is String && newItem is String -> oldItem == newItem
            oldItem is Product && newItem is Product -> oldItem.name == newItem.name
            else -> false
        }
        
        FileLogger.d("DIFF_UTIL", "areItemsTheSame: ${getItemInfo(oldItem)} == ${getItemInfo(newItem)} -> $result")
        return result
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        val result = when {
            oldItem is String && newItem is String -> oldItem == newItem
            oldItem is Product && newItem is Product -> {
                // Более строгое сравнение для MIUI совместимости
                val nameSame = oldItem.name == newItem.name
                val needsToBuySame = oldItem.needsToBuy == newItem.needsToBuy
                val isUrgentSame = oldItem.isUrgent == newItem.isUrgent
                
                FileLogger.d("DIFF_UTIL", "areContentsTheSame: ${getProductInfo(oldItem)} == ${getProductInfo(newItem)}")
                FileLogger.d("DIFF_UTIL", "  nameSame: $nameSame, needsToBuySame: $needsToBuySame, isUrgentSame: $isUrgentSame")
                
                nameSame && needsToBuySame && isUrgentSame
            }
            else -> false
        }
        
        FileLogger.d("DIFF_UTIL", "areContentsTheSame RESULT: $result")
        return result
    }
    
    // Добавляем для точечных обновлений на MIUI
    override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        val payload = when {
            oldItem is Product && newItem is Product -> {
                val urgencyChanged = oldItem.isUrgent != newItem.isUrgent
                FileLogger.d("DIFF_UTIL", "getChangePayload: ${getProductInfo(oldItem)} -> ${getProductInfo(newItem)}")
                FileLogger.d("DIFF_UTIL", "  urgencyChanged: $urgencyChanged")
                
                if (urgencyChanged) "urgency_changed" else null
            }
            else -> null
        }
        
        FileLogger.d("DIFF_UTIL", "getChangePayload RESULT: $payload")
        return payload
    }
    
    private fun getItemInfo(item: Any): String {
        return when (item) {
            is String -> "\"$item\""
            is Product -> getProductInfo(item)
            else -> "Unknown"
        }
    }
    
    private fun getProductInfo(product: Product): String {
        return "\"${product.name}(urgent=${product.isUrgent},buy=${product.needsToBuy})\""
    }
}
