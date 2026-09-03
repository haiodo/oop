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

# type_traits, if constexpr и концепты

## Семинар 14

### Курс: Объектно-ориентированное программирование, ФИТ НГУ

---

## Шаблонное метапрограммирование: Fact<N>

```cpp
// Compile-time factorial via recursive template instantiation.
template<int N>
struct Fact {
  static constexpr int value = N * Fact<N - 1>::value;
};

template<>
struct Fact<0> {
  static constexpr int value = 1;  // base case of the recursion
};

static_assert(Fact<5>::value == 120);
```

Сегодня то же самое короче пишется обычной `constexpr`-функцией:

```cpp
constexpr int fact(int n) { return n <= 1 ? 1 : n * fact(n - 1); }
static_assert(fact(5) == 120);
```

Рекурсивные шаблоны всё ещё нужно уметь читать - такой стиль есть в
старых библиотеках и в чужом коде.

---

## std::integral_constant и alias templates

```cpp
// std::integral_constant<int, 120> хранит 120 как тип, не как значение
using Fact5 = std::integral_constant<int, Fact<5>::value>;

static_assert(Fact5::value == 120);
static_assert(Fact5() == 120);  // integral_constant можно вызвать как функцию

// alias template - синоним семейства шаблонов, а не одного типа
template<int N>
using fact_t = std::integral_constant<int, Fact<N>::value>;

static_assert(fact_t<4>::value == 24);
```

`std::true_type` / `std::false_type` - это `integral_constant<bool, true/false>`.

---

## std::type_traits: is_same_v, is_convertible_v

```cpp
static_assert(std::is_same_v<int, int>);
static_assert(!std::is_same_v<int, const int>);  // const меняет тип

static_assert(std::is_convertible_v<int, double>);        // int -> double OK
static_assert(!std::is_convertible_v<std::string, int>);  // string -> int нет
```

Type traits - шаблоны, проверяющие или преобразующие типы во время
компиляции. Применение: `static_assert`, SFINAE, выбор алгоритма по типу.

---

## std::type_traits: остальные проверки

```cpp
static_assert(std::is_arithmetic_v<int>);
static_assert(std::is_arithmetic_v<double>);
static_assert(!std::is_arithmetic_v<std::string>);

static_assert(std::is_integral_v<int>);
static_assert(!std::is_integral_v<double>);  // double - арифметика, не integral

static_assert(std::is_trivially_copyable_v<int>);
static_assert(!std::is_trivially_copyable_v<std::string>);  // есть конструкторы

auto square = [](int x) { return x * x; };
static_assert(std::is_invocable_v<decltype(square), int>);
static_assert(!std::is_invocable_v<decltype(square), std::string>);
```

`is_arithmetic_v` и `is_integral_v` - ровно та проверка, которая нужна
для ветки разбора поля CSV в Задаче 5.

---

## Удаление квалификаторов: remove_cv_t и decay_t

```cpp
// remove_cv_t убирает const и volatile
static_assert(std::is_same_v<std::remove_cv_t<const volatile int>, int>);

// decay_t убирает ссылки и cv-квалификаторы, превращает массивы и функции
// в указатели - то же правило, по которому компилятор передаёт аргумент по значению
template<typename T>
using bare_t = std::decay_t<T>;

static_assert(std::is_same_v<bare_t<int&>, int>);         // убрали &
static_assert(std::is_same_v<bare_t<const int>, int>);    // убрали const
static_assert(std::is_same_v<bare_t<int[5]>, int*>);      // массив -> указатель
static_assert(std::is_same_v<bare_t<int()>, int (*)()>);  // функция -> указатель
```

`bare_t` - alias template: без `template<typename T>` перед `using` типовой
параметр `T` не объявлен, и все четыре `static_assert` не скомпилируются.

---

## conditional_t: выбор типа по условию

Аналог тернарного оператора, но для типов.

