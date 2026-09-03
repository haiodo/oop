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

# Аллокаторы в контейнерах STL C++

**Семинар 9 (факультатив)**
_Курс ООП, 2 курс_

---

## Статус материала

Материал факультативный: ни одна из пяти задач курса аллокаторы не
использует.

Зачем всё же читать:
- понять, откуда контейнер берёт память при `push_back`/`insert`;
- уметь разобраться в чужом коде, если там встретится нестандартный аллокатор;
- база для реальных задач - см. следующий слайд, когда это пригождается
  на практике.

---

## Что такое аллокатор?

**Аллокатор** - объект, который управляет выделением и освобождением памяти
для контейнера STL.

Минимальный аллокатор в C++20:

```cpp
#include <cstddef>

template <typename T>
struct MyAllocator {
  using value_type = T;

  T* allocate(std::size_t n);
  void deallocate(T* p, std::size_t n);

  bool operator==(const MyAllocator&) const = default;
};
```

Методы `construct`/`destroy` были частью интерфейса аллокатора до C++17,
в C++20 их убрали из `std::allocator` - за них отвечает `std::allocator_traits`.

---

## std::allocator_traits

Контейнеры не вызывают методы аллокатора напрямую - они идут через
`std::allocator_traits<Alloc>`. Traits дополняют минимальный аллокатор
значениями по умолчанию: если в `Alloc` нет `construct`, traits вызовут
placement `new` сами; не хватает `pointer`/`max_size` - подставят разумные
значения.

```cpp
#include <memory>

template <typename Alloc>
void demo(Alloc& alloc) {
  using Traits = std::allocator_traits<Alloc>;
  auto* p = Traits::allocate(alloc, 1);
  Traits::construct(alloc, p, 42);
  Traits::destroy(alloc, p);
  Traits::deallocate(alloc, p, 1);
}

int main() {
  std::allocator<int> alloc;
  demo(alloc);
}
```

---

## Зачем нужны аллокаторы?

- **Гибкость**: разные стратегии управления памятью
- **Производительность**: оптимизация под конкретную нагрузку
- **Контроль**: отслеживание использования памяти
- **Специализация**: работа с нестандартной памятью (shared memory, GPU)

---

## Когда аллокатор действительно нужен

- много мелких однотипных объектов с известным временем жизни (узлы дерева,
  кадры сетевого пакета);
- real-time код, которому нельзя ходить к системному аллокатору;
- встраиваемые системы без кучи или с жёстко ограниченной памятью;
- нужно точно посчитать, кто и сколько выделяет (отладка утечек, лимиты).

В подавляющем большинстве задач - никогда. `std::allocator` уже быстрый,
самописный почти всегда проигрывает, если не измерено обратное.

---

## Стандартный аллокатор

```cpp
#include <memory>
#include <vector>

// std::allocator used by default
std::vector<int> vec1;

// same, written explicitly
std::vector<int, std::allocator<int>> vec2;
```

---

## Полный набор членов аллокатора

```cpp
#include <cstddef>

template <typename T>
struct MyAllocator {
  using value_type = T;

  MyAllocator() = default;

  // Needed for rebind: containers build Alloc<U> from Alloc<T>.
  template <typename U>
  MyAllocator(const MyAllocator<U>&) {}

  T* allocate(std::size_t n);
  void deallocate(T* p, std::size_t n);

  bool operator==(const MyAllocator&) const = default;
};
```

- **Конструктор преобразования** - без него не соберутся узловые контейнеры
  (см. слайд про rebind дальше).
- **`operator==`**: контейнер сравнивает аллокаторы при перемещении и `swap`,
  чтобы решить - забрать чужую память целиком или копировать поэлементно.
  `operator!=` в C++20 компилятор генерирует из `==` сам.

---

## Пример собственного аллокатора

```cpp
#include <cstddef>
#include <cstdlib>
#include <iostream>
#include <new>

template <typename T>
class DebugAllocator {
public:
  using value_type = T;

  T* allocate(std::size_t n) {
    std::cout << "Allocating " << n << " objects\n";
    void* raw = std::malloc(n * sizeof(T));
    if (raw == nullptr) {
      throw std::bad_alloc();
    }
    return static_cast<T*>(raw);
  }
  void deallocate(T* p, std::size_t n) {
    std::cout << "Deallocating " << n << " objects\n";
    std::free(p);
  }
};
```

