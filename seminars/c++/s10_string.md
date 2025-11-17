---
marp: true
theme: default
paginate: true
---

# std::string в C++

## Использование, внутреннее устройство и практики

### Семинар 10

### Курс: Объектно-ориентированное программирование

---

## Что такое std::string?

- **Класс** для работы со строками в C++
- Часть стандартной библиотеки `<string>`
- Заменяет C-style строки (`char*`)
- **RAII** - автоматическое управление памятью
- **Безопасность** типов и границ

```cpp
#include <string>
std::string str = "Hello, World!";
```

---

## Создание строк

```cpp
#include <string>

// Различные способы создания
std::string s1;                    // Пустая строка
std::string s2("Hello");           // Из литерала
std::string s3 = "World";          // Инициализация
std::string s4(s2);                // Копирование
std::string s5(10, 'A');           // 10 символов 'A'
std::string s6{'H', 'i'};          // Из списка
```

---

## Основные операции

### Конкатенация

```cpp
std::string a = "Hello";
std::string b = "World";
std::string c = a + " " + b;       // "Hello World"
a += " there";                     // "Hello there"
```

### Сравнение

```cpp
if (a == b) { /* равны */ }
if (a < b) { /* лексикографически меньше */ }
```

---

## Доступ к символам

```cpp
std::string str = "Hello";

// Безопасный доступ
char c1 = str.at(0);               // 'H', проверка границ
char c2 = str[1];                  // 'e', без проверки

// Итераторы
for (auto it = str.begin(); it != str.end(); ++it) {
  std::cout << *it;
}

// Range-based for
for (char c : str) {
  std::cout << c;
}
```

---

## Размер и емкость

```cpp
std::string str = "Hello";

std::cout << str.size() << std::endl;      // 5
std::cout << str.length() << std::endl;    // 5 (то же самое)
std::cout << str.capacity() << std::endl;  // >= 5
std::cout << str.max_size() << std::endl;  // Максимальный размер

str.reserve(100);                          // Резервирование памяти
str.shrink_to_fit();                       // Уменьшение емкости
```

---

## Модификация строк

### Добавление и удаление

```cpp
std::string str = "Hello";

str.push_back('!');                // "Hello!"
str.pop_back();                    // "Hello"
str.append(" World");              // "Hello World"
str.insert(5, ",");                // "Hello, World"
str.erase(5, 1);                   // "Hello World"
str.clear();                       // ""
```

---

## Подстроки и поиск

### Подстроки

```cpp
std::string str = "Hello World";
std::string sub = str.substr(6, 5);        // "World"
std::string sub2 = str.substr(6);          // "World"
```

### Поиск

```cpp
size_t pos = str.find("World");            // 6
size_t pos2 = str.find('o');               // 4
size_t pos3 = str.rfind('o');              // 7 (последнее вхождение)

if (pos != std::string::npos) {
  // Найдено
}
```

---

## Внутреннее устройство

### Small String Optimization (SSO)

```
Короткие строки (≤ 15-23 символа):
┌─────────────────────────────┐
│ символы прямо в объекте     │
└─────────────────────────────┘

Длинные строки:
┌─────────┬─────────┬─────────┐
│ pointer │  size   │capacity │
└─────────┴─────────┴─────────┘
   │
   ▼
┌─────────────────────────────┐
│    данные в куче            │
└─────────────────────────────┘
```

---

## Производительность

### Избегайте лишних копирований

```cpp
// Плохо
std::string bad_concat(const std::string& a, const std::string& b) {
  return a + b + "!";  // Много временных объектов
}

// Лучше
std::string good_concat(const std::string& a, const std::string& b) {
  std::string result;
  result.reserve(a.size() + b.size() + 1);
  result = a;
  result += b;
  result += "!";
  return result;
}
```

---

## std::string_view (C++17)

```cpp
#include <string_view>

void process_string(std::string_view sv) {
  // Работает с любыми строками без копирования
  std::cout << sv << std::endl;
}

std::string str = "Hello";
const char* cstr = "World";

process_string(str);        // Без копирования
process_string(cstr);       // Без копирования
process_string("Literal");  // Без копирования
```

---

## Преобразования

### Строка ↔ Число

```cpp
// Число в строку
int num = 42;
std::string str = std::to_string(num);      // "42"

// Строка в число
std::string s = "123";
int n = std::stoi(s);                       // 123
double d = std::stod("3.14");               // 3.14

// С проверкой ошибок
try {
  int result = std::stoi("abc");
} catch (const std::invalid_argument& e) {
  // Обработка ошибки
}
```

---

## Лучшие практики

### 1. Используйте const& для параметров

```cpp
void process(const std::string& str) {  // Хорошо
  // ...
}

void process(std::string str) {         // Плохо - копирование
  // ...
}
```

### 2. Резервируйте память

```cpp
std::string result;
result.reserve(expected_size);  // Избегает реаллокаций
```

---

## Лучшие практики (продолжение)

### 3. Используйте emplace для контейнеров

```cpp
std::vector<std::string> vec;
vec.emplace_back("Hello");      // Конструирует на месте
vec.push_back("World");         // Создает временный объект
```

### 4. Предпочитайте string_view для чтения

```cpp
// C++17
void read_only(std::string_view sv) { /* ... */ }

// До C++17
void read_only(const std::string& str) { /* ... */ }
```

---

## Частые ошибки

### 1. Висячие указатели с c_str()

```cpp
const char* get_cstr() {
  std::string str = "temporary";
  return str.c_str();     // ОШИБКА: висячий указатель
}
```

### 2. Неэффективная конкатенация

```cpp
std::string result;
for (const auto& s : strings) {
  result = result + s;    // O(n²) сложность
}

// Лучше:
for (const auto& s : strings) {
  result += s;            // O(n) сложность
}
```

---

## Заключение

- **std::string** - основной инструмент для работы со строками в C++
- Автоматическое управление памятью
- Богатый API для манипуляций
- Оптимизации для коротких строк (SSO)
- Используйте **string_view** в C++17+ для эффективности
- Помните о производительности при работе с большими строками

### Полезные ссылки

- [cppreference.com/string](https://en.cppreference.com/w/cpp/string/basic_string)
- Эффективное использование C++ (Скотт Мейерс)
