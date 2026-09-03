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

# Исключения и обработка ошибок в C++

**Семинар 4**
Курс: Объектно-ориентированное программирование
2 курс

---

## Что такое исключения?

**Исключение** - механизм обработки ошибок, который позволяет:

- Отделить логику обработки ошибок от основного кода
- Передавать информацию об ошибке по стеку вызовов
- Обеспечить безопасное завершение программы

```cpp
// Without exceptions: an error code is easy to miss or misread.
int divide_return_code(int a, int b) {
  if (b == 0) {
    return -1;  // Is -1 an error, or a valid result?
  }
  return a / b;
}

// With exceptions: the error cannot be silently ignored.
int divide_throw(int a, int b) {
  if (b == 0) {
    throw std::invalid_argument("division by zero");
  }
  return a / b;
}
```

---

## Синтаксис try-catch-throw

```cpp
try {
  int result = divide_throw(10, 0);
  std::cout << "Result: " << result << std::endl;
}
catch (const std::invalid_argument& e) {
  // Handle a specific exception type.
  std::cout << "Error: " << e.what() << std::endl;
}
catch (...) {
  // Handle any other exception.
  std::cout << "Unknown error" << std::endl;
}
```

- `try` - блок, где может быть брошено исключение.
- `catch (const Type& e)` - обработчик конкретного типа.
- `catch (...)` - обработчик любого исключения; ставится последним.

---

## Генерация исключений (throw)

```cpp
void check_age(int age) {
  if (age < 0) {
    throw std::invalid_argument("age cannot be negative");
  }
  if (age > 150) {
    throw std::out_of_range("age is too large");
  }
}

void process_user() {
  try {
    check_age(-5);
  }
  catch (const std::invalid_argument& e) {
    std::cout << "Invalid argument: " << e.what() << std::endl;
  }
  catch (const std::out_of_range& e) {
    std::cout << "Out of range: " << e.what() << std::endl;
  }
}
```

---

## Стандартные исключения

Заголовок `<stdexcept>`. Базовый класс всех стандартных исключений - `std::exception`
(объявлен в `<exception>`).

```
std::exception
├── std::logic_error
│   ├── std::invalid_argument
│   ├── std::domain_error
│   ├── std::length_error
│   └── std::out_of_range
├── std::runtime_error
│   ├── std::range_error
│   ├── std::overflow_error
│   ├── std::underflow_error
│   └── std::system_error
├── std::bad_alloc
├── std::bad_cast
└── std::bad_typeid
```

- `logic_error` - ошибку можно было предотвратить проверкой аргументов до вызова.
- `runtime_error` - ошибку нельзя было предсказать заранее (ввод-вывод, вычисления).
- `bad_alloc`, `bad_cast`, `bad_typeid` бросает сам язык (`new`, `dynamic_cast<T&>`,
  `typeid` от null-указателя), а не прикладной код.

---

## Примеры стандартных исключений

```cpp
#include <vector>
#include <stdexcept>

void show_out_of_range() {
  std::vector<int> data = {1, 2, 3};
  try {
    int value = data.at(10);  // at() checks bounds, operator[] does not
    std::cout << value << std::endl;
  }
  catch (const std::out_of_range& e) {
    std::cout << "Index out of range: " << e.what() << std::endl;
  }
}

void show_invalid_argument() {
  try {
    int num = std::stoi("not_a_number");
    std::cout << num << std::endl;
  }
  catch (const std::invalid_argument& e) {
    std::cout << "Bad argument: " << e.what() << std::endl;
  }
}
```

`std::bad_alloc` бросает `new`, когда не может выделить память. Пример
`new int[10000000000000]` нечестный: результат не используется, а на 32-битной
платформе такой запрос заведомо больше адресного пространства - будет
`std::bad_array_new_length`, а не `bad_alloc`. Надёжно вызвать `bad_alloc` на слайде
нельзя - зависит от платформы и оверкоммита памяти.

---

## Что выбирать из стандартных исключений

- `std::invalid_argument` - аргумент синтаксически неверен (несовпадение размеров,
  отрицательный сдвиг).
- `std::out_of_range` - индекс или значение вне допустимого диапазона.
- `std::runtime_error` - ошибка, которую нельзя было предвидеть статически (сбой
  ввода-вывода, ошибка формата данных).
- Не изобретайте свои типы под эти три случая: стандартные исключения уже
  документируют себя именем и ловятся тестами (`EXPECT_THROW(..., std::out_of_range)`).

В Задаче 2 (`cpp-tasks/Task 2 - BitArray.md`) выбор прописан жёстко:

