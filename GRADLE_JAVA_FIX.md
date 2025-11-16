# Исправление проблемы с Gradle и Java

## Проблема
При сборке проекта возникает ошибка:
```
No Java compiler found, please ensure you are running Gradle with a JDK
```

## Причина
1. В системе установлен только JRE (Java Runtime Environment), но не JDK (Java Development Kit)
2. Gradle требует JDK для компиляции кода
3. Текущая версия Java 8 не совместима с новыми версиями Gradle

## Решение

### Вариант 1: Установить JDK вручную

1. Скачать JDK 11 или новее с официального сайта:
   - https://adoptium.net/ (рекомендуется)
   - https://www.oracle.com/java/technologies/downloads/

2. Установить JDK в папку без пробелов, например: `C:\Java\jdk-11`

3. Установить переменные окружения:
   ```cmd
   set JAVA_HOME=C:\Java\jdk-11
   set PATH=%JAVA_HOME%\bin;%PATH%
   ```

4. Проверить установку:
   ```cmd
   java -version
   javac -version
   ```

### Вариант 2: Использовать JDK из Android Studio

1. Найти JDK в Android Studio:
   ```
   C:\Program Files\Android\Android Studio\jbr\
   ```

2. Установить JAVA_HOME:
   ```cmd
   set JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
   ```

3. Добавить в PATH:
   ```cmd
   set PATH=%JAVA_HOME%\bin;%PATH%
   ```

### Вариант 3: Понизить версию Gradle

1. Отредактировать файл `gradle-wrapper.properties`:
   ```properties
   distributionUrl=https\://services.gradle.org/distributions/gradle-7.6.4-bin.zip
   ```

2. Отредактировать файл `build.gradle` (проект):
   ```gradle
   dependencies {
       classpath 'com.android.tools.build:gradle:7.4.2'
   }
   ```

## Рекомендуемое решение

Установить JDK 11 и использовать Gradle 8.0:

1. Установить JDK 11 с https://adoptium.net/
2. Установить JAVA_HOME
3. Использовать текущую версию Gradle 8.0

## Проверка

После установки JDK выполнить:
```cmd
cd android_studio_project
.\gradlew build --no-daemon
```

Проект должен успешно собраться.
