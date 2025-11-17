---
marp: true
theme: default
paginate: true
---

# Исключения и обработка ошибок в C++

**Семинар 5**
Курс: Объектно-ориентированное программирование
2 курс

---

## Содержание

1. Введение в исключения
2. Синтаксис try-catch-throw
3. Стандартные исключения
4. Создание собственных исключений
5. Спецификации исключений
6. RAII и исключения
7. Лучшие практики

---

## Что такое исключения?

**Исключение** — механизм обработки ошибок, который позволяет:

- Отделить логику обработки ошибок от основного кода
- Передавать информацию об ошибке по стеку вызовов
- Обеспечить безопасное завершение программы

```cpp
// Без исключений
int divide(int a, int b) {
  if (b == 0) return -1; // Код ошибки?
  return a / b;
}

// С исключениями
int divide(int a, int b) {
  if (b == 0) throw std::invalid_argument("Division by zero");
  return a / b;
}
```

---

## Синтаксис try-catch-throw

### Базовая структура

```cpp
try {
  // Код, который может вызвать исключение
  int result = divide(10, 0);
}
catch (const std::invalid_argument& e) {
  // Обработка конкретного типа исключения
  std::cout << "Ошибка: " << e.what() << std::endl;
}
catch (...) {
  // Обработка любых других исключений
  std::cout << "Неизвестная ошибка" << std::endl;
}
```

---

## Генерация исключений (throw)

```cpp
void checkAge(int age) {
  if (age < 0) {
    throw std::invalid_argument("Возраст не может быть отрицательным");
  }
  if (age > 150) {
    throw std::out_of_range("Возраст слишком большой");
  }
}

void processUser() {
  try {
    checkAge(-5);
  }
  catch (const std::invalid_argument& e) {
    std::cout << "Неверный аргумент: " << e.what() << std::endl;
  }
  catch (const std::out_of_range& e) {
    std::cout << "Выход за границы: " << e.what() << std::endl;
  }
}
```

---

## Стандартные исключения

```cpp
#include <stdexcept>

// Базовый класс для всех стандартных исключений
std::exception
├── std::logic_error
│   ├── std::invalid_argument
│   ├── std::domain_error
│   ├── std::length_error
│   └── std::out_of_range
└── std::runtime_error
  ├── std::range_error
  ├── std::overflow_error
  └── std::underflow_error
```

---

## Примеры стандартных исключений

```cpp
#include <vector>
#include <stdexcept>

void examples() {
  try {
    // std::out_of_range
    std::vector<int> vec = {1, 2, 3};
    int value = vec.at(10); // Выход за границы

    // std::invalid_argument
    int num = std::stoi("not_a_number");

    // std::bad_alloc
    int* huge_array = new int[10000000000000];
  }
  catch (const std::out_of_range& e) {
    std::cout << "Индекс вне диапазона: " << e.what() << std::endl;
  }
  catch (const std::invalid_argument& e) {
    std::cout << "Неверный аргумент: " << e.what() << std::endl;
  }
  catch (const std::bad_alloc& e) {
    std::cout << "Ошибка выделения памяти: " << e.what() << std::endl;
  }
}
```

---

## Создание собственных исключений

```cpp
// Наследование от стандартного исключения
class MathError : public std::runtime_error {
public:
  explicit MathError(const std::string& message)
    : std::runtime_error("Math Error: " + message) {}
};

class DivisionByZeroError : public MathError {
public:
  DivisionByZeroError()
    : MathError("Division by zero attempted") {}
};

// Использование
double safeDivide(double a, double b) {
  if (b == 0.0) {
    throw DivisionByZeroError();
  }
  return a / b;
}
```

---

## Иерархия собственных исключений

```cpp
class FileError : public std::runtime_error {
protected:
  std::string filename;
public:
  FileError(const std::string& msg, const std::string& file)
    : std::runtime_error(msg), filename(file) {}

  const std::string& getFilename() const { return filename; }
};

class FileNotFoundError : public FileError {
public:
  FileNotFoundError(const std::string& file)
    : FileError("File not found", file) {}
};

class FilePermissionError : public FileError {
public:
  FilePermissionError(const std::string& file)
    : FileError("Permission denied", file) {}
};
```

---

## Передача исключений по стеку

