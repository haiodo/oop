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

# Variadic-шаблоны и std::tuple

## Семинар 12

### Курс: Объектно-ориентированное программирование, ФИТ НГУ

---

## Пакеты параметров: синтаксис

```cpp
template<typename... Args>              // Args - a parameter pack of types
void log_values(const Args&... args) {  // args - a pack of function params
  std::cout << sizeof...(Args) << " values\n";  // pack size, compile-time
}

log_values(1, 2.5, "text");  // Args = {int, double, const char*}
log_values();                // Args = {}, sizeof...(Args) == 0
```

- `typename... Args` объявляет пакет типов, `Args... args` - пакет значений.
- `sizeof...(Args)` - размер пакета, известен на этапе компиляции.
- Пакет - не объект: у него нет `.size()`, нет индексации `Args[0]`.

---

## Пакеты параметров: где стоит "..."

`...` после подвыражения повторяет его для каждого элемента пакета.

```cpp
template<typename... Ts>
void print_size() {
  std::tuple<Ts...> dummy{};  // "..." в списке параметров шаблона
  std::cout << std::tuple_size_v<decltype(dummy)> << '\n';
}

template<typename... Ts>
auto make_row(Ts... values) {
  return std::make_tuple(values...);  // "..." в списке аргументов вызова
}
```

Место `...` определяет, что именно распаковывается: список параметров
шаблона, список аргументов вызова, список инициализации.

---

## Рекурсивная распаковка пакета

Основной способ работы с пакетом до C++17 - и способ, требуемый Задачей 5
для вывода `std::tuple`.

```cpp
// Base case: pack is empty, stop recursion.
void print_all() {
  std::cout << '\n';
}

// Recursive case: peel off the first argument, recurse on the rest.
template<typename T, typename... Rest>
void print_all(const T& first, const Rest&... rest) {
  std::cout << first << ' ';
  print_all(rest...);  // pack shrinks by one on every call
}

print_all(1, 2.5, "text");  // 1 2.5 text
```

Без базового случая (`print_all()`) рекурсия не остановится.

---

## Fold expressions: унарные формы (C++17)

Компактная замена рекурсивной распаковки для одного оператора.

```cpp
template<typename... Args>
auto sum_right(Args... args) {
  return (args + ...);   // unary right fold: (a1 + (a2 + (... + aN)))
}

template<typename... Args>
auto sum_left(Args... args) {
  return (... + args);   // unary left fold: (((a1 + a2) + ...) + aN)
}

sum_right(1, 2, 3, 4);  // 1 + (2 + (3 + 4)) == 10
sum_left(1, 2, 3, 4);   // ((1 + 2) + 3) + 4 == 10
```

Унарный fold требует непустого пакета (кроме `&&`, `||`, `,`).

---

## Fold expressions: бинарные формы (C++17)

Бинарная форма добавляет начальное значение и работает на пустом пакете.

```cpp
template<typename... Args>
auto sum_from_zero(Args... args) {
  // binary right fold: (a1 + (a2 + (... + (aN + 0))))
  return (args + ... + 0);
}

template<typename... Args>
auto sum_from_zero2(Args... args) {
  // binary left fold: ((((0 + a1) + a2) + ...) + aN)
  return (0 + ... + args);
}

sum_from_zero();         // 0, binary fold работает и на пустом пакете
sum_from_zero(1, 2, 3);  // 6
```

Всего четыре формы: `(pack op ...)`, `(... op pack)`,
`(pack op ... op init)`, `(init op ... op pack)`.

---

## Fold expressions: практические примеры

```cpp
template<typename... Args>
bool all_true(Args... args) {
  return (args && ...);  // unary right fold с &&
}

template<typename... Args>
void print_stream(const Args&... args) {
  // binary left fold: init = std::cout, "..." распаковывает пакет
  (std::cout << ... << args) << '\n';
}

all_true(true, true, 1 > 0);          // true
print_stream(1, " + ", 2, " = ", 3);  // 1 + 2 = 3
```

`print_stream` - тот же приём, что понадобится для вывода `std::tuple`.

---

## std::tuple: создание и доступ

