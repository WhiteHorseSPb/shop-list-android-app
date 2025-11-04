package com.example.shoppinglist

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.shoppinglist.R

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var productManager: ProductManager
    private lateinit var dataManager: DataManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Инициализация менеджеров
        productManager = ProductManager()
        dataManager = DataManager(this)
        
        initViews()
        setupRecyclerView()
        loadProducts()
        setupAddProduct()
        setupSwipeToDelete()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd = findViewById(R.id.fabAdd)
    }
    
    private fun setupRecyclerView() {
        adapter = ProductAdapter()
        
        // Устанавливаем callback для сохранения при изменении переключателей
        adapter.onProductChanged = { product ->
            productManager.updateProductPurchaseStatus(product.name, product.needsToBuy)
            updateListAndSave()
        }
        
        // Устанавливаем callback для долгого нажатия и клика на звездочку
        adapter.onProductLongClick = { product ->
            toggleProductUrgency(product)
        }
        
        // Используем GridLayoutManager для двухколоночного списка
        val gridLayoutManager = GridLayoutManager(this, 2)
        
        // Настраиваем SpanSizeLookup для заголовков групп
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    ProductAdapter.TYPE_GROUP_HEADER -> 2 // Заголовки занимают всю ширину
                    ProductAdapter.TYPE_PRODUCT -> 1    // Товары занимают 1 колонку
                    else -> 1
                }
            }
        }
        
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = adapter
    }
    
    private fun loadProducts() {
        // Загружаем продукты из DataManager
        val savedProducts = dataManager.loadProducts()
        if (savedProducts.isNotEmpty()) {
            productManager.loadProducts(savedProducts)
        } else {
            // Если сохраненных продуктов нет, загружаем начальные
            getInitialProducts().forEach { productManager.addProduct(it) }
        }
        updateListAndSave()
    }
    
    private fun getInitialProducts(): List<Product> {
        return listOf(
            // Срочные товары (для демонстрации)
            Product("Молоко", needsToBuy = true, isUrgent = true),
            Product("Хлеб", needsToBuy = true, isUrgent = true),
            
            // Товары для покупки
            Product("Яйца", needsToBuy = true, isUrgent = false),
            Product("Соль", needsToBuy = true, isUrgent = false),
            Product("Сахар", needsToBuy = true, isUrgent = false),
            Product("Масло", needsToBuy = true, isUrgent = false),
            Product("Кофе", needsToBuy = true, isUrgent = false),
            Product("Чай", needsToBuy = true, isUrgent = false),
            
            // Остальные товары
            Product("Макароны", needsToBuy = false, isUrgent = false),
            Product("Рис", needsToBuy = false, isUrgent = false),
            Product("Картофель", needsToBuy = false, isUrgent = false),
            Product("Лук", needsToBuy = false, isUrgent = false),
            Product("Морковь", needsToBuy = false, isUrgent = false),
            Product("Курица", needsToBuy = false, isUrgent = false),
            Product("Сыр", needsToBuy = false, isUrgent = false),
            Product("Йогурт", needsToBuy = false, isUrgent = false),
            Product("Творог", needsToBuy = false, isUrgent = false),
            Product("Колбаса", needsToBuy = false, isUrgent = false),
            Product("Сосиски", needsToBuy = false, isUrgent = false),
            Product("Хлебцы", needsToBuy = false, isUrgent = false),
            Product("Шоколад", needsToBuy = false, isUrgent = false),
            Product("Печенье", needsToBuy = false, isUrgent = false),
            Product("Конфеты", needsToBuy = false, isUrgent = false),
            Product("Вода", needsToBuy = false, isUrgent = false),
            Product("Сок", needsToBuy = false, isUrgent = false)
        ).sortedBy { it.name }
    }
    
    /**
     * Обновляет список и сохраняет данные
     */
    private fun updateListAndSave() {
        // Используем Handler для лучшей совместимости с MIUI
        recyclerView.post {
            val groupedList = productManager.getGroupedList()
            adapter.submitList(groupedList)
            dataManager.saveProducts(productManager.products)
            
            // Убираем fallback который вызывает массовое перемещение товаров
            // Используем только submitList() для корректной работы DiffUtil
        }
    }
    
    private fun setupAddProduct() {
        fabAdd.setOnClickListener {
            showAddProductDialog()
        }
    }
    
    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_product, null)
        val editTextProduct = dialogView.findViewById<EditText>(R.id.editTextNewProduct)
        val checkBoxUrgent = dialogView.findViewById<CheckBox>(R.id.checkBoxUrgent)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonAdd).setOnClickListener {
            val productName = editTextProduct.text?.toString()?.trim() ?: ""
            
            if (productName.isBlank()) {
                Toast.makeText(this, "Введите название товара", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val newProduct = Product(productName.capitalizeWords(), isUrgent = checkBoxUrgent.isChecked)
            if (productManager.addProduct(newProduct)) {
                updateListAndSave()
                dialog.dismiss()
                Toast.makeText(this, "Товар добавлен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Такой товар уже есть в списке", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
        
        // Устанавливаем фокус на поле ввода
        editTextProduct.requestFocus()
    }
    
    private fun toggleProductUrgency(product: Product) {
        // Сначала изменяем состояние в менеджере
        val success = productManager.toggleProductUrgency(product.name)
        
        if (success) {
            // Получаем обновленный продукт
            val updatedProduct = productManager.products.find { it.name == product.name }
            
            // Показываем сообщение
            val message = if (updatedProduct?.isUrgent == true) {
                "Товар отмечен как срочный"
            } else {
                "Товар отмечен как обычный"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            
            // Убираем notifyDataSetChanged() который вызывает проблему на MIUI
            // Используем только updateListAndSave() для корректной работы на Xiaomi
            
            // Обновляем список и сохраняем
            updateListAndSave()
            
            // Убираем fallback с notifyDataSetChanged() который вызывает случайное перемещение товаров
        }
    }
    
    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }
            
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val item = adapter.currentList[position]
                
                // Удаляем только если это товар, а не заголовок группы
                if (item is Product) {
                    if (productManager.removeProduct(item.name)) {
                        updateListAndSave()
                        Toast.makeText(this@MainActivity, "Удален: ${item.name}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Если это заголовок, просто обновляем список без удаления
                    adapter.notifyDataSetChanged()
                }
            }
        }
        
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}

// Extension function для форматирования текста
fun String.capitalizeWords(): String {
    return this.split(" ").map { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }.joinToString(" ")
}