`std::malloc` не гарантирует выравнивание для типов с повышенными
требованиями (`alignas`) - для них нужен `::operator new(size, std::align_val_t{alignof(T)})`.

---

## Использование собственного аллокатора

```cpp
#include <iostream>
#include <vector>

int main() {
  std::vector<int, DebugAllocator<int>> debug_vec;

  debug_vec.push_back(1);
  debug_vec.push_back(2);
  debug_vec.push_back(3);
}
```

Порядок и числа в выводе Allocating/Deallocating зависят от реализации:
стандарт не фиксирует стратегию роста `std::vector`. У libstdc++ и libc++
капасити удваивается, у MSVC растёт в 1.5 раза.

---

## Статистический аллокатор

```cpp
#include <cstddef>
#include <cstdlib>
#include <iostream>
#include <new>

template <typename T>
class StatsAllocator {
public:
  using value_type = T;

  T* allocate(std::size_t n) {
    ++allocations_;
    void* raw = std::malloc(n * sizeof(T));
    if (raw == nullptr) { throw std::bad_alloc(); }
    return static_cast<T*>(raw);
  }
  void deallocate(T* p, std::size_t) {
    ++deallocations_;
    std::free(p);
  }
  static void print_stats() {
    std::cout << allocations_ << " / " << deallocations_ << '\n';
  }

private:
  inline static std::size_t allocations_ = 0;
  inline static std::size_t deallocations_ = 0;
};
```

`inline static` (C++17) задаёт значение прямо в объявлении класса и не
требует отдельного определения вне класса - без `inline` это ошибка
компоновки (linker error), а не компиляции.

---

## Аллокатор для пула памяти: ресурс

Главное правило: **аллокатор - это дешёвый копируемый дескриптор ресурса,
а не сам ресурс**. Если буфер лежит полем внутри аллокатора, то при
копировании аллокатора (а контейнеры копируют их часто) каждая копия
получает свой пул - указатели, выданные оригиналом, ссылаются в чужую
память. Поэтому буфер выносим в отдельный объект.

```cpp
#include <cstddef>

// Owns the actual memory, not copyable - exactly one owner.
struct MemoryPool {
  explicit MemoryPool(std::size_t bytes)
      : buffer(new std::byte[bytes]), size(bytes) {}
  ~MemoryPool() { delete[] buffer; }

  MemoryPool(const MemoryPool&) = delete;
  MemoryPool& operator=(const MemoryPool&) = delete;

  std::byte* buffer;
  std::size_t size;
  std::size_t offset = 0;
};
```

---

## Аллокатор для пула памяти: дескриптор

`PoolAllocator` хранит только указатель на `MemoryPool` - копия дешёвая,
все копии работают с одной и той же памятью. Инклюды - как на предыдущем
слайде, плюс `<new>` для `std::bad_alloc`. Выравнивание для простоты не
учитываем.

```cpp
template <typename T>
class PoolAllocator {
public:
  using value_type = T;
  explicit PoolAllocator(MemoryPool& pool) : pool_(&pool) {}
  template <typename U>
  PoolAllocator(const PoolAllocator<U>& other) : pool_(other.pool_) {}

  T* allocate(std::size_t n) {
    std::size_t bytes = n * sizeof(T);
    if (pool_->offset + bytes > pool_->size) {
      throw std::bad_alloc();
    }
    T* result = reinterpret_cast<T*>(pool_->buffer + pool_->offset);
    pool_->offset += bytes;
    return result;
  }
  // Monotonic: memory is freed at once with the pool, deallocate() is a no-op.
  void deallocate(T*, std::size_t) {}
  bool operator==(const PoolAllocator&) const = default;

private:
  template <typename U> friend class PoolAllocator;
  MemoryPool* pool_;
};
```

---

## Rebind: почему узловым контейнерам он нужен