- неверный индекс в `operator[]`, `set`, `reset` - `std::out_of_range`;
- отрицательный сдвиг, несовпадение размеров, `num_bits < 0` - `std::invalid_argument`.

---

## Множественный catch

```cpp
void process_data(const std::string& data) {
  try {
    if (data.empty()) {
      throw std::invalid_argument("empty data");
    }
    if (data.size() > 1000) {
      throw std::length_error("data too long");
    }
    int value = std::stoi(data);
    if (value < 0) {
      throw std::domain_error("negative value");
    }
  }
  catch (const std::invalid_argument& e) {
    std::cout << "Bad input: " << e.what() << std::endl;
  }
  catch (const std::length_error& e) {
    std::cout << "Size problem: " << e.what() << std::endl;
  }
  catch (const std::logic_error& e) {  // catches domain_error too
    std::cout << "Logic error: " << e.what() << std::endl;
  }
}
```

---

## Передача исключений по стеку

```cpp
void level3() {
  throw std::runtime_error("error at level 3");
}

void level2() {
  level3();  // exception passes through, no try/catch here
}

void level1() {
  try {
    level2();
  }
  catch (const std::runtime_error& e) {
    std::cout << "Caught at level 1: " << e.what() << std::endl;
    throw;  // rethrow the same exception
  }
}
```

Исключение само разматывает стек до ближайшего подходящего `catch` - `level2`
ничего не знает про обработку ошибок.

---

## Ловля по ссылке: срезание объекта

Ловля по значению копирует объект-исключение в переменную типа `catch`. Если тип
брошен производный, а перехватывается базовый - копия становится базовым
подобъектом, производная часть теряется (object slicing).

```cpp
class AppError : public std::runtime_error {
public:
  using std::runtime_error::runtime_error;
  virtual int code() const { return 1; }
};

class WidgetError : public AppError {
public:
  WidgetError() : AppError("widget error") {}
  int code() const override { return 4; }
};

void by_value() {
  try {
    throw WidgetError();
  }
  catch (AppError e) {  // BAD: object sliced to AppError, WidgetError part is gone
    std::cout << e.code() << std::endl;  // prints 1, not 4: dispatch lost
  }
}
```

Правило: всегда `catch (const Type& e)`, никогда `catch (Type e)`. Порядок веток -
от производных к базовым, иначе базовый перехватит всё; компилятор об этом
предупреждает.

---

## `throw;` против `throw e;`

- `throw;` без операнда внутри `catch` перебрасывает исходное исключение целиком,
  сохраняя динамический тип.
- `throw e;` создаёт новое исключение статическим типом переменной `e`. Если `e`
  объявлена как `const AppError&`, а поймано `WidgetError` - объект срезается
  до `AppError`, и дальше по стеку производный тип потерян.

```cpp
void rethrow_full() {
  try {
    throw WidgetError();
  }
  catch (const AppError& e) {
    std::cout << "seen code " << e.code() << std::endl;
    throw;  // GOOD: rethrows WidgetError, dynamic type preserved
  }
}

void rethrow_sliced() {
  try {
    throw WidgetError();
  }
  catch (const AppError& e) {
    throw e;  // BAD: throws a new AppError, WidgetError is gone
  }
}
```

Дальше по стеку `catch (const WidgetError&)` поймает исключение из `rethrow_full`,
но не поймает исключение из `rethrow_sliced` - там уже другой, базовый тип.

---

## Собственные исключения: базовый пример

```cpp
class MathError : public std::runtime_error {
public:
  explicit MathError(const std::string& message)
    : std::runtime_error("math error: " + message) {}
};

class DivisionByZeroError : public MathError {
public:
  DivisionByZeroError() : MathError("division by zero attempted") {}
};

double safe_divide(double a, double b) {
  if (b == 0.0) {
    throw DivisionByZeroError();
  }
  return a / b;
}
```

`b == 0.0` здесь сравнивает с точным нулём, полученным явно - это осмысленно.
В общем случае `double` на равенство не сравнивают: накопленная погрешность
вычислений делает такое сравнение ненадёжным.

---

## Иерархия собственных исключений

```cpp
class FileError : public std::runtime_error {
public:
  FileError(const std::string& message, const std::string& file)
    : std::runtime_error(message), filename_(file) {}

  const std::string& filename() const { return filename_; }

private:
  std::string filename_;
};

class FileNotFoundError : public FileError {
public:
  explicit FileNotFoundError(const std::string& file)
    : FileError("file not found", file) {}
};

class FilePermissionError : public FileError {
public:
  explicit FilePermissionError(const std::string& file)
    : FileError("permission denied", file) {}
};
```

---

