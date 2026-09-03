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

# Move-семантика и умные указатели

**Семинар 5**
Курс: Объектно-ориентированное программирование
2 курс

---

## lvalue и rvalue

- **lvalue** - у значения есть имя: переменная, поле, разыменованный указатель.
  У него можно взять адрес: `&x`.
- **rvalue** - временное значение без имени: результат выражения, литерал,
  возврат функции по значению.

```cpp
void foo(int value);

void demo() {
  int x = 5;      // x - lvalue
  int y = x + 1;  // x + 1 - rvalue, временный результат
  foo(x);         // x передаётся как lvalue
  foo(y);         // y - lvalue, хотя инициализирован из rvalue
  foo(x + 1);     // x + 1 передаётся как rvalue
}
```

Полная классификация значений (glvalue/xvalue/prvalue) сложнее - на этом курсе
она не понадобится.

---

## Зачем нужно перемещение

- Копирование объекта, владеющего памятью (`std::vector`, `std::string`), - это
  выделение новой памяти и побайтовое копирование содержимого.
- Часто исходный объект после этого больше не нужен: `return` временного объекта,
  `push_back` во время реаллокации вектора.
- Перемещение вместо копирования - это перенос указателя на уже выделенную
  память: O(1) вместо O(n).
- Дальше - на классе, устроенном ровно как хранилище `BitArray`: он владеет
  `std::vector<std::uint64_t>`.

---

## Копирование - это выделение и побайтовая копия

```cpp
class WordBuffer {
public:
  explicit WordBuffer(std::size_t count) : words_(count, 0) {}

  WordBuffer(const WordBuffer& other) : words_(other.words_) {}
  // Allocates a new vector and copies every word: O(n) time and memory.

private:
  std::vector<std::uint64_t> words_;
};
```

---

## Перемещение - это перенос указателя

```cpp
class WordBuffer {
public:
  explicit WordBuffer(std::size_t count) : words_(count, 0) {}

  WordBuffer(WordBuffer&& other) noexcept : words_(std::move(other.words_)) {}
  // std::vector's move ctor copies its 3 internal pointers and clears
  // the source: O(1), no allocation, no element-by-element copy.

private:
  std::vector<std::uint64_t> words_;
};
```

---

## Перемещающий конструктор и присваивание

```cpp
class WordBuffer {
public:
  WordBuffer(WordBuffer&& other) noexcept
      : words_(std::move(other.words_)) {}

  WordBuffer& operator=(WordBuffer&& other) noexcept {
    if (this != &other) {
      words_ = std::move(other.words_);
    }
    return *this;
  }

private:
  std::vector<std::uint64_t> words_;
};
```

`std::move(other.words_)` сам по себе ничего не перемещает - это лишь
приведение к rvalue-ссылке, которое разрешает вызвать перемещающую перегрузку
у `std::vector`.

---

## Состояние объекта после перемещения

После `WordBuffer b(std::move(a));` объект `a` переходит в "валидное, но
не определённое" состояние (valid but unspecified state):

- **Валидное**: `a` можно разрушить - деструктор отработает корректно;
  `a` можно присвоить новое значение через `operator=`.
- **Не определённое**: нельзя полагаться на конкретное содержимое `a`. Даже
  если сегодня `std::vector` после перемещения пуст на практике, стандарт
  этого не гарантирует.
- Чтение состояния `a` (кроме присваивания нового значения) - ошибка
  контракта класса, а не UB низкого уровня. Опасность в том, что код
  скомпилируется и может даже "случайно работать".

---

## Почему move-операции обязаны быть noexcept

- При реаллокации (`push_back`, `resize`, ...) `std::vector<T>` обязан
  сохранить сильную гарантию: если что-то бросит исключение, старые элементы
  должны остаться нетронутыми.
- Если перемещающий конструктор `T` может бросить исключение, `std::vector`
  не может дать эту гарантию через перемещение - и **тихо переключается
  на копирование** каждого элемента.
- Класс с move-конструктором без `noexcept` работает внутри вектора как класс
  без move-конструктора вообще, но без единого предупреждения компилятора.
- Выбор делает `std::move_if_noexcept`: он отдаёт rvalue-ссылку, только если
  перемещение не бросает (или нет копирующего конструктора вовсе).
- Это разница в производительности O(1) против O(n) на каждой реаллокации,
  а не вопрос стиля.

---

## Rule of five

Если класс объявляет вручную хотя бы одну из пяти операций - остальные
не появляются "просто так", про все пять надо подумать вместе:

1. Деструктор
2. Копирующий конструктор
3. Копирующее присваивание
4. Перемещающий конструктор
5. Перемещающее присваивание

Компилятор умеет сгенерировать все пять автоматически - но объявление
любой из них (кроме особых случаев) отключает автогенерацию перемещающих
операций.

---

## Rule of zero