```cpp
std::tuple<int, std::string, double> row{42, "answer", 3.14};

int id = std::get<0>(row);            // access by index, not row[0]
std::string name = std::get<1>(row);  // std::get - свободная функция
double weight = std::get<2>(row);

auto row2 = std::make_tuple(1, std::string{"x"}, 2.5);  // тип выводится
```

- `std::tuple` хранит элементы разных типов в одном объекте.
- Индекс в `std::get<I>` обязан быть константой времени компиляции.

---

## tuple_size_v, tuple_element_t, structured bindings

```cpp
using Row = std::tuple<int, std::string, double>;

static_assert(std::tuple_size_v<Row> == 3);
static_assert(std::is_same_v<std::tuple_element_t<1, Row>, std::string>);

Row row{42, "answer", 3.14};
auto [id, name, weight] = row;  // structured bindings, C++17
std::cout << id << ' ' << name << ' ' << weight << '\n';
```

- `tuple_size_v<T>` - число элементов, `tuple_element_t<I, T>` - тип I-го.
- Structured bindings удобны для чтения, но не годятся для generic-кода,
  где число элементов заранее не известно - там нужен `std::get<I>` в цикле.

---

## std::index_sequence и make_index_sequence

Способ получить compile-time последовательность индексов `0, 1, 2, ...`.

```cpp
template<std::size_t... I>
void print_indices(std::index_sequence<I...>) {
  ((std::cout << I << ' '), ...);  // fold через оператор запятая
}

print_indices(std::make_index_sequence<5>{});  // 0 1 2 3 4
```

`std::make_index_sequence<N>` генерирует тип `std::index_sequence<0, ..., N-1>`.
Типичное применение - распаковка `std::tuple` по индексам.

---

## apply: применение функции к tuple

```cpp
template<typename F, typename Tuple, std::size_t... I>
decltype(auto) invoke_with_tuple_impl(F&& f, Tuple&& t,
                                       std::index_sequence<I...>) {
  // std::get<I>(t) берёт I-й элемент, "..." распаковывает все индексы
  return std::invoke(std::forward<F>(f),
                      std::get<I>(std::forward<Tuple>(t))...);
}

template<typename F, typename Tuple>
decltype(auto) invoke_with_tuple(F&& f, Tuple&& t) {
  constexpr auto size = std::tuple_size_v<std::decay_t<Tuple>>;
  return invoke_with_tuple_impl(std::forward<F>(f), std::forward<Tuple>(t),
                                 std::make_index_sequence<size>{});
}
```

**Не называйте свою функцию `apply`:** аргумент - `std::tuple`, поэтому
ADL добавит `std::apply` в кандидаты, и вызов станет неоднозначным.

---

## Оператор вывода tuple: рекурсия по индексу

Механика для Subtask 1. Пример печатает элементы через запятую - формат
скобок и разделителей оформите сами.

```cpp
// Recursive-by-index: print element I, then I + 1, and so on.
template<std::size_t I, typename Tuple>
void print_from(const Tuple& t) {
  if constexpr (I < std::tuple_size_v<Tuple>) {
    if constexpr (I > 0) {
      std::cout << ", ";
    }
    std::cout << std::get<I>(t);
    print_from<I + 1>(t);  // recurse to the next index
  }
  // I == tuple_size: discarded branch, recursion stops here
}
```

`if constexpr` останавливает рекурсию без отдельной перегрузки для
пустого случая - невыбранная ветка не инстанцируется.

---

## Оператор вывода tuple: fold и index_sequence

Второй подход - тот же результат через `index_sequence` и fold expression.

```cpp
template<typename Tuple, std::size_t... I>
void print_impl(const Tuple& t, std::index_sequence<I...>) {
  std::size_t count = 0;
  // fold через запятую: элемент, потом разделитель перед следующим
  ((std::cout << (count++ ? ", " : "") << std::get<I>(t)), ...);
}

template<typename... Args>
void print_tuple(const std::tuple<Args...>& t) {
  print_impl(t, std::make_index_sequence<sizeof...(Args)>{});
}
```

Оба подхода допустимы для задачи. Разница - рекурсия по индексу читается
пошагово, fold компактнее, но менее очевиден при первом чтении.

---

## Подвох: обычный поиск против ADL

`operator<<` для `std::tuple` объявлен в глобальном пространстве имён,
а его аргумент - `std::tuple`. Argument-dependent lookup (ADL) добавляет
в кандидаты только `namespace std` (пространство имени `tuple`) -
глобальное пространство имён ADL не трогает.