## Проектирование иерархии под задачу

Задача 4 (`cpp-tasks/Task 4 - Sound Processor.md`): базовый класс ошибки приложения
и наследник на каждый вид ошибки.

```cpp
class SoundProcessorError : public std::runtime_error {
public:
  using std::runtime_error::runtime_error;
};

class CmdlineError : public SoundProcessorError {
public:
  using SoundProcessorError::SoundProcessorError;
};

class IoError : public SoundProcessorError {
public:
  using SoundProcessorError::SoundProcessorError;
};

class UnsupportedFormatError : public SoundProcessorError {
public:
  using SoundProcessorError::SoundProcessorError;
};

class ConfigError : public SoundProcessorError {
public:
  using SoundProcessorError::SoundProcessorError;
};
```

Наследоваться от `std::runtime_error` (или `std::logic_error`), а не от `std::exception`
напрямую: `what()` и хранение сообщения уже реализованы, писать их самому не нужно.

---

## Проектирование иерархии под задачу: main()

Один `catch` на каждый вид ошибки - свой код возврата.

```cpp
void run(int argc, char** argv);  // rest of the program

int main(int argc, char** argv) {
  try {
    run(argc, argv);
  }
  catch (const CmdlineError& e) {
    std::cerr << e.what() << std::endl;
    return 1;
  }
  catch (const IoError& e) {
    std::cerr << e.what() << std::endl;
    return 2;
  }
  catch (const UnsupportedFormatError& e) {
    std::cerr << e.what() << std::endl;
    return 3;
  }
  catch (const ConfigError& e) {
    std::cerr << e.what() << std::endl;
    return 4;
  }
}
```

Последним - `catch (const SoundProcessorError& e)`, код 5: ловит `ProcessingError`
и всё, что не подошло под более узкие ветки выше. Порядок обязателен: от
производных к базовым.

---

## Совместимость иерархий - часть контракта

Задача 5 (`cpp-tasks/Task 5 - CSV Parser.md`): Subtask #3 добавляет координаты ошибки
(`line`, `column`) поверх парсера из Subtask #2.

```cpp
class CSVParseError : public std::runtime_error {
public:
  CSVParseError(std::size_t line, std::size_t column, const std::string& message)
    : std::runtime_error(format_message(line, column, message)),
      line_(line), column_(column) {}

  std::size_t line() const { return line_; }
  std::size_t column() const { return column_; }

private:
  static std::string format_message(std::size_t line, std::size_t column,
                                     const std::string& message);

  std::size_t line_;
  std::size_t column_;
};
```

- `CSVParseError` наследуется от `std::runtime_error` не случайно: тесты Subtask #2,
  написанные до появления `CSVParseError`, ловят `std::runtime_error` и обязаны
  продолжать работать без единой правки.
- Выбор базового класса исключения - часть публичного контракта наравне
  с сигнатурами методов.

---

## RAII и исключения

**RAII** (Resource Acquisition Is Initialization) - идиома программирования:

- Получение ресурса есть инициализация
- Ресурс получается в конструкторе объекта
- Ресурс освобождается в деструкторе объекта
- Гарантирует освобождение ресурсов даже при исключениях

### Принципы RAII

1. Каждый ресурс должен принадлежать объекту
2. Время жизни ресурса = время жизни объекта
3. Автоматическая очистка при выходе из области видимости

---

## RAII: пример с файлами

```cpp
class FileHandler {
public:
  explicit FileHandler(const std::string& filename) {
    file_ = std::fopen(filename.c_str(), "r");
    if (!file_) {
      throw FileNotFoundError(filename);
    }
  }

  ~FileHandler() {
    if (file_) {
      std::fclose(file_);  // Runs even if an exception unwinds the stack.
    }
  }

  // Rule of three (Seminar 3): owns a raw resource, so copying must be
  // forbidden - two objects would otherwise fclose() the same file.
  FileHandler(const FileHandler&) = delete;
  FileHandler& operator=(const FileHandler&) = delete;

private:
  FILE* file_;
};
```

В реальном коде свою обёртку над `FILE*` не пишут: берут `std::ifstream` (тот же
RAII, копирование уже запрещено) или `std::unique_ptr` с кастомным `deleter` для
`FILE*` - одной строкой, подробно приём разбирается в семинаре 5.

---

## Гарантии безопасности исключений

Что происходит с объектом, если операция бросила исключение посередине.

- **nothrow (`noexcept`)** - операция гарантированно не бросает исключений.
  Обязательна для деструктора и `swap`.
- **strong** - если бросило исключение, объект остаётся ровно в том состоянии,
  в котором был до вызова: операция либо выполнилась целиком, либо не произошла.
