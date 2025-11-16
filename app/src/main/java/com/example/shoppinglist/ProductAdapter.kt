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
                            "visual_changed" -> {
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
            // Сохраняем текущий продукт и callbacks
            currentProduct = product
            currentOnProductChanged = onProductChanged
            currentOnProductLongClick = onProductLongClick
            
            productName.text = product.name
            
            // Вызываем принудительное обновление визуальных элементов
            updateVisualElements(product)
        }
        
        // Вынесен отдельный метод для обновления визуальных элементов
        private fun updateVisualElements(product: Product) {
            val isUrgent = product.isUrgent
            val needsToBuy = product.needsToBuy
            val productGroup = product.getGroup()
            
            // Показываем переключатель для всех товаров
            switchBuy.visibility = View.VISIBLE
            switchBuy.isChecked = needsToBuy
            
            // Разное отображение для разных групп
            urgentStar.visibility = View.VISIBLE
            when (productGroup) {
                ProductGroup.URGENT_TO_BUY -> {
                    urgentStar.alpha = 1.0f
                    urgentStar.setImageResource(android.R.drawable.btn_star_big_on)
                    urgentStar.setColorFilter(itemView.context.getColor(R.color.urgent_color))
                }
                ProductGroup.TO_BUY -> {
                    urgentStar.alpha = 0.4f
                    urgentStar.setImageResource(android.R.drawable.btn_star_big_off)
                    urgentStar.setColorFilter(itemView.context.getColor(R.color.urgent_color))
                }
                ProductGroup.OTHER -> {
                    urgentStar.alpha = 0.3f  // Бледная звездочка для "Остальное"
                    urgentStar.setImageResource(android.R.drawable.btn_star_big_off)
                    urgentStar.setColorFilter(itemView.context.getColor(R.color.text_secondary))  // Серый цвет
                }
            }
            
            // Разный фон для разных групп - используем группировку, а не только флаг isUrgent
            when (productGroup) {
                ProductGroup.URGENT_TO_BUY -> {
                    // Розовый фон для срочных товаров со скругленными углами
                    itemView.setBackgroundResource(R.drawable.urgent_background_rounded)
                }
                ProductGroup.TO_BUY -> {
                    // Градиентный фон для "Важно"
                    itemView.setBackgroundResource(R.drawable.product_item_background_unified)
                }
                ProductGroup.OTHER -> {
                    // Белый фон для "Остальное"
                    itemView.setBackgroundResource(R.drawable.product_item_background_white)
                }
            }
            
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
            
            // Отладочное логирование
            android.util.Log.d("VISUAL_DEBUG", "=== UPDATE VISUAL ELEMENTS ===")
            android.util.Log.d("VISUAL_DEBUG", "Product: ${product.name}")
            android.util.Log.d("VISUAL_DEBUG", "isUrgent: $isUrgent, needsToBuy: $needsToBuy")
            android.util.Log.d("VISUAL_DEBUG", "Group: $productGroup")
            android.util.Log.d("VISUAL_DEBUG", "Background: ${when (productGroup) {
                ProductGroup.URGENT_TO_BUY -> "pink"
                ProductGroup.TO_BUY -> "gradient"
                ProductGroup.OTHER -> "white"
            }}")
            
            // Дополнительная проверка применения фона
            itemView.post {
                val currentBackground = when (productGroup) {
                    ProductGroup.URGENT_TO_BUY -> "pink"
                    ProductGroup.TO_BUY -> "gradient"
                    ProductGroup.OTHER -> "white"
                }
                android.util.Log.d("VISUAL_DEBUG", "Expected background: $currentBackground")
                android.util.Log.d("VISUAL_DEBUG", "Actual background resource: ${when (productGroup) {
                    ProductGroup.URGENT_TO_BUY -> R.drawable.urgent_background_rounded
                    ProductGroup.TO_BUY -> R.drawable.product_item_background_unified
                    ProductGroup.OTHER -> R.drawable.product_item_background_white
                }}")
            }
            
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
                    android.util.Log.d("VISUAL_DEBUG", "=== DELAYED UPDATE ===")
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
                    
                    // ПРИНУДИТЕЛЬНОЕ ОБНОВЛЕНИЕ ВИЗУАЛЬНЫХ ЭЛЕМЕНТОВ
                    // Обновляем визуальные элементы после изменения состояния переключателя
                    updateVisualElements(product)
                    
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
            // Обновляем сохраненный продукт
            currentProduct = product
            
            // Вызываем полное обновление визуальных элементов
            updateVisualElements(product)
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
                // Более строгое сравнение для MIUI совместимости
                val nameSame = oldItem.name == newItem.name
                val needsToBuySame = oldItem.needsToBuy == newItem.needsToBuy
                val isUrgentSame = oldItem.isUrgent == newItem.isUrgent
                val groupSame = oldItem.getGroup() == newItem.getGroup()
                
                nameSame && needsToBuySame && isUrgentSame && groupSame
            }
            else -> false
        }
    }
    
    // Добавляем для точечных обновлений на MIUI
    override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
        return when {
            oldItem is Product && newItem is Product -> {
                val urgencyChanged = oldItem.isUrgent != newItem.isUrgent
                val needsToBuyChanged = oldItem.needsToBuy != newItem.needsToBuy
                val groupChanged = oldItem.getGroup() != newItem.getGroup()
                
                when {
                    urgencyChanged -> "urgency_changed"
                    needsToBuyChanged || groupChanged -> "visual_changed"
                    else -> null
                }
            }
            else -> null
        }
    }
}
