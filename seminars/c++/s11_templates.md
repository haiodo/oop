---
marp: true
theme: default
paginate: true
---

# Шаблоны в C++

Для начинающих (2 курс) — слайды в формате Marp

---

## Цели занятия

- Понять, что такое шаблоны (templates)
- Научиться писать function и class templates
- Познакомиться со специализациями, вариадическими шаблонами и базовыми концепциями C++20

---

## Что такое шаблон?

- Шаблон — обобщённый код, параметризуемый типами или значениями.
- Компилятор генерирует конкретные варианты при использовании.
- Позволяет избежать дублирования кода.

**Пример проблемы без шаблонов:**

```cpp
int maxInt(int a, int b) { return a > b ? a : b; }
double maxDouble(double a, double b) { return a > b ? a : b; }
// Нужно писать одинаковую функцию для каждого типа!
```

---

## Function template — пример

```cpp
template<typename T>  // T — параметр типа (может быть любым)
T add(T a, T b) {
  return a + b;
}

int x = add<int>(1, 2);        // явно указываем T = int
auto y = add(1.5, 2.5);        // компилятор сам выводит T = double
// auto z = add(1, 2.5);       // ОШИБКА: разные типы!
```

**Важно:** Все параметры должны иметь одинаковый тип T (или используйте несколько параметров шаблона).

---

## Вывод параметров шаблона

- Компилятор обычно выводит типы автоматически.
- Явная специализация: `add<double>(...)`.
- Если вывод невозможен, указывайте параметры явно.

**Когда нужна явная специализация:**

```cpp
template<typename T>
T convert(const std::string& s) {
  // компилятор не может вывести T из параметров
}

auto num = convert<int>("42");  // обязательно указать T
```

---

## Class template — пример

```cpp
template<typename T>
class Box {
public:
  Box(T v) : value(v) {}
  T get() const { return value; }
private:
  T value;
};

Box<int> b(42);      // создаём Box для int
Box<std::string> s("hello");  // и для string
```

**Замечание:** Для классов нужно всегда указывать тип явно (до C++17).

---

## Непараметрические (non-type) параметры

```cpp
template<typename T, int N>
struct Array {
  T data[N];  // размер массива известен в compile-time
  int size() const { return N; }
};

Array<int, 10> a;     // массив из 10 int
Array<double, 5> b;   // массив из 5 double
```

- Параметр — значение (int, size_t, указатель, enum) известное на этапе компиляции.
- **Важно:** Значение N должно быть константой времени компиляции (constexpr).

---

## Полная специализация

```cpp
// Общий шаблон
template<typename T>
struct Printer {
  static void print(T val) { std::cout << "value: " << val; }
};

// Специализация для int
template<>
struct Printer<int> {
  static void print(int x) { std::cout << "int: " << x; }
};
```

- Используется для отдельной реализации для конкретного типа.
- **Применение:** оптимизация для конкретных типов, особое поведение.

---

## Частичная специализация (для классов)

```cpp
// Общий случай
template<typename T, typename U>
struct Pair {
  void info() { std::cout << "Разные типы\n"; }
};

// Специализация для одинаковых типов
template<typename T>
struct Pair<T, T> {
  void info() { std::cout << "Одинаковые типы\n"; }
};

Pair<int, double> p1;  // использует общий шаблон
Pair<int, int> p2;     // использует специализацию
```

- Частичная специализация применима только к классам/структурам (не к функциям).

---

## Шаблоны с переменным числом параметров (C++11)

```cpp
template<typename... Args>  // Args — пакет параметров
void printAll(Args... args) {
  (std::cout << ... << args) << '\n'; // fold expression (C++17)
}

printAll(1, " + ", 2);           // вывод: 1 + 2
printAll("Hello", ' ', "World"); // вывод: Hello World
```

- Позволяют передавать любое число параметров типов и/или значений.
- **Variadic templates** используются в std::tuple, std::make_unique и т.д.

---

## Пример: рекурсивная распаковка (до C++17)

```cpp
// Базовый случай
void print() { std::cout << '\n'; }

// Рекурсивный случай
template<typename T, typename... Args>
void print(T first, Args... rest) {
  std::cout << first << ' ';
  print(rest...);  // рекурсивный вызов
}

print(1, 2.5, "text");  // вывод: 1 2.5 text
```

---

## Concepts (C++20) — базовый пример

```cpp
#include <concepts>

// Требуем, чтобы T поддерживал operator+
template<typename T>
concept Addable = requires(T a, T b) {
  { a + b } -> std::convertible_to<T>;
};

template<Addable T>
T add(T a, T b) {
  return a + b;
}
```

**Преимущество:** более понятные сообщения об ошибках компиляции.

---

## Советы и подводные камни

- **Не перегружайте шаблоны** — читаемость важнее.
- **Частичная специализация** — только для классов (для функций используйте перегрузку).
- **Отладка** шаблонов может быть сложной: используйте понятные имена и тесты.
- **Определение в заголовках:** шаблоны обычно размещаются в .h файлах (не .cpp).
- **Ошибки компиляции** могут быть длинными — ищите первую ошибку.
- По умолчанию делайте интерфейс простым — требования вводите постепенно.

---

## Практическое задание

1. Создайте шаблонную функцию `max(a, b)` для любого типа
2. Создайте шаблонный класс `Stack<T>` с методами push/pop
3. Добавьте специализацию для bool (используйте vector<bool>)
4. (\*) Напишите вариадическую функцию `sum(args...)` для суммирования любого количества чисел

---

## Полезные ссылки

- [cppreference.com/templates](https://en.cppreference.com/w/cpp/language/templates)
- Книга: "C++ Templates: The Complete Guide" (Vandevoorde, Josuttis)
- [C++20 Concepts](https://en.cppreference.com/w/cpp/language/constraints)
