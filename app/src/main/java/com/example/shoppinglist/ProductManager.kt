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
     */
    fun updateProductPurchaseStatus(name: String, needsToBuy: Boolean): Boolean {
        val index = _products.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        if (index != -1) {
            _products[index].needsToBuy = needsToBuy
            return true
        }
        return false
    }
    
    /**
     * Переключает срочность продукта
     * Если товар становится срочным, он автоматически становится нужным для покупки
     */
    fun toggleProductUrgency(name: String): Boolean {
        val index = _products.indexOfFirst { it.name.equals(name, ignoreCase = true) }
        if (index != -1) {
            val product = _products[index]
            product.isUrgent = !product.isUrgent
            // Если товар становится срочным, он автоматически становится нужным для покупки
            if (product.isUrgent) {
                product.needsToBuy = true
            }
            return true
        }
        return false
    }
    
    /**
     * Возвращает сгруппированный список для отображения
     */
    fun getGroupedList(): List<Any> {
        // Сортируем продукты по алфавиту
        _products.sortBy { it.name }
        
        // Группируем по категориям
        val groupedProducts = _products.groupBy { it.getGroup() }
        val result = mutableListOf<Any>()
        
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
                // Добавляем товары группы
                result.addAll(productsInGroup)
            }
        }
        
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