Лучшее решение - не объявлять ни одной из пяти вручную: не владеть ресурсом
напрямую, а владеть им через `std::vector`, `std::string`, `std::unique_ptr`.
Тогда все пять генерируются компилятором и корректны сами по себе.

```cpp
class WordBuffer {
public:
  explicit WordBuffer(std::size_t count) : words_(count, 0) {}

private:
  std::vector<std::uint64_t> words_;
  // No custom destructor, no custom copy/move: all five are generated
  // and each of them just delegates to std::vector's own five.
};
```

---

## Ловушка: деструктор подавляет перемещение

```cpp
class Logger {
public:
  ~Logger() {
    // Just wanted to flush on destruction...
    std::cout << "flushing" << std::endl;
  }

private:
  std::vector<std::uint64_t> buffer_;
};
```

- Объявлен деструктор -> компилятор **не генерирует** перемещающий конструктор
  и перемещающее присваивание.
- Копирующие конструктор/присваивание при этом ещё генерируются (устаревшее
  правило ради совместимости), класс продолжает компилироваться.
- Результат: везде, где ожидалось перемещение (`std::vector<Logger>` при
  реаллокации, `return` по значению, `std::move(logger)`), тихо происходит
  **копирование**.
- Нужен свой ресурс, и rule of zero не подходит - объявляйте все пять
  операций явно, вместе.

---

## `= default` и `= delete`

```cpp
class NonCopyable {
public:
  NonCopyable() = default;
  NonCopyable(const NonCopyable&) = delete;
  NonCopyable& operator=(const NonCopyable&) = delete;

  NonCopyable(NonCopyable&&) = default;
  NonCopyable& operator=(NonCopyable&&) = default;
  ~NonCopyable() = default;
};
```

- `= default` - "сгенерируй как обычно", но явно и без побочных эффектов
  от объявления других членов пятёрки.
- `= delete` - перегрузка существует, но её использование - ошибка
  компиляции, а не ошибка времени выполнения.
- Запрет копирования плюс явные `= default` на перемещение - типичный шаблон
  для классов, владеющих неразделяемым ресурсом.

---

## copy-and-swap: идея

- `swap` меняет местами внутренности двух объектов без выделения памяти,
  `noexcept`.
- Копирующее присваивание реализуется через копирующий конструктор
  и `swap`, а не вручную поэлементно.
- Если конструирование временной копии бросит исключение - `*this` ещё
  не тронут. Это и есть сильная гарантия исключений.

---

## copy-and-swap: реализация

```cpp
class WordBuffer {
public:
  WordBuffer(const WordBuffer& other) : words_(other.words_) {}

  void swap(WordBuffer& other) noexcept {
    words_.swap(other.words_);
  }

  WordBuffer& operator=(const WordBuffer& other) {
    WordBuffer tmp(other);  // May throw: *this is untouched so far.
    swap(tmp);              // Never throws.
    return *this;
  }  // tmp is destroyed here, holding the old contents of *this.

private:
  std::vector<std::uint64_t> words_;
};
```

---

## Гарантии исключений: напоминание

- **nothrow (`noexcept`)**: операция гарантированно не бросает. Обязательна
  для `swap`, деструктора, перемещающих конструктора и присваивания.
- **strong**: если бросило исключение - объект остаётся ровно в том
  состоянии, в котором был до вызова.
- **basic**: если бросило исключение - инварианты класса не нарушены,
  утечек нет, но содержимое могло измениться.

Strong разумно требовать от всех модифицирующих операций контейнера:
`resize`, `clear`, `push_back`, `set`/`reset`, `operator&=`/`|=`/`^=`,
`operator<<=`/`>>=`. Способ получить это почти бесплатно - тот же
copy-and-swap: собрать новое состояние отдельно, обменять с `*this` только
когда оно готово.

---

## `std::unique_ptr`: единоличное владение

```cpp
#include <memory>

class Enemy {
public:
  void act() {}
};

void demo() {
  auto enemy = std::make_unique<Enemy>();  // Allocates and constructs.
  enemy->act();

  // std::unique_ptr<Enemy> copy = enemy;  // ПЛОХО: не компилируется,
  // копирующий конструктор unique_ptr удалён.
  std::unique_ptr<Enemy> owner = std::move(enemy);  // Владение передано.
}
```

- Владеет ровно один `unique_ptr` за раз, копирование запрещено.
- `std::make_unique<T>(args...)` выделяет и конструирует за один вызов -
  без голого `new` в вашем коде.
- Передать владение можно только явным `std::move`.

---

## Полиморфные объекты нужно хранить по указателю

```cpp
class GameObject {
public:
  virtual ~GameObject() = default;
  virtual void act() = 0;
};

class Enemy : public GameObject {
public:
  void act() override {}
};

void demo() {
  std::vector<std::unique_ptr<GameObject>> objects;
  objects.push_back(std::make_unique<Enemy>());  // Владение уходит в vector.

  for (const auto& object : objects) {
    object->act();  // Виртуальный вызов через указатель.
  }
}
```