```cpp
// BAD: helper defined BEFORE operator<<, and called with a dependent
// argument. At print_one's definition point ordinary lookup does not
// see operator<< yet; at instantiation only ADL applies - and ADL for
// a std::tuple argument does not reach the global namespace.
template<typename T>
void print_one(std::ostream& os, const T& elem) {
  os << elem;  // fails to compile when elem is itself a std::tuple
}
```

Реальная ошибка компилятора: `call to function 'operator<<' that is
neither visible in the template definition nor found by
argument-dependent lookup` - `should be declared prior to the call site`.

---

## Почему нельзя положить оператор в namespace std

- Добавлять в `namespace std` новые функции и операторы для стандартных
  типов - неопределённое поведение. Разрешены только специализации
  стандартных шаблонов для пользовательских типов ([namespace.std]).
- `std::tuple<Args...>` - стандартный тип, даже если `Args` пользовательские:
  сам `operator<<` для `tuple` в `std` класть нельзя.

**Корректная альтернатива:** держать оператор в глобальном пространстве
имён и не разбивать печать на отдельно объявленные до него функции -
тогда рекурсивный вызов находится обычным (не ADL) поиском, а заголовок
достаточно подключить один раз, до первого использования.

---

## Perfect forwarding и универсальные ссылки

```cpp
template<typename F, typename... Args>
decltype(auto) call_forward(F&& f, Args&&... args) {
  // std::forward сохраняет категорию значения (lvalue/rvalue)
  return std::forward<F>(f)(std::forward<Args>(args)...);
}
```

- `T&&` в шаблоне с выводом типа - это универсальная ссылка, а не rvalue-
  ссылка. Она может связаться и с lvalue, и с rvalue.
- Результат `std::forward` не сохраняйте в переменной - используйте сразу.

---

## Universal references: пример с if constexpr

```cpp
template<typename T>
void process(T&& arg) {
  // check the value category at compile time
  if constexpr (std::is_lvalue_reference_v<T&&>) {
    std::cout << "Lvalue: " << arg + 1 << "\n";
  } else {
    std::cout << "Rvalue: " << arg + 1 << "\n";
  }
}

int main() {
  int a = 5;
  process(a);             // T = int&, lvalue branch
  process(std::move(a));  // T = int, rvalue branch
  process(10);             // T = int, rvalue branch
}
```

---

## Типичные ошибки

- Забыт базовый случай рекурсии по пакету параметров - бесконечная
  рекурсия шаблонов при компиляции.
- `sizeof...(Args)` спутан с обращением к пакету как к контейнеру -
  у пакета нет `.size()` и нет `Args[i]`.
- Оператор вывода `tuple` объявлен внутри `namespace std` - неопределённое
  поведение, задача возвращается без разговора.
- Печать элемента вынесена в отдельную функцию, объявленную до
  `operator<<` - вложенный `tuple` не находит оператор через ADL.
- Заголовок с оператором подключён после точки вызова - обычный поиск не
  находит объявление, компилятор ищет только через ADL.
- Fold-выражение выбрано без учёта порядка вычисления для
  некоммутативного оператора (например вычитания).

---

## Вопросы для самопроверки

- Почему `std::get<0>(t)` работает, а `t[0]` - нет?
- Что сломается, если `operator<<` для `tuple` объявить внутри `namespace std`?
- Почему вывод вложенного `tuple` ломается, если печать элемента ищет
  оператор только через ADL?
- Чем `(args + ...)` отличается от `(... + args)` для оператора, для
  которого важен порядок вычисления?
- Почему `sizeof...(Args)` вычисляется на этапе компиляции, а не выполнения?

---

## Практика на паре

1. Написать `sum(args...)` через fold expression. Проверить, что
   происходит с унарной формой на пустом наборе аргументов, и почему
   бинарная форма с начальным значением от этого не страдает.
2. Написать функцию, печатающую элементы `std::tuple` через запятую -
   любым из двух разобранных подходов.
3. Проверить эту функцию на `std::tuple<int, std::tuple<int, int>>`.
   Если реализация ломается - понять, через что именно она искала
   оператор для внутреннего `tuple`, и исправить.
