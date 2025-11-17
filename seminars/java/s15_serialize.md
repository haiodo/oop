---
marp: true
---

# Сериализация и Десериализация в Java

## Стандартные методы (ObjectStream), XML, JSON

---

### 1. Введение

**Сериализация**: Преобразование объекта в последовательность байтов для сохранения или передачи.  
**Десериализация**: Извлечение объекта из последовательности байтов.

---

### 2. Стандартные классы Java: `ObjectInputStream` и `ObjectOutputStream`

- **Задача**: Сохранение/восстановление состояния объектов в формате бинарной сериализации.
- **Требования**: Класс объекта должен реализовать интерфейс `Serializable`.

---

#### Пример кода:

```java
// Сериализация
FileOutputStream fos = new FileOutputStream("file.txt");
ObjectOutputStream oos = new ObjectOutputStream(fos);
oos.writeObject(obj); // obj implements Serializable
oos.close();

// Десериализация
FileInputStream fis = new FileInputStream("file.txt");
ObjectInputStream ois = new ObjectInputStream(fis);
MyClass deserializedObj = (MyClass) ois.readObject();
ois.close();
```

---

### 3. Интерфейс `Serializable`

- **Описание**: Пустой маркер-интерфейс, который указывает, что класс можно сериализовать.
- **Важно!** Все поля объекта (включая вложенные) должны быть сериализуемыми, кроме transient-полей.

#### Пример:

```java
public class Person implements Serializable {
    private String name;
    private int age;
    // Конструкторы и методы...
}
```

---

### 4. XML-сериализация (JAXB)

**Контекст**: Для взаимодействия с системами, использующими XML (например, веб-сервисы).

#### Основные компоненты:

- **`@XmlRootElement`**: Аннотирует класс как корневой элемент.
- **`@XmlElement`**: Аннотирует поля/методы для сериализации.

---

#### Пример кода:

```java
@XmlRootElement
public class Product {
    @XmlElement(name = "product_id")
    private int id;
    private String name;
}

// Сериализация с JAXBContext:
JAXBContext context = JAXBContext.newInstance(Product.class);
Marshaller marshaller = context.createMarshaller();
marshaller.marshal(product, System.out);
```

---

### 5. JSON-сериализация (Jackson)

**Контекст**: Наиболее популярный формат для API и клиент-серверных взаимодействий.

#### Основные шаги:

1. Добавьте зависимость Jackson в Maven/Gradle.
2. Используйте `ObjectMapper` для конфигурации.

---

#### Пример кода (Jackson):

```java
// pom.xml: <dependency>org.json:jackson-databind</dependency>

ObjectMapper mapper = new ObjectMapper();
String jsonStr = mapper.writeValueAsString(product); // Сериализация в JSON
Product deserializedProd = mapper.readValue(jsonStr, Product.class); // Десериализация
```

---

#### Аннотации Jackson:

- `@JsonProperty`: Указание имени поля в JSON.
- `@JsonInclude(Include.NON_NULL)`: Исключение null-полей.

---

### 6. Сравнение подходов

| Критерий               | Java (ObjectStream)              | XML (JAXB)                     | JSON (Jackson/Gson)  |
| ---------------------- | -------------------------------- | ------------------------------ | -------------------- |
| **Читаемость**         | Бинарный, непонятен для человека | Человекочитаемый               | Человекочитаемый     |
| **Проприетарность**    | Только Java                      | Поддерживается многими языками | Универсальный формат |
| **Производительность** | Быстрая, компактная              | Медленнее                      | Средняя скорость     |

---

### 7. Важные моменты

- **Избегайте** стандартной Java-сериализации для передачи данных между разными системами (используйте XML/JSON).
- **Transient поля**: Не сериализуются в любом формате!
- **Версионность классов**: При изменении структуры объекта в JSON/XML проблемы меньше, чем при бинарной сериализации.

---

### 8. Практическое задание

1. Реализуйте класс `Book` с полями `isbn`, `title`, `author`.
2. Сериализуйте его через `ObjectOutputStream`.
3. Создайте XML-версию объекта с JAXB.
4. Переведите книгу в JSON с помощью Jackson и распечатайте структуру.
