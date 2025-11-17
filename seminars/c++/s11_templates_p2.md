---
marp: true
theme: default
paginate: true
---

# Шаблоны в C++ (часть 2)

## Метапрограммирование и продвинутые техники

---

## Шаблонное метапрограммирование — идея

- Используем типы и значения как вычислительные единицы во время компиляции.
- **Цель:** вычисления происходят при компиляции, а не во время выполнения программы.
- Примеры: вычисления, выбор типа, списки типов.

```cpp
// Пример: вычисление факториала на этапе компиляции
template<int N>
struct Fact {
  static constexpr int value = N * Fact<N-1>::value;
};
template<>
struct Fact<0> {
  static constexpr int value = 1;  // базовый случай рекурсии
};

static_assert(Fact<5>::value == 120);  // проверка во время компиляции!
// Значение уже вычислено компилятором, не тратим время выполнения
```

---

## std::integral_constant и alias templates

- `std::integral_constant` — стандартный способ хранения compile-time значений.
- **alias templates** (using) — создание синонимов шаблонов для удобства.

```cpp
// std::integral_constant<int, 120> хранит значение 120 как тип
template<int N>
using fact_t = std::integral_constant<int, Fact<N>::value>;

static_assert(fact_t<4>::value == 24);
static_assert(fact_t<5>() == 120);  // можно вызывать как функцию

// Пример из стандартной библиотеки:
// std::true_type = std::integral_constant<bool, true>
// std::false_type = std::integral_constant<bool, false>
```

---

## SFINAE — substitution failure is not an error

**SFINAE** = "Ошибка подстановки — это не ошибка"

- Позволяет отбирать перегрузки/шаблоны по возможности подстановки.
- Если подстановка типа невозможна, компилятор просто **убирает** этот вариант из рассмотрения (не выдаёт ошибку).
- Часто через `std::enable_if` или detection idiom.

```cpp
// Функция работает ТОЛЬКО для целочисленных типов
template<typename T>
std::enable_if_t<std::is_integral_v<T>, T> f(T x) {
  return x + 1;
}

// Функция работает для НЕ-целочисленных типов
template<typename T>
std::enable_if_t<!std::is_integral_v<T>, T> f(T x) {
  return x;
}

int a = f(5);      // выбирается первая версия
double b = f(3.14); // выбирается вторая версия
```

---

## Detection idiom и std::void_t (C++17)

**Задача:** Как проверить, есть ли у типа метод `to_string()`?

```cpp
// Шаг 1: Базовый шаблон — по умолчанию false
template<typename, typename = std::void_t<>>
struct has_to_string : std::false_type {};

// Шаг 2: Специализация — если выражение валидно, то true
template<typename T>
struct has_to_string<T, std::void_t< decltype(std::declval<T>().to_string()) >>
  : std::true_type {};

// std::declval<T>() — создаёт "фиктивный" объект типа T для проверки
// std::void_t<...> — превращает любой тип в void (используется для SFINAE)
```

**Использование:**

```cpp
struct A { std::string to_string() const { return "A"; } };
struct B {};

static_assert(has_to_string<A>::value);   // true
static_assert(!has_to_string<B>::value);  // false
```

---

## std::type_traits — мощный инструмент

**Type traits** — это шаблоны для проверки и модификации типов во время компиляции.

```cpp
// Проверка эквивалентности типов
static_assert(std::is_same_v<int, int>);
static_assert(!std::is_same_v<int, const int>);  // const меняет тип!

// Проверка возможности конвертации
static_assert(std::is_convertible_v<int, double>);      // int → double OK
static_assert(!std::is_convertible_v<std::string, int>); // string → int FAIL

// Другие полезные traits:
// is_pointer_v, is_reference_v, is_const_v, is_class_v, is_function_v
```

**Применение:** static_assert, SFINAE, выбор алгоритмов в зависимости от типа.

---

## Удаление квалификаторов и decay

**Проблема:** `const int`, `int&`, `int&&` — это разные типы. Как их нормализовать?

```cpp
// remove_cv_t убирает const и volatile
static_assert(std::is_same_v<
  std::remove_cv_t<const volatile int>,
  int
>);

// decay_t убирает ссылки, const/volatile, превращает массивы в указатели
using bare_t = std::decay_t<T>;

static_assert(std::is_same_v<bare_t<int&>, int>);           // убрали &
static_assert(std::is_same_v<bare_t<const int>, int>);      // убрали const
static_assert(std::is_same_v<bare_t<int[5]>, int*>);        // массив → указатель
static_assert(std::is_same_v<bare_t<int()>, int(*)()>);     // функция → указатель на функцию
```

