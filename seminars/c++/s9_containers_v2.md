---
marp: true
theme: default
paginate: true
---

# Аллокаторы в контейнерах STL C++

**Семинар 9**
_Курс ООП, 2 курс_

---

## Что такое аллокатор?

**Аллокатор** - это объект, который управляет выделением и освобождением памяти для контейнеров STL.

```cpp
template<class T>
class allocator {
public:
  T* allocate(size_t n);           // выделить память
  void deallocate(T* p, size_t n); // освободить память
  void construct(T* p, const T& val); // конструировать объект
  void destroy(T* p);              // уничтожить объект
};
```

---

## Зачем нужны аллокаторы?

- **Гибкость**: различные стратегии управления памятью
- **Производительность**: оптимизация для конкретных задач
- **Контроль**: отслеживание использования памяти
- **Специализация**: работа с особыми типами памяти

---

## Стандартный аллокатор

```cpp
#include <vector>
#include <memory>

// По умолчанию используется std::allocator
std::vector<int> vec1;

// Явное указание аллокатора
std::vector<int, std::allocator<int>> vec2;

// Они эквивалентны
```

---

## Пример собственного аллокатора

```cpp
template<typename T>
class DebugAllocator {
public:
  using value_type = T;

  T* allocate(size_t n) {
    std::cout << "Allocating " << n << " objects\n";
    return static_cast<T*>(std::malloc(n * sizeof(T)));
  }

  void deallocate(T* p, size_t n) {
    std::cout << "Deallocating " << n << " objects\n";
    std::free(p);
  }
};
```

---

## Использование собственного аллокатора

```cpp
#include <vector>

int main() {
  std::vector<int, DebugAllocator<int>> debug_vec;

  debug_vec.push_back(1); // Allocating 1 objects
  debug_vec.push_back(2); // Allocating 2 objects, Deallocating 1 objects
  std::cout << "Push 3" << std::endl;
  debug_vec.push_back(3); // Allocating 4 objects, Deallocating 2 objects

  return 0; // Deallocating 4 objects
}
```

---

## Требования к аллокатору (C++17)

```cpp
template<typename T>
struct MyAllocator {
  using value_type = T;

  // Обязательные методы
  T* allocate(size_t n);
  void deallocate(T* p, size_t n);

  // Операторы сравнения
  bool operator==(const MyAllocator& other) const;
  bool operator!=(const MyAllocator& other) const;
};
```

---

## Аллокатор для пула памяти

```cpp
template<typename T, size_t PoolSize = 1024>
class PoolAllocator {
  char pool[PoolSize];
  size_t offset = 0;

public:
  using value_type = T;

  T* allocate(size_t n) {
    size_t size = n * sizeof(T);
    if (offset + size > PoolSize)
      throw std::bad_alloc();

    T* result = reinterpret_cast<T*>(pool + offset);
    offset += size;
    return result;
  }

  void deallocate(T* p, size_t n) {
    // Простая реализация - не освобождаем
  }
};
```

---

## Статистический аллокатор

```cpp
template<typename T>
class StatsAllocator {
  static size_t allocations;
  static size_t deallocations;

public:
  using value_type = T;

  T* allocate(size_t n) {
    ++allocations;
    return static_cast<T*>(std::malloc(n * sizeof(T)));
  }

  void deallocate(T* p, size_t n) {
    ++deallocations;
    std::free(p);
  }

  static void print_stats() {
    std::cout << "Allocs: " << allocations
          << ", Deallocs: " << deallocations << std::endl;
  }
};
```

---

## Использование с разными контейнерами

```cpp
// Вектор с debug аллокатором
std::vector<int, DebugAllocator<int>> vec;

// Список с pool аллокатором
std::list<std::string, PoolAllocator<std::string>> lst;

// Карта с статистическим аллокатором
std::map<int, std::string,
     std::less<int>,
     StatsAllocator<std::pair<const int, std::string>>> mp;
```

---

## Полиморфные аллокаторы (C++17)

https://habr.com/ru/companies/otus/articles/520502/

```cpp
#include <memory_resource>

// Используем полиморфные аллокаторы
std::pmr::vector<int> vec1;
std::pmr::string str1;

// Можно использовать различные ресурсы памяти
std::pmr::monotonic_buffer_resource mbr(1024);
std::pmr::vector<int> vec2(&mbr);
```

---

## Практические советы

1. **Используйте стандартный аллокатор** для большинства задач
2. **Создавайте собственные** только при необходимости
3. **Тестируйте производительность** перед внедрением
4. **Учитывайте thread-safety** в многопоточных приложениях
5. **Документируйте** особенности своих аллокаторов

---

## Вопросы?

**Спасибо за внимание!**
