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

# Семинар 1. Основы C++, раздельная компиляция, пространства имён

Курс ООП, ФИТ НГУ

---

## Что такое C++?

- Язык программирования общего назначения
- Расширение языка C с поддержкой ООП
- Создан Бьярном Страуструпом в 1985 году
- Компилируемый язык, статическая типизация

---

## Особенности C++

- **Производительность** - близко к "железу"
- **Объектно-ориентированное программирование**
- **Шаблоны** (templates)
- **RAII и умные указатели** вместо ручного управления памятью
- **Множественное наследование**
- **Перегрузка операторов**

---

## Структура программы

```cpp
#include <iostream>

int main() {
  std::cout << "Hello, World!" << std::endl;
  return 0;
}
```

- `#include` - директива препроцессора
- `main()` - точка входа в программу
- `std::cout` - вывод в консоль

---

## Типы данных, переменные, константы

```cpp
// Basic types
int number = 42;
double price = 19.99;
char letter = 'A';
bool is_true = true;
std::string name = "John";

// const and constexpr: both immutable
const int kMaxSize = 100;
constexpr double kPi = 3.14159;

// Type deduced automatically (C++11)
auto value = 42;  // int
auto pi = 3.14;   // double
```

- `const` - значение фиксируется при инициализации, может вычисляться в рантайме.
- `constexpr` - значение обязано быть известно на этапе компиляции.
- Константы (`const` и `constexpr`) в этом курсе называются `kCamelCase`.

---

## Ввод и вывод

```cpp
#include <iostream>
#include <string>

int main() {
  std::string name;
  int age = 0;

  std::cout << "Enter name: ";
  std::cin >> name;

  std::cout << "Enter age: ";
  std::cin >> age;

  std::cout << "Hello, " << name << "! You are " << age << "." << std::endl;
  return 0;
}
```

---

## Условные операторы

```cpp
int score = 85;

if (score >= 90) {
  std::cout << "Excellent!" << std::endl;
} else if (score >= 70) {
  std::cout << "Good!" << std::endl;
} else if (score >= 50) {
  std::cout << "Satisfactory" << std::endl;
} else {
  std::cout << "Unsatisfactory" << std::endl;
}
```

---

## Циклы

```cpp
// for loop
for (int i = 0; i < 10; ++i) {
  std::cout << i << " ";
}

// while loop
int count = 0;
while (count < 5) {
  std::cout << count << std::endl;
  ++count;
}

// do-while loop
int num = 0;
do {
  std::cout << "Enter a number (0 to stop): ";
  std::cin >> num;
} while (num != 0);
```

---

## Функции

```cpp
// Declaration
int add(int a, int b);

// Definition
int add(int a, int b) {
  return a + b;
}

// Pass strings by const reference: no copy
void print_message(const std::string& message) {
  std::cout << message << std::endl;
}

int main() {
  int result = add(5, 3);
  print_message("Result: " + std::to_string(result));
  return 0;
}
```

---

## Массивы

```cpp
// Static array
int numbers[5] = {1, 2, 3, 4, 5};

// Traversal by index
for (int i = 0; i < 5; ++i) {
  std::cout << numbers[i] << " ";
}

// Range-based for (C++11)
for (int num : numbers) {
  std::cout << num << " ";
}
```

---

## Указатели и ссылки

```cpp
int value = 42;

// Pointer: can be reassigned, can be null
int* ptr = &value;
std::cout << "Value: " << *ptr << std::endl;
std::cout << "Address: " << ptr << std::endl;

// Reference: bound once, never null, cannot be reassigned
int& ref = value;
ref = 100;  // modifies value itself
std::cout << "New value: " << value << std::endl;
```

- Ссылку нельзя переприсвоить на другой объект, и она не может быть нулевой -
  в отличие от указателя.
- Параметр `const T&` не копирует объект, поэтому строки и объекты в функции
  передают по `const&` (см. слайд "Функции").

---

## Компиляция одного файла