```cpp
template<bool UseDouble>
using number_t = std::conditional_t<UseDouble, double, int>;

static_assert(std::is_same_v<number_t<true>, double>);
static_assert(std::is_same_v<number_t<false>, int>);

number_t<true> x = 3.14;   // double
number_t<false> y = 42;    // int
```

`std::conditional_t<B, T1, T2>` - это `T1`, если `B == true`, иначе `T2`.

---

## SFINAE: идея

**SFINAE** = substitution failure is not an error.

Если подстановка типа в шаблон не удалась, компилятор молча убирает этот
кандидат из перегрузок вместо ошибки - при условии, что остаётся хотя бы
один подходящий вариант.

```cpp
template<typename T>
std::enable_if_t<std::is_integral_v<T>, T> increment(T x) {
  return x + 1;
}

template<typename T>
std::enable_if_t<!std::is_integral_v<T>, T> increment(T x) {
  return x;
}

int a = increment(5);       // первая перегрузка
double b = increment(3.14); // вторая перегрузка
```

---

## enable_if: перегрузки по свойствам типов

```cpp
template<typename T, typename = std::enable_if_t<std::is_integral_v<T>>>
void require_integral(T) {
  // instantiated only when T is an integral type
}

require_integral(42);      // OK
// require_integral(3.14); // substitution failure -> no matching function
```

**Историческая техника.** До C++20 - единственный способ ограничить
шаблон условием на тип. Сегодня для нового кода вместо SFINAE и
`enable_if` пишут концепты (ниже) - но `enable_if` встречается в
существующем коде, и его нужно уметь читать.

---

## Detection idiom и std::void_t

**Задача:** проверить, есть ли у типа метод `to_string()`, не вызывая
жёсткой ошибки компиляции для типов без него.

```cpp
// Step 1: primary template - false by default.
template<typename, typename = std::void_t<>>
struct has_to_string : std::false_type {};

// Step 2: specialization selected only when the expression is valid.
template<typename T>
struct has_to_string<T, std::void_t<decltype(std::declval<T>().to_string())>>
    : std::true_type {};
```

- `std::declval<T>()` - "фиктивное" значение типа `T` для проверки
  выражения в неисполняемом контексте, без реального конструктора.
- `std::void_t<...>` превращает любой валидный набор типов в `void` -
  если внутри ошибка подстановки, срабатывает SFINAE, выбирается `false_type`.

---

## if constexpr: механика

```cpp
template<typename T>
auto stringify(const T& value) {
  // ветка выбирается на этапе компиляции; невыбранная не инстанцируется
  if constexpr (has_to_string<T>::value) {
    return value.to_string();
  } else if constexpr (std::is_arithmetic_v<T>) {
    return std::to_string(value);
  } else {
    return std::string{"<unknown>"};
  }
}
```

В отличие от обычного `if`, здесь компилятор не проверяет тело
невыбранной ветки на корректность для конкретного `T` - поэтому в одной
функции можно смешивать код, который для другого `T` не скомпилировался бы.

---

## if constexpr: ветка по типу поля в Задаче 5

Ровно этот приём нужен для разбора поля CSV: `std::string` - целиком,
арифметика - через `operator>>` с проверкой полного чтения.

```cpp
template<typename T>
T parse_field(const std::string& field) {
  if constexpr (std::is_same_v<T, std::string>) {
    return field;  // строковое поле берётся как есть, с пробелами
  } else {
    std::istringstream in(field);
    T value{};
    in >> value;
    if (in.fail() || !in.eof()) {  // "12abc" не должно молча стать 12
      throw std::runtime_error("bad field: " + field);
    }
    return value;
  }
}
```

---

## Концепты C++20: определение и requires

Концепт - именованное требование к типу, замена SFINAE, но с понятными
ошибками компиляции.

