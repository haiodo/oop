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

# Итераторы: категории и свои реализации

Семинар 13. Курс ООП (C++), ФИТ НГУ.

---

## Итератор как обобщение указателя

Любой итератор обязан уметь три вещи:

- разыменование - получить элемент (`*it`);
- инкремент - перейти к следующему элементу (`++it`);
- сравнение - понять, кончился диапазон или нет (`it != end`).

Указатель на массив - простейший пример итератора:

```cpp
#include <iostream>

int arr[5] = {1, 2, 3, 4, 5};
int* it = arr;
int* last = arr + 5;
while (it != last) {
  std::cout << *it << " ";
  ++it;
}
```

Свой итератор - класс, повторяющий тот же контракт: `operator*`,
`operator++`, `operator==`/`operator!=`.

---

## Категории итераторов

| Категория | Добавляет к предыдущей | Пример в std |
|---|---|---|
| Input | чтение раз, инкремент, `!=` | `std::istream_iterator` |
| Output | запись через `*it = x`, инкремент | `std::ostream_iterator` |
| Forward | многопроходность, default-конструктор | `std::forward_list` |
| Bidirectional | `operator--` (шаг назад) | `std::list` |
| RandomAccess | `it[n]`, `it + n`, `<`, `-` за O(1) | `std::deque` |
| Contiguous | элементы лежат подряд в памяти | `std::vector` |

Каждая следующая категория - надмножество требований предыдущей.
Output - отдельная ветка: пишет, но не обязана поддерживать чтение.

---

## InputIterator против ForwardIterator

**InputIterator - однопроходный.** После `++it` все ранее сделанные
копии итератора недействительны. Второй проход по тому же диапазону
невозможен - только вперёд и только один раз.

**ForwardIterator - многопроходный.** Два независимых прохода по одному
и тому же диапазону дают одинаковый результат. Копию итератора можно
сохранить и вернуться к ней позже.

Терминологическая ловушка: в русской литературе "однонаправленный
итератор" почти всегда означает ForwardIterator (движение только вперёд,
но многопроходно), а не "однопроходный" (InputIterator). Направление
движения и число проходов - независимые свойства. Задача 5 отдельно
заостряет на этом внимание в разделе "Замечания по реализации".

---

## Постфиксный operator++ у InputIterator

Наивный постфикс возвращает копию итератора **до** инкремента:

```cpp
// ПЛОХО: this copy already reflects state advanced by getline().
LineIterator operator++(int) {
  LineIterator tmp = *this;
  ++(*this);
  return tmp;
}
```

Для InputIterator это не даёт того, что кажется: чтение из потока -
общий побочный эффект, а не значение, которое можно скопировать заранее.
Результат `it++` в реальном коде почти всегда отбрасывается (`it++;`
в цикле), поэтому возврат `iterator` - трата копии без пользы.

```cpp
// Correct: return void, result of it++ is normally discarded anyway.
void operator++(int) { ++(*this); }
```

Стандарт это разрешает: `weakly_incrementable` требует лишь, чтобы
выражение `it++;` компилировалось как отдельная инструкция.

---

## Постфиксный operator++: вариант с прокси

Если код всё же использует `*it++` (частый паттерн у `std::istream_iterator`),
верните лёгкий прокси-объект, хранящий только значение для чтения:

```cpp
class PostfixProxy {
 public:
  explicit PostfixProxy(const std::string& value) : value_(value) {}
  const std::string& operator*() const { return value_; }

 private:
  std::string value_;
};

// LineIterator::operator++(int) using the proxy:
PostfixProxy operator++(int) {
  PostfixProxy proxy(line_);
  ++(*this);
  return proxy;
}
```

Для Задачи 5 обычно достаточно `void` - алгоритмы и тесты результат
`it++` не используют. Прокси нужен только под конкретный паттерн вызова.

---

## Разыменование, возвращающее значение

Обычно `operator*()` возвращает ссылку. Но иногда элемента "в памяти"
физически не существует - его нужно вычислить или распаковать:

- `std::vector<bool>` - бит хранится упаковано, а не как отдельный `bool`;
- битовый массив (Задача 2, BitArray) - то же самое;
- вычисляемые последовательности (например, итератор по чётным числам) -
  значения не хранятся вовсе, а считаются на лету.

В таких случаях `operator*()` возвращает `T` **по значению**. Следствие:
`*it = true` не компилируется - у временного значения нет адреса для
записи, и это правильно, а не недоработка.