```cpp
void level3() {
  throw std::runtime_error("Ошибка на уровне 3");
}

void level2() {
  level3(); // Исключение проходит через эту функцию
}

void level1() {
  try {
    level2();
  }
  catch (const std::runtime_error& e) {
    std::cout << "Поймано на уровне 1: " << e.what() << std::endl;
    // Можно обработать или перебросить
    throw; // Перебрасывание того же исключения
  }
}
```

---

## Множественный catch

```cpp
void processData(const std::string& data) {
  try {
    // Обработка данных
    if (data.empty())
      throw std::invalid_argument("Пустые данные");
    if (data.size() > 1000)
      throw std::length_error("Данные слишком длинные");

    int value = std::stoi(data);
    if (value < 0)
      throw std::domain_error("Отрицательное значение");
  }
  catch (const std::invalid_argument& e) {
    std::cout << "Неверный ввод: " << e.what() << std::endl;
  }
  catch (const std::length_error& e) {
    std::cout << "Проблема с размером: " << e.what() << std::endl;
  }
  catch (const std::logic_error& e) { // Базовый класс
    std::cout << "Логическая ошибка: " << e.what() << std::endl;
  }
  catch (...) {
    std::cout << "Неизвестная ошибка" << std::endl;
  }
}
```

---

## RAII и исключения

**RAII** (Resource Acquisition Is Initialization) — идиома программирования:

- **Resource Acquisition Is Initialization** — Получение Ресурса Есть Инициализация
- Ресурс получается в конструкторе объекта
- Ресурс освобождается в деструкторе объекта
- Гарантирует освобождение ресурсов даже при исключениях

### Принципы RAII

1. Каждый ресурс должен принадлежать объекту
2. Время жизни ресурса = время жизни объекта
3. Автоматическая очистка при выходе из области видимости

---

## RAII: Пример с файлами

```cpp
class FileHandler {
private:
  FILE* file;
public:
  FileHandler(const std::string& filename) {
    file = fopen(filename.c_str(), "r");
    if (!file) {
      throw FileNotFoundError(filename);
    }
  }

  ~FileHandler() {
    if (file) {
      fclose(file); // Автоматическое освобождение при исключении
    }
  }

  void readData() {
    if (!file) throw std::runtime_error("File not open");
    // Чтение данных...
    // Если здесь возникнет исключение,
    // деструктор все равно закроет файл
  }
};
```

---

## noexcept спецификатор

```cpp
// Функция не должна генерировать исключения
int multiply(int a, int b) noexcept {
  return a * b;
}

// Условное noexcept
template<typename T>
void swap(T& a, T& b) noexcept(std::is_nothrow_move_constructible_v<T>) {
  T temp = std::move(a);
  a = std::move(b);
  b = std::move(temp);
}

// В деструкторах (по умолчанию noexcept)
class MyClass {
public:
  ~MyClass() noexcept {
    // Деструктор не должен генерировать исключения
  }
};
```

---

## Лучшие практики

### ✅ Хорошо:

- Используйте RAII для управления ресурсами
- Ловите исключения по const ссылке
- Создавайте иерархию исключений
- Используйте стандартные исключения когда возможно

### ❌ Плохо:

- Не используйте исключения для управления потоком
- Не генерируйте исключения в деструкторах
- Не ловите исключения по значению
- Не игнорируйте исключения

---

## Пример: Безопасный калькулятор

```cpp
class Calculator {
public:
  double divide(double a, double b) {
    if (b == 0.0) {
      throw std::invalid_argument("Division by zero");
    }
    return a / b;
  }

  double sqrt(double x) {
    if (x < 0) {
      throw std::domain_error("Square root of negative number");
    }
    return std::sqrt(x);
  }
};

int main() {
  Calculator calc;
  try {
    double result1 = calc.divide(10, 0);
    double result2 = calc.sqrt(-4);
  }
  catch (const std::exception& e) {
    std::cout << "Ошибка: " << e.what() << std::endl;
  }
  return 0;
}
```

---

## Задания для практики

1. Создайте класс `BankAccount` с методами депозита и снятия, которые генерируют исключения при некорректных операциях

2. Реализуйте функцию чтения файла с обработкой всех возможных ошибок

3. Создайте иерархию исключений для игры (InvalidMoveError, GameOverError и т.д.)

4. Напишите безопасную версию функции парсинга JSON с обработкой ошибок

---

## Вопросы?

**Следующий семинар:** Ввод вывод