**Когда использовать:** перед сравнением типов, в generic коде.

---

## conditional_t — выбор типа по условию

**Аналог тернарного оператора для типов:** `condition ? type1 : type2`

```cpp
template<bool B>
using chosen_t = std::conditional_t<B, std::true_type, std::false_type>;

static_assert(std::is_same_v<chosen_t<true>, std::true_type>);
static_assert(std::is_same_v<chosen_t<false>, std::false_type>);

// Практический пример: выбор типа данных
template<bool UseDouble>
using number_t = std::conditional_t<UseDouble, double, int>;

number_t<true> x = 3.14;   // double
number_t<false> y = 42;    // int
```

---

## enable_if / SFINAE — перегрузки по свойствам типов

**Задача:** разное поведение для чисел и не-чисел.

```cpp
// Вариант 1: через возвращаемый тип
template<typename T>
std::enable_if_t<std::is_arithmetic_v<T>, T>
incr(T x) { return x + 1; }  // только для чисел

template<typename T>
std::enable_if_t<!std::is_arithmetic_v<T>, T>
incr(T x) { return x; }  // для остальных типов

// Вариант 2: через фиктивный параметр шаблона
template<typename T, typename = std::enable_if_t<std::is_integral_v<T>>>
void only_integral(T) { /* работает только для int, long и т.д. */ }

only_integral(42);      // OK
// only_integral(3.14); // ОШИБКА компиляции
```

---

## Detection idiom — подробнее

**Зачем:** Проверить наличие метода/типа без "жёсткой" ошибки компиляции.

```cpp
// Проверяем наличие метода to_string()
template<typename, typename = std::void_t<>>
struct has_to_string : std::false_type {};

template<typename T>
struct has_to_string<T, std::void_t<
  decltype(std::declval<T>().to_string())
>> : std::true_type {};

// Использование в SFINAE
template<typename T>
std::enable_if_t<has_to_string<T>::value, std::string>
stringify(const T& obj) {
  return obj.to_string();
}
```

**Паттерн:** проверить → выбрать правильную перегрузку → предотвратить ошибки.

---

## Комбинирование traits и constexpr if

**C++17:** `if constexpr` — условие проверяется во время компиляции.

```cpp
template<typename T>
auto stringify(T&& t) {
  // Ветка выбирается во время КОМПИЛЯЦИИ
  if constexpr (has_to_string<std::decay_t<T>>::value) {
    return std::forward<T>(t).to_string();
  }
  else if constexpr (std::is_arithmetic_v<std::decay_t<T>>) {
    return std::to_string(t);
  }
  else {
    return std::string{"<unknown>"};
  }
  // Невыбранные ветки даже не компилируются!
}
```

**Преимущество:** проще читать, чем SFINAE с перегрузками.

---

## Практические паттерны проверки

```cpp
// Можно ли копировать тип memcpy?
static_assert(std::is_trivially_copyable_v<int>);
static_assert(!std::is_trivially_copyable_v<std::string>); // есть конструкторы

// Есть ли конструктор по умолчанию?
static_assert(std::is_default_constructible_v<std::string>);

// Можно ли вызвать как функцию?
auto lambda = [](int x) { return x * 2; };
static_assert(std::is_invocable_v<decltype(lambda), int>);
static_assert(!std::is_invocable_v<decltype(lambda), std::string>); // нельзя вызвать с string
```

**Применение:** оптимизация (выбор memcpy vs copy constructor), проверка корректности API.

---

## Variadic шаблоны — продвинутые приёмы

**Fold expressions (C++17)** — компактный способ обработки parameter pack.

```cpp
// Сумма произвольного числа аргументов
template<typename... Args>
auto sum(Args... args) {
  return (args + ... + 0);  // унарный правый fold: (a1 + (a2 + (a3 + 0)))
}

auto result = sum(1, 2, 3, 4);  // 10

// Другие варианты fold:
// (... + args)       — левый fold: (((0 + a1) + a2) + a3)
// (args && ...)      — логическое И всех аргументов
// (std::cout << ... << args)  — вывод всех аргументов
```

---

## Apply функция к кортежу (tuple)

