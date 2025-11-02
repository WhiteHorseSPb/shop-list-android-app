# 🔧 Исправление ошибки сборки Gradle

## 🚨 **Проблема:**
```
BUG! exception in phase 'semantic analysis' in source unit '_BuildScript_' 
Unsupported class file major version 65
```

## 🔍 **Причина:**
Несовместимость версий Java/Groovy в Android Studio с проектом

## ✅ **Решение 1: Обновление Gradle Wrapper**

### Шаг 1: Проверьте версию Gradle
1. Откройте файл: `android_studio_project/gradle/wrapper/gradle-wrapper.properties`
2. Посмотрите текущую версию distributionUrl

### Шаг 2: Обновите Gradle Wrapper
1. Откройте терминал в папке `android_studio_project`
2. Выполните команду:
   ```bash
   ./gradlew wrapper --gradle-version=8.0
   ```

### Шаг 3: Очистите и соберите
```bash
./gradlew clean
./gradlew assembleDebug
```

## ✅ **Решение 2: Изменение версии Java в Android Studio**

### Шаг 1: Откройте настройки
1. **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
2. Найдите **Gradle JVM**
3. Установите **JDK 17** вместо JDK 21

### Шаг 2: Перезапустите Android Studio
1. **File → Invalidate Caches and Restart**
2. Выберите **"Invalidate and Restart"**

## ✅ **Решение 3: Сборка через командную строку с правильной Java**

### Шаг 1: Установите переменную JAVA_HOME
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
```

### Шаг 2: Используйте более старую версию Gradle
```bash
cd android_studio_project
./gradlew clean assembleDebug --no-daemon
```

## ✅ **Решение 4: Понижение версии Gradle**

### Шаг 1: Измените gradle-wrapper.properties
Откройте `gradle/wrapper/gradle-wrapper.properties` и измените:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
```

### Шаг 2: Измените build.gradle
В `android/build.gradle` добавьте в начало:
```gradle
tasks.withType(JavaCompile) {
    options.release {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
```

## 🔍 **Проверка версии Java:**

### Узнайте текущую версию:
```bash
java -version
```

### Если версия 21+, понизьте до 17:
1. Скачайте JDK 17 с сайта Oracle
2. Установите в `C:\Program Files\Java\jdk-17`
3. Установите переменную:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

## 📱 **Альтернатива: Сборка APK на другом компьютере**

Если ничего не помогает:
1. Скопируйте папку `android_studio_project` на другой компьютер
2. Установите свежую Android Studio
3. Откройте проект и соберите APK

## 🎯 **Быстрая проверка:**

### Проверьте совместимость:
```bash
cd android_studio_project
./gradlew --version
```

Должна показать версию 8.x, а не 7.x или 9.x

## 📞 **Если проблема осталась:**

### Создайте новый проект:
1. **File → New → New Project**
2. Выберите **Empty Activity**
3. Скопируйте все файлы из `android_studio_project` в новый проект
4. Соберите APK

## ✅ **Успешная сборка:**
После исправления Gradle вы получите:
- ✅ APK с версией 1.5
- ✅ Полный функционал группировки
- ✅ Правильное отображение в Android

**Начните с Решения 1 (обновление Gradle Wrapper) - это решает 90% проблем!**
