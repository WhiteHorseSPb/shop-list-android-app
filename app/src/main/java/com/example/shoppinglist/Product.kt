package com.example.shoppinglist

enum class ProductGroup {
    URGENT_TO_BUY,    // Товары со звездочкой и для покупки
    TO_BUY,           // Товары для покупки
    OTHER              // Остальные товары
}

data class Product(
    val name: String,
    var needsToBuy: Boolean = false,
    var isUrgent: Boolean = false
) {
    fun getGroup(): ProductGroup {
        android.util.Log.d("PRODUCT_GROUP", "=== ГРУППИРОВКА: $name ===")
        android.util.Log.d("PRODUCT_GROUP", "isUrgent: $isUrgent, needsToBuy: $needsToBuy")
        
        val group = when {
            isUrgent && needsToBuy -> ProductGroup.URGENT_TO_BUY  // Срочно: звездочка + переключатель вправо
            !isUrgent && needsToBuy -> ProductGroup.TO_BUY      // Важно: нет звездочки + переключатель вправо
            !isUrgent && !needsToBuy -> ProductGroup.OTHER      // Остальное: нет звездочки + переключатель влево
            else -> ProductGroup.OTHER  // Сюда попадают: isUrgent && !needsToBuy (звездочка + переключатель влево)
        }
        
        android.util.Log.d("PRODUCT_GROUP", "RESULT: $group (${getGroupName(group)})")
        android.util.Log.d("PRODUCT_GROUP", "=== END ГРУППИРОВКА: $name ===")
        return group
    }
    
    private fun getGroupName(group: ProductGroup): String {
        return when (group) {
            ProductGroup.URGENT_TO_BUY -> "СРОЧНО"
            ProductGroup.TO_BUY -> "ВАЖНО"
            ProductGroup.OTHER -> "ОСТАЛЬНОЕ"
        }
    }
}
