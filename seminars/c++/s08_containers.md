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

# Контейнеры STL и игровой цикл

**Семинар 8**
Курс: Объектно-ориентированное программирование
2 курс

---

## Что такое STL?

**Standard Template Library (STL)** - стандартная библиотека шаблонов C++.

Три части:

- **Контейнеры** - структуры данных для хранения объектов.
- **Итераторы** - объекты для обхода контейнеров.
- **Алгоритмы** - функции, которые работают через итераторы, а не через
  конкретный контейнер.

```cpp
#include <algorithm>
#include <iostream>
#include <vector>
```

---

## Классификация контейнеров

### Последовательные контейнеры

`vector`, `deque`, `list`

### Ассоциативные контейнеры

`set`, `multiset`, `map`, `multimap`

### Неупорядоченные ассоциативные контейнеры

`unordered_set`, `unordered_map`

### Адаптеры контейнеров

`stack`, `queue`

---

## vector - основы

```cpp
#include <iostream>
#include <vector>

int main() {
  std::vector<int> v1;                    // empty
  std::vector<int> v2(5);                 // 5 elements, value-initialized to 0
  std::vector<int> v3(5, 10);             // 5 elements, each equal to 10
  std::vector<int> v4 = {1, 2, 3, 4, 5};  // from an initializer list

  v1.push_back(42);  // append to the end
  v1.pop_back();      // remove the last element

  std::cout << v2.empty() << " " << v3.size() << " " << v4[0] << " "
            << v4.at(0);
}
```

`v4[0]` не проверяет границы, `v4.at(0)` бросает `std::out_of_range`.

---

## vector - обход и resize

```cpp
#include <iostream>
#include <vector>

int main() {
  std::vector<int> numbers = {1, 2, 3, 4, 5};
  numbers.push_back(6);

  for (int num : numbers) {
    std::cout << num << " ";
  }

  numbers.resize(10, 0);  // grow to 10 elements, fill new ones with 0
}
```

---

## reserve и emplace_back

```cpp
#include <vector>

struct Point {
  Point(int x, int y) : x_(x), y_(y) {}
  int x_;
  int y_;
};

int main() {
  std::vector<int> ints;
  ints.reserve(1000);  // one allocation upfront, no reallocations while filling
  for (int i = 0; i < 1000; ++i) {
    ints.push_back(i);
  }

  std::vector<Point> points;
  points.push_back(Point(1, 2));  // builds a temporary, then moves it in
  points.emplace_back(3, 4);      // constructs Point in place, no temporary
}
```

`reserve` без известного числа вставок бессмысленен. Для `vector<unique_ptr<T>>`
всё равно нужен `push_back(std::make_unique<T>(...))` - `make_unique` уже
строит объект на месте, `emplace_back` тут ничего не выигрывает.

---

## deque - двусторонняя очередь

```cpp
#include <deque>
#include <iostream>

int main() {
  std::deque<int> d = {1, 2, 3};

  d.push_back(4);   // append to the end
  d.push_front(0);  // insert at the beginning

  std::cout << d[0] << " " << d.at(1) << " " << d.back();

  d.pop_back();
  d.pop_front();
}
```

**Особенности:** эффективные вставки/удаления с обоих концов, произвольный
доступ по индексу как у `vector`, но память не одним куском.

---

## list - двусвязный список

```cpp
#include <iostream>
#include <list>

int main() {
  std::list<int> l = {1, 2, 3, 4, 5};

  l.push_back(6);
  l.push_front(0);

  auto it = l.begin();
  ++it;               // second element
  l.insert(it, 42);   // insert before the second element
  l.erase(it);        // remove that element

  std::cout << l.front() << " " << l.back();
}
```

Нет произвольного доступа по индексу - только последовательный обход.

---

## set - множество уникальных элементов

```cpp
#include <iostream>
#include <set>

int main() {
  std::set<int> s = {3, 1, 4, 1, 5, 9};  // becomes {1, 3, 4, 5, 9}

  s.insert(2);
  s.erase(3);

  std::cout << s.count(4) << " ";  // 0 or 1: is 4 present?

  auto it = s.find(5);
  if (it != s.end()) {
    std::cout << *it;
  }

  for (int x : s) {
    std::cout << " " << x;
  }
}
```

**Особенности:** автосортировка, уникальные элементы, O(log n) на операцию.

---

## map - ассоциативный массив