- **basic** - если бросило исключение, инварианты класса не нарушены и утечек нет,
  но содержимое объекта могло измениться.

```cpp
void swap(BitArray& other) noexcept {  // nothrow: never allocates, never throws
  data_.swap(other.data_);
  std::swap(size_, other.size_);
}
```

Задача 2 (`cpp-tasks/Task 2 - BitArray.md`, автомат): `swap` обязан быть nothrow,
все модифицирующие операции обязаны давать strong.

---

## Как получить сильную гарантию: copy-and-swap

Без осторожности присваивание может нарушить strong: если выделение памяти для
новых данных бросит исключение посередине, `*this` уже испорчен.

```cpp
BitArray& BitArray::operator=(const BitArray& other) {
  BitArray tmp(other);  // May throw: *this is untouched so far.
  swap(tmp);             // noexcept: never fails.
  return *this;
}  // tmp is destroyed here, holding the old contents of *this.
```

Если конструктор копии `tmp` бросит - `*this` не тронут, strong-гарантия готова.
Приём подробно разбирается в семинаре 5 (move-семантика); здесь важно, зачем он
нужен: без него strong для каждой модифицирующей операции пришлось бы доказывать
вручную.

---

## Исключения и деструкторы

- Деструкторы по умолчанию `noexcept`. Исключение, покинувшее деструктор,
  вызывает `std::terminate` немедленно.
- Причина: если деструктор вызван во время раскрутки стека из-за уже летящего
  исключения, а сам бросает второе - обработать оба одновременно нельзя,
  и стандарт требует `std::terminate`.

```cpp
class Bad {
public:
  ~Bad() {
    // If this runs while another exception is unwinding the stack,
    // throwing here calls std::terminate: the program aborts.
    throw std::runtime_error("thrown from destructor");
  }
};
```

---

## Исключения и деструкторы: что делать вместо

- Операцию, способную упасть (закрытие файла, сброс буфера, коммит), вынести
  в отдельный метод, вызываемый явно, - и дать пользователю обработать исключение.
- В деструкторе - только освобождение, которое не может упасть, либо поймать
  исключение и залогировать, не пробрасывая дальше.

```cpp
class TransactionalFile {
public:
  // May throw: call explicitly, handle the error at the call site.
  void close() {
    if (std::fclose(file_) != 0) {
      throw std::runtime_error("failed to flush file");
    }
    file_ = nullptr;
  }

  ~TransactionalFile() {
    if (file_) {
      std::fclose(file_);  // Best effort: no exception can leave here.
    }
  }

private:
  FILE* file_ = nullptr;
};
```

---

## noexcept спецификатор

```cpp
// Function guarantees it will not throw.
int multiply(int a, int b) noexcept {
  return a * b;
}

// Conditional noexcept: true only if T's move constructor is noexcept.
template <typename T>
void my_swap(T& a, T& b) noexcept(std::is_nothrow_move_constructible_v<T>) {
  T temp = std::move(a);
  a = std::move(b);
  b = std::move(temp);
}
```

`noexcept` - не пожелание, а обещание компилятору. Если функция всё же бросит -
вызывается `std::terminate`; поймать это исключение нельзя, никакой `try/catch`
вокруг вызова не поможет.

---

## Зачем компилятору важен noexcept

При реаллокации `std::vector` переносит существующие элементы в новый буфер. Если
перемещение элемента бросит на середине - часть элементов уже в новом буфере,
часть в старом, восстановить прежнее состояние невозможно.

- Поэтому `std::vector` переносит элементы через move, только если move-конструктор
  типа помечен `noexcept`.
- Если move-конструктор может бросить - `std::vector` копирует элементы вместо
  перемещения: медленнее, но безопасно.

```cpp
class Slow {
public:
  Slow(Slow&&) { /* not noexcept: vector will copy, not move, on growth */ }
};

class Fast {
public:
  Fast(Fast&&) noexcept { /* vector will move on growth */ }
};
```

Подробно перемещающие операции и почему они обязаны быть `noexcept` - в семинаре 5.

---

## Тестирование исключений в GoogleTest

```cpp
#include <gtest/gtest.h>

TEST(BitArrayTest, OperatorBracketThrowsOnBadIndex) {
  BitArray bits(8);
  EXPECT_THROW(bits[8], std::out_of_range);
  EXPECT_THROW(bits[-1], std::out_of_range);
}

TEST(BitArrayTest, ShiftByZeroDoesNotThrow) {
  BitArray bits(8);
  EXPECT_NO_THROW(bits <<= 0);
}
```