- `std::vector<GameObject>` хранить нельзя: элемент вектора - объект
  **точного** типа `GameObject`; кладя туда `Enemy`, мы обрезаем (object
  slicing) его до базовой части.
- База обязана иметь виртуальный деструктор - иначе `unique_ptr<GameObject>`
  вызовет при удалении `~GameObject()`, а не `~Enemy()`.

---

## `std::shared_ptr`: разделяемое владение

```cpp
#include <memory>

class Enemy {
public:
  void act() {}
};

void demo() {
  auto a = std::make_shared<Enemy>();
  std::shared_ptr<Enemy> b = a;  // Копия разрешена: счётчик становится 2.

  std::cout << a.use_count() << std::endl;  // 2

  b.reset();  // Счётчик становится 1, объект жив через a.
}  // a выходит из области видимости: счётчик 0, объект уничтожен.
```

- Объект живёт, пока жив хотя бы один `shared_ptr` на него.
- `use_count()` - число совладельцев прямо сейчас, полезно для отладки.
- `std::weak_ptr` - невладеющая ссылка на объект `shared_ptr`, нужна, чтобы
  разорвать цикл владения (A держит B, B держит A); подробнее - вне рамок
  этого курса.

---

## Copy-on-Write: идея

- Копирующие конструктор и присваивание не копируют буфер, а разделяют его:
  оба объекта временно смотрят на один и тот же `std::shared_ptr` с данными.
- Копирование становится O(1) по времени и памяти.
- Как только объект **меняется**, он обязан сначала отделиться (`detach`) -
  сделать себе уникальную копию буфера, не трогая совладельцев.
- "Разделён ли буфер" проверяется через `use_count() > 1` у `shared_ptr`.

---

## Скелет detach

```cpp
class WordBuffer {
public:
  void detach() {
    if (data_.use_count() > 1) {
      data_ = std::make_shared<std::vector<std::uint64_t>>(*data_);
    }
  }

  void set_word(std::size_t index, std::uint64_t value) {
    detach();
    (*data_)[index] = value;
  }

private:
  std::shared_ptr<std::vector<std::uint64_t>> data_;
};
```

---

## Что детачит, а что нет

- Детачат: всё, что меняет содержимое буфера - `resize`, `clear`,
  `push_back`, `set`/`reset`, `operator&=`/`|=`/`^=`, `operator<<=`/`>>=`.
- Не детачат: константные методы - `operator[]`, `to_string`, `count`,
  `any`/`none`, `operator==`. Они только читают.
- `swap` не детачит: он меняет местами два `shared_ptr` (просто указатели),
  содержимое буферов не трогает - и остаётся `noexcept`.
- Перемещающее присваивание не детачит и не копирует: оно забирает чужой
  `shared_ptr` целиком.
- Копирующее присваивание через copy-and-swap само по себе не детачит -
  детач произойдёт позже, при первой модификации результата.

---

## Типичные ошибки

- Деструктор объявлен "просто чтобы прибраться" - тихо убивает
  автогенерацию move-операций, класс начинает копироваться вместо
  перемещения.
- Move-конструктор или move-присваивание без `noexcept` - `std::vector`
  при реаллокации откажется от перемещения в пользу копирования.
- Чтение полей объекта после `std::move(x)` - компилируется, но нарушает
  контракт класса.
- `return std::move(local);` - мешает NRVO, компилятор и так выполнил бы
  перемещение или вовсе исключил копию.
- Голый `new` вместо `std::make_unique` - при исключении между `new`
  и присваиванием указателю происходит утечка.
- Один сырой указатель передан в конструктор `shared_ptr` дважды - два
  независимых счётчика ссылок на один объект, двойное `delete`.

---

## Вопросы для самопроверки

- Почему перемещающие конструктор и присваивание у вас `noexcept` - что
  сломается, если убрать `noexcept`?
- Что произойдёт, если объявить деструктор `BitArray`, но не объявить
  перемещающие операции явно?
- Откуда в вашем `operator=` берётся сильная гарантия исключений - покажите
  на коде copy-and-swap.
- Как устроен `detach()`: по какому условию он решает копировать буфер
  и какие методы его вызывают?
- Почему `swap` не детачит и остаётся `noexcept`, хотя другие модифицирующие
  операции - нет?

---

## Практика на паре

1. Классу `WordBuffer` (обёртка над `std::vector<std::uint64_t>`) добавить
   перемещающий конструктор и перемещающее присваивание, оба `noexcept`.
2. Доказать тестом, что перемещение не копирует: сравнить `data()` вектора
   до и после перемещения - адрес должен совпасть.
3. Реализовать `swap` и переписать копирующее присваивание через
   copy-and-swap.
4. Написать класс с полем `std::unique_ptr<int>`: `= delete` на копирование,
   `= default` на перемещение. Убедиться, что `std::vector` из таких объектов
   компилируется (используется перемещение), а копирование - нет.