```cpp
template<typename T>
concept Addable = requires(T a, T b) {
  { a + b } -> std::convertible_to<T>;  // a + b должно приводиться к T
};

template<Addable T>          // Addable как ограничение параметра шаблона
T add(T a, T b) { return a + b; }

template<typename T>
T add2(T a, T b) requires Addable<T> {  // тот же смысл, requires-clause
  return a + b;
}

static_assert(Addable<int>);
static_assert(Addable<std::string>);   // string::operator+ есть
static_assert(!Addable<int*>);         // int* + int* не компилируется
```

---

## Концепты C++20: стандартные концепты

```cpp
static_assert(std::integral<int>);
static_assert(!std::integral<double>);
static_assert(std::convertible_to<int, double>);
static_assert(std::same_as<int, int>);

// Задача 5, автомат: проверка итератора концептом, без запуска программы
static_assert(std::input_iterator<std::istream_iterator<int>>);
```

- Стандартная библиотека уже содержит нужные концепты - `std::integral`,
  `std::convertible_to`, `std::same_as`, `std::input_iterator` и другие
  из `<concepts>` и `<iterator>`. Свой концепт пишите, только если
  стандартного нет.
- `static_assert(Concept<T>)` проверяется на этапе компиляции без теста.

---

## Что осталось за кадром

- **CRTP** (Curiously Recurring Template Pattern) - статический
  полиморфизм без виртуальных функций.
- **Template template parameters** - шаблон как параметр шаблона.
- **Tag dispatching** - выбор перегрузки через пустые типы-маркеры,
  предшественник `if constexpr`.

Ни один из них не требуется ни в одной задаче курса. Кому интересно -
книга "C++ Templates: The Complete Guide" (Vandevoorde, Josuttis, Gregor).

---

## Длинные ошибки компиляции шаблонов

Ошибка в глубоко вложенном шаблоне может занимать экраны текста.

- Ищите **первую** ошибку в выводе компилятора - остальные обычно
  следствия первой.
- Используйте `static_assert` для ранней и понятной проверки.
- Концепты (C++20) дают короче и понятнее ошибку, чем SFINAE.
- Инструменты: `-ftemplate-backtrace-limit=1` (gcc, clang),
  `/diagnostics:caret` (MSVC).

---

## Типичные ошибки

- `if constexpr` не используется там, где нужен - обе ветки должны
  компилироваться для всех типов, и код падает на `std::string`.
- `using bare_t = std::decay_t<T>;` без `template<typename T>` перед
  `using` - `T` не объявлен, ошибка компиляции.
- Detection idiom без специализации по умолчанию (`false_type`) для
  отсутствующего метода - вместо SFINAE получаем жёсткую ошибку.
- Концепт написан слишком широко (`requires(T a) { a + a; }` без проверки
  результата) - пропускает типы, которые реально не подходят.
- Для нового кода на C++20 пишут `enable_if` там, где нужен концепт -
  компилируется, но ошибки остаются нечитаемыми.
- `static_assert` на `std::input_iterator` забыт или проверен только
  тестом, а не в самом заголовке парсера.

---

## Вопросы для самопроверки

- Почему `if constexpr` не компилирует невыбранную ветку, а обычный `if`
  компилирует обе?
- Что сломается, если в `bare_t` убрать `template<typename T>` перед `using`?
- Чем концепт лучше `enable_if` с точки зрения сообщений об ошибках?
- Как проверить, что итератор удовлетворяет `std::input_iterator`, не
  запуская программу?
- Почему SFINAE работает только там, где компилятор ещё выводит параметры
  шаблона, а не в произвольном месте тела функции?

---

## Практика на паре

1. Написать концепт `Printable<T>`, проверяющий наличие `operator<<` в
   `std::ostream` для типа `T`.
2. Написать функцию `stringify<T>()` через `if constexpr`: для `Printable`
   типов - вывод через `operator<<`, для остальных - заглушка `"<unknown>"`.
3. Написать `static_assert`, проверяющий стандартный концепт (например
   `std::input_iterator` или `std::same_as`) на паре знакомых типов.
