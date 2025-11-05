package com.example.shoppinglist

/**
 * Менеджер для управления списком продуктов
 * Отвечает за бизнес-логику: добавление, удаление, сортировку, группировку
 */
class ProductManager {
    
    private val _products = mutableListOf<Product>()
    val products: List<Product> get() = _products.toList()
    
    /**
     * Добавляет новый продукт в список
     */
    fun addProduct(product: Product): Boolean {
        if (_products.any { it.name.equals(product.name, ignoreCase = true) }) {
            return false // Товар уже существует
        }
        _products.add(product)
        return true
    }
    
    /**
     * Удаляет продукт по имени
     */
    fun removeProduct(name: String): Boolean {
        return _products.removeAll { it.name.equals(name, ignoreCase = true) }
    }
    
    /**
     * Обновляет статус покупки продукта
     * Звездочка снимается только если товар был в группе "Срочно" и его выключают из покупки
     */
    fun updateProductPurchaseStatus(name: String, needsToBuy: Boolean): Boolean {
        FileLogger.d("UPDATE_PRODUCT", "=== START UPDATE: $name to needsToBuy=$needsToBuy ===")
        FileLogger.d("UPDATE_PRODUCT", "BEFORE: ${_products.map { "${it.name}:buy=${it.needsToBuy},urgent=${it.isUrgent}" }}")
        
        val index = _products.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        if (index != -1) {
            val product = _products[index]
            val currentGroup = product.getGroup()
            
            product.needsToBuy = needsToBuy
            
            // Звездочку снимаем только если товар был в "Срочно" и его выключают из покупки
            if (currentGroup == ProductGroup.URGENT_TO_BUY && !needsToBuy) {
                product.isUrgent = false
            }
            
            FileLogger.d("UPDATE_PRODUCT", "AFTER: ${_products.map { "${it.name}:buy=${it.needsToBuy},urgent=${it.isUrgent}" }}")
            FileLogger.d("UPDATE_PRODUCT", "=== END UPDATE: $name ===")
            
            return true
        }
        return false
    }
    
    /**
     * Переключает срочность продукта
     * Если товар становится срочным, он автоматически становится нужным для покупки
     */
    fun toggleProductUrgency(name: String): Boolean {
        FileLogger.d("PRODUCT_MANAGER", "=== TOGGLE URGENCY: $name ===")
        
        val index = _products.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        if (index != -1) {
            val product = _products[index]
            val oldUrgent = product.isUrgent
            val oldNeedsToBuy = product.needsToBuy
            
            product.isUrgent = !product.isUrgent
            // Если товар становится срочным, он автоматически становится нужным для покупки
            if (product.isUrgent) {
                product.needsToBuy = true
            }
            
            FileLogger.d("PRODUCT_MANAGER", "BEFORE: urgent=$oldUrgent, buy=$oldNeedsToBuy")
            FileLogger.d("PRODUCT_MANAGER", "AFTER: urgent=${product.isUrgent}, buy=${product.needsToBuy}")
            FileLogger.d("PRODUCT_MANAGER", "=== END TOGGLE URGENCY: $name ===")
            
            return true
        }
        
        FileLogger.d("PRODUCT_MANAGER", "PRODUCT NOT FOUND: $name")
        return false
    }
    
    /**
     * Возвращает сгруппированный список для отображения
     */
    fun getGroupedList(): List<Any> {
        FileLogger.d("GROUPED_LIST", "=== НАЧАЛО ГРУППИРОВКИ ===")
        FileLogger.d("GROUPED_LIST", "ВСЕ ТОВАРЫ: ${_products.map { "${it.name}:urgent=${it.isUrgent},buy=${it.needsToBuy}" }}")
        
        // Группируем по категориям без изменения оригинального списка
        val groupedProducts = _products.groupBy { it.getGroup() }
        val result = mutableListOf<Any>()
        
        FileLogger.d("GROUPED_LIST", "ГРУППЫ ПОСЛЕ ГРУППИРОВКИ:")
        groupedProducts.forEach { (group, products) ->
            val groupName = when (group) {
                ProductGroup.URGENT_TO_BUY -> "СРОЧНО"
                ProductGroup.TO_BUY -> "ВАЖНО"
                ProductGroup.OTHER -> "ОСТАЛЬНОЕ"
            }
            FileLogger.d("GROUPED_LIST", "  $groupName: ${products.map { "${it.name}:urgent=${it.isUrgent},buy=${it.needsToBuy}" }}")
        }
        
        // Добавляем заголовки и товары для каждой группы в правильном порядке
        ProductGroup.values().forEach { group ->
            val productsInGroup = groupedProducts[group]?.sortedBy { it.name } ?: emptyList()
            if (productsInGroup.isNotEmpty()) {
                // Добавляем заголовок группы
                val groupTitle = when (group) {
                    ProductGroup.URGENT_TO_BUY -> "🔥 СРОЧНО"
                    ProductGroup.TO_BUY -> "🛒 ВАЖНО"
                    ProductGroup.OTHER -> "📋 ОСТАЛЬНОЕ"
                }
                result.add(groupTitle)
                FileLogger.d("GROUPED_LIST", "Добавлена группа: $groupTitle с ${productsInGroup.size} товарами")
                // Добавляем товары группы
                result.addAll(productsInGroup)
            }
        }
        
        FileLogger.d("GROUPED_LIST", "ФИНАЛЬНЫЙ СПИСОК: $result")
        FileLogger.d("GROUPED_LIST", "=== КОНЕЦ ГРУППИРОВКИ ===")
        return result
    }
    
    /**
     * Загружает продукты из списка
     */
    fun loadProducts(products: List<Product>) {
        _products.clear()
        _products.addAll(products)
    }
    
    /**
     * Очищает список продуктов
     */
    fun clear() {
        _products.clear()
    }
}