```cpp
#include <iostream>
#include <map>
#include <string>

int main() {
  std::map<std::string, int> ages;

  ages["Alice"] = 25;
  ages["Bob"] = 30;
  ages.insert({"Charlie", 35});

  std::cout << ages["Alice"] << "\n";

  auto it = ages.find("Bob");
  if (it != ages.end()) {
    std::cout << it->second << "\n";
  }

  for (const auto& entry : ages) {
    std::cout << entry.first << ": " << entry.second << "\n";
  }
}
```

---

## Осторожно: operator[] вставляет элемент

```cpp
#include <iostream>
#include <map>
#include <string>

int main() {
  std::map<std::string, int> scores;

  // operator[] inserts a default-constructed value if the key is missing.
  if (scores["Alice"] == 0) {
    // "Alice" now exists in the map with value 0, even though we only read it.
  }

  std::cout << scores.contains("Bob") << "\n";                // C++20, no insert
  std::cout << (scores.find("Carl") != scores.end()) << "\n";  // pre-C++20
}
```

Для проверки наличия ключа без побочного эффекта - `contains()` (C++20)
или `find() != end()`. `operator[]` использовать только когда запись
действительно нужна.

---

## unordered_map - хеш-таблица

```cpp
#include <iostream>
#include <string>
#include <unordered_map>

int main() {
  std::unordered_map<std::string, int> scores;

  scores["player1"] = 100;
  scores["player2"] = 200;

  std::cout << scores["player1"];  // same interface as map
}
```

**Отличия от `map`:** элементы не отсортированы, нужна хеш-функция для ключа,
среднее время операций O(1) - подробнее на слайде "Выбор контейнера".

---

## Адаптеры контейнеров: stack и queue

```cpp
#include <iostream>
#include <queue>
#include <stack>

int main() {
  std::stack<int> st;
  st.push(1);
  st.push(2);
  std::cout << st.top() << "\n";  // 2: last in, first out
  st.pop();

  std::queue<int> q;
  q.push(1);
  q.push(2);
  std::cout << q.front() << "\n";  // 1: first in, first out
  q.pop();
}
```

`stack` и `queue` не самостоятельные контейнеры - это ограниченный интерфейс
поверх `deque` по умолчанию.

---

## Итераторы

```cpp
#include <algorithm>
#include <iostream>
#include <vector>

int main() {
  std::vector<int> v = {1, 2, 3, 4, 5};

  auto begin_it = v.begin();  // first element
  auto end_it = v.end();      // one past the last element

  for (auto it = begin_it; it != end_it; ++it) {
    std::cout << *it << " ";
  }

  // Same loop, range-based for.
  for (int x : v) {
    std::cout << x << " ";
  }
}
```

---

## Инвалидация итераторов

Итератор, указатель или ссылка на элемент контейнера может стать
недействительным после изменения контейнера. Обращение к недействительному
итератору - неопределённое поведение (UB): падает не всегда и не сразу.

| Контейнер | Вставка | Удаление |
| --- | --- | --- |
| `vector` | без реаллокации - только `end()`. С реаллокацией - всё | всё от точки удаления и дальше, включая `end()` |
| `deque` | по краям - все итераторы, но указатели/ссылки на элементы живы. В середине - всё | по краям - только к удалённому. В середине - всё |
| `list`, `map`, `set` | ничего не инвалидирует | только к удалённому элементу |

`list`/`map`/`set` хранят каждый элемент в отдельном узле на куче: изменение
одного узла не трогает память соседних. `vector` хранит элементы подряд:
реаллокация копирует весь буфер, `erase` сдвигает хвост.

---

## Модификация контейнера во время обхода

Пули появляются и исчезают прямо внутри `act()`. Если добавлять их в тот же
вектор, который в этот момент обходится циклом, - это UB.

```cpp
#include <memory>
#include <vector>

class GameObject {
public:
  virtual ~GameObject() = default;
  virtual void act() = 0;
};
class Bullet : public GameObject {
public:
  void act() override {}
};

bool should_spawn(const GameObject& obj);

void update(std::vector<std::unique_ptr<GameObject>>& objects) {
  // ПЛОХО: push_back внутри range-based for по этому же вектору.
  for (auto& obj : objects) {
    obj->act();
    if (should_spawn(*obj)) {
      objects.push_back(std::make_unique<Bullet>());  // may reallocate!
    }
  }
}
```

---

## Что покажет ASan