```bash
# Compile one file
g++ -o program main.cpp
clang++ -o program main.cpp

# With warnings and the course standard
g++ -std=c++20 -Wall -Wextra -o program main.cpp
clang++ -std=c++20 -Wall -Wextra -o program main.cpp

# Run
./program
```

- Дальше собираем программы из нескольких `.cpp` - именно так устроена
  задача 1a.

---

## Модель сборки: от исходников к программе

```
main.cpp    -> [preprocessor] -> [compiler] -> main.o      \
module1.cpp -> [preprocessor] -> [compiler] -> module1.o     -> [linker] -> program
module2.cpp -> [preprocessor] -> [compiler] -> module2.o    /
```

- Препроцессор разворачивает `#include`, `#define`, условную компиляцию.
- Компилятор переводит каждый `.cpp` в объектный файл: `.o` у g++/clang,
  `.obj` у MSVC.
- Компоновщик (linker) склеивает объектные файлы и библиотеки
  в один исполняемый файл.

---

## Единица трансляции

- Каждый `.cpp`-файл (после подстановки всех `#include`) - отдельная
  единица трансляции (translation unit).
- Компилятор обрабатывает единицы трансляции независимо и превращает
  каждую в свой объектный файл.
- Поправили только `module1.cpp` - пересобрать нужно только его: система
  сборки видит по времени изменения файла, что `module2.cpp` и `main.cpp`
  не менялись.
- Это ровно пункты 1-2 задачи 1a.

```bash
g++ -std=c++20 -c module1.cpp module2.cpp main.cpp
ls -l *.o                     # three object files

touch module1.cpp
g++ -std=c++20 -c module1.cpp
ls -l *.o                     # only module1.o changed its timestamp
```

---

## Объявление и определение

```cpp
// Declaration: tells the compiler the function exists.
// Can appear in as many translation units as needed.
int add(int a, int b);

// Definition: the actual body. Must exist exactly once
// across the whole program.
int add(int a, int b) {
  return a + b;
}
```

- Заголовочный файл (`.h`) содержит объявления - то, что нужно знать
  другим `.cpp`, чтобы вызвать функцию, не видя её тела.
- `.cpp`-файл содержит определения - реализацию.
- Без заголовка каждый `.cpp`, вызывающий `add`, копировал бы объявление
  вручную.

---

## ODR - правило одного определения

- **One Definition Rule**: у каждой функции, переменной, класса - ровно
  одно определение на всю программу.
- Объявлений может быть сколько угодно - в разных `.cpp`, через `#include`
  одного и того же заголовка.
- Заголовочный файл существует именно ради этого: объявление кладут в `.h`
  и включают везде, а определение оставляют в одном `.cpp`.
- Если определение обычной функции случайно попадёт в `.h` и он подключится
  в двух `.cpp` - ODR нарушится (см. следующие два слайда).

---

## Защита от повторного включения

```cpp
// module1.h
#pragma once

#include <string>

namespace module1 {
  std::string my_name();
}
```

- Один и тот же заголовок может попасть в единицу трансляции дважды
  (через цепочку `#include`). Без защиты компилятор увидит объявление
  дважды - ошибка.
- `#pragma once` - не часть стандарта, но поддерживается g++, clang и MSVC.
- Классический вариант - include guard:

```cpp
#ifndef MODULE1_H
#define MODULE1_H
// ... declarations ...
#endif
```

---

## Ошибка компоновщика: undefined reference

Объявили функцию, но не определили (или не подключили к сборке файл
с определением):

```
/usr/bin/ld: main.o: in function `main':
main.cpp:(.text+0x1a): undefined reference to `module1::my_name()'
collect2: error: ld returned 1 exit status
```

- Возникает на этапе компоновки, а не компиляции: каждый `.o` собрался,
  но линковщик не нашёл тело функции ни в одном объектном файле.
- Частые причины: забыли добавить `module1.cpp` в сборку, опечатались
  в имени функции или пространстве имён, сигнатуры разошлись.