`std::vector` хранит `T` впритык, а `std::list`/`std::map`/`std::set` хранят
служебные узлы, которые оборачивают `T` указателями на соседей - значит, им
нужен не `Allocator<T>`, а `Allocator<Node>`. Это преобразование называется
**rebind**. `std::allocator_traits` выводит его сам, но только для шаблонов
вида `Alloc<T, Args...>`, где все `Args` - типы:

```cpp
// ПЛОХО: nontype parameter breaks automatic rebind
template <typename T, std::size_t PoolSize>
class BrokenPoolAllocator {};

// OK: the only template parameter is a type - rebind is deduced
template <typename T>
class PoolAllocator {};
```

Поэтому размер пула у `PoolAllocator` (два слайда назад) - параметр
конструктора `MemoryPool`, а не параметр шаблона.

---

## Использование с разными контейнерами

```cpp
#include <functional>
#include <list>
#include <map>
#include <string>
#include <vector>

MemoryPool pool(4096);
PoolAllocator<int> alloc(pool);

std::vector<int, PoolAllocator<int>> vec(alloc);
std::list<std::string, PoolAllocator<std::string>> lst(alloc);
std::map<int, std::string, std::less<int>,
         PoolAllocator<std::pair<const int, std::string>>> mp(alloc);
```

`PoolAllocator<std::string>` и `PoolAllocator<std::pair<const int, std::string>>`
строятся из `alloc` через конструктор преобразования - rebind выводится
автоматически, потому что у `PoolAllocator` один параметр шаблона - тип.

---

## std::pmr: ресурс как объект, а не тип шаблона

`std::pmr::memory_resource` - абстрактный интерфейс "как выделять и
освобождать сырую память". Готовые реализации: `monotonic_buffer_resource`
(линейная выдача без поштучного `deallocate`, как наш `PoolAllocator`),
`unsynchronized_pool_resource` (пулы под разные размеры блоков),
`new_delete_resource` (обёртка над `operator new`/`operator delete`).

Главное отличие от `std::vector<T, MyAllocator<T>>`: тип контейнера не
меняется при смене стратегии памяти - ресурс передаётся в конструктор во
время выполнения, а не параметром шаблона.

```cpp
#include <array>
#include <cstddef>
#include <memory_resource>
#include <vector>

int main() {
  std::array<std::byte, 1024> buffer;
  std::pmr::monotonic_buffer_resource pool(buffer.data(), buffer.size());

  std::pmr::vector<int> vec(&pool);
  vec.push_back(1);
  vec.push_back(2);
}
```

---

## Практические советы

1. Для большинства задач хватает стандартного аллокатора.
2. Собственный аллокатор пишите только когда измерили, что он нужен.
3. Проверяйте выигрыш бенчмарком, а не "кажется, будет быстрее".
4. В многопоточном коде учитывайте thread-safety счётчиков и пулов.
5. Документируйте, какой ресурс использует аллокатор и кто им владеет.

---

## Типичные ошибки

- Аллокатор хранит ресурс по значению (буфер как поле класса) - копия
  аллокатора получает свой буфер, указатели старой копии виснут.
- Нетиповой параметр шаблона или забытый конструктор преобразования
  `template<typename U> Alloc(const Alloc<U>&)` - ломает rebind, узловые
  контейнеры (`list`, `map`, `set`) не соберутся.
- Нет `operator==` - контейнер не может решить, можно ли перенести память
  при перемещении/`swap`, вместо этого копирует поэлементно.
- Реализация `construct`/`destroy` вручную - в C++20 не часть требований
  аллокатора, за это отвечает `std::allocator_traits`.
- `static`-член без `inline` и без определения вне класса - ошибка
  компоновки, а не компиляции, искать её дольше.
- `allocate` не проверяет результат `malloc` на `nullptr` и не бросает
  `std::bad_alloc`.

---

## Ссылки

- cppreference: [Allocator requirements](https://en.cppreference.com/w/cpp/named_req/Allocator)
- cppreference: [std::allocator_traits](https://en.cppreference.com/w/cpp/memory/allocator_traits)
- cppreference: [std::pmr::memory_resource](https://en.cppreference.com/w/cpp/memory/memory_resource)
- Habr: [Полиморфные аллокаторы в C++17](https://habr.com/ru/companies/otus/articles/520502/)