Для BitArray это явное требование зачёта: прокси-класс для записи через
`operator[]` или итератор добавлять **запрещено**. Для записи есть `set(n, val)`.

---

## Что объявляет свой итератор

Любой итератор для алгоритмов STL обязан объявить пять псевдонимов типов:

- `value_type` - тип элемента;
- `difference_type` - тип разности между итераторами, обычно `std::ptrdiff_t`;
- `reference` - тип, который возвращает `operator*`;
- `pointer` - тип, который возвращает `operator->`;
- `iterator_category` - тег категории: `std::input_iterator_tag`,
  `std::forward_iterator_tag` и так далее.

Алгоритмы (`std::copy`, `std::distance`, `std::count_if`) не читают класс
итератора напрямую - они берут эти псевдонимы через
`std::iterator_traits<It>`. Без них код компилируется, но не работает
ни с одним алгоритмом STL.

---

## Свой InputIterator: постановка

Итератор по строкам потока: читает следующую строку при `++`, отдаёт
текущую строку при `*`. Состояние - минимальное:

- указатель на источник (`std::istream*`);
- текущее прочитанное значение (`std::string`);
- флаг конца (`bool`).

Сравнение: любой итератор, дошедший до конца, равен `end()`. Два ещё
не дошедших до конца итератора считаются равными между собой - как
у `std::istream_iterator`. Осмысленно только `it != end()`.

Это ровно тот же контракт, что нужен для итератора CSVParser в Задаче 5,
но на простом примере "одна строка - один `std::string`".

---

## Скелет InputIterator, часть 1

```cpp
class LineIterator {
 public:
  using iterator_category = std::input_iterator_tag;
  using value_type = std::string;
  using difference_type = std::ptrdiff_t;
  using pointer = const std::string*;
  using reference = const std::string&;

  LineIterator() : in_(nullptr), at_end_(true) {}
  explicit LineIterator(std::istream& in) : in_(&in), at_end_(false) {}

  const std::string& operator*() const { return line_; }

  // operator++, operator==/!=, private section: next slide
```

---

## Скелет InputIterator, часть 2

```cpp
  // LineIterator continued from the previous slide:
  LineIterator& operator++() {
    if (!std::getline(*in_, line_)) {
      at_end_ = true;
    }
    return *this;
  }
  void operator++(int) { ++(*this); }

  bool operator==(const LineIterator& other) const {
    return at_end_ == other.at_end_;
  }
  bool operator!=(const LineIterator& other) const { return !(*this == other); }

 private:
  std::istream* in_;
  std::string line_;
  bool at_end_;
};
```

---

## Ленивость: где именно происходит чтение

- Конструктор `LineIterator(std::istream&)` не читает ничего - только
  запоминает источник. Иначе итератор до первого `++` уже "стоил" бы
  чтения, которое может не понадобиться.
- Первое чтение происходит в `begin()` - там, где вызывающий код
  действительно запрашивает первый элемент.
- Каждое следующее чтение - в `operator++`, по требованию, а не заранее.

```cpp
class LineRange {
 public:
  explicit LineRange(std::istream& in) : in_(in) {}

  LineIterator begin() {
    LineIterator it(in_);
    ++it;  // the first read happens here, not in the iterator constructor
    return it;
  }
  LineIterator end() { return LineIterator(); }

 private:
  std::istream& in_;
};
```

---

## Свой ForwardIterator: постановка

Итератор по индексам контейнера: хранит указатель на контейнер и текущий
индекс. Копия такого итератора - независимый снимок (индекс - обычное
число), поэтому два скопированных итератора двигаются независимо:
диапазон можно пройти дважды с одинаковым результатом. Это и есть
многопроходность.

Инвалидация: итератор недействителен после любой операции, меняющей
размер контейнера (`resize`, `clear`, `push_back` - как и у обычных
итераторов контейнеров из семинара 8), а для BitArray с Copy-on-Write -
ещё и после любой модифицирующей операции, вызвавшей detach.

---

## Скелет ForwardIterator, часть 1

```cpp
template <typename Container>
class IndexIterator {
 public:
  using iterator_category = std::forward_iterator_tag;
  using value_type = typename Container::value_type;
  using difference_type = std::ptrdiff_t;
  using pointer = const value_type*;
  using reference = value_type;

  IndexIterator() : c_(nullptr), index_(0) {}
  IndexIterator(const Container* c, std::size_t index) : c_(c), index_(index) {}

  reference operator*() const { return (*c_)[index_]; }

  // operator++, operator==/!=, private section: next slide
```