- У MSVC та же ошибка называется `LNK2019: unresolved external symbol`.

---

## Ошибка компоновщика: multiple definition

Определение обычной функции положили в заголовок и подключили его
в двух `.cpp`:

```
/usr/bin/ld: module2.o: in function `module1::my_name()':
module1.h:6: multiple definition of `module1::my_name()';
module1.o:module1.h:6: first defined here
collect2: error: ld returned 1 exit status
```

- Заголовок подключился и в `module1.cpp`, и в `module2.cpp` - тело
  функции попало в оба объектных файла, линковщик увидел два определения.
- Лечится: перенести тело в `.cpp` или, если функция должна жить
  в заголовке, пометить её `inline`.
- У MSVC та же ошибка называется `LNK2005: already defined`.

---

## Что можно и нельзя писать в заголовке

Можно:

- объявления функций и переменных;
- определения классов и структур;
- `inline`-функции и `inline`-переменные (C++17);
- шаблоны.

Нельзя без `inline`:

- определения обычных функций;
- определения глобальных переменных.

```cpp
// header.h - OK: inline function, safe to include everywhere
#pragma once

inline int square(int x) {
  return x * x;
}
```

---

## Зачем нужны пространства имён

- Два `.cpp`-файла из разных частей проекта могут определить функции
  с одинаковым именем - без пространств имён это была бы ошибка
  `multiple definition` при компоновке.
- Пространство имён группирует связанные имена и отделяет их
  от одноимённых имён из других частей программы.

```cpp
namespace module1 {
  std::string my_name();
}
```

- Пространства имён можно вкладывать:

```cpp
namespace company {
  namespace project {
    void run();
  }
}

// C++17: то же самое короче
namespace company::project {
  void run();
}
```

---

## Квалифицированный вызов

```cpp
#include "module1.h"
#include "module2.h"
#include <iostream>

int main() {
  std::cout << module1::my_name() << std::endl;  // "John"
  std::cout << module2::my_name() << std::endl;  // "James"
  return 0;
}
```

- `module1::my_name()` - квалифицированное имя: пространство имён указано
  явно, компилятор точно знает, какую функцию вызвать.
- Самый надёжный способ вызова, когда одноимённых функций несколько.

---

## using-директива и using-объявление

```cpp
// Directive: brings in ALL names from module1.
using namespace module1;
my_name();  // calls module1::my_name()

// Declaration: brings in ONE specific name.
using module2::my_name;
my_name();  // calls module2::my_name()
```

- `using namespace X;` (директива) - вносит в текущую область видимости
  все имена из `X`. Удобно, но легко получить конфликт.
- `using X::name;` (объявление) - вносит только одно конкретное имя.
  Точнее и безопаснее.

---

## Пример: два пространства имён, один вызов

```cpp
#include "module1.h"
#include "module2.h"
#include <iostream>

int main() {
  using namespace module1;
  std::cout << my_name() << std::endl;  // (A) "John"

  using namespace module2;
  std::cout << my_name() << std::endl;  // (C) COMPILATION ERROR
}
```

- После `using namespace module1;` неквалифицированный `my_name()`
  разрешается в пользу `module1::my_name` - другого кандидата нет.
  Вывод (A): "John".
- Добавили `using namespace module2;` - у `my_name()` появилось два
  одинаково подходящих кандидата, вызов стал неоднозначным.

---

## Ошибка: ambiguous call

```
main.cpp:9:24: error: call to 'my_name' is ambiguous
  std::cout << my_name() << std::endl;
                       ^~~~~~~
module1.h:4:15: note: candidate function
  std::string my_name();
              ^
module2.h:4:15: note: candidate function
  std::string my_name();
              ^
```

- Компилятор не выбирает "более новую" директиву - обе `using namespace`
  равноправны, поэтому он честно отказывается решать за программиста.
- У g++ и MSVC текст оформлен иначе, но смысл тот же: два одинаково
  подходящих кандидата.