**Задача:** вызвать функцию, передав элементы tuple как аргументы.

```cpp
// Вспомогательная функция с index_sequence
template<typename F, typename Tuple, std::size_t... I>
decltype(auto) apply_impl(F&& f, Tuple&& t, std::index_sequence<I...>) {
  // std::get<I>(t) — получает I-й элемент кортежа
  // ... — распаковывает все индексы
  return std::invoke(std::forward<F>(f), std::get<I>(std::forward<Tuple>(t))...);
}

template<typename F, typename Tuple>
decltype(auto) apply(F&& f, Tuple&& t) {
  constexpr auto size = std::tuple_size_v<std::decay_t<Tuple>>;
  return apply_impl(std::forward<F>(f), std::forward<Tuple>(t),
                    std::make_index_sequence<size>{});
}

// Использование
auto add = [](int a, int b) { return a + b; };
auto result = apply(add, std::make_tuple(3, 4));  // 7
```

---

## integer_sequence и индексные последовательности

**Зачем:** генерация compile-time последовательности индексов 0, 1, 2, ...

```cpp
template<std::size_t... I>
void print_indices(std::index_sequence<I...>) {
  ((std::cout << I << ' '), ...);  // fold expression
}

print_indices(std::make_index_sequence<5>{});  // Выведет: 0 1 2 3 4

// Типичное применение: распаковка tuple, массивов, parameter packs
```

---

## Perfect forwarding и universal references

**Проблема:** как передать аргументы без копирования и с сохранением категории (lvalue/rvalue)?

```cpp
template<typename F, typename... Args>
decltype(auto) call_forward(F&& f, Args&&... args) {
  // std::forward сохраняет "категорию значения"
  return std::forward<F>(f)(std::forward<Args>(args)...);
}

// T&& в шаблоне — это НЕ rvalue ссылка, а "универсальная ссылка"
// Правило: T&& + вывод типа = может быть и lvalue, и rvalue
```

**Совет:** не храните `std::forward` результаты в переменных — используйте сразу.

---

## Universal references — пример с if constexpr

```cpp
#include <iostream>
#include <utility>

template<typename T>
void process(T&& arg) {
    // Проверяем категорию значения во время компиляции
    if constexpr (std::is_lvalue_reference_v<T&&>) {
        std::cout << "Lvalue: " << arg + 1 << "\n";
    } else {
        std::cout << "Rvalue: " << arg + 1 << "\n";
    }
}

int main() {
    int a = 5;
    process(a);              // T = int&, вызовет ветку lvalue
    process(std::move(a));   // T = int, вызовет ветку rvalue
    process(10);             // T = int, вызовет ветку rvalue
}
```

---

## Concepts и requires (C++20)

**Концепты** — именованные требования к типам (замена SFINAE, но читабельнее).

```cpp
template<typename T>
concept Addable = requires(T a, T b) {
  { a + b } -> std::convertible_to<T>;  // результат должен конвертироваться в T
};

// Использование концепта
template<Addable T>
T add(T a, T b) { return a + b; }

// Альтернативный синтаксис
template<typename T>
T add2(T a, T b) requires Addable<T> { return a + b; }

add(5, 3);        // OK
add(1.5, 2.5);    // OK
add(std::string{"a"}, std::string{"b"});  // OK
/// add("a", "b");  // Error
```

**Преимущество:** понятные сообщения об ошибках компиляции.

---

## Пользовательские концепты — примеры

**Создавайте маленькие переиспользуемые концепты.**

```cpp
// Проверка, что тип можно инкрементировать
template<typename T>
concept Incrementable = requires(T x) {
  { ++x } -> std::same_as<T&>;
  { x++ } -> std::same_as<T>;
};

// Проверка, что тип итерируемый (есть begin/end)
template<typename T>
concept Iterable = requires(T x) {
  std::begin(x);
  std::end(x);
};

// Проверка, что можно вызвать с аргументом типа Arg
template<typename F, typename Arg>
concept Callable = requires(F f, Arg arg) {
  { f(arg) };
};
```

---

## CRTP (Curiously Recurring Template Pattern)

**Идея:** класс наследуется от шаблона, передавая сам себя как параметр.