- `EXPECT_THROW(statement, ExceptionType)` - проверяет и тип исключения, и то, что
  оно вообще брошено. Тест падает, если брошено не то исключение или не брошено
  никакое.
- `EXPECT_NO_THROW(statement)` - проверяет, что исключение не брошено.
- Задачи 2, 4, 5 требуют покрыть тестами каждую ветку, бросающую исключение,
  а не только успешный путь.

---

## Когда исключения не нужны

- Исключения - для **исключительных** ситуаций, которые нарушают предусловие
  операции. Ожидаемый результат поиска, ввода пользователя, разбора данных -
  не исключение, даже если это "ошибка".
- Пример: файл не найден при поиске по маске - штатный случай, а не авария.
  Пользователь ввёл не то в интерактивном режиме - тоже штатный случай.
- Альтернативы: `std::optional<T>` - "значение или его отсутствие" без причины;
  `std::expected<T, E>` (C++23) - "значение или причина ошибки" без исключения.
- Критерий: если вызывающий код в норме проверяет результат каждый раз - это не
  исключительная ситуация, используйте возвращаемое значение.

---

## Лучшие практики

Хорошо:

- Использовать RAII для управления ресурсами.
- Ловить исключения по `const` ссылке.
- Строить иерархию исключений под виды ошибок.
- Использовать стандартные исключения, когда они подходят по смыслу.

Плохо:

- Использовать исключения для управления обычным потоком выполнения.
- Бросать исключения из деструктора.
- Ловить исключения по значению.
- Молча игнорировать пойманное исключение (`catch (...) {}`).

---

## Пример: Безопасный калькулятор

```cpp
class Calculator {
public:
  double divide(double a, double b) {
    if (b == 0.0) {  // exact zero check, obtained explicitly - fine here
      throw std::invalid_argument("division by zero");
    }
    return a / b;
  }

  double sqrt(double x) {
    if (x < 0) {
      throw std::domain_error("square root of negative number");
    }
    return std::sqrt(x);
  }
};
```

`b == 0.0` сравнивает с точным нулём. В общем случае `double` на равенство не
сравнивают - это отдельная тема.

---

## Безопасный калькулятор: main()

```cpp
int main() {
  Calculator calc;
  try {
    double result1 = calc.divide(10, 0);
    std::cout << "10 / 0 = " << result1 << std::endl;
    double result2 = calc.sqrt(-4);
    std::cout << "sqrt(-4) = " << result2 << std::endl;
  }
  catch (const std::exception& e) {
    std::cout << "Error: " << e.what() << std::endl;
  }
  return 0;
}
```

---

## Типичные ошибки

- `throw e;` вместо `throw;` в блоке `catch` при перебросе - срезает объект до
  типа переменной `e`, производный тип теряется дальше по стеку.
- Ловля исключения по значению (`catch (std::exception e)`) - лишняя копия и та же
  потеря производного типа.
- `swap` не помечен `noexcept` или падает при аллокации - ломает и nothrow-гарантию,
  и copy-and-swap, который на неё опирается.
- Наследование пользовательского исключения от `std::exception` напрямую вместо
  `std::runtime_error`/`std::logic_error` - `what()` и хранение сообщения приходится
  писать самому, обычно с багом.
- Исключение, брошенное из деструктора при уже летящем исключении - `std::terminate`
  без диагностики, где именно это случилось.
- `operator[]` бросает не тот тип (`std::runtime_error` вместо `std::out_of_range`) -
  тесты, написанные по заданию, не проходят.

---

## Вопросы для самопроверки

- Что сломается, если убрать `noexcept` у вашего `swap`?
- Покажите на своём коде: где вы даёте strong-гарантию, а где только basic,
  и почему именно так.
- Что произойдёт, если в вашем `catch` написать `throw e;` вместо `throw;`?
- Почему ваше исключение наследуется от `std::runtime_error`, а не от `std::exception`
  напрямую?
- Почему тесты Subtask #2 в Задаче 5 обязаны проходить без правок после Subtask #3?

---

## Практика на паре

1. Написать класс с операцией, которая может бросить исключение посередине
   изменения состояния (например, `resize`, выделяющий память). Тестом показать
   нарушение сильной гарантии: после неудачного вызова объект испорчен.
2. Починить операцию через copy-and-swap, тем же тестом показать, что гарантия
   стала сильной.
3. Написать иерархию из трёх исключений (одна база, два наследника) и функцию
   `main`, где каждый вид ошибки даёт свой код возврата через отдельный `catch`.
4. Добавить тесты `EXPECT_THROW`/`EXPECT_NO_THROW` на все ветки из пункта 3.