```
$ cmake --preset asan && cmake --build --preset asan
$ ./build/asan/task3

==48213==ERROR: AddressSanitizer: heap-use-after-free
READ of size 8 at 0x602000000350 thread T0
    #0 update(std::vector<std::unique_ptr<GameObject>>&) game.cpp:15
    #1 main game.cpp:30

0x602000000350 is located 16 bytes inside a 48-byte region
freed by thread T0 here:
    #0 operator delete(void*)
    #1 std::vector<...>::_M_realloc_insert(...)
    #2 update(std::vector<std::unique_ptr<GameObject>>&) game.cpp:14

SUMMARY: AddressSanitizer: heap-use-after-free game.cpp:15 in update
```

Реаллокация случается, только когда `capacity()` исчерпана. В одном запуске
в векторе было место про запас - сработало. В другом, после других действий
игрока, - нет. Отсюда "не на каждом запуске".

---

## Решение 1: буфер для новых объектов

Тот же `GameObject`/`Bullet`, что и на слайде с плохим кодом, меняется только
`update()`.

```cpp
#include <memory>
#include <vector>

class GameObject {
public:
  virtual ~GameObject() = default;
  virtual void act() = 0;
};
class Bullet : public GameObject {
public:
  void act() override {}
};
bool should_spawn(const GameObject& obj);
void update(std::vector<std::unique_ptr<GameObject>>& objects) {
  std::vector<std::unique_ptr<GameObject>> spawned;
  for (auto& obj : objects) {
    obj->act();
    if (should_spawn(*obj)) {
      spawned.push_back(std::make_unique<Bullet>());
    }
  }
  for (auto& obj : spawned) {
    objects.push_back(std::move(obj));  // merge only after the loop is done
  }
}
```

Новые объекты копятся отдельно и вливаются в основной вектор только после
того, как цикл по нему закончился - итераторы цикла уже не используются.

---

## Решение 2: флаг и std::erase_if

`GameObject` получает состояние `is_alive()`/`mark_dead()` - это состояние,
а не проверка типа.

```cpp
#include <memory>
#include <vector>

class GameObject {
public:
  virtual ~GameObject() = default;
  virtual void act() = 0;
  bool is_alive() const { return alive_; }
  void mark_dead() { alive_ = false; }

private:
  bool alive_ = true;
};

void update(std::vector<std::unique_ptr<GameObject>>& objects) {
  for (auto& obj : objects) {
    obj->act();  // act() may call mark_dead() on itself or on others
  }
  std::erase_if(objects, [](const std::unique_ptr<GameObject>& obj) {
    return !obj->is_alive();
  });
}
```

Удаление происходит одним проходом после цикла обновления, а не внутри него.

---

## Идиома erase-remove

```cpp
#include <algorithm>
#include <iostream>
#include <vector>

int main() {
  std::vector<int> v = {1, 2, 3, 4, 5, 6};

  // remove_if moves survivors to the front and returns the new logical end;
  // it does NOT shrink the vector or destroy the tail elements.
  auto new_end =
      std::remove_if(v.begin(), v.end(), [](int x) { return x % 2 == 0; });

  v.erase(new_end, v.end());  // erase physically drops the tail

  std::cout << v.size();
}
```

`remove_if` сам по себе ничего не удаляет - без `erase()` в конце вектора
останутся "хвостовые" копии, а `size()` не изменится.

---

## std::erase_if (C++20)

```cpp
#include <memory>
#include <vector>

class GameObject {
public:
  virtual ~GameObject() = default;
  bool is_alive() const { return alive_; }

private:
  bool alive_ = true;
};

int main() {
  std::vector<int> v = {1, 2, 3, 4, 5, 6};
  std::erase_if(v, [](int x) { return x % 2 == 0; });  // C++20, one call

  std::vector<std::unique_ptr<GameObject>> objects;
  std::erase_if(objects, [](const std::unique_ptr<GameObject>& obj) {
    return !obj->is_alive();
  });
}
```

Делает то же, что erase-remove, за один вызов. Объявлен в заголовке самого
контейнера (`<vector>`, `<list>`, ...), `<algorithm>` не нужен.

---

## Алгоритмы STL

```cpp
#include <algorithm>
#include <iostream>
#include <vector>

int main() {
  std::vector<int> v = {3, 1, 4, 1, 5, 9};

  std::sort(v.begin(), v.end());

  auto it = std::find(v.begin(), v.end(), 4);
  if (it != v.end()) {
    std::cout << "found: " << *it << "\n";
  }

  int ones = std::count(v.begin(), v.end(), 1);  // how many 1s
  std::cout << ones << "\n";

  std::transform(v.begin(), v.end(), v.begin(),
                  [](int x) { return x * 2; });  // double every element
}
```