---

## Скелет ForwardIterator, часть 2

```cpp
  // IndexIterator continued from the previous slide:
  IndexIterator& operator++() {
    ++index_;
    return *this;
  }
  IndexIterator operator++(int) {
    IndexIterator tmp = *this;
    ++(*this);
    return tmp;
  }

  bool operator==(const IndexIterator& other) const { return index_ == other.index_; }
  bool operator!=(const IndexIterator& other) const { return !(*this == other); }

 private:
  const Container* c_;
  std::size_t index_;
};
```

Постфикс здесь возвращает копию по значению - и это правильно: индекс
дешёвый, а копия остаётся независимым и корректным снимком состояния.

---

## begin()/end() и range-based for

```cpp
for (auto x : c) {
  std::cout << x << "\n";
}
```

Разворачивается компилятором примерно так:

```cpp
{
  auto&& range = c;
  auto it = range.begin();
  auto stop = range.end();
  for (; it != stop; ++it) {
    auto x = *it;
    std::cout << x << "\n";
  }
}
```

Минимальный набор для этого разворачивания: `begin()`, `end()`,
`operator!=`, `operator*`, `operator++`. Категория (Forward,
Bidirectional...) для простого `for` не нужна вообще - поэтому цикл
одинаково работает и с `std::forward_list`, и с `std::vector`.

---

## C++20: концепты для проверки категории

```cpp
#include <iterator>
#include <vector>

static_assert(std::input_iterator<LineIterator>);
static_assert(std::forward_iterator<IndexIterator<std::vector<int>>>);
```

- `std::input_iterator<It>` и `std::forward_iterator<It>` из `<iterator>`
  проверяют весь контракт категории на этапе компиляции.
- `forward_iterator` дополнительно требует `incrementable`: постфикс
  обязан возвращать `I` по значению. `input_iterator` этого не требует -
  отсюда разница из слайда про постфиксный `operator++`.
- `std::ranges` допускает, чтобы `end()` возвращал тип, отличный от
  `begin()` (sentinel), не обязательно итератор - в наших примерах
  не понадобится.

---

## Типичные ошибки

- Постфиксный `operator++` у InputIterator возвращает копию `iterator` -
  копия ссылается на уже прочитанное состояние.
- Чтение происходит в конструкторе итератора, а не в `begin()` - лишняя
  работа, если диапазон не используется, и не совпадает с ленивой семантикой.
- Сравнение итераторов по позиции в потоке (`tellg()`) вместо флага
  конца - ломается на неseekable потоках (`std::cin`, сетевой сокет).
- Забытый или неверный `iterator_category` - алгоритмы STL молча
  выбирают не ту реализацию или не компилируются с непонятной ошибкой.
- Попытка сделать InputIterator многопроходным (кэшировать все значения
  "на всякий случай") - лишняя память и не то, что просит категория.
- Итератор хранит ссылку или указатель на временный объект (например,
  на `std::istringstream`, созданный внутри функции и уничтоженный при выходе).

---

## Вопросы для самопроверки

- Почему нельзя вызвать `begin()` у CSVParser дважды - что именно сломается?
- Что произойдёт, если постфиксный `operator++` вернуть `iterator` по значению?
- Почему `operator*` у итератора BitArray возвращает `bool`, а не ссылку -
  почему `*it = true` не должно компилироваться?
- Чем итератор BitArray отличается по категории от итератора CSVParser -
  почему один можно пройти дважды, а другой нет?
- Что потребуется изменить в CSVParser, чтобы сделать его итератор
  ForwardIterator?

---

## Практика на паре

1. Написать `EvenIterator` - ForwardIterator по чётным числам `[0, n)`.
   Хранит текущее значение, `operator++` прибавляет 2.
2. Дать диапазону `EvenRange` методы `begin()`/`end()`, прогнать в
   range-based for: напечатать все числа.
3. Прогнать тот же диапазон через `std::count_if` - посчитать числа,
   кратные 4. Если работает без переделок - категория выбрана верно.
4. Добавить `static_assert(std::forward_iterator<EvenIterator>);`.
   Если не компилируется - каких typedef или операций не хватает?
