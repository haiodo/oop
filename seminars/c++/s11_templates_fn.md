---
marp: true
theme: default
paginate: true
style: |
  section { font-size: 22px; padding: 40px; line-height: 1.35; }
  section h1 { font-size: 1.8em; margin: 0 0 .5em; }
  section h2 { font-size: 1.45em; margin: 0 0 .6em; }
  section p, section ul, section ol { margin: .45em 0; }
  section li { margin: .15em 0; }
  section pre { font-size: .82em; line-height: 1.3; margin: .5em 0; padding: .6em .8em; }
  section table { font-size: .85em; }
---

# Шаблоны в C++: функции и классы

## Семинар 11

### Курс: Объектно-ориентированное программирование, ФИТ НГУ

---

## Что такое шаблон?

- Шаблон - обобщённый код, параметризуемый типами или значениями.
- Компилятор порождает код конкретного варианта из шаблона, когда
  встречает использование с конкретными типами.
- Позволяет не писать одинаковую логику для каждого типа отдельно.

**Проблема без шаблонов:**

```cpp
int max_int(int a, int b) {
  return a > b ? a : b;
}
double max_double(double a, double b) {
  return a > b ? a : b;
}
// Same logic, one function per type - and so on for every type.
```

---

## Function template - пример

```cpp
template <typename T>  // T is a type parameter, can be anything
T add(T a, T b) {
  return a + b;
}

int x = add<int>(1, 2);   // T is specified explicitly
auto y = add(1.5, 2.5);   // T is deduced from arguments: double
// auto z = add(1, 2.5);  // error: both arguments must have the same T
```

**Важно:** оба параметра имеют один и тот же тип `T`. Для разных типов
нужно два параметра шаблона (`template <typename T, typename U>`).

---

## Вывод параметров шаблона

- Компилятор обычно выводит `T` из аргументов функции.
- Если вывести `T` нельзя, его указывают явно: `convert<int>(...)`.
  Это называется **явное указание аргументов шаблона**, а не
  специализация - специализацию разберём отдельно, дальше по семинару.

```cpp
#include <sstream>
#include <string>

template <typename T>
T convert(const std::string& s) {
  std::istringstream in(s);  // T does not appear in the parameter list
  T value{};
  in >> value;                // T cannot be deduced from a string
  return value;
}

auto num = convert<int>("42");  // T must be given explicitly
```

---

## Три разные вещи, которые называют похоже

1. **Явное указание аргументов шаблона** - программист пишет тип
   в `<>` при вызове, когда вывести его нельзя или он неоднозначен.
2. **Инстанцирование** - компилятор порождает код конкретной функции
   или класса из шаблона. Происходит при каждом использовании.
3. **Специализация** - программист сам пишет отдельное определение
   шаблона для конкретного набора параметров.

```cpp
convert<int>("42");    // (1) explicit arguments: T is not deducible here

add(1.5, 2.5);          // (2) instantiation: compiler generates add<double>

template <typename T>
struct Printer {
  static void print(T x) { std::cout << x; }
};

template <>              // (3) specialization: hand-written, for T = int
struct Printer<int> {
  static void print(int x) { std::cout << "int: " << x; }
};
```

---

## Шаблон - это рецепт, а не код

- Шаблон сам по себе не порождает никакого кода - это рецепт для
  компилятора, а не готовая функция или класс.
- Код появляется только при **инстанцировании**: когда компилятор
  видит конкретные типы (`add<int>` или вызов `add(1, 2)`).
- Следствие: компилятору нужно видеть **полное определение** шаблона
  в каждой единице трансляции, где шаблон используется - не только
  объявление.
- Отсюда правило: определения шаблонов кладут в заголовки, а не
  в `.cpp` (следующий слайд показывает, что случится иначе).

---

## Шаблон в .cpp - ошибка компоновщика

```cpp
// math_utils.h - BAD: only a declaration
template <typename T>
T add(T a, T b);

// math_utils.cpp - BAD: definition hidden from every other translation unit
template <typename T>
T add(T a, T b) {
  return a + b;
}

// main.cpp
#include "math_utils.h"
int result = add(1, 2);
// linking main.cpp: undefined reference to `int add<int>(int, int)`
```

```cpp
// math_utils.h - GOOD: definition visible wherever the template is used
template <typename T>
T add(T a, T b) {
  return a + b;
}
```

Обойти можно явным инстанцированием (`template int add<int>(int, int);`
в `.cpp`) - на этом курсе не понадобится.