---

## Алгоритмы STL: предикаты

```cpp
#include <algorithm>
#include <memory>
#include <vector>

class GameObject {
public:
  virtual ~GameObject() = default;
  bool is_alive() const { return alive_; }
private:
  bool alive_ = true;
};
using Objects = std::vector<std::unique_ptr<GameObject>>;
auto alive = [](const std::unique_ptr<GameObject>& obj) { return obj->is_alive(); };

int count_alive(const Objects& objects) {
  return static_cast<int>(std::count_if(objects.begin(), objects.end(), alive));
}

bool any_alive(const Objects& objects) {
  return std::any_of(objects.begin(), objects.end(), alive);
}

auto find_alive(const Objects& objects) {
  return std::find_if(objects.begin(), objects.end(), alive);
}
```

`find_if` ищет первый подходящий объект (например, для попадания пули),
`any_of` удобен для условия конца игры, `count_if` - для счётчика врагов.

---

## Отделение логики от вывода

Классы игровых объектов и правила игры не вызывают функции терминала
напрямую - иначе модель нельзя протестировать без терминала.

```
GameWorld (model)                 Renderer (view)
- holds the GameObject list       - reads GameWorld state
- update(dt): moves objects,      - draws characters via curses
  resolves collisions             - never changes game state
- no calls to curses, no I/O

        GameWorld  <---- read-only access ----  Renderer
```

`GameWorld` и классы объектов ничего не знают про curses. Тесты на GoogleTest
создают `GameWorld`, вызывают `update()` нужное число раз и проверяют
состояние - без единого вызова терминала.

---

## std::chrono: steady_clock и system_clock

```cpp
#include <chrono>
#include <iostream>

int main() {
  auto wall = std::chrono::system_clock::now();  // can jump: NTP, clock change
  auto mono = std::chrono::steady_clock::now();   // monotonic, never jumps back

  std::cout << wall.time_since_epoch().count() << " "
            << mono.time_since_epoch().count();
}
```

Для измерения интервалов (кадр, прошедшее время) - всегда `steady_clock`.
`system_clock` годится только для календарного времени и отметок времени.

---

## std::chrono: time_point, duration, литералы

```cpp
#include <chrono>
#include <iostream>

using namespace std::chrono_literals;  // enables 50ms, 1s literals

int main() {
  std::chrono::milliseconds frame_budget = 50ms;
  std::chrono::seconds one_second = 1s;

  auto start = std::chrono::steady_clock::now();
  auto elapsed = std::chrono::steady_clock::now() - start;
  auto elapsed_ms = std::chrono::duration_cast<std::chrono::milliseconds>(elapsed);

  std::cout << frame_budget.count() << " " << one_second.count() << " "
            << elapsed_ms.count();
}
```

`using namespace std::chrono_literals` - единственное место в курсе, где
`using namespace` уместен: объявляет только литералы длительностей, не более.

---

## Почему цикл со sleep не годится

```cpp
#include <chrono>
#include <thread>

using namespace std::chrono_literals;

void update();
void render();

// ПЛОХО: скорость игры зависит от того, сколько занимает render().
void run(bool& running) {
  while (running) {
    update();
    render();
    std::this_thread::sleep_for(50ms);
  }
}
```

Реальный интервал кадра = время `render()` + 50ms, а не 50ms. Медленная
отрисовка (большой терминал, слабая машина) - игра замедляется. Быстрая
машина - ускоряется. Скорость игры оказывается свойством железа,
а не правил игры.

---

## Игровой цикл: фиксированный шаг

```cpp
#include <chrono>

using Clock = std::chrono::steady_clock;
constexpr std::chrono::nanoseconds kStep{1'000'000'000 / 60};  // 60 updates/sec

void update();  // moves objects by speed (units per second) times step duration
void render();

void run(bool& running) {
  Clock::time_point previous = Clock::now();
  std::chrono::nanoseconds accumulator{0};

  while (running) {
    Clock::time_point current = Clock::now();
    accumulator += current - previous;
    previous = current;

    while (accumulator >= kStep) {
      update();
      accumulator -= kStep;
    }

    render();
  }
}
```