```cpp
// Базовый класс знает о своём наследнике через шаблон!
template<typename Derived>
struct Base {
  void interface() {
    static_cast<Derived*>(this)->impl();  // вызов метода наследника
  }
};

struct Impl : Base<Impl> {
  void impl() { std::cout << "Impl::impl()\n"; }
};

// Использование
Impl obj;
obj.interface();  // вызовет Impl::impl() без виртуальных функций!
```

**Применение:** static polymorphism (без накладных расходов виртуальных функций), миксины, интерфейсы.

---

## Template template parameters

**Передача шаблона как параметра шаблона.**

```cpp
// Container принимает ТИП (T) и ШАБЛОН (Alloc)
template<typename T, template<typename> class Alloc>
struct Container {
  Alloc<T> allocator;  // Alloc — это шаблон, применяем его к T

  void allocate(size_t n) {
    // используем allocator для выделения памяти
  }
};

// std::allocator — это шаблон
Container<int, std::allocator> c;
```

**Применение:** policy-based design, гибкая настройка контейнеров.

---

## Дедукция шаблонов и deduction guides

**C++17:** компилятор может выводить параметры шаблона класса из конструктора.

```cpp
template<typename T>
struct Wrapper {
  Wrapper(T val) : value(val) {}
  T value;
};

// До C++17: нужно было писать Wrapper<int>(42)
// C++17: можно просто
Wrapper w(42);  // автоматически выводится Wrapper<int>

// Пользовательский deduction guide
Wrapper(const char*) -> Wrapper<std::string>;  // const char* → string

Wrapper w2("hello");  // Wrapper<std::string>, а не Wrapper<const char*>
```

---

## Tag dispatching — выбор реализации

**Идея:** использовать "пустые" типы-маркеры для выбора перегрузки.

```cpp
struct fast_tag {};
struct safe_tag {};

// Две разные реализации
template<typename T>
void sort_impl(T* data, size_t n, fast_tag) {
  // быстрая сортировка (возможно небезопасная)
}

template<typename T>
void sort_impl(T* data, size_t n, safe_tag) {
  // безопасная сортировка
}

// Публичная функция выбирает реализацию
template<typename T>
void sort(T* data, size_t n) {
  if constexpr (std::is_trivially_copyable_v<T>)
    sort_impl(data, n, fast_tag{});
  else
    sort_impl(data, n, safe_tag{});
}
```

---

## Подводные камни и советы

- **Избегайте чрезмерной сложности** — шаблоны должны быть понятными.
- **SFINAE vs concepts:** в новых проектах (C++20+) используйте concepts (понятнее ошибки).
- **Старайтесь ограничивать интерфейс:** лучше несколько маленьких концептов, чем один большой.
- **static_assert с сообщениями:** помогите пользователю понять ошибку.
  ```cpp
  static_assert(std::is_integral_v<T>, "T must be an integral type");
  ```
- **Не злоупотребляйте метапрограммированием:** используйте только когда действительно нужно.

---

## Ошибки компиляции шаблонов

**Проблема:** ошибки шаблонов могут быть очень длинными и запутанными.

**Советы:**

- Ищите **первую** ошибку в выводе компилятора
- Используйте `static_assert` для ранней проверки
- В C++20 концепты дают гораздо более понятные ошибки
- Инструменты: `-ftemplate-backtrace-limit=1` (clang), `/diagnostics:caret` (MSVC)

---

## Практическое задание

1. Создайте концепт `Printable<T>` — проверяет наличие `operator<<`
2. Напишите функцию `stringify<T>()` с использованием detection idiom и `if constexpr`
3. Реализуйте CRTP-миксин `Comparable<T>` с операторами сравнения
4. (\*) Создайте variadic `max(args...)` через fold expression
5. (\*\*) Реализуйте простой `tuple_transform` — применить функцию ко всем элементам tuple

---

## Итог и ресурсы

- **Понимайте модель инстанциации** — шаблоны инстанцируются при использовании.
- **Читайте сообщения компилятора** — первая ошибка обычно самая важная.
- **Используйте современные фичи:** concepts вместо SFINAE, `if constexpr` вместо tag dispatching (где возможно).

**Ресурсы:**

- [cppreference.com/templates](https://en.cppreference.com/w/cpp/language/templates)
- Книга: "C++ Templates: The Complete Guide" (Vandevoorde, Josuttis, Gregor)
- [C++20 Concepts](https://en.cppreference.com/w/cpp/language/constraints)
- [Type Traits reference](https://en.cppreference.com/w/cpp/header/type_traits)

---