---

## Задача 5: .cpp всё равно нужен

Весь код `CSVParser` и `operator<<` для `tuple` лежит в заголовках -
это следует из предыдущих двух слайдов. Но `oop_task()` в шаблоне
курса требует непустой `SOURCES`.

```cpp
// csv_parser.cpp
#include "csv_parser.h"
#include "tuple_print.h"
// No code here: this file only checks that both headers
// are self-contained and compile on their own.
```

Он не обходит проблему с прошлого слайда - шаблоны всё равно живут
в заголовках. Его роль другая: проверить, что заголовки собираются
сами по себе, без скрытой зависимости от порядка `#include` в чужом
файле.

---

## Class template - пример

```cpp
template <typename T>
class Box {
public:
  explicit Box(T v) : value_(v) {}
  T get() const { return value_; }

private:
  T value_;
};

Box<int> b(42);               // instantiates Box for int
Box<std::string> s("hello");  // and for std::string
```

**Замечание:** тип шаблона класса указывается явно при создании
объекта (`Box<int>`, а не `Box`).

---

## Непараметрические (non-type) параметры

```cpp
template <typename T, int N>
struct Array {
  T data[N];  // size is known at compile time
  int size() const { return N; }
};

Array<int, 10> a;     // array of 10 ints
Array<double, 5> b;   // array of 5 doubles
```

- Параметр - значение (int, size_t, указатель, enum), известное
  на этапе компиляции, а не тип.
- `N` обязан быть выражением `constexpr` - обычное значение времени
  выполнения сюда не подставить.

---

## Параметры шаблона по умолчанию

Как и у обычных функций - параметры справа можно не указывать.
Так устроены `std::set`, `std::map`, `std::vector` в стандартной
библиотеке.

```cpp
#include <functional>

template <typename T, typename Compare = std::less<T>>
struct SortedBox {
  bool before(const T& a, const T& b) const { return Compare{}(a, b); }
};

SortedBox<int> a;                     // Compare = std::less<int>
SortedBox<int, std::greater<int>> b;  // Compare указан явно
```

---

## auto и decltype(auto) в возвращаемом типе

Полезно, когда тип результата шаблонной функции неизвестен заранее
или зависит от типов параметров.

```cpp
template <typename T, typename U>
auto add(T a, U b) {  // return type deduced, like a variable
  return a + b;
}

auto x = add(1, 2.5);  // deduced as double

template <typename F>
decltype(auto) call(F f) {  // preserves references, unlike plain auto
  return f();
}
```

- `auto` выводится как для переменной: ссылки и `const` отбрасываются.
- `decltype(auto)` сохраняет точный тип выражения `return`, включая
  ссылку. Пригодится дальше, когда шаблонные функции будут возвращать
  ссылки.

---

## Полная специализация

```cpp
// Primary template
template <typename T>
struct Printer {
  static void print(T val) { std::cout << "value: " << val; }
};

// Specialization for int
template <>
struct Printer<int> {
  static void print(int x) { std::cout << "int: " << x; }
};
```

- Отдельная реализация под конкретный тип.
- Применение: особое поведение или оптимизация для этого типа.

---

## Частичная специализация

```cpp
template <typename T, typename U>
struct Pair {
  void info() const { std::cout << "different types\n"; }
};

template <typename T>
struct Pair<T, T> {
  void info() const { std::cout << "same type\n"; }
};

Pair<int, double> p1;  // uses the primary template
Pair<int, int> p2;     // uses the partial specialization
```

- Частичная специализация применима только к классам/структурам.
- Для функций аналог - обычная перегрузка.

---

## `std::vector<bool>` - специализация, о которой нужно знать

- `std::vector<bool>` - полная специализация `vector` для `bool`,
  которая хранит биты, а не байты: экономия памяти в 8 раз.
- Плата: это уже не совсем `Container` из требований стандарта.
  `operator[]` возвращает не `bool&`, а объект-прокси, эмулирующий
  ссылку через побитовые операции.
- Комитет C++ считает это признанной ошибкой в дизайне стандартной
  библиотеки: специализация контейнера незаметно поменяла его интерфейс.

```cpp
#include <vector>

std::vector<bool> v(3, false);
auto proxy = v[0];   // proxy object, not a reference to bool
proxy = true;         // writes through the proxy into the bit
// bool& ref = v[0];  // does not compile: no such conversion
```

