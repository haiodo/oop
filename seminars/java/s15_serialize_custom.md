---
marp: true
---

# Кастомизация Стандартной Java-Сериализации

---

### 1. Использование методов `writeObject()` и `readObject()`

**Цель**: Надежное управление процессом сериализации/десериализации.

#### Когда использовать:

- Если нужно исключить некоторые поля (кроме transient).
- Добавлять дополнительную логику (например, шифрование данных).

---

#### Пример кода:

```java
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private transient String password; // Не сериализуется автоматически

    // Метод для кастомной сериализации
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject(); // Сериализация остальных полей
        // Добавим хэш пароля вместо самого пароля
        String hashedPass = hash(password);
        oos.writeUTF(hashedPass);
    }

    // Метод для кастомной десериализации
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // Восстановление остальных полей
        String hashedPass = ois.readUTF();
        password = unhash(hashedPass); // Дешифровка пароля
    }
}
```

---

### 2. Управление версионностью: `serialVersionUID`

**Что это**: Идентификатор класса, позволяющий Java проверять совместимость версий.

#### Важно!

- Если значение `serialVersionUID` не совпадает между сериализованным и десериализующим классами → исключение `InvalidClassException`.
- Автоматически генерируется компилятором, но лучше задавать явно.

#### Пример:

```java
private static final long serialVersionUID = 1L; // Версия 1

// После добавления нового поля:
private static final long serialVersionUID = 2L; // Изменили версию!
```

---

### 3. Кастомизация transient полей

**Сценарий**: Игнорирование некоторых полей (например, сенстивных данных).

---

#### Пример:

```java
public class BankAccount implements Serializable {
    private double balance;
    private transient String pinCode; // Не сохраняется автоматически

    // Добавим кастомный writeObject для безопасности
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject(); // Сериализация всех кроме transient
        // Пин-код шифруем перед записью
        String encryptedPin = encrypt(pinCode);
        oos.writeUTF(encryptedPin);
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        ois.defaultReadObject();
        String encryptedPin = ois.readUTF();
        pinCode = decrypt(encryptedPin); // Дешифровка при восстановлении
    }
}
```

---

### 4. Наследование и сериализация

**Правила**:

- Если базовый класс **не** реализует `Serializable`, потомок тоже **не** будет сериализуемым.
- В десериализации восстанавливается полное наследованное состояние объекта (исключая transient поля).

#### Пример проблемы:

```java
public class Animal { // Не Serializable!
    private String sound;
}

public class Dog extends Animal implements Serializable {} // ❌ Ошибка, так как Animal не сериализуемый

// Решение: Добавить implements Serializable в Animal
```

---

### 5. Практическое задание

1. Реализуйте класс `SecureMessage` с полями `text`, `encryptedFlag`.
2. Используйте `transient` для исключения поля `secretKey`.
3. Добавьте методы `writeObject/readObject` для шифрования/дешифровки текста перед сохранением.
4. Проверьте работу с измененной версией serialVersionUID.

---

### 6. Итоги

- Кастомные методы `writeObject()` и `readObject()` дают полный контроль над сериализацией.
- `serialVersionUID` — ключевой элемент для предотвращения конфликтов версий.
- Transient поля + кастомная логика = безопасность и гибкость.

**Важно!** Стандартная сериализация уязвима к атакам (например, десериализации объектов из ненадежных источников). Для production-систем используйте JSON/XML + валидацию данных.