Скорости объектов задаются в единицах в секунду, а не за вызов `update()`.

---

## Скорость и пропуск препятствий

Если объект смещается больше чем на клетку за один `update()`, проверка
столкновений видит его "до" и "после", но не "между" - объект может
перепрыгнуть стену или пулю.

Из текста Задачи 3:

- Простое допустимое упрощение: ограничить скорость объекта одной клеткой
  за шаг обновления. Назвать это упрощение явно в `REPORT.md`.
- Точнее, но сложнее: проверять столкновения вдоль отрезка движения,
  а не только в конечной точке.

---

## Выбор контейнера

| Операция | vector | deque | list | set | map | unordered_map |
| --- | --- | --- | --- | --- | --- | --- |
| Доступ по индексу | O(1) | O(1) | O(n) | - | - | - |
| Вставка в конец | O(1) ам. | O(1) | O(1) | O(log n) | O(log n) | - |
| Вставка в начало | O(n) | O(1) | O(1) | O(log n) | O(log n) | - |
| Поиск | O(n) | O(n) | O(n) | O(log n) | O(log n) | O(1) в среднем |
| Память | компактная | фрагмент. | фрагмент. | overhead | overhead | overhead |

У `unordered_map`/`unordered_set` O(1) - это **среднее** время, не
гарантированное: при плохом хеше или массовых коллизиях операция
деградирует до O(n).

---

## Практические рекомендации

### Используйте vector когда:

Нужен произвольный доступ, часто добавляете элементы в конец, важна
компактность в памяти.

### Используйте deque когда:

Нужны вставки/удаления и в начале, и в конце, но при этом нужен и
произвольный доступ.

### Используйте list когда:

Часто вставляете/удаляете в середине, размер сильно меняется.

### Используйте set/map когда:

Нужна сортировка или уникальность. Используйте unordered_map когда:
сортировка не нужна, а важна скорость поиска по ключу.

---

## Заключение

**STL-контейнеры** дают готовые структуры данных, единообразный интерфейс
и совместимость с алгоритмами STL.

**Помните:**

- Выбирайте контейнер под задачу и изучайте сложность его операций.
- Изменение контейнера во время обхода - источник почти всех ошибок
  с итераторами, проверяйте это до сдачи задачи, а не после.
- Предпочитайте range-based for и алгоритмы STL самописным циклам.

---

## Типичные ошибки

- Добавление или удаление элементов `vector` внутри range-based for по этому
  же вектору - UB, проявляется не сразу и не на каждом запуске.
- `dynamic_cast`/`switch` по типу/`isEnemy()`-подобные методы в движке вместо
  единообразного виртуального вызова.
- Игровой цикл вида `update - render - sleep(50ms)` - скорость игры зависит
  от машины и от отрисовки.
- `map["key"]` используется для проверки наличия ключа - молча создаёт запись.
- Хранение указателей/итераторов на элементы `vector`, которые потом
  инвалидируются реаллокацией.
- Классы игровых объектов вызывают функции curses напрямую - логику нельзя
  протестировать без терминала.

---

## Вопросы для самопроверки

- Что сломается, если внутри `for (auto& obj : objects)` вызвать
  `objects.push_back(...)`? Покажите, где в своём коде вы этого избежали.
- Почему вы выбрали буфер или флаг+`erase_if`, а не другой вариант?
  Что изменится, если выбрать второй?
- Почему игровой цикл использует `steady_clock`, а не `system_clock`?
  Что случится, если перевести системные часы во время игры?
- Что будет, если объект в вашей игре сдвинется быстрее одной клетки
  за шаг обновления?
- Покажите класс игрового объекта - где гарантия, что он не вызывает
  функции curses напрямую?

---

## Практика на паре

1. Напишите цикл, который добавляет элементы в `vector` во время обхода
   range-based for по этому же вектору. Соберите под `cmake --preset asan`,
   запустите и поймайте отчёт про heap-use-after-free.
2. Почините получившийся код обоими способами: буфер для новых элементов
   и флаг с последующим `std::erase_if`.
3. Напишите игровой цикл с фиксированным шагом (`steady_clock` + аккумулятор)
   и объектом, который двигается с постоянной скоростью в единицах в секунду.
   Добавьте `sleep_for` внутрь отрисовки, чтобы искусственно замедлить кадр,
   и убедитесь, что за одинаковое реальное время объект проходит одинаковое
   расстояние независимо от этой задержки.
