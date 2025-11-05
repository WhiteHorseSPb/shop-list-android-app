package com.example.shoppinglist

import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Файловый логгер для отладки с версионированием и автоматической очисткой
 */
object FileLogger {
    
    private const val LOGS_DIR = "logs"
    private const val MAX_LOG_FILES = 5
    private const val MAX_LOG_SIZE_MB = 10
    
    // Контекст приложения для доступа к файловой системе
    private var appContext: android.content.Context? = null
    
    /**
     * Инициализация логгера с контекстом
     */
    fun initialize(context: android.content.Context) {
        appContext = context.applicationContext
        cleanupOldLogs()
        d("FileLogger", "=== НАЧАЛО СЕССИИ ЛОГИРОВАНИЯ ===")
        d("FileLogger", "Версия приложения: v1.5")
        d("FileLogger", "Текущий лог-файл: ${getCurrentLogFileName()}")
        d("FileLogger", "Путь к логам: ${ensureLogsDirectory().absolutePath}")
    }
    
    /**
     * Получает имя текущего лог-файла с версией и временной меткой
     */
    private fun getCurrentLogFileName(): String {
        val version = "v1.5" // TODO: брать из BuildConfig.VERSION_NAME
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
        return "debug_${version}_${timestamp}.log"
    }
    
    /**
     * Создает директорию для логов, если она не существует
     */
    private fun ensureLogsDirectory(): File {
        val context = appContext ?: throw IllegalStateException("FileLogger не инициализирован")
        
        // Пытаемся создать во внутренней памяти
        val internalLogsDir = File(context.filesDir, LOGS_DIR)
        if (internalLogsDir.exists() || internalLogsDir.mkdirs()) {
            return internalLogsDir
        }
        
        // Если не получилось, пробуем внешнюю память
        val externalLogsDir = File(context.getExternalFilesDir(null), LOGS_DIR)
        externalLogsDir.mkdirs()
        return externalLogsDir
    }
    
    /**
     * Очищает старые лог-файлы, оставляя только MAX_LOG_FILES самых новых
     */
    private fun cleanupOldLogs() {
        try {
            val logsDir = ensureLogsDirectory()
            val logFiles = logsDir.listFiles { file -> 
                file.name.endsWith(".log") && file.name.startsWith("debug_")
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
            
            // Удаляем старые файлы, оставляя только MAX_LOG_FILES
            logFiles.drop(MAX_LOG_FILES).forEach { file ->
                file.delete()
                Log.d("FileLogger", "Удален старый лог-файл: ${file.name}")
            }
        } catch (e: Exception) {
            Log.e("FileLogger", "Ошибка при очистке старых логов: ${e.message}")
        }
    }
    
    /**
     * Проверяет размер текущего лог-файла и создает новый если нужно
     */
    private fun checkLogSize(currentLogFile: File): File {
        val maxSizeBytes = MAX_LOG_SIZE_MB * 1024 * 1024L
        if (currentLogFile.length() > maxSizeBytes) {
            // Создаем новый файл с новой временной меткой
            val newLogFile = File(ensureLogsDirectory(), getCurrentLogFileName())
            Log.d("FileLogger", "Создан новый лог-файл из-за размера: ${newLogFile.name}")
            return newLogFile
        }
        return currentLogFile
    }
    
    /**
     * Записывает отладочное сообщение в файл и Logcat
     */
    fun d(tag: String, message: String) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
                .format(Date())
            val logEntry = "[$timestamp] $tag: $message\n"
            
            val logsDir = ensureLogsDirectory()
            val logFileName = getCurrentLogFileName()
            var logFile = File(logsDir, logFileName)
            
            // Если файл существует, проверяем его размер
            if (logFile.exists()) {
                logFile = checkLogSize(logFile)
            }
            
            // Записываем в файл
            logFile.appendText(logEntry)
            
            // И в Logcat для совместимости
            Log.d(tag, message)
            
        } catch (e: Exception) {
            Log.e("FileLogger", "Ошибка записи в лог-файл: ${e.message}")
            Log.d(tag, message) // Fallback в Logcat
        }
    }
    
    /**
     * Записывает информационное сообщение
     */
    fun i(tag: String, message: String) {
        d(tag, "INFO: $message")
    }
    
    /**
     * Записывает сообщение об ошибке
     */
    fun e(tag: String, message: String) {
        d(tag, "ERROR: $message")
        Log.e(tag, message)
    }
    
    /**
     * Записывает предупреждение
     */
    fun w(tag: String, message: String) {
        d(tag, "WARNING: $message")
        Log.w(tag, message)
    }
    
    /**
     * Очищает все лог-файлы
     */
    fun clearAllLogs() {
        try {
            val logsDir = ensureLogsDirectory()
            logsDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".log")) {
                    file.delete()
                    Log.d("FileLogger", "Удален лог-файл: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.e("FileLogger", "Ошибка при очистке логов: ${e.message}")
        }
    }
    
    /**
     * Возвращает путь к текущему лог-файлу
     */
    fun getCurrentLogPath(): String {
        val logsDir = ensureLogsDirectory()
        val logFile = File(logsDir, getCurrentLogFileName())
        return logFile.absolutePath
    }
}