Задача 2 явно запрещает такой прокси-класс для своего `operator[]` -
теперь понятно, от какого антипаттерна.

---

## Двухфазный поиск имён

Шаблон компилируется в два прохода: сначала независимые от параметров
имена, потом - при инстанцировании - зависимые от `T`.

Если зависимое имя - **тип**, компилятор по умолчанию считает его
значением. Нужна подсказка `typename`:

```cpp
template <typename T>
void f(const T& container) {
  // T::value_type first = *container.begin();  // error: expected expression
  typename T::value_type first = *container.begin();  // OK: this is a type
}
```

Если зависимое имя - **шаблон** (метод-шаблон), нужна подсказка
`template`:

```cpp
template <typename T>
void g(T& obj) {
  // obj.call<int>();          // error: '<' parsed as "less than"
  obj.template call<int>();    // OK: this name is a template
}
```

---

## Что дальше

Это первый семинар из четырёх про шаблоны - сегодня только база.

- Семинар 12: шаблоны с переменным числом параметров (variadic
  templates), `std::tuple`, `index_sequence`, fold expressions.
- Семинар 13: итераторы - категории, требования, свой итератор.
- Семинар 14: `type_traits`, `if constexpr`, концепты (C++20) -
  строгие и понятные ограничения на параметры шаблона.

---

## Ошибки компиляции шаблонов - длинные

Одна ошибка в шаблоне часто печатает экран текста: всю цепочку
инстанцирования, от места вызова до самого глубокого шаблона.

- Читайте цепочку **снизу вверх** и ищите первое сообщение с реальной
  причиной - остальное это её следствия.
- `static_assert` с сообщением даёт понятную ошибку в точке нарушения
  условия вместо страницы текста от компилятора:

```cpp
#include <type_traits>

template <typename T>
T divide(T a, T b) {
  static_assert(std::is_arithmetic_v<T>, "T must be an arithmetic type");
  return a / b;
}
```

- Флаги, укорачивающие вывод: `-ftemplate-backtrace-limit=1`
  (gcc/clang), `/diagnostics:caret` (MSVC).

---

## Типичные ошибки

- Определение шаблона кладут в `.cpp` - на этапе линковки
  `undefined reference`, а не при чтении кода.
- Путают явное указание аргументов шаблона (`f<int>(x)`) со
  специализацией - это два разных механизма на разных этапах сборки.
- Забывают `typename` перед зависимым именем типа - ошибка компиляции
  вида "expected expression" в шаблоне, использующем `T::value_type`.
- Не проверяют, что для разных типов аргументов (`add(1, 2.5)`)
  компилятор не выводит общий `T`, и удивляются ошибке вместо того,
  чтобы дать два параметра шаблона или привести типы явно.
- Реализуют `Stack<bool>` поверх `std::vector<bool>` вместо ручной
  упаковки битов - переносят проблему прокси-`operator[]` в свой код.
- Правят код по последней строке ошибки компилятора, а не по первой
  в цепочке инстанцирования.

---

## Вопросы для самопроверки

- Что сломается, если определение шаблонной функции или класса
  переехать из заголовка в `.cpp`?
- Чем явное указание аргументов шаблона (`convert<int>(...)`)
  отличается от специализации шаблона?
- Почему в `typename T::value_type first = ...;` нужен `typename`?
- Почему `std::vector<bool>` нельзя использовать как основу
  для `Stack<bool>`, если нужен настоящий ссылочный `operator[]`?
- Зачем в Задаче 5 нужен `csv_parser.cpp`, если весь код и так лежит
  в заголовках?

---

## Практика на паре

1. Напишите шаблонную функцию `max(a, b)` для любого типа
   с `operator>`. Проверьте `max(1, 2)` и `max(1.5, 2)` - обсудите,
   почему второй вызов не компилируется, и какие есть варианты
   исправления (два параметра шаблона, явное приведение, явное `T`).
2. Напишите шаблонный класс `Stack<T>` с `push`/`pop`/`top`/`empty`
   поверх `std::vector<T>`.
3. Добавьте полную специализацию `Stack<bool>`, которая хранит биты
   компактно - без `std::vector<bool>`, например через
   `std::vector<std::uint64_t>`, как в Задаче 2.
4. Перенесите определение любого метода `Stack<T>` в отдельный `.cpp`,
   соберите проект и разберите сообщение компоновщика
   `undefined reference` - объясните, почему оно появилось на этапе
   линковки, а не компиляции.