---

## Три способа исправить неоднозначность

1. Квалифицировать вызов явно:

```cpp
std::cout << module1::my_name() << std::endl;
std::cout << module2::my_name() << std::endl;
```

2. Убрать одну из директив `using namespace`, оставить только нужную.
3. Точечное using-объявление вместо директивы - оно однозначно выбирает
   функцию:

```cpp
using module2::my_name;
std::cout << my_name() << std::endl;  // (D) "James"
```

- В задаче 1a именно вариант 3 используется в точке (D).

---

## using namespace std; - почему опасно

```cpp
// header.h - ПЛОХО: заражает каждый .cpp, который подключит этот заголовок
using namespace std;

// main.cpp - допустимо: действует только внутри функции
int main() {
  using namespace std;
  std::cout << "Hello" << std::endl;
}
```

- В заголовке `using namespace std;` действует на все файлы, которые его
  подключат - они получают все имена `std` целиком, включая неожиданные
  (`std::count`, `std::size`, `std::data` конфликтуют с именами, которые
  часто заводят сами разработчики).
- Внутри функции область действия ограничена телом функции - риск
  конфликта локальный и предсказуемый.
- Практика курса: `std::` пишется явно везде, `using namespace std;`
  не используется вовсе.

---

## Анонимное пространство имён

```cpp
// helper.cpp
namespace {
  int call_count = 0;  // internal linkage: invisible outside this .cpp

  void log_call() {
    ++call_count;
  }
}
```

- Имена в безымянном пространстве имён видны только внутри своей единицы
  трансляции (внутренняя связь, internal linkage) - как раньше делали
  через `static` для функций и глобальных переменных.
- Современный C++ предпочитает анонимное пространство имён: работает
  одинаково для функций, переменных и классов.

---

## ADL: поиск, зависящий от аргументов

```cpp
std::string name = "John";
std::cout << name << std::endl;  // operator<< found without std:: qualification
```

- `operator<<` для `std::string` определён в пространстве имён `std`,
  но вызывается без `std::operator<<`.
- Компилятор ищет функцию не только в текущей области видимости, но
  и в пространствах имён её аргументов (`std::cout` и `std::string` - оба
  из `std`) - это argument-dependent lookup (ADL).
- Подробно - в семинаре про шаблоны и перегрузку операторов.

---

## Типичные ошибки

- Определение обычной функции или переменной в `.h` без `inline` -
  `multiple definition` при линковке.
- Забыли добавить `.cpp` с определением в сборку - `undefined reference`.
- Нет `#pragma once`/include guard - заголовок подключается дважды.
- `using namespace std;` на уровне заголовка или глобально в `.cpp`.
- Голые `cout`/`string`/`endl` без `std::` вне тела функции с `using`.
- Строка или объект передаются в функцию по значению там, где нужен
  `const&`.

---

## Вопросы для самопроверки

- Почему при правке одного `.cpp` пересобирается только он - что для
  этого делает система сборки?
- Что выведется в точках (A) и (D) в `main.cpp` задачи 1a и почему?
- Почему `using namespace module1;` и `using namespace module2;` вместе
  дают ошибку компиляции, а не выбирают "последний" вариант?
- Чем `using namespace X;` отличается от `using X::name;`?
- Почему `using namespace std;` в заголовке опаснее, чем внутри функции
  `main`?

---

## Практика на паре

1. Собрать программу из трёх `.cpp` (`main.cpp`, `module1.cpp`,
   `module2.cpp`), убедиться, что объектных файлов получилось три.
2. Изменить один `.cpp`, пересобрать и убедиться, что дата изменения
   обновилась только у соответствующего `.o` (остальные - прежние).
3. Завести два пространства имён с функцией `my_name()`, воспроизвести
   ошибку ambiguous call, затем починить её всеми тремя способами
   по очереди.
4. Объяснить соседу по парте, почему `using namespace std;` в заголовке
   хуже, чем внутри функции.
