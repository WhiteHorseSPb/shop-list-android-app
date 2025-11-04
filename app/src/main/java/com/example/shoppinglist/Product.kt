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
        return when {
            isUrgent -> ProductGroup.URGENT_TO_BUY  // Все срочные товары в первую группу
            !isUrgent && needsToBuy -> ProductGroup.TO_BUY
            else -> ProductGroup.OTHER
        }
    }
}
