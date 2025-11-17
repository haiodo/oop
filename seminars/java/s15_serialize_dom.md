---
marp: true
---

# Сериализация и Десериализация в Java с XML

**Использование W3C DOM (DocumentBuilder)**

---

## Содержание

1. **Что такое сериализация/десериализация?**
2. **W3C DOM API**
3. **Создание DocumentBuilderFactory**
4. **Сериализация в XML**
5. **Десериализация из XML**
6. **Примеры кода и сценарии**

---

## 1. Что такое сериализация/десериализация?

- **Сериализация**: Преобразование объектов Java → формате, удобном для хранения/передачи (например, XML).
- **Десериализация**: Обратный процесс: формат (XML) → объекты Java.

**Цели**:

- Сохранение состояния объекта в файл.
- Передача данных между приложениями/сервисами.

---

## 2. W3C DOM API

- **W3C Document Object Model (DOM)**: Графическое представление XML/HTML как дерева узлов.
- **Главные компоненты**:
  - `Document`: Корень дерева.
  - `Element`: Узел элемента (`<user>`, `<name>`).
  - `Attr`: Атрибут (`id="1"`).
  - `Text`: Текст внутри узла.

---

## **DOM vs SAX**:

- DOM загружает весь документ в память → удобно для модификации, но требует ресурсов.
- SAX парсит по событиям → эффективнее для больших файлов.

---

## 3. Создание DocumentBuilderFactory

```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
DocumentBuilder builder = factory.newDocumentBuilder(); // Может бросать исключения!
```

**Важно**:

- `newInstance()` → получение фабрики по умолчанию.
- Обработка исключений: `ParserConfigurationException`, `DOMException`.

---

## 4. Сериализация в XML (Пример)

**Цель**: Сохранить объект типа `User` в файл `user.xml`.

```java
public class User {
    private String name;
    private int age;

    // Конструктор, геттеры/сеттеры...
}

// Создаем Document:
Document doc = builder.newDocument();
Element root = doc.createElement("user");
doc.appendChild(root);

// Добавляем элементы и атрибуты:
Element nameEl = doc.createElement("name");
nameEl.setTextContent(user.getName());
root.appendChild(nameEl);

```

---

```java

// Сохраняем в файл:
TransformerFactory transformerFactory = TransformerFactory.newInstance();
Transformer transformer = transformerFactory.newTransformer();
transformer.transform(new DOMSource(doc), new StreamResult(file));
```

**Структура XML**:

```xml
<user>
    <name>Alice</name>
    <age>30</age>
</user>
```

---

## 5. Десериализация из XML (Пример)

**Цель**: Загрузить данные из `user.xml` в объект `User`.

```java
// Парсим файл:
Document doc = builder.parse(file);
Element root = doc.getDocumentElement();

// Извлекаем элементы:
NodeList nameNodes = root.getElementsByTagName("name");
String name = ((Text) nameNodes.item(0).getFirstChild()).getData();

int age = Integer.parseInt(
    root.getElementsByTagName("age").item(0).getTextContent()
);

User user = new User(name, age);
```

---

**Важно**:

- Обработка отсутствия элементов/атрибутов (проверка `null`).
- Конвертация строк в нужные типы (`Integer.parseInt()`).

---

## 6. Полезные методы и советы

### Методы DOM:

- `createElement()`, `createAttribute()`, `setTextContent()`.
- `getElementsByTagName()` → поиск элементов по имени.
- `getAttributeValue()` для атрибутов.

### Советы:

1. Используйте `try-with-resources` для работы с файлами!
2. Обрабатывайте исключения: `IOException`, `SAXException`.
3. Проверяйте наличие элементов перед доступом к их свойствам.

---

## 7. Полный пример кода

```java
// Серилизация:
public static void saveUser(User user, String filePath) throws Exception {
    Document doc = builder.newDocument();
    Element root = doc.createElement("user");

    // Добавление элементов...

    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(new DOMSource(doc), new StreamResult(filePath));
}

// Десериализация:
public static User loadUser(String filePath) throws Exception {
    Document doc = builder.parse(filePath);
    Element root = doc.getDocumentElement();

    String name = root.getElementsByTagName("name").item(0).getTextContent();
    int age = Integer.parseInt(root.getElementsByTagName("age").item(0).getTextContent());

    return new User(name, age);
}
```

---

## 8. Ошибки и их решение

- **`ParserConfigurationException`**:  
  Не удалось создать `DocumentBuilder`. Проверьте классы в classpath (напр., Xerces).

- **`IOException/SAXParseException`**:  
  Неверный путь к файлу или некорректная XML-структура.

- **Null Pointer**:  
  Элемент не найден (`getElementsByTagName()` вернул пустой список).

---

## Задачи для студентов

1. Реализуйте сериализацию списка `User` в XML.
2. Добавьте обработку исключений в пример кода.
3. Создайте класс `Car` и выполните его сериализацию/десериализацию.
