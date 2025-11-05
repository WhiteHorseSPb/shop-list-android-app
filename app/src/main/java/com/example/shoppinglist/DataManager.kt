package com.example.shoppinglist

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Менеджер для работы с данными (SharedPreferences)
 * Отвечает за сохранение и загрузку продуктов
 */
class DataManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "shopping_list_prefs"
        private const val PRODUCTS_KEY = "products_list"
    }
    
    /**
     * Сохраняет список продуктов в SharedPreferences
     */
    fun saveProducts(products: List<Product>) {
        try {
            val json = gson.toJson(products)
            sharedPreferences.edit()
                .putString(PRODUCTS_KEY, json)
                .apply()
        } catch (e: Exception) {
            // Логирование ошибки можно добавить позже
        }
    }
    
    /**
     * Загружает список продуктов из SharedPreferences
     */
    fun loadProducts(): List<Product> {
        return try {
            val json = sharedPreferences.getString(PRODUCTS_KEY, null)
            if (json != null) {
                // Пытаемся десериализовать как новый формат Product
                val type = object : TypeToken<List<Product>>() {}.type
                val products = gson.fromJson<List<Product>>(json, type)
                
                // Обеспечиваем обратную совместимость для старых данных без поля isUrgent
                products?.map { product ->
                    // Проверяем, есть ли поле isUrgent в данных
                    try {
                        // Если поле isUrgent существует, используем текущий объект
                        product.isUrgent // Просто проверяем доступность поля
                        product
                    } catch (e: Exception) {
                        // Если поля нет (старые данные), создаем новый объект с isUrgent = false
                        Product(product.name, product.needsToBuy, false)
                    }
                } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // Если произошла ошибка при десериализации, возможно это старый формат
            try {
                // Пробуем загрузить как старый формат (только name и needsToBuy)
                val json = sharedPreferences.getString(PRODUCTS_KEY, null)
                if (json != null) {
                    // Создаем временный класс для старого формата
                    val oldType = object : TypeToken<List<OldProduct>>() {}.type
                    val oldProducts = gson.fromJson<List<OldProduct>>(json, oldType)
                    oldProducts?.map { Product(it.name, it.needsToBuy, false) } ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e2: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Очищает все сохраненные данные
     */
    fun clearAll() {
        sharedPreferences.edit()
            .remove(PRODUCTS_KEY)
            .apply()
    }
    
    /**
     * Очищает только список продуктов (для тестирования)
     */
    fun clearProducts() {
        sharedPreferences.edit()
            .remove(PRODUCTS_KEY)
            .apply()
    }
    
    /**
     * Временный класс для обратной совместимости со старыми данными
     */
    private data class OldProduct(
        val name: String,
        var needsToBuy: Boolean = false
    )
}
